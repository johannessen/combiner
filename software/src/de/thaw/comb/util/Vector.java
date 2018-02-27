/* encoding UTF-8
 * 
 * Copyright (c) 2013 Arne Johannessen
 * 
 * This project and all of its individual parts may be used in accordance
 * with the terms of the 3-clause BSD licence. See LICENSE for details.
 */

package de.thaw.comb.util;


/**
 * A free spatial vector in the euclidian space. Vectors offer methods to
 * obtain their components in the cartesian and the polar coordinate system.
 * They also offer methods for comparing themselves with other
 * <code>Vector</code>s.
 * <p>
 * This interface does not specify any particular unit of length to be used.
 * Implementations are expected to have some sort of internal plane coordinate
 * system (like a single UTM zone) which all coordinate values and vectors
 * refer to. Angular units are defined to be the natural unit (radians).
 * 
 * @see <a href="http://en.wikipedia.org/wiki/Euclidean_vector"
 *  >Euclidean vector (Wikipedia)</a>
 */
public interface Vector {
	
	
	/**
	 * 2 π is the angle of a full circle (in radians; equal to 360°).
	 */
	static final double FULL_CIRCLE = Math.PI * 2.0;
	
	
	/**
	 * π is the angle of a semi-circle (in radians; equal to 180°).
	 */
	static final double SEMI_CIRCLE = Math.PI;
	
	
	/**
	 * ½ π is a right angle (in radians; equal to 90°).
	 */
	static final double RIGHT_ANGLE = Math.PI / 2.0;
	
	
	/**
	 * The ordinate (horizontal / longitudinal) aspect of the vector's
	 * coordinate representation.
	 * 
	 * @return the difference in easting (in internal units)
	 */
	double easting () ;
	
	
	/**
	 * The abscissa (vertical / latitudinal) aspect of the vector's
	 * coordinate representation.
	 * 
	 * @return the difference in northing (in internal units)
	 */
	double northing () ;
	
	
	/**
	 * The vector's length (norm).
	 * 
	 * @return the length (in internal units)
	 */
	double distance () ;
	
	
	/**
	 * The angle from grid north to the vector's direction, measured clockwise.
	 * This value must always be returned in radians, facilitating easy use
	 * with {@link java.lang.Math}.
	 * <p>
	 * For example, if a vector pointed to the west, this method would return
	 * 3/2 π (equal to 270°).
	 * <p>
	 * It is strongly recommended for implementations to return bearings
	 * normalised to the interval [0, 2 π), but clients should not depend upon
	 * this behaviour.
	 * 
	 * @return the vector's bearing (direction)
	 * @see SimpleVector#normaliseAbsoluteBearing
	 * @see <a href="http://en.wikipedia.org/wiki/Bearing_(navigation)"
	 *  >Bearing (Wikipedia)</a>
	 */
	double bearing () ;
	
	
	/**
	 * The angle from this vector's direction to the specified vector's
	 * direction, measured counterclockwise or clockwise, whichever is nearer.
	 * Clockwise measurements return positive values, counterclockwise ones
	 * return negative values. This value must always be returned in radians,
	 * facilitating easy use with {@link Math java.lang.Math}.
	 * <p>
	 * For example, if this vector pointed to the south and the specified
	 * vector pointed east, this method would return −½ π (equal to −90°).
	 * <p>
	 * It is strongly recommended for implementations to return bearings
	 * normalised to the interval [−π, π), but clients should not depend upon
	 * this behaviour.
	 * 
	 * @param v the vector to calculate the bearing of in relation to this
	 *  vector
	 * @return the relative bearing (difference in direction) from this vector
	 *  to the specified vector; if the specified vector is zero-length,
	 *  <code>+0.0</code> must be returned
	 * @throws NullPointerException iff <code>v == null</code>
	 * @see SimpleVector#normaliseRelativeBearing
	 * @see <a href="https://en.wikipedia.org/wiki/Relative_bearing"
	 *  >Relative bearing (Wikipedia)</a>
	 */
	double relativeBearing (Vector v) ;
	
	
	/**
	 * Returns a reversed representation of this vector.
	 * 
	 * @return a view of this vector, reversed
	 */
	Vector reversed () ;
	
	
	/**
	 * Compares this vector to the specified vector with regards to their
	 * convergence. Implementations might use code that basically produces the
	 * same result as the following line does:
	 * <code><pre>
	 * return isAligned(v) ? clone() : reversed();
	 * </pre></code>
	 * If the vectors already are aligned, implementations are free to return
	 * either <code>this</code> itself or another <code>Vector</code> object of
	 * equivalent value, at their option.
	 * 
	 * @param v the vector to be compared with this one
	 * @return a view of <em>this</em> vector that is aligned with
	 *  <code>v</code>
	 * @throws NullPointerException iff <code>v == null</code>
	 * @see #isAligned
	 */
	Vector aligned (Vector v) ;
	
	
	/**
	 * Compares this vector's bearing to the specified vector's bearing. For
	 * the purpose of this interface, two vectors are defined as being
	 * "aligned" if the absolute value of their relative bearing is less than
	 * a right angle. In other words, they are aligned if they form an acute
	 * angle and not aligned if they form an obtuse angle.
	 * <p>
	 * Implementations might use code that basically produces the same result
	 * as the following line does:
	 * <code><pre>
	 * return Math.abs( relativeBearing(v) ) < RIGHT_ANGLE;
	 * </pre></code>
	 * The result of this method if the vectors are orthogonal is undefined.
	 * 
	 * @param v the vector to be compared with this one
	 * @return whether the two vectors differ by an acute angle
	 * @throws NullPointerException iff <code>v == null</code>.
	 * @see #relativeBearing
	 */
	boolean isAligned (Vector v) ;
	
}
