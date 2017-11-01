/* encoding UTF-8
 * 
 * Copyright (c) 2013 Arne Johannessen
 * 
 * This project and all of its individual parts may be used in accordance
 * with the terms of the 3-clause BSD licence. See LICENSE for details.
 */

package de.thaw.thesis.comb.io;

import de.thaw.thesis.comb.Dataset;
import de.thaw.thesis.comb.Node;
import de.thaw.thesis.comb.Nodes;
import de.thaw.thesis.comb.Segment;
import de.thaw.thesis.comb.SourceNode;
import de.thaw.thesis.comb.SourceSegment;
import de.thaw.thesis.comb.highway.Highway;
//import de.thaw.thesis.comb.io.StatSink;
import de.thaw.thesis.comb.util.AttributeProvider;

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
public final class InputDataset implements Dataset {
	
	final public static long ID_UNKNOWN = 0L;
	final public static long ID_NONEXISTENT = -1L;  // newly created feature
	
	private final NavigableSet<Node> nodes;
	
	private final List<Highway> ways;
	
	private List<SourceSegment> allSegments = null;
	
	public Collection<Segment[]> parallelFragments = new LinkedList<Segment[]>();  // :DEBUG:
	
//	public StatSink stats = null;
	
	
	/**
	 * 
	 */
	public InputDataset () {
		nodes = new TreeSet<Node>();
		ways = new LinkedList<Highway>();
	}
	
	
	/**
	 * Create a way in this dataset with no segments. This is only useful if
	 * this way will be populated with segments before it is used.
	 * Expect this method to be deprecated or removed.
	 */
	public Highway createOsmWay (final AttributeProvider tags, final int segmentCount) {
		Highway way = new Highway(tags, this, segmentCount);
		ways.add(way);
		return way;
	}
	
	
	/**
	 * Create a way in this dataset with segments based on a list of nodes.
	 */
	public Highway createOsmWay (final AttributeProvider tags, final List<SourceNode> nodes) {
		final Highway way = new Highway(tags, this, nodes);
		ways.add(way);
		return way;
	}
	
	
	/**
	 * 
	 */
	public Node getNode (final Node newNode) {
		final boolean didAdd = nodes.add(newNode);
		if (! didAdd) {
			final Node existingNode = nodes.floor(newNode);
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
	public Node getNodeAtEastingNorthing (final double e, final double n) {
		return getNode( Nodes.createWithEastingNorthing(e, n) );
		/* This wastes some memory by creating a Node instance that is only
		 * used for retrieving the equal canonical instance already present in
		 * the node collection. However, directly searching the node collection
		 * for the primitive coordinates isn't possible because the Java
		 * Collection Framework requires a single object for the comparison;
		 * it can't use the decomposed parts. We /might/ be able to work around
		 * this by creating our own SortedSet implementation (e. g. hand-tune
		 * the java.util.TreeSet class's source code accordingly).
		 */
	}
	
	
	/**
	 * 
	 */
	public Collection<Node> allNodes () {
		return Collections.unmodifiableCollection(nodes);
	}
	
	
	/**
	 * 
	 */
	public List<SourceSegment> allSegments () {
		
		if (allSegments == null) {
			// :BUG: expensive; write our own Queue implementation and work directly on its Entry objects
			// (OTOH, this happens only once...)
			final List<SourceSegment> list = new LinkedList<SourceSegment>();
			for (final Highway way : ways) {
				list.addAll(way);
			}
			allSegments = Collections.unmodifiableList(list);
		}
		
		return allSegments;
	}
	
	
	/**
	 * 
	 */
	public Collection<Segment[]> parallelFragments () {
		return parallelFragments;  // :DEBUG: full access
	}
	
	
	/**
	 * 
	 */
	public List<Highway> ways () {
		return Collections.unmodifiableList(ways);
	}
	
}
