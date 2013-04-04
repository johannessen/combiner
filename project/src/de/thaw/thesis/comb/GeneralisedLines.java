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
import java.util.LinkedList;



/**
 * 
 */
public class GeneralisedLines {
	
	private CorrelationGraph graph = null;
	
	private Collection<GeneralisedSection> lines = new LinkedList<GeneralisedSection>();
	
	private Collection<Section> lines2 = new LinkedList<Section>();
	
	
	
	/**
	 * 
	 */
	GeneralisedLines () {
	}
	
	
	
	/**
	 * 
	 */
	public Collection<GeneralisedSection> lines () {
		return Collections.unmodifiableCollection( lines );
	}
	
	
	
	/**
	 * 
	 */
	public Collection<Section> lines2 () {
		return Collections.unmodifiableCollection( lines2 );
	}
	
	
	
	/**
	 * 
	 */
	void traverse (final CorrelationGraph graph) {
		this.graph = graph;
		CorrelationEdge startEdge = null;  // E_S
		OsmNode startNode = null;  // E
		
		/* This is reasonably fast because both of the inner loops usually have
		 * only 2 or 3 items to loop through.
		 */
		
		// (TG 1) choose segment S
		for (final CorrelationEdge edge : graph.edges()) {
				
			for (int i = 0; i < 2; i++) {
				OsmNode node = i == 0 ? edge.node0 : edge.node1;
				
				// get segment with ID
				for (final LineSegment segment : node.connectingSegments) {
					if (segment.wasGeneralised > 0 || segment.notToBeGeneralised) {
						continue;
					}
					
					// :FIX: #111 - backward/forward logic can't handle junctions
					if (node.connectingSegments.size() > 2
							|| edge.other(node).connectingSegments.size() > 2) {
						continue;
					}
					
					generaliseSectionAt(edge, node);
				}
			}
		}
	}
	
	
	
	/**
	 * 
	 */
	private void generaliseSectionAt (final CorrelationEdge edge, final OsmNode node) {
		final GeneralisedSection section = new GeneralisedSection(graph);
		section.startAt(edge, node);
		
//		section.filterShortSection();
		if ( ! section.valid() ) {
			return;
		}
		
		lines.add(section);
	}
	
	
	
	void concatUncombinedLines (final OsmDataset dataset) {
		for (final LineSegment segment : dataset.allSegments()) {
			if (segment.wasGeneralised > 0) {
				continue;
			}
			Section section = new Section();
			section.startAt(segment);
			
//			section.filterShortSection();
			if (! section.valid()) {
				continue;
			}
			
			lines2.add(section);
		}
	}
	
}



