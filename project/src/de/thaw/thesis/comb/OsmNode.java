/* encoding UTF-8
 * 
 * Copyright (c) 2013 Arne Johannessen
 * 
 * This project and all of its individual parts may be used in accordance
 * with the terms of the 3-clause BSD licence. See LICENSE for details.
 */

package de.thaw.thesis.comb;

import java.util.Set;
import java.util.TreeSet;


/**
 * A defined point in the euclidian plane. Instances may or may not have
 * relationships with actual nodes in the OSM planet database, and may or may
 * not be part of an <code>OsmDataset</code>.
 */
public final class OsmNode implements Comparable<OsmNode> {
	
	/* :FIX: QGIS Shapefile Snapping
	 * When editing a Shapefile, QGIS 1.8.0 sometimes snaps vertices of
	 * linestrings to coordinates that don't quite match those of the vertix
	 * they're being snapped to. This appears to be a bug in QGIS. The mismatch
	 * distance seems to be very small, usually just in the least significant
	 * bit of the double's significand.
	 */
	static private final double MAX_COORDINATE_VALUE = 10000000.0;  // 10_000 km false northing
	static private final double EPSILON = Math.ulp(MAX_COORDINATE_VALUE * 2.0);
//	static private final double EPSILON = .0000001;  // metres
	// :BUG: very short segments in source data are folded to a zero-length segment using this method if EPSILON is too large
	
	final double e;
	final double n;
	
	public long id = OsmDataset.ID_UNKNOWN;  // :BUG: shouldn't be public
	// not all OsmNodes exist in the OSM planet (e. g. splitPts, Frederik's shapefile)
	
	Set<LineSegment> connectingSegments;
	
	
	private OsmNode (final double e, final double n) {
		this.e = e;
		this.n = n;
		this.connectingSegments = new TreeSet<LineSegment>();
	}
	
	
	/**
	 * 
	 */
	public OsmNode (final OsmNode start, final Vector vector) {
		this.e = start.e + vector.easting();
		this.n = start.n + vector.northing();
	}
	
	
	/**
	 * 
	 */
	public static OsmNode createWithEastingNorthing (final double easting, final double northing) {
		return new OsmNode(easting, northing);
	}
	
	
	static OsmNode createAtMidPoint (final OsmNode node, final Vector vector) {
		return new OsmNode(node, Vector.createFromDistanceBearing( vector.distance() / 2.0, vector.bearing() ));
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
	
	
	void addSegment (LineSegment segment) {
		connectingSegments.add(segment);
	}
	
	
	/**
	 * 
	 */
	public String toString () {
		return "E" + ((double)(int)(e * 10.0 + .5) / 10.0) + "m N" + ((double)(int)(n * 10.0 + .5) / 10.0) + "m" + (id != OsmDataset.ID_UNKNOWN ? " [" + id + "]" : "");
//		return "E " + Double.toHexString(e) + " / N " + Double.toHexString(n) + (id != OsmDataset.ID_UNKNOWN ? " [" + id + "]" : "");  // :DEBUG:
	}
	
	
	/**
	 * 
	 */
	public int compareTo (final OsmNode that) {
		if (that.equals(this)) {
			return 0;  // :FIX: QGIS Shapefile Snapping
		}
		final int compare = Double.compare(that.e, this.e);
		if (compare == 0) {
			return Double.compare(that.n, this.n);
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
		if (! (object instanceof OsmNode)) {
			return false;
		}
//		return this.compareTo( (OsmNode)object ) == 0;
		
		// :FIX: QGIS Shapefile Snapping
		// :BUG: there are equal objects with unequal hash codes
		final OsmNode that = (OsmNode)object;
		
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
