/* encoding UTF-8
 * 
 * Copyright (c) 2013 Arne Johannessen
 * 
 * This project and all of its individual parts may be used in accordance
 * with the terms of the 3-clause BSD licence. See LICENSE for details.
 */

package de.thaw.thesis.comb;

import java.util.Collection;
import java.util.Iterator;


/**
 * An ordered tuple of two defined points in the euclidian plane. Instances may
 * or may not have relationships with actual points or ways in the OSM planet
 * database. Instances are always indirectly a part of an
 * <code>OsmDataset</code>.
 */
public interface LinePart extends Comparable<LinePart> {
	
	// :TODO: filter and organise these methods -- they're prolly not all strictly required to be part of the interface
	// :TODO: rework structure to better fit the Composite pattern
	
	LineSegment segment () ;
	
	Vector vector () ;
	
	boolean wasSplit () ;
	
	Collection<? extends LinePart> lineParts () ;
	
	OsmNode start () ;
	OsmNode end () ;
	OsmNode midPoint () ;
	
	Collection<LinePart> splitTargets () ;
	OsmNode findPerpendicularFoot (OsmNode node) ;
	void splitCloseParallels (final SplitQueueListener sink) ;
	void splitAt (OsmNode node, final SplitQueueListener sink) ;
	
	void analyse (Analyser visitor) ;
	
	void addBestLeftMatch (LinePart bestMatch) ;
	void addBestRightMatch (LinePart bestMatch) ;
	
	
}
