/* encoding UTF-8
 * 
 * Copyright (c) 2013 Arne Johannessen
 * 
 * This project and all of its individual parts may be used in accordance
 * with the terms of the 3-clause BSD licence. See LICENSE for details.
 */

package de.thaw.thesis.comb;

import java.util.Collections;
import java.util.Collection;
import java.util.LinkedList;



public class Section implements SectionInterface {
	
	static double MIN_LENGTH = 80.0;  // :TODO: check what works best
	
	LinkedList<OsmNode> combination = new LinkedList<OsmNode>();
	
	private boolean valid = false;
	
	private OsmTags tags = null;
	
	
	
	Section () {
	}
	
	
	
	public boolean valid () {
		return valid;
	}
	
	
	
	public OsmTags tags () {
		return tags;
	}
	
	
	
	public Collection<OsmNode> combination () {
		return Collections.unmodifiableCollection(combination);
	}
	
	
	
	OsmNode other (final OsmNode node, final LineSegment segment) {
		if (segment == null) {
			return null;
		}
		if (segment.start.equals(node)) {
			return segment.end;
		}
		else {
			assert segment.end.equals(node);
			return segment.start;
		}
	}
	
	
	
	LineSegment other (final LineSegment segment, final Collection<LineSegment> segments) {
		if (segments.size() > 2) {
			return null;  // "the other one" is only well-defined for at most two segments total
		}
		for (final LineSegment other : segments) {
			if (other != segment) {
				return other;
			}
		}
		return null;
	}
	
	
	
	void startAt (final LineSegment startSegment) {
		if (startSegment == null) {
			valid = false;
			return;
		}
		
		LineSegment segment = null;
		OsmNode node = null;
		
		assert combination.size() == 0 : combination;
		
		for (int i = 0; i < 2; i++) {
			final boolean forward = i == 0;
			segment = startSegment;
			node = forward ? startSegment.end : startSegment.start;
			startSegment.wasGeneralised -= 1;  // startSegment's counter will be incremented twice
			while (node != null && segment.wasGeneralised <= 0) {
				segment.wasGeneralised += 1;
				
				addGeneralisedPoint(node, forward);
				
				segment = other(segment, node.connectingSegments);
				node = other(node, segment);
			}
		}
		
		valid = combination.size() >= 2;
	}
	
	
	
	private void addGeneralisedPoint (final OsmNode node, final boolean addAsLast) {
		if (node == null) {
			return;
		}
		
		if (addAsLast) {
			combination.addLast( node );
		}
		else {
			combination.addFirst( node );
		}
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
//			valid = false;
		}
	}
	
}


