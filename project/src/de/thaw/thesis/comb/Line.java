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



public interface Line extends List<LineSegment>, SpatialFeature {
	
	OsmDataset dataset () ;  // optional
	
	long id () ;  // optional
	
	OsmTags tags () ;
	
	OsmNode start () ;
	
	OsmNode end () ;
	
	int size () ;  // segment count!
	
	Iterable<OsmNode> coordinates () ;  // nodes
	
}


