/* encoding UTF-8
 * 
 * Copyright (c) 2013 Arne Johannessen
 * 
 * This project and all of its individual parts may be used in accordance
 * with the terms of the 3-clause BSD licence. See LICENSE for details.
 */

package de.thaw.comb.util;


/**
 * An euclidian coordinate.
 */
public interface PlaneCoordinates {
	
	
	/**
	 * The ordinate (horizontal / longitudinal) aspect.
	 */
	double easting () ;
	
	
	/**
	 * The abscissa (vertical / latitudinal) aspect.
	 */
	double northing () ;
	
}
