/* encoding UTF-8
 * 
 * Copyright (c) 2013 Arne Johannessen
 * 
 * This project and all of its individual parts may be used in accordance
 * with the terms of the 3-clause BSD licence. See LICENSE for details.
 */

package de.thaw.thesis.comb;

import de.thaw.thesis.comb.util.SimpleVector;

import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.Iterator;
//import java.util.LinkedHashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.NavigableSet;
import java.util.TreeSet;


// ex CorrelationGraph
/**
 * A graph of segments and node matches, to be used for generalisation.
 */
public final class NodeGraph {
	
	final Dataset dataset;
	
	NodeMatch[] sortedEdges;
	
	NavigableSet<NodeMatch> sortedEdgesSet;
	
	
	NodeGraph (final Dataset dataset) {
		this.dataset = dataset;
		
		sortedEdgesSet = new TreeSet<NodeMatch>();
		createGraph();
	}
	
	
	void createGraph () {
		
		/* This is reasonably fast because all the inner loops have only very
		 * few items to loop through (about 2 each).
		 */
		
		// ∀ segments S
		final List<SourceSegment> segments = dataset.allSegments();
		for (final SourceSegment segment : segments) {
			if (segment.leftRealParallels.size() == 0 && segment.rightRealParallels.size() == 0) {
				continue;
			}
			assert ! segment.wasCorrelated;
			
			// ∀ nodes T1 {start,end} of S
			for (int j = 0; j < 2; j++) {
				final SourceNode segmentNode = node(segment, j);
				
				// ∀ sides A {left,right} of S
				for (int i = 0; i < 2; i++) {
					final Collection<SourceSegment> side = i == 0 ? segment.leftRealParallels : segment.rightRealParallels;
					
					SourceNode closestNode = null;
					double closestNodeDistance = Double.POSITIVE_INFINITY;
					
					// ∀ parallels P of S on side A
					for (final SourceSegment parallel : side) {
						
						// ∀ nodes T2 (of any category) in P
						for (int k = 0; k < 2; k++) {
							final SourceNode parallelNode = node(parallel, k);
							
							final double d = SimpleVector.distance(segmentNode, parallelNode);
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
		
		sortedEdges = sortedEdgesSet.toArray(new NodeMatch[0]);
		sortedEdgesSet = null;
	}
	
	
	private SourceNode node (final SourceSegment segment, final int i) {
		assert i == 0 || i == 1;
		return i == 0 ? segment.start() : segment.end();
	}
	
	
	private void add (final SourceNode segmentNode, final SourceNode closestNode) {
		if (closestNode == null) {
			// <=> no parallel exists to S on side A
			return;
		}
		
		// prevent edges from node to parallelNode if a connectedSegment of node leads to parallelNode (fixes #113)
/*
		for (SourceSegment connectedSegment : segmentNode.connectingSegments) {
			Node otherNode = segmentNode == connectedSegment.start ? connectedSegment.end : connectedSegment.start;
			if (otherNode == closestNode) {
				assert otherNode != segmentNode : segmentNode;
				return;
			}
		}
*/
		
		if (segmentNode == closestNode) {
			// <=> merge point (e. g. end of dual carriageway)
			// (this condition isn't always true in such situations, so let's skip them entirely and clean up later)
			return;
		}
		
		assert contains(new NodeMatch(segmentNode, closestNode)) == contains(new NodeMatch(closestNode, segmentNode)) : new NodeMatch(segmentNode, closestNode);  // testing Set comparisons
		
		add(new NodeMatch( segmentNode, closestNode ));
	}
	
	
	private boolean add (final NodeMatch edge) {
		assert sortedEdges == null;  // adding only to collection, not to array (Illegal State)
		final boolean didAdd = sortedEdgesSet.add(edge);
		if (didAdd) {
			edge.node0().addEdge(edge);
			edge.node1().addEdge(edge);
		}
		return didAdd;
	}
	
	
	NodeMatch get (final SourceNode node0, final SourceNode node1) {
		NodeMatch testEdge = new NodeMatch(node0, node1);
		return intern(testEdge);
	}
	
	
	boolean contains (final NodeMatch edge) {
		if (sortedEdges == null) {
			return sortedEdgesSet.contains(edge);
		}
		return Arrays.binarySearch(sortedEdges, edge) >= 0;
	}
	
	
	// the point of this method is to return the original collection element, enabling the client to operate on that instead of some "equal" clone
	NodeMatch intern (final NodeMatch edge) {
		assert sortedEdgesSet == null;  // Collection doesn't support get, only from array (Illegal State)
		final int i = Arrays.binarySearch(sortedEdges, edge);
		if (i < 0) {
			return null;  // No Such Element
		}
		return sortedEdges[i];
	}
	
	
	Collection<NodeMatch> edges () {
		return Collections.unmodifiableList( Arrays.asList(sortedEdges) );
	}
	
}
