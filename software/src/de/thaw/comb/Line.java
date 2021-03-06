/* encoding UTF-8
 * 
 * Copyright (c) 2013 Arne Johannessen
 * 
 * This project and all of its individual parts may be used in accordance
 * with the terms of the 3-clause BSD licence. See LICENSE for details.
 */

package de.thaw.comb;

import de.thaw.comb.highway.HighwayType;
import de.thaw.comb.highway.HighwayRef;
import de.thaw.comb.util.AttributeProvider;
import de.thaw.comb.util.SpatialFeature;

import java.util.Collection;
import java.util.List;



/** An ordered string of segments defining a contiguous line. */
public interface Line extends List<SourceSegment>, SpatialFeature {
	
	Dataset dataset () ;  // optional
	
	long id () ;  // optional
	
	AttributeProvider tags () ;
	
	HighwayType type () ;
	
	HighwayRef ref () ;
	
	Node start () ;
	
	Node end () ;
	
	int size () ;  // segment count!
	// iterateable: segments
	
	Iterable<Node> coordinates () ;  // nodes
	
}


