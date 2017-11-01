/* encoding UTF-8
 * 
 * Copyright (c) 2013 Arne Johannessen
 * 
 * This project and all of its individual parts may be used in accordance
 * with the terms of the 3-clause BSD licence. See LICENSE for details.
 */

package de.thaw.thesis.comb;

import de.thaw.thesis.comb.highway.HighwayRef;
import de.thaw.thesis.comb.util.AttributeProvider;
import de.thaw.thesis.comb.util.SimpleVector;

import java.util.AbstractSequentialList;
import java.util.Collections;
import java.util.Collection;
import java.util.LinkedList;
import java.util.ListIterator;



// ex Section
/**
 * A contiguous line in the Combiner's result, created by copying segments from the source dataset without modification.
 */
public class ConcatenatedSection extends ResultLine {
	
	
	
	ConcatenatedSection (final SourceSegment startSegment) {
		
		if (startSegment == null) {
			valid = false;
			return;
		}
		
		SourceSegment segment = null;
		SourceNode node = null;
		Line way = startSegment.way;
		HighwayRef osmRef = way.ref();
		// special values for osmRef:
		// "" -> no known ref; null -> conflicting refs
		
		assert size() == 0 : this;
		
		for (int i = 0; i < 2; i++) {
			final boolean forward = i == 0;
			segment = startSegment;
			node = forward ? startSegment.end() : startSegment.start();
			
			/* wasGeneralised is a bit of a misnomer here. Modifying
			 * wasGeneralised at this point doesn't influence the rest of
			 * the Combiner, but it does influence the wasGeneralised counter
			 * in the output data. While this counter is actually spec'ed as
			 * a boolean, using this field for debug purposes is useful.
			 */
			startSegment.wasGeneralised -= 1;  // startSegment's counter will be incremented twice
			while (node != null && segment.wasGeneralised <= 0) {
				segment.wasGeneralised += 1;
				
				addGeneralisedPoint(node, forward);
				
				segment = other(segment, node.connectingSegments());
				node = other(node, segment);
				
				// analyse OSM tags
				if (segment != null && segment.way != way) {
					if (segment.way.type() != way.type()) {
						break;
					}
					if (osmRef != null) {
						// no conflicting refs encountered yet
						final HighwayRef segmentRef = segment.way.ref();
						if (osmRef.isEmpty()) {
							osmRef = segmentRef;
						}
						else if (! segmentRef.equals(osmRef)) {
							// conflicting refs encountered; stop processing refs
							osmRef = null;
						}
					}
				}
			}
		}
		
		valid = size() > 0;
		
		if (valid) {
			highwayType = way.type();
			highwayRef = osmRef == null ? HighwayRef.valueOf("") : osmRef;
			tags = new Tags(highwayType.name(), highwayRef.toString());
		}
	}
	
	
	
	private void addGeneralisedPoint (final SourceNode node, final boolean addAsLast) {
		if (node == null) {
			return;
		}
		
		if (addAsLast) {
			addLast( node );
		}
		else {
			addFirst( node );
		}
	}
	
	
	
	private static SourceNode other (final SourceNode node, final SourceSegment segment) {
		if (segment == null) {
			return null;
		}
		return segment.other(node);
	}
	
	
	
	private static SourceSegment other (final SourceSegment segment, final Collection<SourceSegment> segments) {
		if (segments.size() > 2) {
			return null;  // "the other one" is only well-defined for at most two segments total
		}
		for (final SourceSegment other : segments) {
			if (other != segment) {
				return other;
			}
		}
		return null;
	}
	
	
	
	protected void relocateGeneralisedNodes () {
		for (int i = 0; i < 2; i++) {
			final SourceNode node = i == 0 ? start() : end();
			
			// find the closest existing vertex on the generalised section (if any)
			// (there's some collateral damage because the closest point may not be the best one, particularly at major intersections)
			NodeMatch theEdge = null;
			for (final NodeMatch anEdge : node.edges()) {
				if (theEdge == null || anEdge.distance() < theEdge.distance()) {
					theEdge = anEdge;
				}
			}
			if (theEdge == null) {
				continue;  // section doesn't end on generalised node
			}
			
			// "move" (actually: replace) first/last nodes as appropriate
			
// "Edge" als Namen fuer Typ mit midpoint-logik
// -> kann man machen ("NodePair"), bringt aber eigentlich nix
			final GeneralisedNode midPoint = theEdge.midPoint();
			
/*
			if (midPoint.id == 0L) {
				midPoint.id = -2L;
			}
*/
			
// i als parameter für for-i-methode mitgeben
			if (i == 0) {
				set(0, midPoint);
			}
			else {
				set(size(), midPoint);  // sic
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
