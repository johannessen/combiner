/* encoding UTF-8
 * 
 * Copyright (c) 2013 Arne Johannessen
 * 
 * This project and all of its individual parts may be used in accordance
 * with the terms of the 3-clause BSD licence. See LICENSE for details.
 */

package de.thaw.comb;

import de.thaw.comb.util.Vector;

import java.util.Collection;
import java.util.Iterator;


// ex LinePart
/**
 * An <em>ordered</em> tuple of two defined points in the euclidian plane.
 * Instances may or may not have relationships with actual points or ways in
 * the OSM planet database. Instances are always indirectly a part of a
 * <code>Dataset</code>.
 */
public interface Segment extends NodePair, Comparable<Segment>, Vector {
	
	// :TODO: filter and organise these methods -- they're prolly not all strictly required to be part of the interface
	// :TODO: rework structure to better fit the Composite pattern
	
	SourceSegment root () ;
	
	/**
	 * Whether this <code>Segment</code> was split into two new
	 * <code>Segment</code>s.
	 * <p>
	 * The SPLITTEN algorithm is defined to remove the original segment that
	 * was split into two parts from the collection of line parts to be split.
	 * Keeping the original would only duplicate work as the splitting is based
	 * on nodes, and both of the original's nodes are also present in its two
	 * parts after it was split. Rather than removing (which would likely be
	 * expensive), we introduce a simple flag to skip any line part that has
	 * already been split.
	 * @see #splitAt(Node,SplitQueueListener)
	 */
	boolean shouldIgnore () ;
	
	Node start () ;
	Node end () ;
	
	Collection<Segment> splitTargets () ;
	Node findPerpendicularFoot (Node node) ;
	void splitCloseParallels (final SplitQueueListener sink) ;
	
	/**
	 * Split this <code>Segment</code> at the given node. This
	 * <code>Segment</code> will be marked as having been split so that it
	 * will be ignored by future processing steps. The split will be reported
	 * to the split queue listener so that the two fragments created by the
	 * split can again be used for splitting.
	 * @see #shouldIgnore()
	 */
	void splitAt (Node node, final SplitQueueListener sink) ;
	
	void analyse (Analyser visitor) ;
	
	void addBestLeftMatch (Segment bestMatch) ;
	void addBestRightMatch (Segment bestMatch) ;
	
	
}
