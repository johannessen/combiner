/* encoding UTF-8
 * 
 * Copyright (c) 2013 Arne Johannessen
 * 
 * This project and all of its individual parts may be used in accordance
 * with the terms of the 3-clause BSD licence. See LICENSE for details.
 */

package de.thaw.comb;

import de.thaw.comb.util.SimpleVector;

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
	
	private final NodeMatch[] NODE_MATCH_ARRAY = new NodeMatch[0];
	
	private NodeMatch[] sortedMatches;
	
	private NavigableSet<NodeMatch> sortedMatchesSet;
	
	
	/**
	 * Create the node matching graph. <p>
	 * 
	 * The segments in the dataset must have been analysed for parallelisms
	 * before this graph is created. Otherwise no matches will be found.
	 * 
	 * @param dataset the dataset for which to create the graph
	 */
	NodeGraph (final Dataset dataset) {
		sortedMatchesSet = new TreeSet<NodeMatch>();
		createGraph(dataset);
	}
	
	
	private void createGraph (final Dataset dataset) {
		
		/* This is reasonably fast because all the inner loops have only very
		 * few items to loop through (about 2 each).
		 */
		
		// ∀ segments S
		final List<SourceSegment> segments = dataset.allSegments();
		for (final SourceSegment segment : segments) {
			if (segment.leftRealParallels.size() == 0 && segment.rightRealParallels.size() == 0) {
				continue;
			}
			
			// ∀ nodes T1 {start,end} of S
			for (int j = 0; j < 2; j++) {
				final SourceNode segmentNode = node(segment, j);
				
				// ∀ sides A {left,right} of S
				for (int i = 0; i < 2; i++) {
					final Collection<SourceSegment> side = i == 0 ? segment.leftRealParallels : segment.rightRealParallels;
					
					/* NodeGraph wird für segments erzeugt, wobei für mehrere
					 * Parallelen der jeweils nahegelegenste Node gesucht wird;
					 * dies ist inkonsequent, nachdem bei der Analyse extra
					 * fragmentiwert wurde und funktioniert bei versetzten
					 * Paralleln vmtl. nicht gut
					 */
					
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
		}
		
		sortedMatches = sortedMatchesSet.toArray(NODE_MATCH_ARRAY);
		sortedMatchesSet = null;
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
		
		// prevent matches of node and parallelNode if a connectedSegment of node leads to parallelNode (fixes #113)
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
	
	
	private boolean add (final NodeMatch match) {
		assert sortedMatches == null;  // adding only to collection, not to array (Illegal State)
		final boolean didAdd = sortedMatchesSet.add(match);
		if (didAdd) {
			match.node0().addMatch(match);
			match.node1().addMatch(match);
		}
		return didAdd;
	}
	
	
	/**
	 * Get the match found in the dataset for the two given nodes.
	 * 
	 * @return the canonical <code>NodeMatch</code> object for this match or
	 *  <code>null</code> if no such match exists in the dataset.
	 */
	public NodeMatch getMatch (final SourceNode node0, final SourceNode node1) {
		if (node0 == null || node1 == null || node0 == node1) {
			return null;
		}
		NodeMatch testMatch = new NodeMatch(node0, node1);
		return intern(testMatch);
	}
	
	
	private boolean contains (final NodeMatch match) {
		if (sortedMatches == null) {
			return sortedMatchesSet.contains(match);
		}
		return Arrays.binarySearch(sortedMatches, match) >= 0;
	}
	
	
	private NodeMatch intern (final NodeMatch match) {
		/* Warum existiert CorrelationGraph.intern? Warum sollten Klone
		 * erzeugt werden? Gibt es wirklich keinen eleganteren Weg?
		 */
		assert sortedMatchesSet == null;  // Collection doesn't support get, only from array (Illegal State)
		final int i = Arrays.binarySearch(sortedMatches, match);
		if (i < 0) {
			return null;  // No Such Element
		}
		return sortedMatches[i];
	}
	
	
	/**
	 * Get all matches found in the dataset.
	 * 
	 * @return a sorted list of matches supporting random access
	 */
	public List<NodeMatch> matches () {
		return Collections.unmodifiableList( Arrays.asList(sortedMatches) );
	}
	
}
