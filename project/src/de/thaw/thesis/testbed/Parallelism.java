/* encoding UTF-8
 * 
 * Copyright (c) 2013 Arne Johannessen
 * 
 * This project and all of its individual parts may be used in accordance
 * with the terms of the 3-clause BSD licence. See LICENSE for details.
 */

package de.thaw.thesis.testbed;

import com.vividsolutions.jts.geom.Geometry;
import com.vividsolutions.jts.geom.LineString;


/**
 * Represents a relation between a line and a (likely) parallel line,
 * including the percentage to which the two lines are (likely) parallel.
 * <p>
 * This "percentage" is merely a rough measure based on the geometric length of
 * the two lines.
 */
class Parallelism implements Comparable<Parallelism> {
	
	final Geometry origin;
	
	final double overlap;
	
	
	Parallelism (final LineString baseLine, final LineString fragment) {
//		this.origin = LinePartMeta.origin(fragment);
		this.origin = fragment;
		this.overlap = fragment.getLength() / baseLine.getLength();
	}
	
	
	public String toString () {
//		return LineMeta.description(this.origin) + " (" + Math.max(1, Math.round((float)this.overlap * 100f)) + "%)";
		return LinePartMeta.description(this.origin) + " (" + Math.max(1, Math.round((float)this.overlap * 100f)) + "%)";
	}
	
	
	// we want instances to be easily sortable so that in a list of parallelisms,
	// the one with the highest degree of overlap comes up first
	public int compareTo (final Parallelism that) {
		return -1 * Double.compare(this.overlap, that.overlap);
	}
	
	
	// if we need to implement compareTo, we also need to override equals (by contract terms)
	public boolean equals (final Object object) {
		if (! (object instanceof Parallelism)) {
			return false;
		}
		final Parallelism that = (Parallelism)object;
		return this.overlap == that.overlap && this.origin == that.origin;
	}
	
	
	// if we need to override equals, we also need to override hashCode (by contract terms)
	public int hashCode () {
		return (int)(this.overlap * (double)(Integer.MAX_VALUE - Integer.MIN_VALUE));
	}
	
}
