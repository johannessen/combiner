/* encoding UTF-8
 * 
 * Copyright (c) 2013 Arne Johannessen
 * 
 * This project and all of its individual parts may be used in accordance
 * with the terms of the 3-clause BSD licence. See LICENSE for details.
 */

package de.thaw.thesis.comb;

import de.thaw.thesis.comb.highway.HighwayRef;
import de.thaw.thesis.comb.highway.HighwayType;
import de.thaw.thesis.comb.util.AttributeProvider;
import de.thaw.thesis.comb.util.SimpleVector;
import de.thaw.thesis.comb.util.Vector;

import java.util.AbstractSequentialList;
import java.util.Iterator;
import java.util.Collection;
import java.util.Collections;
import java.util.LinkedList;
import java.util.ListIterator;



/**
 * A contiguous line in the Combiner's result, created by combining two parallel lines read from the source dataset.
 */
public class GeneralisedSection extends ResultLine {
	
	static double MIN_LENGTH = 50.0;  // :TODO: check what works best
	
	public LinkedList<SourceSegment> originalSegments = new LinkedList<SourceSegment>();
	public LinkedList<SourceNode> originalNodes = new LinkedList<SourceNode>();
	
	private NodeGraph graph;
	
	
	
	private final NodeMatch startMatch;  // E
	private final SourceNode startNode;  // E_S
	
	HighwayType osmHighway = null;
	String osmRef = null;
	
	GeneralisedSection (final NodeGraph graph, final NodeMatch startMatch, final SourceNode startNode) {
		this.graph = graph;
		this.startMatch = startMatch;
		this.startNode = startNode;
		
		if (startNode == null) {
			valid = false;
			return;
		}
		
		assert startNode.connectingSegments().size() <= 2 : startNode;  // see issue #111
		assert startNode.connectingSegments().size() > 0 : startNode;
		
		
		boolean forward = true;
		for (final SourceSegment segment : startNode.connectingSegments()) {
			if (segment.wasGeneralised > 0) {
				continue;
			}
			
			traverseGraph(segment, forward);
			
			/* Move into both directions along segments from startNode:
			 * 
			 * The ZUSAMMENFASSEN algorithm only defines moving into one
			 * direction and leaves concatenating the arbitrary number of line
			 * strings that result to the client. However, we can just as
			 * easily execute this step twice (once per direction), to yield
			 * the maximum possible line string length right away.
			 */
			forward = false;
		}
		
		valid = size() >= 1;
		
		if (valid) {
			highwayType = osmHighway;
			highwayRef = osmRef == null || osmRef.length() == 0 ? HighwayRef.valueOf("") : HighwayRef.valueOf(osmRef);
			tags = new Tags(osmHighway.name(), osmRef);
		}
	}
	
	
	
	SourceSegment segment1 = null;  // A (s)
	SourceSegment segment2 = null;  // B (t)
	boolean segment1Aligned = true;
	boolean segment2Aligned = true;
	SourceNode currentNode1 = null;  // E_S
	SourceNode currentNode2 = null;  // E_T
	NodeMatch currentMatch = null;  // E
	
	private void traverseGraph (final SourceSegment startSegment, final boolean forward) {
		
		currentMatch = startMatch;
		currentNode1 = startNode;
		currentNode2 = startMatch.other(startNode);
		segment1 = startSegment;
		segment1Aligned = segment1.start() == currentNode1;
		segment2 = findOppositeSegment(currentNode2, segment1, segment1Aligned);
		if (segment2 == null) {
			segment1.notToBeGeneralised = true;  // no parallels == no need for generalisation
			// :TODO: why does this occur? -- doesn't matter much, it seems to do the right thing anyhow
			return;
		}
		segment2Aligned = segment2.start() == currentNode2;
		
		if (forward) {
			addGeneralisedPoint(startMatch, true);
			currentNode1.addGeneralisedSection(this);
			currentNode2.addGeneralisedSection(this);
			originalNodes.add(currentNode1);
			originalNodes.add(currentNode2);
			
			osmHighway = segment1.way.type();
			osmRef = "";
			// special values for osmRef:
			// ""/AttributeProvider.NO_VALUE -> no known ref; null -> conflicting refs
		}
		
		// (TG 3a)
		// :TODO: this condition can surely be simplified
		while (currentMatch != null && segment1 != null && segment2 != null && (segment1.wasGeneralised == 0 || segment2.wasGeneralised == 0)) {
			
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
			
			currentMatch.genCounter += 1;  // :DEBUG:
			
			// (TG 5) find next nodes
			SourceNode nextNode1 = segment1Aligned ? segment1.end() : segment1.start();  // X
			SourceNode nextNode2 = segment2Aligned ? segment2.end() : segment2.start();  // Y
			
			NodeMatch nextMatch;
			
			// (TG 6/7)
			nextMatch = graph.getMatch(nextNode1, currentNode2);
			if ( nextMatch != null && nextNode1 != currentNode1 ) {
				
				// Fall "es gibt eine Kante vom naechsten Punkt-1 (X) zurueck zum aktuellen Punkt-2 (E_T)"
				
				advance1(nextNode1);
				currentMatch = nextMatch;
				
			}
			else {
				nextMatch = graph.getMatch(nextNode2, currentNode1);
				if ( nextMatch != null && nextNode2 != currentNode2 ) {
					
					// Fall "es gibt eine Kante vom naechsten Punkt-2 (Y) zurueck zum aktuellen Punkt-1 (E_S)"
					
					advance2(nextNode2);
					currentMatch = nextMatch;
					
				}
				else {
					nextMatch = graph.getMatch(nextNode1, nextNode2);
					if ( nextMatch != null && (nextNode1 != currentNode1 || nextNode2 != currentNode2) ) {
						
						// Fall "es gibt zwei naechste unabhaengige Segmente, die auch parallel sind und es gibt agnz normal eine naechste kante"
						
						advance1(nextNode1);
						advance2(nextNode2);
						currentMatch = nextMatch;
						
						
					}
					else {
						
						// Fall "es gibt naechste unabhaengige Segmente, die aber nicht parallel sind"
						assert nextMatch == null || nextMatch == currentMatch;
						
						segment1.notToBeGeneralised = segment1.wasGeneralised == 0;
						segment2.notToBeGeneralised = segment2.wasGeneralised == 0;
						
						segment1 = null;  // break
					}
				}
			}
			
			
			addGeneralisedPoint(currentMatch, forward);
			
		}
		
		return;
	}
	
	
	
	void advance1 (SourceNode nextNode1) {
		
		nextNode1.addGeneralisedSection(this);
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
	
	
	
	void advance2 (SourceNode nextNode2) {
		
		nextNode2.addGeneralisedSection(this);
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
	
	
	
	// (TG 4) find M
	private void addGeneralisedPoint (NodeMatch match, boolean addAsLast) {
		if (match == null) {
			return;
		}
		
		if (addAsLast) {
			addLast( match.midPoint() );
		}
		else {
			addFirst( match.midPoint() );
		}
	}
	
	
	
	// (TG 2a) find T
	private SourceSegment findOppositeSegment (SourceNode oppositeNode, SourceSegment thisSegment, boolean thisIsAligned) {
		SourceSegment oppositeSegment = null;
		
		Vector thisVector = thisIsAligned ? thisSegment : thisSegment.reversed();
		for (SourceSegment segment : oppositeNode.connectingSegments()) {
			Vector vector = segment.start() == oppositeNode ? segment : segment.reversed();
			if ( Math.abs( vector.relativeBearing(thisVector) ) < Vector.RIGHT_ANGLE ) {
//					assert oppositeSegment == null;  // only one should match, normally (at least for trivial datasets?) (might be some exceptions in some datasets, this is a :HACK:)
				oppositeSegment = segment;
			}
			
		}
		return oppositeSegment;
	}
	
	
	
	private SourceSegment findNextSegment (SourceNode pivot, SourceSegment currentSegment) {
		SourceSegment nextSegment = null;
		for (SourceSegment segment : pivot.connectingSegments()) {
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
	
	
	
/*
	void ungeneralise () {
		for (SourceSegment segment : originalSegments) {
			segment.wasGeneralised -= 1;
			segment.notToBeGeneralised = true;  // avoid infinite loop in GeneralisedLines#traverse()
			
			for (SourceNode node : originalNodes) {
				while ( node.generalisedSections().remove(this) ) {
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
*/
	
	
	
	protected void relocateGeneralisedNodes () {
		// no-op (not yet implemented, possibly not even necessary)
	}
	
}


