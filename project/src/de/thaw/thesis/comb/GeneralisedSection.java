/* encoding UTF-8
 * 
 * Copyright (c) 2013 Arne Johannessen
 * 
 * This project and all of its individual parts may be used in accordance
 * with the terms of the 3-clause BSD licence. See LICENSE for details.
 */

package de.thaw.thesis.comb;

import de.thaw.thesis.comb.util.SimpleVector;
import de.thaw.thesis.comb.util.Vector;

import java.util.AbstractSequentialList;
import java.util.Iterator;
import java.util.Collection;
import java.util.Collections;
import java.util.LinkedList;
import java.util.ListIterator;



public class GeneralisedSection extends AbstractLine {
	
	static double MIN_LENGTH = 50.0;  // :TODO: check what works best
	
	public LinkedList<SourceSegment> originalSegments = new LinkedList<SourceSegment>();
	public LinkedList<OsmNode> originalNodes = new LinkedList<OsmNode>();
	
	public CorrelationEdge startConnector = null;
	public CorrelationEdge endConnector = null;
	
	private boolean valid = false;
	
	private CorrelationGraph graph;
	
	
	
	CorrelationEdge startEdge = null;  // E
	OsmNode startNode = null;  // E_S
	OsmWay startWay = null;
	
	HighwayType osmHighway = null;
	String osmRef = null;
	
	GeneralisedSection (final CorrelationGraph graph, final CorrelationEdge edge, final OsmNode node) {
		this.graph = graph;
		
		startEdge = edge;
		startNode = node;
		
		if (startNode == null) {
			valid = false;
			return;
		}
		
		addConnector(startEdge, true);
		addConnector(startEdge, false);
		
		assert startNode.connectingSegments.size() <= 2 : startNode;  // see issue #111
		assert startNode.connectingSegments.size() > 0 : startNode;
		
		
		Iterator<SourceSegment> iterator = startNode.connectingSegments.iterator();
		for (int i = 0; i < 2 && iterator.hasNext(); i++) {
			final SourceSegment segment = iterator.next();
			
			// move into both directions along segments from startNode
			boolean forward = i == 0;
			
			if (segment.wasGeneralised > 0) {
				continue;
			}
			
			CorrelationEdge lastEdge = traverseGraph(segment, forward);
			if (lastEdge == startEdge) {
				continue;
			}
			
			addConnector(lastEdge, forward);
		}
		
		assert ! iterator.hasNext() : startNode;  // see issue #111
		
		valid = size() >= 2;
		
		if (valid) {
			highwayType = osmHighway;
			highwayRef = osmRef == null || osmRef.length() == 0 ? HighwayRef.valueOf("") : HighwayRef.valueOf(osmRef);
			tags = new Tags(osmHighway.name(), osmRef);
		}
	}
	
	
	
	SourceSegment segment1 = null;  // A
	SourceSegment segment2 = null;  // B
	boolean segment1Aligned = true;
	boolean segment2Aligned = true;
	OsmNode currentNode1 = null;  // E_S
	OsmNode currentNode2 = null;  // E_T
	CorrelationEdge currentEdge = null;  // E
	
	private CorrelationEdge traverseGraph (final SourceSegment segment, final boolean forward) {
		
		currentEdge = startEdge;
		currentNode1 = startNode;
		currentNode2 = startEdge.other(startNode);
		segment1 = segment;
		segment1Aligned = segment1.start == currentNode1;
		segment2 = findOppositeSegment(currentNode2, segment1, segment1Aligned);
		if (segment2 == null) {
			segment1.notToBeGeneralised = true;  // no parallels == no need for generlaisation
			// :TODO: does this occur? if so, why? -- doesn't matte rmuch, it seems to do the right thing anyhow
			return null;
		}
		segment2Aligned = segment2.start == currentNode2;
		
		if (forward) {
			addGeneralisedPoint(startEdge, true);
			currentNode1.generalisedSections.add(this);
			currentNode2.generalisedSections.add(this);
			originalNodes.add(currentNode1);
			originalNodes.add(currentNode2);
			
			osmHighway = segment1.way.type();
			osmRef = "";
			// special values for osmRef:
			// ""/OsmTags.NO_VALUE -> no known ref; null -> conflicting refs
		}
		
		// (TG 3a)
		// :TODO: this condition can surely be simplified
		while (currentEdge != null && segment1 != null && segment2 != null && (segment1.wasGeneralised == 0 || segment2.wasGeneralised == 0)) {
			
			if (segment2.way.type() != osmHighway && segment1.way.type() != osmHighway) {
//System.err.println(" --break1 " + segment2.way.tags.get("highway") + segment2.way.tags.get("ref") + " " + segment1.way.tags.get("highway") + segment1.way.tags.get("ref") + " " + osmHighway);
				break;  // apparently the highway class has changed somewhere along the line; let's interrupt the generalised line and leave the continuation for the next run (which may then pick up the lower class)
			}
//			if (segment2.way.type() != osmHighway) {
				if (segment2.way.type().compareTo(osmHighway) > 0) {
					osmHighway = segment2.way.type();
				}
//			}
			if (osmRef != null) {
				// osmRef == null  -> conflicting refs
				// we're dealing with intern()ed values here, so == is safe to use
				if (osmRef == "") {
					osmRef = segment2.way.tags().get("ref");
				}
				if (osmRef == "") {
					osmRef = segment1.way.tags().get("ref");
				}
				if (osmRef != "" &&
						( osmRef != segment1.way.tags().get("ref")
						|| osmRef != segment2.way.tags().get("ref") )) {
					osmRef = null;
				}
			}
			
			currentEdge.genCounter += 1;  // :DEBUG:
			
			// (TG 5) find next nodes
			OsmNode nextNode1 = segment1Aligned ? segment1.end : segment1.start;  // X
			OsmNode nextNode2 = segment2Aligned ? segment2.end : segment2.start;  // Y
			
			CorrelationEdge nextEdge;
			
			// (TG 6/7)
			nextEdge = graph.get(nextNode1, currentNode2);
			if ( nextEdge != null && nextNode1 != currentNode1 ) {
				
				// Fall "es gibt eine Kante vom naechsten Punkt-1 (X) zurueck zum aktuellen Punkt-2 (E_T)"
				
				advance1(nextNode1);
				currentEdge = nextEdge;
				
			}
			else {
				nextEdge = graph.get(nextNode2, currentNode1);
				if ( nextEdge != null && nextNode2 != currentNode2 ) {
					
					// Fall "es gibt eine Kante vom naechsten Punkt-2 (Y) zurueck zum aktuellen Punkt-1 (E_S)"
					
					advance2(nextNode2);
					currentEdge = nextEdge;
					
				}
				else {
					nextEdge = graph.get(nextNode1, nextNode2);
					if ( nextEdge != null && (nextNode1 != currentNode1 || nextNode2 != currentNode2) ) {
						
						// Fall "es gibt zwei naechste unabhaengige Segmente, die auch parallel sind und es gibt agnz normal eine naechste kante"
						
						advance1(nextNode1);
						advance2(nextNode2);
						currentEdge = nextEdge;
						
						
					}
					else {
						
						// Fall "es gibt naechste unabhaengige Segmente, die aber nicht parallel sind"
						assert nextEdge == null || nextEdge == currentEdge;
						
						segment1.notToBeGeneralised = segment1.wasGeneralised == 0;
						segment2.notToBeGeneralised = segment2.wasGeneralised == 0;
						
						segment1 = null;  // break
					}
				}
			}
			
			
			addGeneralisedPoint(currentEdge, forward);
			
		}
		
		return currentEdge;
	}
	
	
	
	void advance1 (OsmNode nextNode1) {
		
		nextNode1.generalisedSections.add(this);
		originalNodes.add(nextNode1);
		
		segment1.wasGeneralised += 1;
		originalSegments.add(segment1);

		SourceSegment nextSegment1 = findNextSegment(nextNode1, segment1);
		if (nextSegment1 != null) {
			segment1Aligned ^= ! nextSegment1.isAligned(segment1);  // :TODO: unclear; can prolly be replaced by node comparison
		}
		segment1 = nextSegment1;

		currentNode1 = nextNode1;
	}
	
	
	
	void advance2 (OsmNode nextNode2) {
		
		nextNode2.generalisedSections.add(this);
		originalNodes.add(nextNode2);
		
		segment2.wasGeneralised += 1;
		originalSegments.add(segment2);

		SourceSegment nextSegment2 = findNextSegment(nextNode2, segment2);
		if (nextSegment2 != null) {
			segment2Aligned ^= ! nextSegment2.isAligned(segment2);  // :TODO: unclear; can prolly be replaced by node comparison
		}
		segment2 = nextSegment2;

		currentNode2 = nextNode2;
	}
	
	
	
	private void addConnector (final CorrelationEdge edge, final boolean addAtEnd) {
		if (addAtEnd) {
			endConnector = edge;
		}
		else {
			assert ! addAtEnd;
			startConnector = edge;
		}
	}
	
	
	
	// (TG 4) find M
	private void addGeneralisedPoint (CorrelationEdge edge, boolean addAsLast) {
		if (edge == null) {
			return;
		}
		
		double e = (edge.start.e + edge.end.e) / 2.0;
		double n = (edge.start.n + edge.end.n) / 2.0;
//			final OsmNode midPoint = OsmNode.createWithEastingNorthing(e, n);
		final OsmNode midPoint = graph.dataset.getNodeAtEastingNorthing(e, n);  // :TODO: why this line? perhaps so that the node is inserted into the debug output?
		if (addAsLast) {
			addLast( midPoint );
		}
		else {
			addFirst( midPoint );
		}
	}
	
	
	
	// (TG 2a) find T
	private SourceSegment findOppositeSegment (OsmNode oppositeNode, SourceSegment thisSegment, boolean thisIsAligned) {
		SourceSegment oppositeSegment = null;
		
		Vector thisVector = thisIsAligned ? thisSegment : thisSegment.reversed();
		for (SourceSegment segment : oppositeNode.connectingSegments) {
			Vector vector = segment.start == oppositeNode ? segment : segment.reversed();
			if ( Math.abs( vector.relativeBearing(thisVector) ) < Vector.RIGHT_ANGLE ) {
//					assert oppositeSegment == null;  // only one should match, normally (at least for trivial datasets?) (might be some exceptions in some datasets, this is a :HACK:)
				oppositeSegment = segment;
			}
			
		}
		return oppositeSegment;
	}
	
	
	
	private SourceSegment findNextSegment (OsmNode pivot, SourceSegment currentSegment) {
		SourceSegment nextSegment = null;
		for (SourceSegment segment : pivot.connectingSegments) {
			if (segment != currentSegment) {
//					assert nextSegment == null;  // :BUG: handles trivial case only
				nextSegment = segment;
			}
		}
		if (nextSegment == null) {
			return currentSegment;
		}
		return nextSegment;
	}
	
	
	
	boolean valid () {
		return valid;
	}
	
	
	
	public long id () {
		return Dataset.ID_NONEXISTENT;
	}
	
	
	
	double length () {
		if (size() < 2) {
			return 0.0;
		}
		// :BUG: calculate intermediate segments
		return SimpleVector.distance( start(), end() );
	}
	
	
	
	void ungeneralise () {
		for (SourceSegment segment : originalSegments) {
			segment.wasGeneralised -= 1;
			segment.notToBeGeneralised = true;  // avoid infinite loop in GeneralisedLines#traverse()
			
			for (OsmNode node : originalNodes) {
				while ( node.generalisedSections.remove(this) ) {
					// remove as side effect of the loop condition
				}
			}
			originalNodes.clear();
		}
	}
	
	
	
	void filterShortSection () {
//		assert valid == true;
		
		if (length() < MIN_LENGTH) {
			valid = false;
			ungeneralise();
		}
	}
	
	
	
	private static class Tags implements OsmTags {
		private final String highway;
		private final String ref;
		Tags (final String highway, final String ref) {
			this.highway = highway != null ? highway.intern() : OsmTags.NO_VALUE;
			this.ref = (ref != null && ref.length() > 0) ? ref.intern() : OsmTags.NO_VALUE;
		}
		public String get (final String key) {
			if (key.equals("highway")) {
				return highway;
			}
			if (key.equals("ref")) {
				return ref;
			}
			return OsmTags.NO_VALUE;
		}
	}
	
}


