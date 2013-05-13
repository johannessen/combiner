/* encoding UTF-8
 * 
 * Copyright (c) 2013 Arne Johannessen
 * 
 * This project and all of its individual parts may be used in accordance
 * with the terms of the 3-clause BSD licence. See LICENSE for details.
 */

package de.thaw.thesis.comb;

import java.util.AbstractSequentialList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.ListIterator;
import java.util.LinkedList;


/**
 * An ordered collection of several line segments in the euclidian plane.
 * Instances may or may not have relationships with actual ways in the OSM
 * planet database. Instances are always part of an <code>OsmDataset</code>.
 */
public final class OsmWay extends AbstractLine {
	
	public long id = OsmDataset.ID_UNKNOWN;  // :BUG: shouldn't be public
	// not all OsmWays have a unique ID (e. g. splitPts, Frederik's shapefile)
	
	OsmDataset dataset = null;
	
	
	OsmWay (final OsmTags tags, final OsmDataset dataset) {
		this(tags, dataset, 10);
	}
	
	
	OsmWay (final OsmTags tags, final OsmDataset dataset, final int segmentCount) {
		super(segmentCount);
		assert tags != null && dataset != null;
		
		super.tags = tags;
		this.dataset = dataset;
	}
	
	
	public OsmDataset dataset () {
		return dataset;
	}
	
	
	public long id () {
		return id;
	}
	
}
