/* encoding UTF-8
 * 
 * Copyright (c) 2013 Arne Johannessen
 * 
 * This project and all of its individual parts may be used in accordance
 * with the terms of the 3-clause BSD licence. See LICENSE for details.
 */

package de.thaw.thesis.comb;

import de.thaw.thesis.comb.util.SimpleVector;


/**
 * 
 */
public final class CorrelationEdge implements Comparable<CorrelationEdge> {
	
	private final SourceNode start;
	private final SourceNode end;
	private GeneralisedNode midPoint = null;
	
	public SourceNode node0 () {
		return start;
	}
	
	public SourceNode node1 () {
		return end;
	}
	
	
	// in how many directions (out of 2 in the trivial case) has this edge been used for generalisation?
	int genCounter = 0;
	
	
 	CorrelationEdge (final SourceNode start, final SourceNode end) {
		assert (start == null) == (end == null);
		if (start != null) {
			assert ! Double.isNaN(start.easting() + start.northing() + end.easting() + end.northing()) : start + " / " + end;  // don't think this is useful
		}
		
		this.start = start;
		this.end = end;
	}
	
	
	boolean contains (final Node node) {
		return start.equals(node) || end.equals(node);
	}
	
	
	SourceNode other (final Node node) {
		if (start.equals(node)) {
			return end;
		}
		else {
			assert end.equals(node);
			return start;
		}
	}
	
	
	GeneralisedNode midPoint () {
		if (midPoint == null) {
			final double e = (start.easting() + end.easting()) / 2.0;
			final double n = (start.northing() + end.northing()) / 2.0;
			midPoint = new GeneralisedNode(e, n);
		}
		return midPoint;
	}
	
	
	double length () {
		assert ! Double.isNaN(start.easting() + start.northing() + end.easting() + end.northing()) : start + " / " + end;
		
		return SimpleVector.distance(start, end);
	}
	
	
	public int compareTo (CorrelationEdge that) {
		if (this.equals(that)) {
			return 0;
		}
		
		// we don't actually care about the exact ordering, except that it MUST meet contract terms!
		// :BGUG: we acrtually do
		// :TODO: rewrite this to compare nodes instead of coordinates; should yield the same result
		final double eMinA = Math.min(this.start.easting(), this.end.easting());
		final double eMaxA = Math.max(this.start.easting(), this.end.easting());
		final double nMinA = Math.min(this.start.northing(), this.end.northing());
		final double nMaxA = Math.max(this.start.northing(), this.end.northing());
		final double eMinB = Math.min(that.start.easting(), that.end.easting());
		final double eMaxB = Math.max(that.start.easting(), that.end.easting());
		final double nMinB = Math.min(that.start.northing(), that.end.northing());
		final double nMaxB = Math.max(that.start.northing(), that.end.northing());
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
		boolean e = this.start.equals(that.start) && this.end.equals(that.end)
				|| this.start.equals(that.end) && this.end.equals(that.start);
//		boolean f = this.node0.id == that.node0.id && this.node1.id == that.node1.id
//				|| this.node0.id == that.node1.id && this.node1.id == that.node0.id;
//		assert e == f;
		return e;
	}
	
	
	// if we need to override equals, we also need to override hashCode (by contract terms)
	public int hashCode () {
		// symmetric behaviour required!
		return 29 * (start.hashCode() + end.hashCode());
	}
	
	
	public String toString () {
		return "{gen: " + genCounter + " | " + start + " <--> " + end + "}";
	}
	
}



