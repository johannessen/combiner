/* encoding UTF-8
 * 
 * Copyright (c) 2013 Arne Johannessen
 * 
 * This project and all of its individual parts may be used in accordance
 * with the terms of the 3-clause BSD licence. See LICENSE for details.
 */

package de.thaw.thesis.comb;

import de.thaw.thesis.comb.util.PlaneCoordinate;
import de.thaw.thesis.comb.util.SimpleVector;
import de.thaw.thesis.comb.util.Vector;


/**
 * This class consists exclusively of static utility methods for nodes.
 */
public final class Nodes {
	
	
	// no instances
	private Nodes () {
		throw new UnsupportedOperationException();
	}
	
	
	/**
	 * Creates a new node object for the position provided. This method defines
	 * the parameter ordering, which a Java constructor does not do.
	 */
	public static Node createWithEastingNorthing (final double easting, final double northing) {
		return new NonexistentNode(easting, northing);
	}
	
	
	/**
	 * Creates a new node object for the position defined by the given polar
	 * coordinates.
	 */
	public static Node createWithDistanceBearing (final PlaneCoordinate node, final double distance, final double bearing) {
		return new NonexistentNode(
				node.easting() + SimpleVector.eastingFromDistanceBearing(distance, bearing),
				node.northing() + SimpleVector.northingFromDistanceBearing(distance, bearing) );
	}
	
	
	/**
	 * Creates a new node object for the position defined by the given polar
	 * coordinates.
	 */
/* 
	public static Node createWithDistanceBearing (final PlaneCoordinate node, final Vector vector) {
		return new NonexistentNode(start.easting() + vector.easting(), start.northing() + vector.northing());
	}
*/
	
	
	/**
	 * Creates a new node object for the position defined by the given polar
	 * coordinates. The vector provided is scalar multiplied with the factor
	 * 0.5 to yield the mid point.
	 */
	public static Node createAtMidPoint (final PlaneCoordinate node, final Vector vector) {
		return new NonexistentNode(node.easting() + vector.easting() * 0.5, node.northing() + vector.northing() * 0.5);
	}
	
	
	/**
	 * A simple node that doesn't yet exist in the source dataset. Instances
	 * are only created by utility methods of the Nodes class.
	 */
	final static class NonexistentNode extends AbstractNode {
		
		private NonexistentNode (final double e, final double n) {
			super(e, n);
		}
		
		/**
		 * @return {@link Dataset#ID_NONEXISTENT} - always
		 */
		public long id () {
			return Dataset.ID_NONEXISTENT;
		}
		
	}
	
}
