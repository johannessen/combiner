/* encoding UTF-8
 * 
 * Copyright (c) 2013 Arne Johannessen
 * 
 * This project and all of its individual parts may be used in accordance
 * with the terms of the 3-clause BSD licence. See LICENSE for details.
 */

package de.thaw.thesis.comb;

import de.thaw.thesis.comb.util.SpatialFeature;

import java.util.Collection;
import java.util.List;



/** An ordered string of segments defining a contiguous line. */
public interface Line extends List<SourceSegment>, SpatialFeature {
	
	Dataset dataset () ;  // optional
	
	long id () ;  // optional
	
	OsmTags tags () ;
	
	HighwayType type () ;
	
	HighwayRef ref () ;
	
	Node start () ;
	
	Node end () ;
	
	int size () ;  // segment count!
	// iterateable: segments
	
	Iterable<Node> coordinates () ;  // nodes
	
}


