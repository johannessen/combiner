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
public interface Vector {
	
	static final double FULL_CIRCLE = Math.PI * 2.0;
	static final double HALF_CIRCLE = Math.PI;
	static final double RIGHT_ANGLE = Math.PI / 2.0;
	
	
	/**
	 * 
	 */
	double easting () ;
	
	
	/**
	 * 
	 */
	double northing () ;
	
	
	/**
	 * 
	 */
	double distance () ;
	
	
	/**
	 * 
	 */
	double bearing () ;
	
	
	/**
	 * 
	 */
	double relativeBearing (Vector v) ;
	
	
	/**
	 * 
	 */
	Vector reversed () ;
	
	
	/**
	 * 
	 */
	Vector aligned (Vector v) ;
	
	
	/**
	 * 
	 */
	boolean isAligned (Vector v) ;
	
}
