/* encoding UTF-8
 * 
 * Copyright (c) 2013 Arne Johannessen
 * 
 * This project and all of its individual parts may be used in accordance
 * with the terms of the 3-clause BSD licence. See LICENSE for details.
 */

package de.thaw.thesis.comb;

import java.util.Collection;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.LinkedList;
import java.util.NavigableSet;
import java.util.Set;
import java.util.TreeSet;


/**
 * A set of spatial data in the euclidian plane. Instances may or may not have
 * relationships with actual data from the global OSM planet database.
 */
public interface OsmDataset {
	
	final public static long ID_UNKNOWN = 0L;
	final public static long ID_NONEXISTENT = -1L;  // newly created feature
	
	Collection<LinePart[]> parallelFragments () ;  // :DEBUG:
	
	
	/**
	 * 
	 */
	List<OsmWay> ways () ;
	
	
	/**
	 * 
	 */
	OsmNode getNode (final OsmNode newNode) ;
	
	
	/**
	 * 
	 */
	OsmNode getNodeAtEastingNorthing (final double e, final double n) ;
	
	
	/**
	 * 
	 */
	Collection<OsmNode> allNodes () ;
	
	
	/**
	 * 
	 */
	List<LineSegment> allSegments () ;
	
}
