/* encoding UTF-8
 * 
 * Copyright (c) 2013 Arne Johannessen
 * 
 * This project and all of its individual parts may be used in accordance
 * with the terms of the 3-clause BSD licence. See LICENSE for details.
 */

package de.thaw.comb;

import de.thaw.comb.util.PlaneCoordinates;


/**
 * A defined point in the euclidian plane. Instances may or may not have
 * relationships with actual nodes in the OSM planet database, and may or may
 * not be part of a <code>Dataset</code>.
 */
// Comparable because...?
public interface Node extends PlaneCoordinates, Comparable<Node> {
	
	
	/**
	 * A number identifying this node in some context. Note that this number is
	 * not necessarily unique. For example, nodes newly created will have a
	 * special value signifying the fact that they're not (yet) part of the OSM
	 * planet file.
	 */
	long id () ;
	
}
