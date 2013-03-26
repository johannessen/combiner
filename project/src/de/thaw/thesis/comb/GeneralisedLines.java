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
import java.util.LinkedList;
import java.util.List;



public class GeneralisedLines {
	
	private List<GeneralisedSection> lines;
	
	private OsmNode startNode = null;  // E_S
	private CorrelationEdge startEdge = null;  // E
	
	
	
	GeneralisedLines () {
		lines = new LinkedList<GeneralisedSection>();
	}
	
	
	
	Collection<GeneralisedSection> lines () {
		List<GeneralisedSection> immutableLines = Collections.unmodifiableList( lines );
		lines = immutableLines;
		return immutableLines;
	}
	
	
	
	void traverse (CorrelationGraph graph) {
		// :TODO: is this loop terminating?
		while (true) {
			findStartEdge(graph);
			if ( startEdge == null || startNode == null ) {
				break;
			}
			
			GeneralisedSection section = new GeneralisedSection(graph);
			section.startAt(startEdge, startNode);
			section.filterShortSection();
			if ( ! section.valid() ) {
				continue;
			}
			lines.add(section);
		}
	}
	
	
	
	void findStartEdge (CorrelationGraph graph) {
		startEdge = null;
		startNode = null;
		
		// we don't actually care where to start
		
		// (TG 1) choose segment S
		for (final CorrelationEdge edge : graph.edges()) {
				
			OsmNode[] edgeNodes = new OsmNode[]{ edge.node0, edge.node1 };
			for (int i = 0; i < 2; i++) {
				OsmNode node = edgeNodes[i];
				
				// get segment with ID
				for (final LineSegment segment : node.connectingSegments) {
					if (segment.wasGeneralised > 0 || segment.notToBeGeneralised) {
						continue;
					}
					
					if (node.connectingSegments.size() > 2
							|| edge.other(node).connectingSegments.size() > 2) {
						continue;
					}
					
					startEdge = edge;
					startNode = node;
					
					return;
				}
			}
		}
	}
	
}
