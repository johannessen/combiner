/* encoding UTF-8
 * 
 * Copyright (c) 2013 Arne Johannessen
 * 
 * This project and all of its individual parts may be used in accordance
 * with the terms of the 3-clause BSD licence. See LICENSE for details.
 */

package de.thaw.comb;

import de.thaw.comb.highway.HighwayType;
import de.thaw.comb.highway.HighwayRef;
import de.thaw.comb.util.AttributeProvider;

import java.util.AbstractList;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.ListIterator;
import java.util.LinkedList;


/**
 * An ordered collection of several segments in the euclidian plane.
 * Instances may or may not have relationships with actual ways in the OSM
 * planet database. Instances are always part of a <code>Dataset</code>.
 * 
 * this IS a collection of SEGMENTS!
 * 
 */
public abstract class AbstractLine extends AbstractList<SourceSegment> implements Line {
	
	final private List<SourceSegment> segments;
	
	// we now do guarantee that the segments are oriented the same way as the line is!
	
	protected AttributeProvider tags = null;
	
	protected HighwayType highwayType = null;
	
	protected HighwayRef highwayRef = null;
	
	private boolean mutable = true;
	
	
	public AbstractLine (final int capacity) {
		segments = new ArrayList<SourceSegment>(capacity);
	}
	
	
	public AbstractLine () {
		segments = new LinkedList<SourceSegment>();
	}
	
	
	/**
	 * 
	 */
	// es müsste der Zeck des Konzepts erklärt werden
	public void mutable (final boolean mutable) {
		if (mutable == true) {
			throw new IllegalArgumentException();
			// the LinkedList's listIterator uses default methods for adding stuff; we'll need to implement our own listIterator to keep the start and end nodes correct
		}
		this.mutable = mutable;
	}
	
	
	/**
	 * 
	 */
/*
	public SourceSegment add (final Node node0, final Node node1) {
		assert node0 != null && node1 != null;
		
		SourceSegment segment = new SourceSegment(node0, node1, this);
		node0.addSegment(segment);
		node1.addSegment(segment);
		segments.add(segment);
		
		return segment;
	}
*/
	
	
	private SourceNode nodeCache = null;
	
	void addFirst (final SourceNode node) {
		add(node, false);
	}
	
	
	private void add (final SourceNode node, final boolean asLast) {
		assert node != null;
		SourceNode adjacentNode = asLast ? end() : start();
		
		if (adjacentNode == null) {
			if (nodeCache == null || nodeCache == node) {
				nodeCache = node;
				return;
			}
			adjacentNode = nodeCache;
			nodeCache = null;
		}
		
		if (adjacentNode == node) {
			return;
		}
		assert adjacentNode != null && node != null : adjacentNode + " " + node;
		
		if (asLast) {
			add(new SourceSegment( adjacentNode, node, this ));
		}
		else {
			add(0, new SourceSegment( node, adjacentNode, this ));
		}
	}
	
	
	public void addLast (final SourceNode node) {
		add(node, true);
	}
	
	
	//////////////////// abstract LIST
	
	
	public SourceSegment get (final int index) {
		return segments.get(index);
	}
	
	
	public int size () {
		return segments.size();
	}
	
	
	// index 0 -> start, index size -> end
	public void set (final int index, final SourceNode node) {
		if (size() == 0) {
			if (nodeCache == null) {
				throw new IndexOutOfBoundsException();
			}
			nodeCache = node;
			return;
		}
		
		ListIterator<SourceSegment> iterator = listIterator(index);
		if (iterator.hasNext()) {
			iterator.next().start = node;  // :BUG: add/remove connectingSegments of the old and new nodes
			iterator.previous();
		}
		if (iterator.hasPrevious()) {
			iterator.previous().end = node;  // :BUG: add/remove connectingSegments of the old and new nodes
		}
	}
	
	
	public SourceSegment set (final int index, final SourceSegment element) {
		throw new UnsupportedOperationException();
		//return segments.set(index, element);
	}
	
	
	public void add (final int index, final SourceSegment element) {
		if (index != size() && index != 0) {
			throw new IllegalArgumentException();
		}
		
		assert element.way == null || element.way == this : element.way;
		element.way = this;
		
		// :TODO:
		element.start().addSegment(element);
		element.end().addSegment(element);
		
		// rotate all segments such that their ends point away from the line start
		if ( index > 0 && element.end() == end() || index == 0 && element.start() == start() ) {
System.err.println("segment reversed in AbstractLine.add");
			element.reverse();
		}
		segments.add(index, element);
	}
	
	
	public SourceSegment remove (final int index) {
		throw new UnsupportedOperationException();
		//segments.remove(index);
	}
	
	
	///////////////// abstract LINE
	
	
	public Dataset dataset () {
		throw new UnsupportedOperationException();
	}
	
	
	abstract public long id () ;
	
	
	public SourceNode start () {
		return size() > 0 ? get(0).start() : null;
	}
	
	
	public SourceNode end () {
		return size() > 0 ? get(size() - 1).end() : null;
	}
	
	
	public Iterable<Node> coordinates () {
		// :BUG: implement a view for this; faster
		// (inner list class with custom iterator)
		
		List<Node> nodeList = new LinkedList<Node>();
		if (size() == 0) {
			assert nodeCache == null : nodeCache;  // this would mean a one-point line
			return nodeList;
		}
		
		nodeList.add( start() );
		for (SourceSegment segment : segments) {
			nodeList.add( segment.end() );
		}
		
		return nodeList;
	}
	
	
	/**
	 * 
	 */
	public AttributeProvider tags () {
		return tags;
	}
	
	
	/**
	 * 
	 */
	public HighwayType type () {
		return highwayType;
	}
	
	
	/**
	 * 
	 */
	public HighwayRef ref () {
		return highwayRef;
	}
	
	
	////////////////////////////////
	
	
/*
	private Node connectedNode (final SourceSegment segment1, final SourceSegment segment2) {
		if (segment1.end().equals(segment2.start())) {
			return segment1.end();
		}
		if (segment1.end().equals(segment2.end())) {
			return segment1.end();
		}
		if (segment1.start().equals(segment2.start())) {
			return segment1.start();
		}
		if (segment1.start().equals(segment2.end())) {
			return segment1.start();
		}
		return null;  // the two segments are unconnected
	}
	
	
	private Node otherNode (final SourceSegment segment, final Node node) {
		if (segment.start().equals(node)) {
			return segment.end();
		}
		else {
			assert segment.end().equals(node);
			return segment.start();
		}
	}
*/
	
}
