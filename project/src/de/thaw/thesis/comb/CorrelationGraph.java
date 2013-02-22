/* encoding UTF-8
 * 
 * Copyright (c) 2013 Arne Johannessen
 * 
 * This project and all of its individual parts may be used in accordance
 * with the terms of the 3-clause BSD licence. See LICENSE for details.
 */

package de.thaw.thesis.comb;

import java.util.Collection;
//import java.util.LinkedHashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.NavigableSet;
import java.util.TreeSet;


/**
 * 
 */
public final class CorrelationGraph {
	
	final OsmDataset dataset;
	
	final NavigableSet<CorrelationEdge> edges;
	
	Collection<Collection<OsmNode>> generalisedLines;
	
	
	CorrelationGraph (final OsmDataset dataset) {
		this.dataset = dataset;
		
		generalisedLines = new LinkedList<Collection<OsmNode>>();
		edges = new TreeSet<CorrelationEdge>();
		createGraph();
	}
	
	
	void createGraph () {
		
		/* This is reasonably fast because all the inner loops have only very
		 * few items to loop through (about 2 each).
		 */
		
		// ∀ segments S
		final List<LineSegment> segments = dataset.allSegments();
		for (final LineSegment segment : segments) {
			if (segment.leftRealParallels.size() == 0 && segment.rightRealParallels.size() == 0) {
				continue;
			}
			if (segment.wasCorrelated) {
				continue;
			}
			
			// ∀ nodes T1 {start,end} of S
			for (int j = 0; j < 2; j++) {
				final OsmNode segmentNode = node(segment, j);
				
				// ∀ sides A {left,right} of S
				for (int i = 0; i < 2; i++) {
					final Collection side = i == 0 ? segment.leftRealParallels : segment.rightRealParallels;
					
					OsmNode closestNode = null;
					double closestNodeDistance = Double.POSITIVE_INFINITY;
					
					// ∀ parallels P of S on side A
					for (final Object p : side) {
						if (! (p instanceof LineSegment)) {
							throw new ClassCastException();  // shouldn't happen
						}
						final LineSegment parallel = (LineSegment)p;
						
						// find nearest node T2 of same category (start/end) as T1
						final boolean isAligned = segment.vector().isAligned(parallel.vector());
						final OsmNode parallelNode = isAligned ? node(parallel, j) : node(parallel, 1 - j);
						final double d = new Vector(segmentNode, parallelNode).distance();
						if (d < closestNodeDistance) {
							closestNodeDistance = d;
							closestNode = parallelNode;
						}
					}
					
					if (closestNode == null) {
						// <=> no parallel exists to S on side A
						assert side.size() == 0;
						continue;
					}
					assert side.size() > 0;
					
					assert contains(new CorrelationEdge(segmentNode, closestNode)) == contains(new CorrelationEdge(closestNode, segmentNode)) : new CorrelationEdge(segmentNode, closestNode);  // testing Set comparisons
					
					add( new CorrelationEdge(
//							null,  // node0Backward
							segmentNode,  // node0
//							null,  // node0Forward
//							null,  // node1Backward
							closestNode,  // node1
//							null,  // node1Forward
							j == 0) );  // node0isSegmentStart
				}
			}
			
			// mark S as done "7"
			segment.wasCorrelated = true;
		}
	}
	
	
	private OsmNode node (final LineSegment segment, final int i) {
		assert i == 0 || i == 1;
		return i == 0 ? segment.start : segment.end;
	}
	
	
	boolean contains (final CorrelationEdge edge) {
		return edges.contains(edge);
	}
	
	
	boolean add (final CorrelationEdge edge) {
		return edges.add(edge);
	}
	
	
	LineSegment findStart (long startId) {
		// "find" appropriate start location
		
		// (insert magic here)
		
		// :BUG:
//		startId = startId == 0L ? -100L : startId;
		startId = startId == 0L ? 24630291L : startId;
		LineSegment start = null;
		for (final LineSegment segment : dataset.allSegments()) {
//			if (segment.way.id == -100L && segment.leftRealParallels.size() == 1) {
			if (segment.way.id == startId) {
				start = segment;
				break;
			}
		}
		return start;
	}
	
	
	boolean findMaxLPoint (final LineSegment segment, final OsmNode node, final double relativeBearingLimit) {
		
		// :TODO: traverse local graph crosswise to max end
		
		// :BUG: expensive; replace this with "ably" sorted edges
		
		// find max-L-point
		double minRelativeBearing = 0.0;  // left bearing <=> negative => "min"
		OsmNode minOtherNode = null;
		Vector minEdgeVector = null;
		for (final CorrelationEdge edge : edges) {
			if (! edge.contains(node)) {
				continue;
			}
			final OsmNode otherNode = node.equals(edge.node0) ? edge.other(edge.node0) : edge.node0;
			final Vector edgeVector = new Vector( node, otherNode );
			double relativeBearing = segment.vector().relativeBearing( edgeVector );
			if (relativeBearing <= minRelativeBearing && relativeBearing > relativeBearingLimit) {
				minRelativeBearing = relativeBearing;
				minOtherNode = otherNode;
				minEdgeVector = edgeVector;
			}
		}
		
		// :TODO: returning multiple values; bad hack
		lastMinRelativeBearing = minRelativeBearing;
		lastMinOtherNode = minOtherNode;
		lastMinEdgeVector = minEdgeVector;
		return lastMinOtherNode != null;
	}
	
	
	// :TODO: multiple return values; bad hack
	private double lastMinRelativeBearing;
	private OsmNode lastMinOtherNode;
	private Vector lastMinEdgeVector;
	
	List<OsmNode> traverseCrosswise (final LineSegment segment) {
		
		List<OsmNode> generalisedSection = new LinkedList<OsmNode>();
		
		
		for (int j = 0; j < 2; j++) {
			final OsmNode node = node(segment, j);
			
			double relativeBearingLimit = Vector.HALF_CIRCLE * -1.0;
			
			OsmNode LPoint;
			while ( findMaxLPoint(segment, node, relativeBearingLimit) ) {
				// :TODO: receiving multiple return values; bad hack
				final double minRelativeBearing = this.lastMinRelativeBearing;
				final OsmNode minOtherNode = this.lastMinOtherNode;
				final Vector minEdgeVector = this.lastMinEdgeVector;
				
				relativeBearingLimit = minRelativeBearing;
			
				OsmNode midPoint = OsmNode.createAtMidPoint(node, minEdgeVector);
				midPoint = dataset.getNodeAtEastingNorthing(midPoint.easting(), midPoint.northing());
				generalisedSection.add(midPoint);
			}
		}
		
		return generalisedSection;
	}
	
	
	void traverseLengthwise (final LineSegment startSegment) {
		
		List<OsmNode> generalisedSection = traverseCrosswise(startSegment);
		generalisedLines.add(generalisedSection);
		
		// :BUG: choosing indiscriminately...
		for (final LineSegment nextSegment : startSegment.end().connectingSegments) {
			if (nextSegment == startSegment) {
				continue;
			}
			
			traverseLengthwise(nextSegment);
			break;
		}
	}
	
	
	void traverse (final LineSegment segment) {
		// :BUG: only works in forward direction
		traverseLengthwise(segment);
	}
	
}
