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

import java.util.Arrays;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.LinkedList;
import java.util.NoSuchElementException;


// ex AbstractLinePart
/**
 * Implements the common structure and behaviour shared by all
 * <code>Segment</code>s in this project. Each <code>AbstractSegment</code>
 * may be split into two other <code>AbstractSegment</code>s, yielding a
 * recursive composition, implemented as a Composite pattern, with this
 * abstract class defining the "Component" participant. Concrete
 * implementations of this class participate either as "Leaf" or as
 * "Composite", depending on whether or not fragments (children) exist.
 * <p>
 * This class implements the <code>Iterable</code> interface such that
 * iteration is performed on all Leafs in the composition. In other words,
 * the iterator touches every fragment created by splitting. For example, if
 * this segment has been split exactly once, the iterator touches 2 fragments;
 * if this segment has been split twice, the iterator touches 3 fragments. If
 * this segment has never been split, the iterator touches only 1 fragment,
 * which is this segment itself. The iterator always touches at least 1
 * fragment. Note that fragments are <code>Segment</code>s themselves.
 * <p>
 * Because of this it is easy to have loops touch every fragment of this
 * segment, even if it was split many times:
 * <pre>
 *     for (Segment fragment : segment) { ... }
 * </pre>
 * @see Segment
 * @see AbstractSegment#parent
 * @see AbstractSegment#fragments
 */
abstract class AbstractSegment implements Segment, Vector, Iterable<Segment> {
	
	protected Node start;
	protected Node end;
	
	
	// "wurzel(t)"
	public SourceSegment root () {
		AbstractSegment root = this;
		AbstractSegment parent = null;
		while ( (parent = root.parent()) != null ) {
			root = parent;
		}
		return (SourceSegment)root;
	}
	
	
	/**
	 * The parent of this object in the composition tree.
	 * <code>null</code> if this object is the root of the tree.
	 * @return The parent or <code>null</code>
	 */
	abstract protected AbstractSegment parent () ;
	
	
	/**
	 * The children of this Composite in the composition (if any).
	 * <code>null</code> iff this object does not represent a Composite, but a
	 * Leaf. A split converts this object from a Leaf into a Composite by
	 * assigning a non-null value to this field; in particular, a split creates
	 * exactly two fragments, thus an array of size 2 must be assigned.
	 */
	protected AbstractSegment[] fragments = null;
	
	
	final static double MIN_FRAGMENT_LENGTH = 4.0;  // metres
	// short values decrease cost (?)
	// large values increase the tolerance required during analysis because the start/endpoints of the other line will no longer be exactly orthogonal to the current line's points
	
	
	private boolean wasSplit = false;  // only used for assertion in shouldIgnore()
	
	
	AbstractSegment (final Node start, final Node end) {
		assert (start == null) == (end == null);
		if (start != null) {
			assert ! Double.isNaN(start.easting() + start.northing() + end.easting() + end.northing()) : start + " / " + end;  // don't think this is useful
		}
		
		this.start = start;
		this.end = end;
		
		assert start != end : start + " === " + end;
		assert ! start.equals(end) : start + " == " + end;
	}
	
	
	/**
	 * 
	 */
	public Node start () {
		assert start != null;
		return start;
	}
	
	
	/**
	 * 
	 */
	public Node end () {
		assert end != null;
		return end;
	}
	
	
	public boolean shouldIgnore () {
		assert (fragments != null) == wasSplit : "Composite logic failure";
		return fragments != null;
	}
	
	
	/**
	 * 
	 */
	public Node midPoint () {
		return Nodes.createAtMidPoint(start, this);
	}
	
	
	/**
	 * 
	 */
	public void splitCloseParallels (final SplitQueueListener listener) {
		splitCloseParallels(start, listener);
		splitCloseParallels(end, listener);
	}
	
	
	private void splitCloseParallels (final Node node, final SplitQueueListener listener) {
		for (Segment target : splitTargets()) {  // "T"
			
			if (target.shouldIgnore()) {
				/* This check is not actually necessary as this condition is
				 * never true with the current implementation. But since that
				 * depends upon the implementation of the SplitQueue and
				 * splitTargets(), it makes sense to check it anyway.
				 */
				continue;  // "set-minus t"
			}
			
			final Node foot = target.findPerpendicularFoot(node);  // "f"
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
			for (final Segment fragment : closeParallel) {
				targets.add(fragment);
			}
		}
		return targets;
	}
	
	
	/**
	 * 
	 */
	public Node findPerpendicularFoot (final Node node) {
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
		
//		final Node foot = new AbstractNode(start, qVector);
		final Node foot = Nodes.createWithDistanceBearing(start, q, ba.bearing());
//		foot.id() = Dataset.ID_NONEXISTENT;
		
		if (SimpleVector.distance(start, foot) < MIN_FRAGMENT_LENGTH || SimpleVector.distance(foot, end) < MIN_FRAGMENT_LENGTH) {
			// foot points creating extremely short line parts are defined to not exist because such line parts are of little use to us
			return null;
		}
		
		return foot;
	}
	
	
	public void splitAt (Node node, final SplitQueueListener listener) {
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
		assert fragments == null : "segment " + this + " is not a leaf";
		fragments = new AbstractSegment[2];
		fragments[0] = new Fragment(start, node, this);
		fragments[1] = new Fragment(node, end, this);
		
		listener.didSplit(Arrays.asList( (Segment[])fragments ), node);
		this.wasSplit = true;
	}
	
	
	// ex LineFragment
	/**
	 * A <code>Segment</code> implementation representing incomplete
	 * <em>fragments</em> of segments read from source data. Each fragment is
	 * linked to its parent.
	 */
	final static class Fragment extends AbstractSegment {
		
		final AbstractSegment parent;
		
		Fragment (final Node start, final Node end, final AbstractSegment parent) {
			super(start, end);
			assert parent != null;
			this.parent = parent;
		}
		
		protected AbstractSegment parent () {
			return parent;
		}
		
	}
	
	
	/**
	 * An iterator over all segments, including any fragments <em>this</em>
	 * segment may have been split up into.
	 * @return an Iterator.
	 * @see AbstractSegment
	 * @see AbstractSegment.FragmentIterator
	 */
	public Iterator<Segment> iterator () {
		return new FragmentIterator();
	}
	
	
	/**
	 * An iterator to perform a depth-first traversal of the Composite tree
	 * below this segment, returning all Leafs.
	 */
	final class FragmentIterator implements Iterator<Segment> {
		
		private AbstractSegment next = AbstractSegment.this;
		private AbstractSegment current = null;
		
		FragmentIterator () {
			while (next.fragments != null) {  // find first Leaf
				next = next.fragments[0];
			}
		}
		
		
		public boolean hasNext () {
			if (next != null) {
				return true;
			}
			else if (current == null) {
				return false;
			}
			assert current.fragments == null;
			// this iterator only returns Leafs, never Composites
			
			next = current.parent();
			while (next != null) {
				if (next.fragments[0] == current) {
					next = next.fragments[1];
					while (next.fragments != null) {  // find Leaf
						next = next.fragments[0];
					}
					return true;
				}
				while (next != null && next.fragments[1] == current) {
					current = next;
					next = next.parent();
				}
			}
			
			// iterator exhausted
			current = null;
			return false;
		}
		
		
		public Segment next () {
			if (! hasNext()) {
				throw new NoSuchElementException();
			}
			// this.next is properly initialised as a side-effect of hasNext()
			current = next;
			next = null;
			return current;
		}
		
		
		/**
		 * @throws UnsupportedOperationException
		 */
		public void remove () {
			throw new UnsupportedOperationException();
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
			for (final Segment that : closeParallel) {
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
	
	
	
	public Node other (final Node node) {
		if (start.equals(node)) {
			return end;
		}
		else {
			assert end.equals(node);
			return start;
		}
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
		assert ! Double.isNaN(end.easting() + start.easting()) : start + " / " + end;
		
		return end.easting() - start.easting();
	}
	
	
	/**
	 * 
	 */
	public double northing () {
		assert ! Double.isNaN(end.northing() + start.northing()) : start + " / " + end;
		
		return end.northing() - start.northing();
	}
	
	
	/**
	 * 
	 */
	public double distance () {
		assert ! Double.isNaN(start.easting() + start.northing() + end.easting() + end.northing()) : start + " / " + end;
		
		return SimpleVector.distance(start, end);
	}
	
	
	/**
	 * 
	 */
	public double bearing () {
		assert ! Double.isNaN(start.easting() + start.northing() + end.easting() + end.northing()) : start + " / " + end;
		
		return SimpleVector.normaliseAbsoluteBearing( Math.atan2(end.easting() - start.easting(), end.northing() - start.northing()) );
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
		Node start = this.start;
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
