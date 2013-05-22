/* encoding UTF-8
 * 
 * Copyright (c) 2013 Arne Johannessen
 * 
 * This project and all of its individual parts may be used in accordance
 * with the terms of the 3-clause BSD licence. See LICENSE for details.
 */

package de.thaw.thesis.comb;

import de.thaw.thesis.comb.util.SimpleVector;
import de.thaw.thesis.comb.util.Vector;

import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.LinkedList;


/**
 * A skeletal implementation of the <code>LinePart</code> interface. Minimises
 * the effort required to implement this interface.
 */
abstract class AbstractLinePart implements LinePart, Vector {
	
	// :DEBUG: shouldn't be public!
	public OsmNode start;
	public OsmNode end;
	
	
	public abstract LineSegment segment () ;
	
	
	// :TODO: rework structure to better fit the Composite pattern
	
	
	final static double MIN_FRAGMENT_LENGTH = 4.0;  // metres
	// short values decrease cost (?)
	// large values increase the tolerance required during analysis because the start/endpoints of the other line will no longer be exactly orthogonal to the current line's points
	
	
	boolean wasSplit = false;
	
	
	AbstractLinePart (final OsmNode start, final OsmNode end) {
		assert (start == null) == (end == null);
		if (start != null) {
			assert ! Double.isNaN(start.e + start.n + end.e + end.n) : start + " / " + end;  // don't think this is useful
		}
		
		this.start = start;
		this.end = end;
		
		assert ! start.equals(end) : start + " / " + end;
	}
	
	
	/**
	 * 
	 */
	public OsmNode start () {
		assert start != null;
		return start;
	}
	
	
	/**
	 * 
	 */
	public OsmNode end () {
		assert end != null;
		return end;
	}
	
	
	/**
	 * 
	 */
	public boolean wasSplit () {
		return wasSplit;
	}
	
	
	/**
	 * 
	 */
	public OsmNode midPoint () {
		return OsmNode.createAtMidPoint(start, this);
	}
	
	
	/**
	 * 
	 */
	public void splitCloseParallels (final SplitQueueListener listener) {
		// :BUG: expensive: current design requires accumulating closeParallels for _each_ node
		splitCloseParallels(start, listener);
		splitCloseParallels(end, listener);
	}
	
	
	private void splitCloseParallels (final OsmNode node, final SplitQueueListener listener) {
		for (LinePart target : splitTargets()) {
			
			if (target.wasSplit()) {
				continue;  // :BUG: not entirely sure if this is necessary
			}
			
			final OsmNode foot = target.findPerpendicularFoot(node);
			if (foot == null) {
				continue;  // no split necessary for this target
			}
			
			target.splitAt(foot, listener);
		}
	}
	
	
	/**
	 * 
	 */
	// get all line parts of close parallel segments
	// NB: the fragment lists will change during splitting, hence this list needs to be recompiled each time
	public Collection<LinePart> splitTargets () {
		final LinkedList<LinePart> targets = new LinkedList<LinePart>();
		for (final LineSegment closeParallel : segment().closeParallels()) {
			targets.addAll( closeParallel.lineParts() );
		}
		return targets;
	}
	
	
	/**
	 * 
	 */
	public OsmNode findPerpendicularFoot (final OsmNode node) {
		// name: see http://mathworld.wolfram.com/PerpendicularFoot.html
		
//		assert node.equals(start) == (node == start) : start + " " + node;
//		assert node.equals(end) == (node == end) : end + " " + node;
		
		if (node.equals(start) || node.equals(end)) {
			return null;
		}
		
		// :BUG: expensive due to dynamic Vector object creation; implement a static method instead
		
		final Vector ba = this;
		final Vector bc = new SimpleVector(start, node);
		final Vector ca = new SimpleVector(node, end);
		final double alpha = ba.relativeBearing(ca);
		final double beta = ba.relativeBearing(bc);
		if (Math.abs(alpha) >= Vector.RIGHT_ANGLE || Math.abs(beta) >= Vector.RIGHT_ANGLE) {
			// the foot is not on the line segment
			return null;
		}
		final double a = bc.distance();
		final double q = a * Math.cos(beta);
//		final Vector qVector = SimpleVector.createFromDistanceBearing(q, ba.bearing());
		
//		final OsmNode foot = new OsmNode(start, qVector);
		final OsmNode foot = OsmNode.createWithDistanceBearing(start, q, ba.bearing());
		foot.id = OsmDataset.ID_NONEXISTENT;
		
		return foot;
	}
	
	
	/**
	 * 
	 */
	public void splitAt (OsmNode node, final SplitQueueListener listener) {
		if (SimpleVector.distance(start, node) < MIN_FRAGMENT_LENGTH || SimpleVector.distance(node, end) < MIN_FRAGMENT_LENGTH) {
			// extremely short lines are of little use to us
			return;
		}
		
		/* :BUG:
		 * We use the dataset-global node store to retrieve the canoncical node
		 * instance for the split point's location. This enables us to compare
		 * for identity (==) instead of equality. It may also help to conserve
		 * memory by making the new node instances available to the garbage
		 * collector right away.
		 * The downside of this is that two not-identical nodes at the same
		 * location (belonging to two different ways) are not supported.
		 */
		node = segment().way.dataset().getNode( node );
		
		final LineSegment segment = this.segment();
		final LineFragment fragment1 = new LineFragment(start, node, segment);
		final LineFragment fragment2 = new LineFragment(node, end, segment);
		
		segment.fragments.add(fragment1);
		segment.fragments.add(fragment2);
		
		if (this instanceof LineFragment) {
			/* The next stage compares close fragments with each other. If we
			 * left those fragmenst which have been further split into more
			 * (sub-)fragments in the list, we end up with overlapping
			 * fragments being checked against each other. These checks are
			 * unnecessary and a performance hit.
			 */
			// :TODO: check whether this line has an influence on the end result as well (testing so far seems to indicate that it might)
			segment.fragments.remove(this);
		}
		
		listener.didSplit(fragment1, fragment2, node);
		this.wasSplit = true;
	}
	
	
	/**
	 * 
	 */
	public void analyse (final Analyser visitor) {
		double leftBestDistance = visitor.worstResult();
		LinePart leftBestMatch = null;
		double rightBestDistance = visitor.worstResult();
		LinePart rightBestMatch = null;
		
		// all fragments of all close and parallel segments
		for (final LineSegment closeParallel : segment().closeParallels()) {
			for (final LinePart that : closeParallel.lineParts()) {
				
				final boolean shouldEvaluate = visitor.shouldEvaluate(this, that);
				if (! shouldEvaluate) {
					continue;
				}
				
				boolean isLeftMatch = false;
				final double leftDistance = visitor.evaluateLeft(this, that);
				if (visitor.compare(leftDistance, leftBestDistance) < 0) {
					leftBestDistance = leftDistance;
					leftBestMatch = that;
					
					isLeftMatch = true;
				}
				
				final double rightDistance = visitor.evaluateRight(this, that);
				if (visitor.compare(rightDistance, rightBestDistance) < 0) {
					rightBestDistance = rightDistance;
					rightBestMatch = that;
					
					assert ! isLeftMatch;
				}
			}
		}
		
		// :TODO: figure out a way to get rid of code duplication here
		addBestLeftMatch(leftBestMatch);
		addBestRightMatch(rightBestMatch);
	}
	
	
	
	/* Issue here: The fragments need to be cross-checked, (see screenshot
	 * "bug-crosscheck"). The "list" of real parallels needs to be at the
	 * fragment level and needs to be limited to one per side. Whenever the
	 * reverse relation is "added" (currently: bestMatch.parallels.add(this) ),
	 * the reverse needs to be checked first for existing parallels such that
	 * only the one with the shortest distance remains. The segment can then
	 * congregate their own fragments' real parallels into their own list of
	 * real parallels.
	 * Ideally, this should prevent the Triangle Problem at diverging lines.
	 */
	
	/**
	 * 
	 */
	public void addBestLeftMatch (final LinePart bestMatch) {
		assert this instanceof LineFragment || segment().fragments.size() == 0;  // :BUG: Composite
		if (bestMatch == null) {
			return;
		}
		assert bestMatch.segment() != segment();
		
//		assert ! segment().midPoint().equals(bestMatch.segment().midPoint());
		segment().leftRealParallels.add(bestMatch.segment());
		if (isAligned(bestMatch)) {
			bestMatch.segment().rightRealParallels.add(segment());
		}
		else {
			bestMatch.segment().leftRealParallels.add(segment());
		}
		
		segment().way.dataset().parallelFragments.add(new LinePart[]{ bestMatch, this });
	}
	
	
	/**
	 * 
	 */
	public void addBestRightMatch (final LinePart bestMatch) {
		assert this instanceof LineFragment || segment().fragments.size() == 0;  // :BUG: Composite
		if (bestMatch == null) {
			return;
		}
		assert bestMatch.segment() != segment();
		
//		assert ! segment().midPoint().equals(bestMatch.segment().midPoint());
		segment().rightRealParallels.add(bestMatch.segment());
		if (isAligned(bestMatch)) {
			bestMatch.segment().leftRealParallels.add(segment());
		}
		else {
			bestMatch.segment().rightRealParallels.add(segment());
		}
		
		segment().way.dataset().parallelFragments.add(new LinePart[]{ bestMatch, this });
	}
	
	
	/**
	 * 
	 */
	public int compareTo (final LinePart other) {
		return midPoint().compareTo(other.midPoint());
	}
	// :BUG: override equals/hashCode!
	
	
	/**
	 * 
	 */
	public String toString () {
		return this.getClass().getSimpleName() + " " + start.toString() + " / " + end.toString() + " (" + new SimpleVector(start, end) + ")" + (segment().way.id() != OsmDataset.ID_UNKNOWN ? " [" + segment().way.id() + "]" : "");
	}
	
	
	
	/////////////////////// VECTOR
	
/*
	public String toString () {
		return "e=" + ((double)(int)(easting() * 10.0 + .5) / 10.0)
				+ "m n=" + ((double)(int)(northing() * 10.0 + .5) / 10.0)
				+ "m d=" + ((double)(int)(distance() * 10.0 + .5) / 10.0)
				+ "m a=" + (int)(bearingDegrees() + .5) + "d";
	}
*/
	
	
	/**
	 * 
	 */
	public double easting () {
		assert ! Double.isNaN(end.e + start.e) : start + " / " + end;
		
		return end.e - start.e;
	}
	
	
	/**
	 * 
	 */
	public double northing () {
		assert ! Double.isNaN(end.n + start.n) : start + " / " + end;
		
		return end.n - start.n;
	}
	
	
	/**
	 * 
	 */
	public double distance () {
		assert ! Double.isNaN(start.e + start.n + end.e + end.n) : start + " / " + end;
		
		return SimpleVector.distance(start, end);
	}
	
	
	/**
	 * 
	 */
	public double bearing () {
		assert ! Double.isNaN(start.e + start.n + end.e + end.n) : start + " / " + end;
		
		return SimpleVector.normaliseAbsoluteBearing( Math.atan2(end.e - start.e, end.n - start.n) );
	}
	
	
	/**
	 * 
	 */
	double bearingDegrees () {
		return bearing() * 180.0 / Math.PI;
	}
	
	
	/**
	 * 
	 */
	public double relativeBearing (final Vector v) {
		return SimpleVector.normaliseRelativeBearing(v.bearing() - bearing());
	}
	
	
	/**
	 * 
	 */
	public Vector reversed () {
		return new SimpleVector(end, start);
	}
	
	
	/**
	 * 
	 */
	void reverse () {
		OsmNode start = this.start;
		this.start = this.end;
		this.end = start;
	}
	
	
	/**
	 * 
	 */
	public Vector aligned (final Vector v) {
		if (! isAligned(v)) {
			return reversed();
		}
		return this;
	}
	
	
	/**
	 * 
	 */
	public boolean isAligned (final Vector v) {
		return Math.abs(relativeBearing(v)) <= RIGHT_ANGLE;
	}
	
}
