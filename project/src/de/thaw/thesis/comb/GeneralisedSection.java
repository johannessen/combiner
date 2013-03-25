/* encoding UTF-8
 * 
 * Copyright (c) 2013 Arne Johannessen
 * 
 * This project and all of its individual parts may be used in accordance
 * with the terms of the 3-clause BSD licence. See LICENSE for details.
 */

package de.thaw.thesis.comb;

import java.util.LinkedList;



public class GeneralisedSection {
	
	public LinkedList<OsmNode> combination = new LinkedList<OsmNode>();
	
	double length () {
		// :BUG: calculate intermediate segments
		return new Vector( combination.getFirst(), combination.getLast() ).distance();
	}
	
}


