/* encoding UTF-8
 * 
 * Copyright (c) 2013 Arne Johannessen
 * 
 * This project and all of its individual parts may be used in accordance
 * with the terms of the 3-clause BSD licence. See LICENSE for details.
 */

package de.thaw.thesis.comb;

import java.util.LinkedList;



public class GeneralisedSection {
	
	static double MIN_LENGTH = 80.0;  // :TODO: check what works best
	
	public LinkedList<OsmNode> combination = new LinkedList<OsmNode>();
	
	public LinkedList<LineSegment> originals = new LinkedList<LineSegment>();
	
	public CorrelationEdge startConnector = null;
	public CorrelationEdge endConnector = null;
	
	private boolean valid = false;
	
	private CorrelationGraph graph;
	
	
	
	GeneralisedSection (CorrelationGraph theGraph) {
		graph = theGraph;
	}
	
	
	
	boolean valid () {
		return valid;
	}
	
	
	
	void startAt (final CorrelationEdge startEdge, final OsmNode startNode) {
		
		// startEdge -- E
		// startNode -- E_S
		LineSegment segment1 = null;  // A
		LineSegment segment2 = null;  // B
		boolean segment1Aligned = true;
		boolean segment2Aligned = true;
		OsmNode currentNode1 = null;  // E_S
		OsmNode currentNode2 = null;  // E_T
		CorrelationEdge currentEdge = null;  // E
		
		if (startNode == null) {
			valid = false;
			return;
		}
		
		addGeneralisedPoint(startEdge, true);
		
		addConnector(startEdge, true);
		addConnector(startEdge, false);
		
		assert startNode.connectingSegments.size() <= 2 : startNode;
		boolean forward = true;
		int directionsCounter = 0;
		for (final LineSegment segment : startNode.connectingSegments) {
			if (segment.wasGeneralised > 0) {
				continue;
			}
			
			currentEdge = startEdge;
			currentNode1 = startNode;
			currentNode2 = startEdge.other(startNode);
			segment1 = segment;
			segment1Aligned = segment1.start == currentNode1;
			segment2 = findOppositeSegment(currentNode2, segment1, segment1Aligned);
			if (segment2 == null) {
				segment1.notToBeGeneralised = true;  // no parallels == no need for generlaisation
				continue;
			}
			segment2Aligned = segment2.start == currentNode2;
			
			/* :BUG:
			 * This toggling logic only works for "trivial" locations, i. e.
			 * no nodes with more than 2 connecting segments. If there are
			 * more segments, the flag is toggled more than once, which
			 * yields visuals comparable to those of #111.
			 */
			
			// (TG 3) ∀ D
			// :BUG: only works in forward direction
			
			// (TG 3a)
			// :TODO: this condition can surely be simplified
			while (currentEdge != null && segment1 != null && segment2 != null && (segment1.wasGeneralised == 0 || segment2.wasGeneralised == 0)) {
//					assert currentNode1.connectingSegments.size() <= 2 : currentNode1;  // trivial case
//					assert currentNode2.connectingSegments.size() <= 2 : currentNode2;  // trivial case
				
				// (TG 4) find M, draw it
//					generalisedSection.add(generalisedPoint( currentEdge ));
				
				// (TG 5) find next nodes
				OsmNode nextNode1 = segment1Aligned ? segment1.end : segment1.start;  // X
				OsmNode nextNode2 = segment2Aligned ? segment2.end : segment2.start;  // Y
				
				// (TG 6/7)
				CorrelationEdge nextEdge = new CorrelationEdge(nextNode1, currentNode2);
				nextEdge = graph.intern(nextEdge);
				if ( nextEdge != null && nextEdge != currentEdge ) {
					
					// Fall "es gibt eine Kante vom naechsten Punkt-1 (X) zurueck zum aktuellen Punkt-2 (E_T)"
					
					segment1.wasGeneralised += 1;
					originals.add(segment1);
					
					currentEdge.genCounter++;
					nextEdge.genCounter++;
					
					LineSegment nextSegment1 = findNextSegment(nextNode1, segment1);
					if (nextSegment1 != null) {
						segment1Aligned ^= ! nextSegment1.vector().isAligned(segment1.vector());  // :TODO: unclear; can prolly be replaced by node comparison
					}
					segment1 = nextSegment1;
					segment2 = segment2;  // no change
					currentNode1 = nextNode1;
					currentNode2 = currentNode2;  // no change
					currentEdge = nextEdge;
					
				}
				else {
					nextEdge = new CorrelationEdge(nextNode2, currentNode1);
					nextEdge = graph.intern(nextEdge);
					if ( nextEdge != null && nextEdge != currentEdge ) {
						
						// Fall "es gibt eine Kante vom naechsten Punkt-2 (Y) zurueck zum aktuellen Punkt-1 (E_S)"
						
						segment2.wasGeneralised += 1;
						originals.add(segment2);
						
						currentEdge.genCounter += 10;
						nextEdge.genCounter += 10;
						
						segment1 = segment1;  // no change
						LineSegment nextSegment2 = findNextSegment(nextNode2, segment2);
						if (nextSegment2 != null) {
							segment2Aligned ^= ! nextSegment2.vector().isAligned(segment2.vector());
						}
						segment2 = nextSegment2;
						currentNode1 = currentNode1;  // no change
						currentNode2 = nextNode2;
						currentEdge = nextEdge;
						
					}
					else {
						nextEdge = new CorrelationEdge(nextNode1, nextNode2);
						nextEdge = graph.intern(nextEdge);
						if ( nextEdge != null && nextEdge != currentEdge ) {
							
							// Fall "es gibt zwei naechste unabhaengige Segmente, die auch parallel sind und es gibt agnz normal eine naechste kante"
							
							segment1.wasGeneralised += 1;
							originals.add(segment1);
							segment2.wasGeneralised += 1;
							originals.add(segment2);
							
							currentEdge.genCounter += 1000;
							nextEdge.genCounter += 1000;
							
							LineSegment nextSegment1 = findNextSegment(nextNode1, segment1);
							if (nextSegment1 != null) {
								segment1Aligned ^= ! nextSegment1.vector().isAligned(segment1.vector());
							}
							segment1 = nextSegment1;
							LineSegment nextSegment2 = findNextSegment(nextNode2, segment2);
							if (nextSegment2 != null) {
								segment2Aligned ^= ! nextSegment2.vector().isAligned(segment2.vector());
							}
							segment2 = nextSegment2;
							currentNode1 = nextNode1;
							currentNode2 = nextNode2;
							currentEdge = nextEdge;
						}
						else {
							
							// Fall "es gibt naechste unabhaengige Segmente, die aber nicht parallel sind"
							assert nextEdge == null || nextEdge == currentEdge;
							
							segment1.notToBeGeneralised = segment1.wasGeneralised == 0;
							segment2.notToBeGeneralised = segment2.wasGeneralised == 0;
							
							currentEdge.genCounter += 100;
							segment1 = null;  // break
						}
					}
				}
				
				addGeneralisedPoint(currentEdge, forward);
			}
			
			addConnector(currentEdge, forward);
			
			// move into both directions along segments from startNode
			forward = ! forward;
			directionsCounter += 1;
		}
		assert directionsCounter <= 2 : startNode;
		
		valid = true;
	}
	
	
	
	void addConnector (final CorrelationEdge edge, final boolean addAtEnd) {
		if (addAtEnd) {
			endConnector = edge;
		}
		else {
			assert ! addAtEnd;
			startConnector = edge;
		}
	}
	
	
	
	// (TG 4) find M
	void addGeneralisedPoint (CorrelationEdge edge, boolean addAsLast) {
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
	LineSegment findOppositeSegment (OsmNode oppositeNode, LineSegment thisSegment, boolean thisIsAligned) {
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
	
	
	
	LineSegment findNextSegment (OsmNode pivot, LineSegment currentSegment) {
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
	
	
	
	double length () {
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
	
}


