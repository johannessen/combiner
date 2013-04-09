/* encoding UTF-8
 * 
 * Copyright (c) 2013 Arne Johannessen
 * 
 * This project and all of its individual parts may be used in accordance
 * with the terms of the 3-clause BSD licence. See LICENSE for details.
 */

package de.thaw.thesis.comb;

import java.util.Collection;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.LinkedList;
import java.util.NavigableSet;
import java.util.Set;
import java.util.TreeSet;


/**
 * A set of spatial data in the euclidian plane. Instances may or may not have
 * relationships with actual data from the global OSM planet database.
 */
public final class OsmDataset {
	
	final public static long ID_UNKNOWN = 0L;
	final public static long ID_NONEXISTENT = -1L;  // newly created feature
	
	private boolean dataComplete;
	
	private final NavigableSet<OsmNode> nodes;
	
	final List<OsmWay> ways;
	
	private List<LineSegment> allSegments;
	
	public Collection<LinePart[]> parallelFragments = new LinkedList<LinePart[]>();  // :DEBUG:
	
	public StatSink stats = null;
	
	
	/**
	 * 
	 */
	public OsmDataset () {
		dataComplete = false;
		nodes = new TreeSet<OsmNode>();
		ways = new LinkedList<OsmWay>();
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
	public OsmWay createOsmWay (final OsmTags tags) {
		assert ! dataComplete;
		
		OsmWay way = new OsmWay(tags, this);
		ways.add(way);
		return way;
	}
	
	
	boolean addNode (final OsmNode node) {
		return nodes.add(node);
	}
	
	
	/**
	 * 
	 */
	public OsmNode getNodeAtEastingNorthing (final double e, final double n) {
		
		final OsmNode newNode = OsmNode.createWithEastingNorthing(e, n);
		final boolean didAdd = nodes.add(newNode);
		
		if (! didAdd) {
			final OsmNode existingNode = nodes.floor(newNode);
			assert existingNode.equals(newNode) : newNode + " / " + existingNode;
			return existingNode;
		}
		return newNode;
		
		// if there is a node at this location (or very close by), we assume that this is in fact the node requested; otherwise, a new node is created
		// consequence: `nodes` should be sorted (for speed)!
		// (ideally we'd choose a hashmap and "ably" compute appropriate hashes to do this)
	}
	
	
	/**
	 * 
	 */
	public Collection<OsmNode> allNodes () {
		assert dataComplete;
		
		return Collections.unmodifiableCollection(nodes);
	}
	
	
	/**
	 * 
	 */
	public List<LineSegment> allSegments () {
		assert dataComplete;
		
		if (allSegments == null) {
			// :BUG: expensive; write our own Queue implementation and work directly on its Entry objects
			// (OTOH, this happens only once...)
			final List<LineSegment> list = new LinkedList<LineSegment>();
			for (final OsmWay way : ways) {
				list.addAll(way.segments());
			}
			allSegments = Collections.unmodifiableList(list);
		}
		
		return allSegments;
	}
	
}
