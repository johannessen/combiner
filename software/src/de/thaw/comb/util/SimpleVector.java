/* encoding UTF-8
 * 
 * Copyright (c) 2013 Arne Johannessen
 * 
 * This project and all of its individual parts may be used in accordance
 * with the terms of the 3-clause BSD licence. See LICENSE for details.
 */

package de.thaw.comb.util;


/**
 * An euclidian vector implementation. Objects are defined either by cartesian
 * differences or -- at the client's option -- by distance and bearing.
 * Instances represent free vectors: The initial and terminal points are not
 * stored, only their difference.
 */
public final class SimpleVector implements Vector {
	
	private double e = Double.NaN;
	private double n = Double.NaN;
	private double d = Double.NaN;
	private double a = Double.NaN;
	
	private SimpleVector reversed = null;
	
	
	private SimpleVector () {
	}
	
	
	/**
	 * Creates a vector from <code>start</code> to <code>end</code>.
	 * 
	 * @param start the initial point
	 * @param end the terminal point
	 * @throws NullPointerException iff <code>start == null || end == null</code>
	 */
	public SimpleVector (final PlaneCoordinates start, final PlaneCoordinates end) {
		e = end.easting() - start.easting();
		n = end.northing() - start.northing();
		
		assert ! Double.isNaN(e + n) : e + " / " + n;
	}
	
	
	/**
	 * Creates a vector given in polar coordinates.
	 * 
	 * @param distance the length
	 * @param bearing the direction
	 * @throws NullPointerException iff <code>start == null || end == null</code>
	 * @see Vector#bearing
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
	 */
	public double easting () {
		if (Double.isNaN(e)) {
			assert ! Double.isNaN(d + a) : d + " / " + a;
			e = d * Math.sin(a);
		}
		
		return e;
	}
	
	
	/**
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
	 * Obtain the distance (in internal coordinates) between two points. This
	 * distance is equivalent to the length of a vector created from the two
	 * points, but this method forgoes creating a new object for this purpose.
	 * 
	 * @return distance between <code>point1</code> and <code>point2</code>
	 * @throws NullPointerException
	 *  iff <code>point1 == null || point2 == null</code>
	 */
	public static double distance (final PlaneCoordinates point1, final PlaneCoordinates point2) {
		final double e = point2.easting() - point1.easting();
		final double n = point2.northing() - point1.northing();
		return Math.sqrt( e*e + n*n );
	}
	
	
	/**
	 */
	public double distance () {
		if (Double.isNaN(d)) {
			assert ! Double.isNaN(e + n) : e + " / " + n;
			d = Math.sqrt( e*e + n*n );
		}
		
		return d;
	}
	
	
	/**
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
	
	
	private double bearingDegrees () {
		return bearing() * 180.0 / Math.PI;
	}
	
	
	/**
	 * The angle from this vector's direction to the specified vector's
	 * direction, measured counterclockwise or clockwise, whichever is nearer.
	 * 
	 * @return relative bearing in interval [−π, π);
	 *  <code>0.0</code> if <code>v</code> is zero-length
	 */
	public double relativeBearing (final Vector v) {
		if (v.distance() == 0.0) {
			/* The concept of bearing doesn't make much sense for vectors with
			 * no length, so we ignore whatever is reported as bearing for the
			 * vector passed in, resulting in a relative bearing of zero.
			 */
			return 0.0;
		}
		return normaliseRelativeBearing(v.bearing() - bearing());
	}
	
	
	/**
	 * Reduces a bearing to the interval [0, 2 π). Bearings returned by this
	 * method are safe to compare with <code>==</code> for congruency.
	 * 
	 * @param bearing the bearing to normalise
	 * @return <code>bearing</code>, normalised
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
	 * Reduces a bearing to the interval [−π, π). Bearings returned by this
	 * method are safe to compare with <code>==</code> for congruency.
	 * 
	 * @param bearing the bearing to normalise
	 * @return <code>bearing</code>, normalised
	 */
	public static double normaliseRelativeBearing (double bearing) {
		while (bearing < SEMI_CIRCLE) {
			bearing += FULL_CIRCLE;
		}
		while (bearing >= SEMI_CIRCLE) {
			bearing -= FULL_CIRCLE;
		}
		return bearing;
	}
	
	
	/**
	 */
	public SimpleVector reversed () {
		if (reversed == null) {
			reversed = new SimpleVector();
			reversed.e = -e;
			reversed.n = -n;
			reversed.d = d;
			reversed.a = normaliseAbsoluteBearing(a + SEMI_CIRCLE);
//			reversed.reversed = this;  // :BUG: memory leak through circular references if a reversed vector is again reversed
		}
		return reversed;
	}
	
	
	/**
	 */
	public SimpleVector aligned (final Vector v) {
		if (! isAligned(v)) {
			return reversed();
		}
		return this;
	}
	
	
	/**
	 */
	public boolean isAligned (final Vector v) {
		return Math.abs(relativeBearing(v)) <= RIGHT_ANGLE;
	}
	
	
	/**
	 * Returns a string representation of this object.
	 * 
	 * @return a string representation of this object
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
	 * Obtain the distance in the ordinate (horizontal / longitudinal) aspect
	 * of a vector given in polar coordinates. This distance is equivalent to
	 * the {@link #easting} of a vector created from the polar coordinates, but
	 * this method forgoes creating a new object for this purpose.
	 * 
	 * @param distance the length
	 * @param bearing the direction
	 * @return easting distance for the specified vector
	 */
	public static double eastingFromDistanceBearing (final double distance, final double bearing) {
		assert ! Double.isNaN(distance + bearing) : distance + " / " + bearing;
		
		return distance * Math.sin(bearing);
	}
	
	
	/**
	 * Obtain the distance in the abscissa (vertical / latitudinal) aspect of
	 * a vector given in polar coordinates. This distance is equivalent to the
	 * {@link #northing} of a vector created from the polar coordinates, but
	 * this method forgoes creating a new object for this purpose.
	 * 
	 * @param distance the length
	 * @param bearing the direction
	 * @return northing distance for the specified vector
	 */
	public static double northingFromDistanceBearing (final double distance, final double bearing) {
		assert ! Double.isNaN(distance + bearing) : distance + " / " + bearing;
		
		return distance * Math.cos(bearing);
	}
	
	
}
