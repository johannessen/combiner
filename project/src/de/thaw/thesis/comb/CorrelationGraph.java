/* encoding UTF-8
 * 
 * Copyright (c) 2013 Arne Johannessen
 * 
 * This project and all of its individual parts may be used in accordance
 * with the terms of the 3-clause BSD licence. See LICENSE for details.
 */

package de.thaw.thesis.comb;

import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.Iterator;
//import java.util.LinkedHashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.NavigableSet;
import java.util.TreeSet;


/**
 * 
 */
public final class CorrelationGraph {
	
	final OsmDataset dataset;
	
	CorrelationEdge[] sortedEdges;
	
	NavigableSet<CorrelationEdge> sortedEdgesSet;
	
	Collection<GeneralisedSection> generalisedLines;
	
	
	CorrelationGraph (final OsmDataset dataset) {
		this.dataset = dataset;
		
		generalisedLines = new LinkedList<GeneralisedSection>();
		sortedEdgesSet = new TreeSet<CorrelationEdge>();
		createGraph();
	}
	
	
	void createGraph () {
		
		/* This is reasonably fast because all the inner loops have only very
		 * few items to loop through (about 2 each).
		 */
		
		// ∀ segments S
		final List<LineSegment> segments = dataset.allSegments();
		for (final LineSegment segment : segments) {
			if (segment.leftRealParallels.size() == 0 && segment.rightRealParallels.size() == 0) {
				continue;
			}
			assert ! segment.wasCorrelated;
			
			// ∀ nodes T1 {start,end} of S
			for (int j = 0; j < 2; j++) {
				final OsmNode segmentNode = node(segment, j);
				
				// ∀ sides A {left,right} of S
				for (int i = 0; i < 2; i++) {
					final Collection<LineSegment> side = i == 0 ? segment.leftRealParallels : segment.rightRealParallels;
					
					OsmNode closestNode = null;
					double closestNodeDistance = Double.POSITIVE_INFINITY;
					
					// ∀ parallels P of S on side A
					for (final LineSegment parallel : side) {
						
						// ∀ nodes T2 (of any category) in P
						for (int k = 0; k < 2; k++) {
							final OsmNode parallelNode = node(parallel, k);
							
							final double d = new Vector(segmentNode, parallelNode).distance();
							if (d < closestNodeDistance) {
								closestNodeDistance = d;
								closestNode = parallelNode;
							}
						}
					}
					
					add( segmentNode, closestNode );
				}
			}
			
			// mark S as done "7"
			segment.wasCorrelated = true;
		}
		
		sortedEdges = sortedEdgesSet.toArray(new CorrelationEdge[0]);
		sortedEdgesSet = null;
	}
	
	
	private OsmNode node (final LineSegment segment, final int i) {
		assert i == 0 || i == 1;
		return i == 0 ? segment.start : segment.end;
	}
	
	
	private void add (final OsmNode segmentNode, final OsmNode closestNode) {
		if (closestNode == null) {
			// <=> no parallel exists to S on side A
			return;
		}
		
		// prevent edges from node to parallelNode if a connectedSegment of node leads to parallelNode (fixes #113)
		for (LineSegment connectedSegment : segmentNode.connectingSegments) {
			OsmNode otherNode = segmentNode == connectedSegment.start ? connectedSegment.end : connectedSegment.start;
			if (otherNode == closestNode) {
				assert otherNode != segmentNode : segmentNode;
				return;
			}
		}
		
		if (segmentNode == closestNode) {
			// <=> merge point (e. g. end of dual carriageway)
			// (this condition isn't always true in such situations, so let's skip them entirely and clean up later)
			return;
		}
		
		assert contains(new CorrelationEdge(segmentNode, closestNode)) == contains(new CorrelationEdge(closestNode, segmentNode)) : new CorrelationEdge(segmentNode, closestNode);  // testing Set comparisons
		
		add(new CorrelationEdge( segmentNode, closestNode ));
	}
	
	
	private boolean add (final CorrelationEdge edge) {
		assert sortedEdges == null;  // adding only to collection, not to array (Illegal State)
		return sortedEdgesSet.add(edge);
	}
	
	
	CorrelationEdge get (final OsmNode node0, final OsmNode node1) {
		CorrelationEdge testEdge = new CorrelationEdge(node0, node1);
		return intern(testEdge);
	}
	
	
	boolean contains (final CorrelationEdge edge) {
		if (sortedEdges == null) {
			return sortedEdgesSet.contains(edge);
		}
		return Arrays.binarySearch(sortedEdges, edge) >= 0;
	}
	
	
	// the point of this method is to return the original collection element, enabling the client to operate on that instead of some "equal" clone
	CorrelationEdge intern (final CorrelationEdge edge) {
		assert sortedEdgesSet == null;  // Collection doesn't support get, only from array (Illegal State)
		final int i = Arrays.binarySearch(sortedEdges, edge);
		if (i < 0) {
			return null;  // No Such Element
		}
		return sortedEdges[i];
	}
	
	
	Collection<CorrelationEdge> edges () {
		return Collections.unmodifiableList( Arrays.asList(sortedEdges) );
	}
	
}
