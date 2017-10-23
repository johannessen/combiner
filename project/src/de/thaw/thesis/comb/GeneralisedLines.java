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
 * 
 */
public class GeneralisedLines {
	
	private CorrelationGraph graph = null;
	
	private Collection<Line> lines = new LinkedList<Line>();
	
	private Collection<GeneralisedSection> lines1 /*= new LinkedList<GeneralisedSection>()*/;
	
	private Collection<Section> lines2 /*= new LinkedList<Section>()*/;
	
	
	
	/**
	 * 
	 */
	GeneralisedLines () {
	}
	
	
	
	/**
	 * 
	 */
//	public Collection<GeneralisedSection> lines1 () {
//		return Collections.unmodifiableCollection( lines1 );
//	}
	
	
	
	/**
	 * 
	 */
	public Collection<? extends Line> lines () {
		return Collections.unmodifiableCollection( lines );
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
				OsmNode node = i == 0 ? edge.start : edge.end;
				
				
// eigene funktion!
				
				// get segment with ID
				for (final SourceSegment segment : node.connectingSegments) {
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
		final GeneralisedSection section = new GeneralisedSection(graph, edge, node);
		
//		section.filterShortSection();
		if ( ! section.valid() ) {
			return;
		}
		
//		lines1.add(section);
		lines.add(section);
	}
	
	
	
	void cleanup (final Dataset dataset) {
		concatUncombinedLines(dataset);
		relocateGeneralisedNodes();
//		cleanup2();
	}
	
	
	
	void concatUncombinedLines (final Dataset dataset) {
		for (final SourceSegment segment : dataset.allSegments()) {
			if (segment.wasGeneralised > 0) {
				continue;
			}
			Section section = new Section(segment);
			
/*
			section.filterShortSection();
			if (! section.valid()) {
				continue;
			}
*/
			
//			lines2.add(section);
			lines.add(section);
		}
	}
	
	
	
	// move nodes such that line ends are no longer dangling, but end on other lines
	void relocateGeneralisedNodes () {
		for (final Line line : lines) {
			
			// just to keep bevahiour intact; we'll prolly want to loose it
			if (! (line instanceof Section)) {
				continue;
			}
			final Section section = (Section)line;
			
// muss eigentlich methode in Line sein! (in GenSection dann nullop)
			
			
			for (int i = 0; i < 2; i++) {
				final OsmNode node = i == 0 ? section.start() : section.end();
				
				// find the closest existing vertex on the generalised section (if any)
				// (there's some collateral damage because the closest point may not be the best one, particularly at major intersections)
				CorrelationEdge theEdge = null;
				for (final CorrelationEdge anEdge : node.edges) {
					if (theEdge == null || anEdge.length() < theEdge.length()) {
						theEdge = anEdge;
					}
				}
				if (theEdge == null) {
					continue;  // section doesn't end on generalised node
				}
				
				// "move" (actually: replace) first/last nodes as appropriate
				
// "Edge" als Namen fuer Typ mit midpoint-logik
				final OsmNode midPoint = theEdge.midPoint();
				
/*
				if (midPoint.id == 0L) {
					midPoint.id = -2L;
				}
*/
				
// i als parameter für for-i-methode mitgeben
				if (i == 0) {
					section.set(0, midPoint);
				}
				else {
					section.set(section.size(), midPoint);  // sic
				}
			}
		}
		
/*
		// remove short sections
		// (very dumb algorithms; we shouldn't remvoe everything)
		Iterator<Section> i = lines2.iterator();
		while (i.hasNext()) {
			Section section = i.next();
			section.filterShortSection();
			if (! section.valid()) {
				i.remove();
			}
		}
*/
	}
	
}
