/* encoding UTF-8
 * 
 * Copyright (c) 2013 Arne Johannessen
 * 
 * This project and all of its individual parts may be used in accordance
 * with the terms of the 3-clause BSD licence. See LICENSE for details.
 */

package de.thaw.thesis.comb;

import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.LinkedList;


/**
 * A skeletal implementation of the <code>LinePart</code> interface. Minimises
 * the effort required to implement this interface.
 */
abstract class AbstractLinePart implements LinePart {
	
	
	public abstract LineSegment segment () ;
	
	
	// :TODO: rework structure to better fit the Composite pattern
	
	
	final static double MIN_FRAGMENT_LENGTH = 4.0;  // metres
	// short values decrease cost (?)
	// large values increase the tolerance required during analysis because the start/endpoints of the other line will no longer be exactly orthogonal to the current line's points
	
	
	// LinePart
	final OsmNode start;
	final OsmNode end;
	
	private Vector vector;
	
	boolean wasSplit = false;
	
	
	AbstractLinePart (final OsmNode start, final OsmNode end) {
		assert start != null && end != null;
		assert ! start.equals(end) : start + " / " + end;
		
		this.start = start;
		this.end = end;
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
	public Vector vector () {
		if (vector == null) {
			vector = new Vector(start, end);
		}
		return vector;
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
		return OsmNode.createAtMidPoint(start, vector());
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
		
		assert node.equals(start) == (node == start) : start + " " + node;
		assert node.equals(end) == (node == end) : end + " " + node;
		
		if (node.equals(start) || node.equals(end)) {
			return null;
		}
		
		// :BUG: expensive due to dynamic Vector object creation; implement a static method instead
		
		final Vector ba = vector();
		final Vector bc = new Vector(start, node);
		final Vector ca = new Vector(node, end);
		final double alpha = ba.relativeBearing(ca);
		final double beta = ba.relativeBearing(bc);
		if (Math.abs(alpha) >= Vector.RIGHT_ANGLE || Math.abs(beta) >= Vector.RIGHT_ANGLE) {
			// the foot is not on the line segment
			return null;
		}
		final double a = bc.distance();
		final double q = a * Math.cos(beta);
		final Vector qVector = Vector.createFromDistanceBearing(q, ba.bearing());
		
		final OsmNode foot = new OsmNode(start, qVector);
		foot.id = OsmDataset.ID_NONEXISTENT;
		
		return foot;
	}
	
	
	/**
	 * 
	 */
	public void splitAt (final OsmNode node, final SplitQueueListener listener) {
		// :BUG: expensive due to dynamic Vector object creation; implement a static method instead 
		if (new Vector(start, node).distance() < MIN_FRAGMENT_LENGTH || new Vector(node, end).distance() < MIN_FRAGMENT_LENGTH) {
			// extremely short lines are of little use to us
			return;
		}
		
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
	
	
	/**
	 * 
	 */
	public void addBestLeftMatch (LinePart bestMatch) {
		assert this instanceof LineFragment || segment().fragments.size() == 0;  // :BUG: Composite
		if (bestMatch == null) {
			return;
		}
		assert bestMatch.segment() != segment();
		
		assert ! segment().midPoint().equals(bestMatch.segment().midPoint());
		segment().leftRealParallels.add(bestMatch.segment());
		if (this.vector().isAligned(bestMatch.vector())) {
			bestMatch.segment().rightRealParallels.add(segment());
		}
		else {
			bestMatch.segment().leftRealParallels.add(segment());
		}
	}
	
	
	/**
	 * 
	 */
	public void addBestRightMatch (LinePart bestMatch) {
		assert this instanceof LineFragment || segment().fragments.size() == 0;  // :BUG: Composite
		if (bestMatch == null) {
			return;
		}
		assert bestMatch.segment() != segment();
		
		assert ! segment().midPoint().equals(bestMatch.segment().midPoint());
		segment().rightRealParallels.add(bestMatch.segment());
		if (this.vector().isAligned(bestMatch.vector())) {
			bestMatch.segment().leftRealParallels.add(segment());
		}
		else {
			bestMatch.segment().rightRealParallels.add(segment());
		}
	}
	
	
	/**
	 * 
	 */
	public int compareTo (LinePart other) {
		return midPoint().compareTo(other.midPoint());
	}
	// :BUG: override equals/hashCode!
	
	
	/**
	 * 
	 */
	public String toString () {
		return this.getClass().getSimpleName() + " " + start.toString() + " / " + end.toString() + " (" + vector() + ")" + (segment().way.id != OsmDataset.ID_UNKNOWN ? " [" + segment().way.id + "]" : "");
	}
	
}
