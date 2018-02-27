/* encoding UTF-8
 * 
 * Copyright (c) 2017 Arne Johannessen
 * 
 * This project and all of its individual parts may be used in accordance
 * with the terms of the 3-clause BSD licence. See LICENSE for details.
 */

package de.thaw.comb;



/**
 * A tuple of two defined points in the euclidian plane. This particular
 * interface does not impose a particular ordering on these two points;
 * implementations may hence be explicitly ordered or explicitly unordered.
 */
public interface NodePair {
	
	
	/**
	 * The distance of the two nodes.
	 * 
	 * @return the length between the nodes (in internal units)
	 */
	double distance () ;
	
	
	/**
	 * A node located exactly half-way between the two nodes.
	 * 
	 * @return a (possibly new) Node object between the nodes
	 */
	Node midPoint () ;
	
	
	/**
	 * The opposite node of the pair. If one of the two nodes in the pair is
	 * provided, the one not provided is returned. The behaviour when the given
	 * value is not a node in this pair is currently undefined; clients should
	 * expect an AssertionError in that case.
	 * 
	 * @param node the pair member that shouldn't be returned
	 * @return the pair member that isn't equal to <code>node</code>
	 */
	Node other (Node node) ;
	
}
