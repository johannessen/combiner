/* encoding UTF-8
 * 
 * Copyright (c) 2013 Arne Johannessen
 * 
 * This project and all of its individual parts may be used in accordance
 * with the terms of the 3-clause BSD licence. See LICENSE for details.
 */

package de.thaw.comb.util;


/**
 * A source of coordinates.
 */
public interface SpatialFeature {
	
	
	/**
	 * Gives an ordered sequence of coordinates specifying this feature's geometry.
	 */
	Iterable<? extends PlaneCoordinates> coordinates () ;
	
}
