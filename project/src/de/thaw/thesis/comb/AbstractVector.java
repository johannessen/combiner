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


/**
 * An euclidian vector.
 */
abstract class AbstractVector implements Vector {
	
	// :DEBUG: shouldn't be public!
	public final OsmNode start;
	public final OsmNode end;
	
	
	/**
	 * 
	 */
	public AbstractVector (final OsmNode start, final OsmNode end) {
		assert (start == null) == (end == null);
		if (start != null) {
			assert ! Double.isNaN(start.e + start.n + end.e + end.n) : start + " / " + end;  // don't think this is useful
		}
		
		this.start = start;
		this.end = end;
	}
	
	
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
	static double eastingFromDistanceBearing (final double distance, final double bearing) {
		assert ! Double.isNaN(distance + bearing) : distance + " / " + bearing;
		
		return distance * Math.sin(bearing);
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
	static double northingFromDistanceBearing (final double distance, final double bearing) {
		assert ! Double.isNaN(distance + bearing) : distance + " / " + bearing;
		
		return distance * Math.cos(bearing);
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
	
	
	/**
	 * 
	 */
	public String toString () {
		return "e=" + ((double)(int)(easting() * 10.0 + .5) / 10.0)
				+ "m n=" + ((double)(int)(northing() * 10.0 + .5) / 10.0)
				+ "m d=" + ((double)(int)(distance() * 10.0 + .5) / 10.0)
				+ "m a=" + (int)(bearingDegrees() + .5) + "d";
	}
	
}
