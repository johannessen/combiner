/* encoding UTF-8
 * 
 * Copyright (c) 2013 Arne Johannessen
 * 
 * This project and all of its individual parts may be used in accordance
 * with the terms of the 3-clause BSD licence. See LICENSE for details.
 */

package de.thaw.thesis.comb.util;


/**
 * An euclidian vector.
 */
public final class SimpleVector implements Vector {
	
	private double e = Double.NaN;
	private double n = Double.NaN;
	private double d = Double.NaN;
	private double a = Double.NaN;
	
	private SimpleVector reversed = null;
	
	
	/**
	 * 
	 */
	public SimpleVector (final PlaneCoordinate start, final PlaneCoordinate end) {
		e = end.easting() - start.easting();
		n = end.northing() - start.northing();
		
		assert ! Double.isNaN(e + n) : e + " / " + n;
	}
	
	
	private SimpleVector () {
	}
	
	
	/**
	 * 
	 */
	static SimpleVector createFromDistanceBearing (final double distance, final double bearing) {
		assert ! Double.isNaN(distance + bearing) : distance + " / " + bearing;
		
		final SimpleVector vector = new SimpleVector();
		vector.e = Double.NaN;
		vector.n = Double.NaN;
		vector.d = distance;
		vector.a = bearing;
		return vector;
	}
	
	
	/**
	 * 
	 */
	public double easting () {
		if (Double.isNaN(e)) {
			assert ! Double.isNaN(d + a) : d + " / " + a;
			e = d * Math.sin(a);
		}
		
		return e;
	}
	
	
	/**
	 * 
	 */
	public double northing () {
		if (Double.isNaN(n)) {
			assert ! Double.isNaN(d + a) : d + " / " + a;
			n = d * Math.cos(a);
		}
		
		return n;
	}
/*
	static {
		// easting()/northing() test cases
		SimpleVector v;
		v = new SimpleVector(true); v.d = 1.0; v.a = -.5 * Math.PI;
		assert q( -1.0, v.easting() ) && q( v.northing(),  0.0 ) : v.easting() + " / " + v.northing();
		v = new SimpleVector(true); v.d = 1.0; v.a = 0.0 * Math.PI;
		assert q(  0.0, v.easting() ) && q( v.northing(), +1.0 ) : v.easting() + " / " + v.northing();
		v = new SimpleVector(true); v.d = 1.0; v.a = 0.5 * Math.PI;
		assert q( +1.0, v.easting() ) && q( v.northing(),  0.0 ) : v.easting() + " / " + v.northing();
		v = new SimpleVector(true); v.d = 1.0; v.a = 1.0 * Math.PI;
		assert q(  0.0, v.easting() ) && q( v.northing(), -1.0 ) : v.easting() + " / " + v.northing();
		v = new SimpleVector(true); v.d = 1.0; v.a = 1.5 * Math.PI;
		assert q( -1.0, v.easting() ) && q( v.northing(),  0.0 ) : v.easting() + " / " + v.northing();
		v = new SimpleVector(true); v.d = 1.0; v.a = 2.0 * Math.PI;
		assert q(  0.0, v.easting() ) && q( v.northing(), +1.0 ) : v.easting() + " / " + v.northing();
		v = new SimpleVector(true); v.d = 1.0; v.a = 2.5 * Math.PI;
		assert q( +1.0, v.easting() ) && q( v.northing(),  0.0 ) : v.easting() + " / " + v.northing();
	}
	static boolean q (double x, double y) {
		return Math.abs(x - y) < Math.cbrt((double)Float.MIN_NORMAL);
	}
*/
	
	
	/**
	 * 
	 */
	public static double distance (final PlaneCoordinate point1, final PlaneCoordinate point2) {
		final double e = point2.easting() - point1.easting();
		final double n = point2.northing() - point1.northing();
		return Math.sqrt( e*e + n*n );
	}
	
	
	/**
	 * 
	 */
	public double distance () {
		if (Double.isNaN(d)) {
			assert ! Double.isNaN(e + n) : e + " / " + n;
			d = Math.sqrt( e*e + n*n );
		}
		
		return d;
	}
	
	
	/**
	 * 
	 */
	public double bearing () {
		if (Double.isNaN(a)) {
			assert ! Double.isNaN(e + n) : e + " / " + n;
			a = normaliseAbsoluteBearing( Math.atan2(e, n) );
		}
		
		return a;
	}
/*
	static {
		// bearing() test cases
		SimpleVector v;
		v = new SimpleVector(false); v.e =  0.0; v.n = +1.0;
		assert   0.0 == v.bearingDegrees() : v.bearingDegrees();
		v = new SimpleVector(false); v.e = +1.0; v.n = +1.0;
		assert  45.0 == v.bearingDegrees() : v.bearingDegrees();
		v = new SimpleVector(false); v.e = +1.0; v.n = -1.0;
		assert 135.0 == v.bearingDegrees() : v.bearingDegrees();
		v = new SimpleVector(false); v.e = -1.0; v.n = -1.0;
		assert 225.0 == v.bearingDegrees() : v.bearingDegrees();
		v = new SimpleVector(false); v.e = -1.0; v.n = +1.0;
		assert 315.0 == v.bearingDegrees() : v.bearingDegrees();
	}
*/
	
	
	/**
	 * 
	 */
	private double bearingDegrees () {
		return bearing() * 180.0 / Math.PI;
	}
	
	
	/**
	 * 
	 */
	public double relativeBearing (final Vector v) {
		return normaliseRelativeBearing(v.bearing() - bearing());
	}
	
	
	/**
	 * 
	 */
	public static double normaliseAbsoluteBearing (double bearing) {
		while (bearing < 0.0) {
			bearing += FULL_CIRCLE;
		}
		while (bearing >= FULL_CIRCLE) {
			bearing -= FULL_CIRCLE;
		}
		return bearing;
	}
	
	
	/**
	 * 
	 */
	public static double normaliseRelativeBearing (double bearing) {
		while (bearing < HALF_CIRCLE) {
			bearing += FULL_CIRCLE;
		}
		while (bearing > HALF_CIRCLE) {
			bearing -= FULL_CIRCLE;
		}
		return bearing;
	}
	
	
	/**
	 * 
	 */
	public SimpleVector reversed () {
		if (reversed == null) {
			reversed = new SimpleVector();
			reversed.e = -e;
			reversed.n = -n;
			reversed.d = d;
			reversed.a = normaliseAbsoluteBearing(a + HALF_CIRCLE);
//			reversed.reversed = this;  // :BUG: memory leak through circular references if a reversed vector is again reversed
		}
		return reversed;
	}
	
	
	/**
	 * 
	 */
	public SimpleVector aligned (final Vector v) {
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
	
	
	/**
	 * Computes a hash code for a double value, using the algorithm from
	 * Joshua Bloch's book <i>Effective Java"</i>
	 * 
	 * @return a hashcode for the double value
	 */
	private static int hashCode (double x) {
		long f = Double.doubleToLongBits(x);
		return (int)(f ^ (f >>> 32));
	}
	
	
	/**
	 * 
	 */
	public static double eastingFromDistanceBearing (final double distance, final double bearing) {
		assert ! Double.isNaN(distance + bearing) : distance + " / " + bearing;
		
		return distance * Math.sin(bearing);
	}
	
	
	/**
	 * 
	 */
	public static double northingFromDistanceBearing (final double distance, final double bearing) {
		assert ! Double.isNaN(distance + bearing) : distance + " / " + bearing;
		
		return distance * Math.cos(bearing);
	}
	
	
}
