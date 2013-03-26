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
	
	static double MIN_LENGTH = 80.0;  // :TODO: check what works best
	
	public LinkedList<OsmNode> combination = new LinkedList<OsmNode>();
	
	public LinkedList<LineSegment> originals = new LinkedList<LineSegment>();
	
	double length () {
		// :BUG: calculate intermediate segments
		return new Vector( combination.getFirst(), combination.getLast() ).distance();
	}
	
	public CorrelationEdge startConnector = null;
	public CorrelationEdge endConnector = null;
	
}


