/* encoding UTF-8
 * 
 * Copyright (c) 2013 Arne Johannessen
 * 
 * This project and all of its individual parts may be used in accordance
 * with the terms of the 3-clause BSD licence. See LICENSE for details.
 */

package de.thaw.thesis.comb;

import de.thaw.thesis.comb.util.SimpleVector;

import java.util.AbstractSet;
import java.util.Iterator;
import java.util.NoSuchElementException;


/**
 *  
 * <p>
 * This class implements the <code>Set</code> interface chiefly to indicate
 * that instances in fact represent an <em>unordered</em> collection of
 * (exactly two) nodes. None of the optional operations in the collections
 * framework have been implemented, and the required operations should not be
 * expected to be particularly efficient.
 */
public final class CorrelationEdge extends AbstractSet<SourceNode> implements NodePair, Comparable<CorrelationEdge> {
	
	// there is no intrinsic ordering of the two nodes
	private final SourceNode node0;
	private final SourceNode node1;
	
	private GeneralisedNode midPoint = null;
	
	public SourceNode node0 () {
		return node0;
	}
	
	public SourceNode node1 () {
		return node1;
	}
	
	
	// in how many directions (out of 2 in the trivial case) has this edge been used for generalisation?
	int genCounter = 0;
	
	
 	CorrelationEdge (final SourceNode node0, final SourceNode node1) {
		assert (node0 == null) == (node1 == null);
		if (node0 != null) {
			assert ! Double.isNaN(node0.easting() + node0.northing() + node1.easting() + node1.northing()) : node0 + " / " + node1;  // don't think this is useful
		}
		
		this.node0 = node0;
		this.node1 = node1;
	}
	
	
	boolean contains (final Node node) {
		return node0.equals(node) || node1.equals(node);
	}
	
	
	public SourceNode other (final Node node) {
		if (node0.equals(node)) {
			return node1;
		}
		else {
			assert node1.equals(node);
			return node0;
		}
	}
	
	
	public GeneralisedNode midPoint () {
		if (midPoint == null) {
			final double e = (node0.easting() + node1.easting()) / 2.0;
			final double n = (node0.northing() + node1.northing()) / 2.0;
			midPoint = new GeneralisedNode(e, n);
		}
		return midPoint;
	}
	
	
	public double distance () {
		assert ! Double.isNaN(node0.easting() + node0.northing() + node1.easting() + node1.northing()) : node0 + " / " + node1;
		
		return SimpleVector.distance(node0, node1);
	}
	
	
	public int compareTo (CorrelationEdge that) {
		if (this.equals(that)) {
			return 0;
		}
		
		// we don't actually care about the exact ordering, except that it MUST meet contract terms!
		// :BGUG: we acrtually do
		// :TODO: rewrite this to compare nodes instead of coordinates; should yield the same result
		final double eMinA = Math.min(this.node0.easting(), this.node1.easting());
		final double eMaxA = Math.max(this.node0.easting(), this.node1.easting());
		final double nMinA = Math.min(this.node0.northing(), this.node1.northing());
		final double nMaxA = Math.max(this.node0.northing(), this.node1.northing());
		final double eMinB = Math.min(that.node0.easting(), that.node1.easting());
		final double eMaxB = Math.max(that.node0.easting(), that.node1.easting());
		final double nMinB = Math.min(that.node0.northing(), that.node1.northing());
		final double nMaxB = Math.max(that.node0.northing(), that.node1.northing());
		int compare = Double.compare(eMinA, eMinB);
		if (compare == 0) {
			compare = Double.compare(nMinA, nMinB);
		}
		if (compare == 0) {
			compare = Double.compare(eMaxA, eMaxB);
		}
		if (compare == 0) {
			compare = Double.compare(nMaxA, nMaxB);
		}
		assert compare != 0 : this + " " + that;  // equals is supposed to cover this particular case
		return compare;
	}
	
	
	// if we need to implement compareTo, we also need to override equals (by contract terms)
	public boolean equals (final Object object) {
		if (this == object) {
			return true;
		}
		if (! (object instanceof CorrelationEdge)) {
			return false;
		}
		CorrelationEdge that = (CorrelationEdge)object;
		
		// symmetric behaviour: T1->T2 <=> T2->T1
		boolean e = this.node0.equals(that.node0) && this.node1.equals(that.node1)
				|| this.node0.equals(that.node1) && this.node1.equals(that.node0);
//		boolean f = this.node0.id == that.node0.id && this.node1.id == that.node1.id
//				|| this.node0.id == that.node1.id && this.node1.id == that.node0.id;
//		assert e == f;
		return e;
	}
	
	
	// if we need to override equals, we also need to override hashCode (by contract terms)
	public int hashCode () {
		// symmetric behaviour / commutative operation required
		// this implementation provides the same numeric result as the default AbstractSet implementation
		return node0.hashCode() + node1.hashCode();
	}
	
	
	// Set implementation
	public int size () {
		return 2;
	}
	
	
	// Set implementation
	public Iterator<SourceNode> iterator () {
		return new Iterator<SourceNode>() {
			int nextIndex = 0;
			public boolean hasNext () {
				return nextIndex < size();
			}
			public SourceNode next () {
				if (! hasNext()) { throw new NoSuchElementException(); }
				return nextIndex++ == 0 ? node0 : node1;
			}
			public void remove () {
				throw new UnsupportedOperationException();
			}
		};
	}
	
	
	public String toString () {
		return "{gen: " + genCounter + " | " + node0 + " <--> " + node1 + "}";
	}
	
}



