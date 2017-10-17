/* encoding UTF-8
 * 
 * Copyright (c) 2013 Arne Johannessen
 * 
 * This project and all of its individual parts may be used in accordance
 * with the terms of the 3-clause BSD licence. See LICENSE for details.
 */

package de.thaw.thesis.comb;


/**
 * Should be implemented by an object that wants to receive notifications of
 * <code>Segment</code> splits. In particular, this is of interest to objects
 * that maintain a queue of line parts yet to be considered as a base for
 * splitting <em>other</em> lines, as any split makes it necessary to (re)visit
 * the newly created fragments for split base consideration.
 */
interface SplitQueueListener extends java.util.EventListener {
	
	// tight coupling instead of the usual event object interface for optimisation
	
	
	/**
	 * 
	 */
	void didSplit (Segment fragment1, Segment fragment2, OsmNode splitNode) ;
	
}
