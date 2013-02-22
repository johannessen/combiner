/* encoding UTF-8
 * 
 * Copyright (c) 2013 Arne Johannessen
 * 
 * This project and all of its individual parts may be used in accordance
 * with the terms of the 3-clause BSD licence. See LICENSE for details.
 */

package de.thaw.thesis.comb;

import de.thaw.thesis.comb.util.OneItemList;

import java.util.Collection;
import java.util.Iterator;
import java.util.LinkedList;



/**
 * A <code>LinePart</code> implementation representing incomplete
 * <em>fragments</em> of segments read from source data.
 */
final class LineFragment extends AbstractLinePart implements LinePart {
	
	// :TODO: rework structure to better fit the Composite pattern
	
	
	// LineFragment
	LineSegment segment;
	
	
	LineFragment (final OsmNode start, final OsmNode end, final LineSegment segment) {
		super(start, end);
		assert segment != null;
		
		this.segment = segment;
	}
	
	
	/**
	 * 
	 */
	public LineSegment segment () {
		return segment;
	}
	
	
	/**
	 * 
	 */
	public Collection<? extends LinePart> lineParts () {
		return new OneItemList<LineFragment>(this);
	}
	
}
