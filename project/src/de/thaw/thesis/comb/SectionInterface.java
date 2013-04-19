/* encoding UTF-8
 * 
 * Copyright (c) 2013 Arne Johannessen
 * 
 * This project and all of its individual parts may be used in accordance
 * with the terms of the 3-clause BSD licence. See LICENSE for details.
 */

package de.thaw.thesis.comb;

import java.util.Collection;



public interface SectionInterface {
	
	Collection<OsmNode> combination () ;
	
	OsmTags tags () ;
	
	OsmNode start () ;
	
	OsmNode end () ;
	
}


