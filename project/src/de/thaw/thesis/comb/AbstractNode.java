/* encoding UTF-8
 * 
 * Copyright (c) 2013 Arne Johannessen
 * 
 * This project and all of its individual parts may be used in accordance
 * with the terms of the 3-clause BSD licence. See LICENSE for details.
 */

package de.thaw.thesis.comb;


/**
 * A skeletal implementation of the <code>Node</code> interface, minimising
 * the effort required to implement it.
 */
public abstract class AbstractNode implements Node {
	
	/* :FIX: QGIS Shapefile Snapping
	 * When editing a Shapefile, QGIS 1.8.0 sometimes snaps vertices of
	 * linestrings to coordinates that don't quite match those of the vertix
	 * they're being snapped to. This appears to be a bug in QGIS. The mismatch
	 * distance seems to be very small, usually just in the least significant
	 * bit of the double's significand.
	 */
//	static private final double MAX_COORDINATE_VALUE = 10000000.0;  // 10_000 km false northing
//	static private final double EPSILON = Math.ulp(MAX_COORDINATE_VALUE * 2.0);
//	static private final double EPSILON = .0000001;  // metres
	static private final double EPSILON = 0.0;  // metres
	// :BUG: very short segments in source data are folded to a zero-length segment using this method if EPSILON is too large
	
	protected final double e;
	protected final double n;
	
	
	protected AbstractNode (final double e, final double n) {
		this.e = e;
		this.n = n;
	}
	
	
	/**
	 * 
	 */
	public double easting () {
		return e;
	}
	
	
	/**
	 * 
	 */
	public double northing () {
		return n;
	}
	
	
	/**
	 * 
	 */
	public String toString () {
		return "E" + ((double)(int)(e * 10.0 + .5) / 10.0) + "m N" + ((double)(int)(n * 10.0 + .5) / 10.0) + "m" + (id() != Dataset.ID_UNKNOWN ? " [" + id() + "]" : "");
//		return "E " + Double.toHexString(e) + " / N " + Double.toHexString(n) + (id != Dataset.ID_UNKNOWN ? " [" + id + "]" : "");  // :DEBUG:
	}
	
	
	/**
	 * 
	 */
	public int compareTo (final Node that) {
		if (that.equals(this)) {
			return 0;  // :FIX: QGIS Shapefile Snapping
		}
		final int compare = Double.compare(that.easting(), this.easting());
		if (compare == 0) {
			return Double.compare(that.northing(), this.northing());
		}
		return compare;
	}
	
	
	/**
	 * 
	 */
	// if we need to implement compareTo, we also need to override equals (by contract terms)
	public boolean equals (final Object object) {
		if (this == object) {
			return true;
		}
		if (! (object instanceof AbstractNode)) {
			return false;
		}
//		return this.compareTo( (AbstractNode)object ) == 0;
		
		// :FIX: QGIS Shapefile Snapping
		// :BUG: there are equal objects with unequal hash codes
		final AbstractNode that = (AbstractNode)object;
		
		if (Math.abs(that.e - this.e) > EPSILON) {
			return false;
		}
		if (Math.abs(that.n - this.n) > EPSILON) {
			return false;
		}
		return true;
	}
	
	
	/**
	 * 
	 */
	// if we need to override equals, we also need to override hashCode (by contract terms)
	public int hashCode () {
		
		// :BUG: there are equal objects with unequal hash codes
		// for very small EPSILONs, casting to floats for the hashing should minimise this problem
		
		//Algorithm from Effective Java by Joshua Bloch
		int hashCode = 17;
		hashCode = hashCode * 37 + Float.floatToIntBits( (float)e );
		hashCode = hashCode * 37 + Float.floatToIntBits( (float)n );
		return hashCode;
	}
	
}
