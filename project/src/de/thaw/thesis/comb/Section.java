/* encoding UTF-8
 * 
 * Copyright (c) 2013 Arne Johannessen
 * 
 * This project and all of its individual parts may be used in accordance
 * with the terms of the 3-clause BSD licence. See LICENSE for details.
 */

package de.thaw.thesis.comb;

import de.thaw.thesis.comb.util.SimpleVector;

import java.util.AbstractSequentialList;
import java.util.Collections;
import java.util.Collection;
import java.util.LinkedList;
import java.util.ListIterator;



public class Section extends AbstractLine {
	
	static double MIN_LENGTH = 10.0;  // :TODO: check what works best
	
	private boolean valid = false;
	
	
	
	Section (final SourceSegment startSegment) {
		
		if (startSegment == null) {
			valid = false;
			return;
		}
		
		SourceSegment segment = null;
		OsmNode node = null;
		Line way = startSegment.way;
		HighwayRef osmRef = way.ref();
		// special values for osmRef:
		// "" -> no known ref; null -> conflicting refs
		
		assert size() == 0 : this;
		
		for (int i = 0; i < 2; i++) {
			final boolean forward = i == 0;
			segment = startSegment;
			node = forward ? startSegment.end : startSegment.start;
			
// wasGeneralised ??
			startSegment.wasGeneralised -= 1;  // startSegment's counter will be incremented twice
			while (node != null && segment.wasGeneralised <= 0) {
				segment.wasGeneralised += 1;
				
				addGeneralisedPoint(node, forward);
				
				segment = other(segment, node.connectingSegments);
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
	
	
	
	private void addGeneralisedPoint (final OsmNode node, final boolean addAsLast) {
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
	
	
	
	public boolean valid () {
		return valid;
	}
	
	
	
	public long id () {
		return Dataset.ID_NONEXISTENT;
	}
	
	
	
	private static OsmNode other (final OsmNode node, final SourceSegment segment) {
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
	
	
	
	double length () {
		if (size() < 2) {
			return 0.0;
		}
		// :BUG: calculate intermediate segments
		return SimpleVector.distance( start(), end() );
	}
	
	
	
// häh?
	void filterShortSection () {
		assert valid == true;
		
		if (length() < MIN_LENGTH) {
			valid = false;
		}
	}
	
	
	
	private static class Tags implements OsmTags {
		private final String highway;
		private final String ref;
		Tags (final String highway, final String ref) {
			this.highway = highway != null ? highway.intern() : OsmTags.NO_VALUE;
			this.ref = ref != null ? ref.intern() : OsmTags.NO_VALUE;
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


