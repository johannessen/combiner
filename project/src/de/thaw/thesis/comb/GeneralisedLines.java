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



/**
 * Façade to the generalisation.
 */
public class GeneralisedLines {
	
	private NodeGraph graph = null;
	
	private Collection<ResultLine> lines = new LinkedList<ResultLine>();
	
	
	
	/**
	 * 
	 */
	GeneralisedLines () {
	}
	
	
	
	/**
	 * 
	 */
	public Collection<ResultLine> lines () {
		return Collections.unmodifiableCollection( lines );
	}
	
	
	
	/**
	 * 
	 */
	void traverse (final NodeGraph graph) {
		this.graph = graph;
//		NodeMatch startMatch = null;  // E_S
//		Node startNode = null;  // E
		
		/* This is reasonably fast because both of the inner loops usually have
		 * only 2 or 3 items to loop through.
		 */
		
		// (TG 1) choose segment S
		for (final NodeMatch match : graph.matches()) {
			
			for (final SourceNode node : match) {
				
// eigene funktion!
				
				// get segment with ID
				for (final SourceSegment segment : node.connectingSegments()) {
					if (segment.wasGeneralised > 0 || segment.notToBeGeneralised) {
						continue;
					}
					
					// :FIX: #111 - backward/forward logic can't handle junctions
					if (node.connectingSegments().size() > 2
							|| match.other(node).connectingSegments().size() > 2) {
						continue;
					}
					
					generaliseSectionAt(match, node);
				}
			}
		}
	}
	
	
	
	/**
	 * 
	 */
	private void generaliseSectionAt (final NodeMatch match, final SourceNode node) {
		final GeneralisedSection section = new GeneralisedSection(graph, match, node);
		
//		section.filterShortSection();
		if ( ! section.valid() ) {
			return;
		}
		
		lines.add(section);
	}
	
	
	
	void cleanup () {
		for (final ResultLine line : lines) {
			line.relocateGeneralisedNodes();
		}
	}
	
	
	
	void concatUncombinedLines (final Dataset dataset) {
		for (final SourceSegment segment : dataset.allSegments()) {
			if (segment.wasGeneralised > 0) {
				continue;
			}
			ConcatenatedSection section = new ConcatenatedSection(segment);
			
/*
			section.filterShortSection();
			if (! section.valid()) {
				continue;
			}
*/
			
			lines.add(section);
		}
	}
	
}
