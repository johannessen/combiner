/* encoding UTF-8
 * 
 * Copyright (c) 2013 Arne Johannessen
 * 
 * This project and all of its individual parts may be used in accordance
 * with the terms of the 3-clause BSD licence. See LICENSE for details.
 */

package de.thaw.thesis.comb;

import java.util.List;
import java.util.Iterator;
import java.util.NoSuchElementException;


/**
 * An ordered collection of several line segments in the euclidian plane.
 * Instances represent an OpenStreetMap way, but do not necessarily have a
 * relationship with the OSM planet database. Instances are always part of a
 * <code>Dataset</code>.
 */
public final class OsmWay extends AbstractLine {
	
	
	/**
	 * A number identifying this way in some context.
	 * That this field is currently public is considered a BUG.
	 * @see #id()
	 */
	public long id = Dataset.ID_UNKNOWN;
	// not all OsmWays have a unique ID (e. g. splitPts, Frederik's shapefile)
	
	
	private Dataset dataset = null;
	
	
	
	/**
	 * Create a way with no segments. This is only useful if this way will be
	 * populated with segments before it is used.
	 * Expect this constructor to be deprecated or removed.
	 */
	public OsmWay (final OsmTags tags, final Dataset dataset, final int segmentCount) {
		super(segmentCount);
		assert tags != null && dataset != null;
		
		super.tags = tags;
		super.highwayType = HighwayType.valueOf(tags.get("highway"));
		super.highwayRef = HighwayRef.valueOf(tags.get("ref"));
		this.dataset = dataset;
	}
	
	
	
	/**
	 * Create a way with segments based on a list of nodes.
	 */
	public OsmWay (final OsmTags tags, final Dataset dataset, final List<SourceNode> nodes) {
		this(tags, dataset, nodes.size() - 1);
		
		segmentation(nodes);
	}
	
	
	
	private void segmentation (final List<SourceNode> nodes) {
		if (nodes.size() < 2) {
			throw new IllegalArgumentException("A way must have at least two nodes");
		}
		
		// SEGMENTIERUNG, see chapter 4.3.1
		final Iterator<SourceNode> iterator = nodes.iterator();
		SourceNode prevNode = iterator.next();  // m
		while (iterator.hasNext()) {
			final SourceNode node = iterator.next();  // n
			add(new SourceSegment( prevNode, node, this ));
			prevNode = node;
		}
	}
	
	
	
	/**
	 * The Dataset that this way is a part of.
	 */
	public Dataset dataset () {
		return dataset;
	}
	
	
	
	/**
	 * A number identifying this way in some context. Note that this number is
	 * not necessarily unique. For example, ways from the OSM planet file may
	 * be split into several parts during a preprocessing step. Negative values
	 * imply that this way is not a part of the OSM planet database, but this
	 * should not be relied upon.
	 */
	public long id () {
		return id;
	}
	
}
