/* encoding UTF-8
 * 
 * Copyright (c) 2013 Arne Johannessen
 * 
 * This project and all of its individual parts may be used in accordance
 * with the terms of the 3-clause BSD licence. See LICENSE for details.
 */

package de.thaw.thesis.comb;

import de.thaw.thesis.comb.util.OneItemList;
import de.thaw.thesis.comb.util.SimpleVector;
import de.thaw.thesis.comb.util.Vector;

import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.LinkedList;


// ex AbstractLinePart
/**
 * A skeletal implementation of the <code>Segment</code> interface. Minimises
 * the effort required to implement this interface.
 */
abstract class AbstractSegment implements Segment, Vector {
	
	// :DEBUG: shouldn't be public!
	public OsmNode start;
	public OsmNode end;
	
	protected SourceSegment segment = null;
	
	
	public SourceSegment root () {
		if (segment == null) {
			throw new IllegalStateException("root segment not initialised");
		}
		return segment;
	}
	
	
	// :TODO: rework structure to better fit the Composite pattern
	
	
	final static double MIN_FRAGMENT_LENGTH = 4.0;  // metres
	// short values decrease cost (?)
	// large values increase the tolerance required during analysis because the start/endpoints of the other line will no longer be exactly orthogonal to the current line's points
	
	
	private boolean wasSplit = false;
	
	
	AbstractSegment (final OsmNode start, final OsmNode end) {
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
	
	
	public boolean shouldIgnore () {
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
		splitCloseParallels(start, listener);
		splitCloseParallels(end, listener);
	}
	
	
	private void splitCloseParallels (final OsmNode node, final SplitQueueListener listener) {
		for (Segment target : splitTargets()) {  // "T"
			
			if (target.shouldIgnore()) {
				/* This check is not actually necessary as this condition is
				 * never true with the current implementation. But since that
				 * depends upon the implementation of the SplitQueue and
				 * splitTargets(), it makes sense to check it anyway.
				 */
				continue;  // "set-minus t"
			}
			
			final OsmNode foot = target.findPerpendicularFoot(node);  // "f"
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
	public Collection<Segment> splitTargets () {
		final LinkedList<Segment> targets = new LinkedList<Segment>();
		for (final SourceSegment closeParallel : root().closeParallels()) {
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
		
		// FUSSPUNKT, see chapter 4.3.1
		
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
		foot.id = Dataset.ID_NONEXISTENT;
		
		if (SimpleVector.distance(start, foot) < MIN_FRAGMENT_LENGTH || SimpleVector.distance(foot, end) < MIN_FRAGMENT_LENGTH) {
			// foot points creating extremely short line parts are defined to not exist because such line parts are of little use to us
			return null;
		}
		
		return foot;
	}
	
	
	public void splitAt (OsmNode node, final SplitQueueListener listener) {
		/* :BUG:
		 * We use the dataset-global node store to retrieve the canoncical node
		 * instance for the split point's location. This enables us to compare
		 * for identity (==) instead of equality. It may also help to conserve
		 * memory by making the new node instances available to the garbage
		 * collector right away.
		 * The downside of this is that two not-identical nodes at the same
		 * location (belonging to two different ways) are not supported.
		 */
		
		node = root().way.dataset().getNode( node );
		final SourceSegment rootSegment = this.root();  // "wurzel(t)"
		final Segment fragment1 = new Fragment(start, node, rootSegment);
		final Segment fragment2 = new Fragment(node, end, rootSegment);
		
		rootSegment.fragments.add(fragment1);
		rootSegment.fragments.add(fragment2);
		
		listener.didSplit(fragment1, fragment2, node);
		this.wasSplit = true;
	}
	
	
	// ex LineFragment
	/**
	 * A <code>Segment</code> implementation representing incomplete
	 * <em>fragments</em> of segments read from source data.
	 */
	final static class Fragment extends AbstractSegment {
		
		// :TODO: rework structure to better fit the Composite pattern
		// (Composite: (root) SourceSegment; Leaf: concrete AbstractSegment; Component: Segment interface)
		
		private Collection<? extends Segment> list = null;
		
		Fragment (final OsmNode start, final OsmNode end, final AbstractSegment parent) {
			super(start, end);
			assert segment != null;
			super.segment = segment;
		}
		
		public Collection<? extends Segment> lineParts () {
			if (list == null) {
				list = new OneItemList<Fragment>(this);
			}
			return list;
		}
		
	}
	
	
	/**
	 * 
	 */
	public void analyse (final Analyser visitor) {
		if (this.shouldIgnore()) {
			return;  // SPLITTEN: "set-minus t"
		}

		double leftBestDistance = visitor.worstResult();
		Segment leftBestMatch = null;
		double rightBestDistance = visitor.worstResult();
		Segment rightBestMatch = null;
		
		// all fragments of all close and parallel segments
		for (final SourceSegment closeParallel : root().closeParallels()) {
			for (final Segment that : closeParallel.lineParts()) {
				if (that.shouldIgnore()) {
					continue;  // SPLITTEN: "set-minus t"
				}
				
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
	public void addBestLeftMatch (final Segment bestMatch) {
//		assert this instanceof Fragment || root().allFragments.size() == 0;  // :BUG: Composite
		if (bestMatch == null) {
			return;
		}
		assert bestMatch.root() != root();
		
//		assert ! root().midPoint().equals(bestMatch.root().midPoint());
		root().leftRealParallels.add(bestMatch.root());
		if (isAligned(bestMatch)) {
			bestMatch.root().rightRealParallels.add(root());
		}
		else {
			bestMatch.root().leftRealParallels.add(root());
		}
		
		root().way.dataset().parallelFragments().add(new Segment[]{ bestMatch, this });
	}
	
	
	/**
	 * 
	 */
	public void addBestRightMatch (final Segment bestMatch) {
//		assert this instanceof Fragment || root().allFragments.size() == 0;  // :BUG: Composite
		if (bestMatch == null) {
			return;
		}
		assert bestMatch.root() != root();
		
//		assert ! root().midPoint().equals(bestMatch.root().midPoint());
		root().rightRealParallels.add(bestMatch.root());
		if (isAligned(bestMatch)) {
			bestMatch.root().leftRealParallels.add(root());
		}
		else {
			bestMatch.root().rightRealParallels.add(root());
		}
		
		root().way.dataset().parallelFragments().add(new Segment[]{ bestMatch, this });
	}
	
	
	/**
	 * 
	 */
	public int compareTo (final Segment other) {
		return midPoint().compareTo(other.midPoint());
	}
	// :BUG: override equals/hashCode!
	
	
	/**
	 * 
	 */
	public String toString () {
		return this.getClass().getSimpleName() + " " + start.toString() + " / " + end.toString() + " (" + new SimpleVector(start, end) + ")" + (root().way.id() != Dataset.ID_UNKNOWN ? " [" + root().way.id() + "]" : "");
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
