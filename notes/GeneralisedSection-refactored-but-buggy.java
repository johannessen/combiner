/* encoding UTF-8
 * 
 * Copyright (c) 2013 Arne Johannessen
 * 
 * This project and all of its individual parts may be used in accordance
 * with the terms of the 3-clause BSD licence. See LICENSE for details.
 */

package de.thaw.thesis.comb;

import java.util.Iterator;
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
	
	
	
	CorrelationEdge startEdge = null;  // E
	OsmNode startNode = null;  // E_S
	
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
		
		valid = true;
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
		}
		
		// (TG 3a)
		// :TODO: this condition can surely be simplified
		while (currentEdge != null && segment1 != null && segment2 != null && (segment1.wasGeneralised == 0 || segment2.wasGeneralised == 0)) {
			
			currentEdge.genCounter += 1;  // :DEBUG:
			
			// (TG 5) find next nodes
			OsmNode nextNode1 = segment1Aligned ? segment1.end : segment1.start;  // X
			OsmNode nextNode2 = segment2Aligned ? segment2.end : segment2.start;  // Y
			
			
			CorrelationEdge nextEdge;
			
			nextEdge = advance(nextNode1, currentNode2, 1);
			if (nextEdge != null) {
				currentEdge = nextEdge;
				nextEdge.genCounter += 1;  // :DEBUG:
				addGeneralisedPoint(currentEdge, forward);
				continue;
			}
			
			nextEdge = advance(nextNode2, currentNode1, 2);
			if (nextEdge != null) {
				currentEdge = nextEdge;
				nextEdge.genCounter += 1;  // :DEBUG:
				addGeneralisedPoint(currentEdge, forward);
				continue;
			}
			
			nextEdge = advance(nextNode1, nextNode2, 3);
			if (nextEdge != null) {
				currentEdge = nextEdge;
				nextEdge.genCounter += 1;  // :DEBUG:
				addGeneralisedPoint(currentEdge, forward);
				continue;
			}
			
			// Fall "es gibt naechste unabhaengige Segmente, die aber nicht parallel sind"
			
			segment1.notToBeGeneralised = segment1.wasGeneralised == 0;
			segment2.notToBeGeneralised = segment2.wasGeneralised == 0;
			
			segment1 = null;  // break
			
			addGeneralisedPoint(currentEdge, forward);
		}
		
		return currentEdge;
	}
	
	
	
	CorrelationEdge advance (OsmNode node1, OsmNode node2, int a) {
		
		CorrelationEdge nextEdge = graph.get(node1, node2);
		if (nextEdge == null) {
			return null;
		}
		
		
		if (a == 1) {
//		assert (a == 1) == (node1 != currentNode1 && node2 == currentNode2) : currentEdge;
			if (node1 == currentNode1) {
				return null;
			}
			advance1(node1);
		}
		if (a == 2) {
//		assert (a == 2) == (node1 == currentNode1 && node2 != currentNode2) : currentEdge;
			if (node2 == currentNode2) {
				return null;
			}
			advance2(node2);
		}
		if (a == 3) {
//		assert (a == 3) == (node1 != currentNode1 && node2 != currentNode2) : currentEdge;
			if (node1 == currentNode1 && node2 == currentNode2) {
				return null;
			}
			advance1(node1);
			advance2(node2);
		}
		
		return nextEdge;
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
	
}


