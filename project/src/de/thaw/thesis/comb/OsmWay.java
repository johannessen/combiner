/* encoding UTF-8
 * 
 * Copyright (c) 2013 Arne Johannessen
 * 
 * This project and all of its individual parts may be used in accordance
 * with the terms of the 3-clause BSD licence. See LICENSE for details.
 */

package de.thaw.thesis.comb;

import java.util.List;
import java.util.LinkedList;


/**
 * An ordered collection of several line segments in the euclidian plane.
 * Instances may or may not have relationships with actual ways in the OSM
 * planet database. Instances are always part of an <code>OsmDataset</code>.
 */
public final class OsmWay {
	
	List<LineSegment> segments;
	OsmTags tags;
	OsmDataset dataset;
	
	public long id = OsmDataset.ID_UNKNOWN;  // :BUG: shouldn't be public
	// not all OsmWays have a unique ID (e. g. splitPts, Frederik's shapefile)
	
	private boolean dataComplete;
	
	
	OsmWay (final OsmTags tags, final OsmDataset dataset) {
		assert tags != null && dataset != null;
		
		this.segments = new LinkedList<LineSegment>();
		this.tags = tags;
		this.dataset = dataset;
		this.dataComplete = false;
	}
	
	
	/**
	 * 
	 */
	public void setCompleted () {
		dataComplete = true;
	}
	
	
	/**
	 * 
	 */
	public LineSegment createSegment (final OsmNode node0, final OsmNode node1) {
		assert node0 != null && node1 != null;
		assert ! dataComplete;
		
		LineSegment segment = new LineSegment(node0, node1, this);
		node0.addSegment(segment);
		node1.addSegment(segment);
		segments.add(segment);
		
		return segment;
	}
	
	
	List<LineSegment> segments () {
		assert dataComplete;
		
		return segments;
	}
	
	
	/**
	 * 
	 */
	public OsmTags tags () {
		return tags;
	}
	
}
