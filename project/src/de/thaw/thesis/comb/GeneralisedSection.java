/* encoding UTF-8
 * 
 * Copyright (c) 2013 Arne Johannessen
 * 
 * This project and all of its individual parts may be used in accordance
 * with the terms of the 3-clause BSD licence. See LICENSE for details.
 */

package de.thaw.thesis.comb;

import java.util.Iterator;
import java.util.Collection;
import java.util.Collections;
import java.util.LinkedList;



public class GeneralisedSection implements SectionInterface {
	
	static double MIN_LENGTH = 80.0;  // :TODO: check what works best
	
	public LinkedList<OsmNode> combination = new LinkedList<OsmNode>();
	
	public LinkedList<LineSegment> originals = new LinkedList<LineSegment>();
	
	public CorrelationEdge startConnector = null;
	public CorrelationEdge endConnector = null;
	
	private boolean valid = false;
	
	private CorrelationGraph graph;
	
	private OsmTags tags = null;
	
	
	
	GeneralisedSection (CorrelationGraph theGraph) {
		graph = theGraph;
	}
	
	
	
	boolean valid () {
		return valid;
	}
	
	
	
	public OsmTags tags () {
		return tags;
	}
	
	
	
	public Collection<OsmNode> combination () {
		return Collections.unmodifiableCollection(combination);
	}
	
	
	
	CorrelationEdge startEdge = null;  // E
	OsmNode startNode = null;  // E_S
	OsmWay startWay = null;
	
	String osmHighway = null;
	String osmRef = null;
	
	void startAt (final CorrelationEdge edge, final OsmNode node) {
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
		
		
		Iterator<LineSegment> iterator = startNode.connectingSegments.iterator();
		for (int i = 0; i < 2 && iterator.hasNext(); i++) {
			final LineSegment segment = iterator.next();
			
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
		
		valid = combination.size() >= 2;
		
		if (valid) {
			tags = new Tags(osmHighway, osmRef);
		}
	}
	
	
	
	LineSegment segment1 = null;  // A
	LineSegment segment2 = null;  // B
	boolean segment1Aligned = true;
	boolean segment2Aligned = true;
	OsmNode currentNode1 = null;  // E_S
	OsmNode currentNode2 = null;  // E_T
	CorrelationEdge currentEdge = null;  // E
	
	private CorrelationEdge traverseGraph (final LineSegment segment, final boolean forward) {
		
		currentEdge = startEdge;
		currentNode1 = startNode;
		currentNode2 = startEdge.other(startNode);
		segment1 = segment;
		segment1Aligned = segment1.start == currentNode1;
		segment2 = findOppositeSegment(currentNode2, segment1, segment1Aligned);
		if (segment2 == null) {
			segment1.notToBeGeneralised = true;  // no parallels == no need for generlaisation
			return null;
		}
		segment2Aligned = segment2.start == currentNode2;
		
		if (forward) {
			addGeneralisedPoint(startEdge, true);
			
			osmHighway = segment1.way.tags.get("highway");
			osmRef = "";
			// special values for osmRef:
			// ""/OsmTags.NO_VALUE -> no known ref; null -> conflicting refs
		}
		
		// (TG 3a)
		// :TODO: this condition can surely be simplified
		while (currentEdge != null && segment1 != null && segment2 != null && (segment1.wasGeneralised == 0 || segment2.wasGeneralised == 0)) {
			
			if (highwayClass(segment2.way.tags.get("highway")) != highwayClass(osmHighway)
					&& highwayClass(segment1.way.tags.get("highway")) != highwayClass(osmHighway)) {
//System.err.println(" --break1 " + segment2.way.tags.get("highway") + segment2.way.tags.get("ref") + " " + segment1.way.tags.get("highway") + segment1.way.tags.get("ref") + " " + osmHighway);
				break;  // apparently the highway class has changed somewhere along the line; let's interrupt the generalised line and leave the continuation for the next run (which may then pick up the lower class)
			}
			if (segment2.way.tags.get("highway") != osmHighway) {  // get() returns intern()ed value
				if (highwayClass(segment2.way.tags.get("highway")) > highwayClass(osmHighway)) {
					osmHighway = segment2.way.tags.get("highway");
				}
			}
			if (osmRef != null) {
				// osmRef == null  -> conflicting refs
				// we're dealing with intern()ed values here, so == is safe to use
				if (osmRef == "") {
					osmRef = segment2.way.tags.get("ref");
				}
				if (osmRef == "") {
					osmRef = segment1.way.tags.get("ref");
				}
				if (osmRef != "" &&
						( osmRef != segment1.way.tags.get("ref")
						|| osmRef != segment2.way.tags.get("ref") )) {
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
		
		segment1.wasGeneralised += 1;
		originals.add(segment1);

		LineSegment nextSegment1 = findNextSegment(nextNode1, segment1);
		if (nextSegment1 != null) {
			segment1Aligned ^= ! nextSegment1.vector().isAligned(segment1.vector());  // :TODO: unclear; can prolly be replaced by node comparison
		}
		segment1 = nextSegment1;

		currentNode1 = nextNode1;
	}
	
	
	
	void advance2 (OsmNode nextNode2) {
		
		segment2.wasGeneralised += 1;
		originals.add(segment2);

		LineSegment nextSegment2 = findNextSegment(nextNode2, segment2);
		if (nextSegment2 != null) {
			segment2Aligned ^= ! nextSegment2.vector().isAligned(segment2.vector());  // :TODO: unclear; can prolly be replaced by node comparison
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
		
		double e = (edge.node0.e + edge.node1.e) / 2.0;
		double n = (edge.node0.n + edge.node1.n) / 2.0;
//			final OsmNode midPoint = OsmNode.createWithEastingNorthing(e, n);
		final OsmNode midPoint = graph.dataset.getNodeAtEastingNorthing(e, n);  // :TODO: why this line? perhaps so that the node is inserted into the debug output?
		if (addAsLast) {
			combination.addLast( midPoint );
		}
		else {
			combination.addFirst( midPoint );
		}
	}
	
	
	
	// (TG 2a) find T
	private LineSegment findOppositeSegment (OsmNode oppositeNode, LineSegment thisSegment, boolean thisIsAligned) {
		LineSegment oppositeSegment = null;
		
		Vector thisVector = thisIsAligned ? thisSegment.vector() : thisSegment.vector().reversed();
		for (LineSegment segment : oppositeNode.connectingSegments) {
			Vector vector = segment.start == oppositeNode ? segment.vector() : segment.vector().reversed();
			if ( Math.abs( vector.relativeBearing(thisVector) ) < Vector.RIGHT_ANGLE ) {
//					assert oppositeSegment == null;  // only one should match, normally (at least for trivial datasets?) (might be some exceptions in some datasets, this is a :HACK:)
				oppositeSegment = segment;
			}
			
		}
		return oppositeSegment;
	}
	
	
	
	private LineSegment findNextSegment (OsmNode pivot, LineSegment currentSegment) {
		LineSegment nextSegment = null;
		for (LineSegment segment : pivot.connectingSegments) {
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
	
	
	
	int highwayClass (final String tag) {
		if (tag == "motorway") { return 21; }
		if (tag == "motorway_link") { return 20; }
		if (tag == "trunk") { return 19; }
		if (tag == "trunk_link") { return 18; }
		if (tag == "primary") { return 17; }
		if (tag == "primary_link") { return 16; }
		if (tag == "secondary") { return 15; }
		if (tag == "secondary_link") { return 14; }
		if (tag == "tertiary") { return 13; }
		if (tag == "tertiary_link") { return 12; }
		if (tag == "unclassified") { return 11; }
		if (tag == "unclassified_link") { return 10; }
		if (tag == "residential") { return 9; }
		if (tag == "residential_link") { return 8; }
		if (tag == "service") { return 7; }
		if (tag == "road") { return 6; }
		if (tag == "track") { return 5; }
		if (tag == "cycleway") { return 4; }
		if (tag == "bridleway") { return 3; }
		if (tag == "footway") { return 2; }
		if (tag == "path") { return 1; }
		return 0;
	}
	
	
	
	double length () {
		if (combination.size() < 2) {
			return 0.0;
		}
		// :BUG: calculate intermediate segments
		return new Vector( combination.getFirst(), combination.getLast() ).distance();
	}
	
	
	
	void filterShortSection () {
		assert valid == true;
		
		if (length() < GeneralisedSection.MIN_LENGTH) {
			valid = false;
			for (LineSegment segment : originals) {
				segment.wasGeneralised -= 1;
				segment.notToBeGeneralised = true;  // avoid infinite loop in GeneralisedLines#traverse()
			}
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


