/* encoding UTF-8
 * 
 * Copyright (c) 2013 Arne Johannessen
 * 
 * This project and all of its individual parts may be used in accordance
 * with the terms of the 3-clause BSD licence. See LICENSE for details.
 */

package de.thaw.thesis.comb;

import java.util.Arrays;
import java.util.Collection;
import java.util.Iterator;
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
	
	CorrelationEdge[] sortedEdges;
	
	NavigableSet<CorrelationEdge> sortedEdgesSet;
	
	Collection<Collection<OsmNode>> generalisedLines;
	
	
	CorrelationGraph (final OsmDataset dataset) {
		this.dataset = dataset;
		
		generalisedLines = new LinkedList<Collection<OsmNode>>();
		sortedEdgesSet = new TreeSet<CorrelationEdge>();
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
//				continue;
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
					
					add(new CorrelationEdge( segmentNode, closestNode ));
				}
			}
			
			// mark S as done "7"
			segment.wasCorrelated = true;
		}
		
		sortedEdges = sortedEdgesSet.toArray(new CorrelationEdge[0]);
		sortedEdgesSet = null;
	}
	
	
	private OsmNode node (final LineSegment segment, final int i) {
		assert i == 0 || i == 1;
		return i == 0 ? segment.start : segment.end;
	}
	
	
	boolean contains (final CorrelationEdge edge) {
		if (sortedEdges == null) {
			return sortedEdgesSet.contains(edge);
		}
		return Arrays.binarySearch(sortedEdges, edge) >= 0;
	}
	
	
	// the point of this method is to return the original collection element, enabling the client to operate on that instead of some "equal" clone
	CorrelationEdge intern (final CorrelationEdge edge) {
		assert sortedEdgesSet == null;  // Collection doesn't support get, only from array (Illegal State)
		final int i = Arrays.binarySearch(sortedEdges, edge);
		if (i < 0) {
			return null;  // No Such Element
		}
		return sortedEdges[i];
	}
	
	
	boolean add (final CorrelationEdge edge) {
		assert sortedEdges == null;  // adding only to collection, not to array (Illegal State)
		return sortedEdgesSet.add(edge);
	}
	
	
	Collection<CorrelationEdge> edges () {
		return Arrays.asList(sortedEdges);
	}
	
	
	void traverse () {
		while (true) {  // :TODO: does this terminate?
			Walker walker = new Walker();
			walker.run();
			Collection<OsmNode> generalisedSection = walker.generalisedSection;
			if (generalisedSection == null) {
				break;
			}
			if (generalisedSection.size() == 1) {
				break;
			}
			generalisedLines.add(generalisedSection);
		}
	}
	
	
	class Walker {
			
		LinkedList<OsmNode> generalisedSection;
		
		private OsmNode startNode = null;  // E_S
		private CorrelationEdge startEdge = null;  // E
		
		private LineSegment segment1 = null;  // A
		private LineSegment segment2 = null;  // B
		private boolean segment1Aligned = true;
		private boolean segment2Aligned = true;
		private OsmNode currentNode1 = null;  // E_S
		private OsmNode currentNode2 = null;  // E_T
		private CorrelationEdge currentEdge = null;  // E
		
		
		Walker () {
			generalisedSection = new LinkedList<OsmNode>();
			findStartEdge();
		}
		
		
		void findStartEdge () {
			
			// we don't actually care where to start
			
			// (TG 1) choose segment S
			for (final CorrelationEdge edge : edges()) {
				// :TODO: is this counter even needed?
				if (edge.genCounter < 2) {
					
					OsmNode[] edgeNodes = new OsmNode[]{ edge.node0, edge.node1 };
					for (int i = 0; i < 2; i++) {
						OsmNode node = edgeNodes[i];
						
						// get segment with ID
						for (final LineSegment segment : node.connectingSegments) {
							if (segment.wasGeneralised) {
								continue;
							}
							
//							if (segment.way.id == -2L) {
								startEdge = edge;
								startNode = node;
								return;
//							}
						}
						
					}
				}
			}
		}
		
		
		/* :BUG:
		 * Certain edge cases exist in many input datasets. These lead to
		 * generalised lines overlapping each other (most often, but not
		 * necessarily always, at or near ends or begins of generalised
		 * sections). Probably something to do with the genCOunter...
		 */
		
		void run () {
			if (startNode == null) {
				generalisedSection = null;
				return;
			}
			
			addGeneralisedPoint(startEdge, true);
//System.out.println("\n" + startEdge);
			
			assert startNode.connectingSegments.size() <= 2;
			boolean forward = false;
			for (final LineSegment segment : startNode.connectingSegments) {
				forward = ! forward;
				if (segment.wasGeneralised) {
					continue;
				}
/*
				// :DEBUG: check if both forward and backward work
				if (segment.way.id != -2L) {
					continue;
				}
*/
				
				currentEdge = startEdge;
				currentNode1 = startNode;
				currentNode2 = startEdge.other(startNode);
				segment1 = segment;
				segment1Aligned = segment1.start == currentNode1;
				segment2 = findOppositeSegment(currentNode2, segment1, segment1Aligned);
				if (segment2 == null) {
					continue;
				}
				segment2Aligned = segment2.start == currentNode2;
				
				// (TG 3) ∀ D
				// :BUG: only works in forward direction
				
				// (TG 3a)
				while (currentEdge != null && currentEdge.genCounter < 2 && segment1 != null && segment2 != null) {
					assert currentNode1.connectingSegments.size() <= 2 : currentNode1;  // trivial case
					assert currentNode2.connectingSegments.size() <= 2 : currentNode2;  // trivial case
					
					// (TG 4) find M, draw it
//					generalisedSection.add(generalisedPoint( currentEdge ));
					
					// (TG 5) find next nodes
					OsmNode nextNode1 = segment1Aligned ? segment1.end : segment1.start;  // X
					OsmNode nextNode2 = segment2Aligned ? segment2.end : segment2.start;  // Y
					
					// (TG 6/7)
					CorrelationEdge nextEdge = new CorrelationEdge(nextNode1, currentNode2);
					nextEdge = intern(nextEdge);
					if ( nextEdge != null /*&& nextEdge.genCounter < 2*/ ) {
						
						currentEdge.genCounter++;
						nextEdge.genCounter++;
						assert currentEdge.genCounter <= 2 : currentEdge;
//						assert nextEdge.genCounter <= 2 : nextEdge;
						
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
						nextEdge = intern(nextEdge);
						if ( nextEdge != null /*&& nextEdge.genCounter < 2*/ ) {
							
							currentEdge.genCounter++;
							nextEdge.genCounter++;
							assert currentEdge.genCounter <= 2 : currentEdge;
//							assert nextEdge.genCounter <= 2 : nextEdge;
							
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
							nextEdge = intern(nextEdge);
							if ( nextEdge != null /*&& nextEdge.genCounter < 2*/ ) {
								
								currentEdge.genCounter++;
								nextEdge.genCounter++;
								assert currentEdge.genCounter <= 2 : currentEdge;
//								assert nextEdge.genCounter <= 2 : nextEdge;
								
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
								assert nextEdge == null || nextEdge.genCounter >= 2;
								
								currentEdge.genCounter++;
								assert currentEdge.genCounter <= 2 : currentEdge;
								currentEdge = null;  // break
							}
						}
					}
					
					addGeneralisedPoint(currentEdge, forward);
				}
			}
			startEdge.genCounter = 2;  // :TODO: this line may be wrong
//System.out.println(generalisedSection.size());
		}
		
		
		// (TG 4) find M
		void addGeneralisedPoint (CorrelationEdge edge, boolean addAsLast) {
			if (edge == null) {
				return;
			}
			
			double e = (edge.node0.e + edge.node1.e) / 2.0;
			double n = (edge.node0.n + edge.node1.n) / 2.0;
//			final OsmNode midPoint = OsmNode.createWithEastingNorthing(e, n);
			final OsmNode midPoint = dataset.getNodeAtEastingNorthing(e, n);  // :TODO: why this line? perhaps so that the node is inserted into the debug output?
			if (addAsLast) {
				generalisedSection.addLast( midPoint );
			}
			else {
				generalisedSection.addFirst( midPoint );
			}
		}
		
		
		// (TG 2a) find T
		LineSegment findOppositeSegment (OsmNode oppositeNode, LineSegment thisSegment, boolean thisIsAligned) {
			LineSegment oppositeSegment = null;
			
			Vector thisVector = thisIsAligned ? thisSegment.vector() : thisSegment.vector().reversed();
			for (LineSegment segment : oppositeNode.connectingSegments) {
				Vector vector = segment.start == oppositeNode ? segment.vector() : segment.vector().reversed();
				if ( Math.abs( vector.relativeBearing(thisVector) ) < Vector.RIGHT_ANGLE ) {
					assert oppositeSegment == null;  // only one should match, normally (might be some exceptions in some datasets, this is a :HACK:)
					oppositeSegment = segment;
				}
				
			}
			return oppositeSegment;
		}
		
		
		LineSegment findNextSegment (OsmNode pivot, LineSegment currentSegment) {
			LineSegment nextSegment = null;
			for (LineSegment segment : pivot.connectingSegments) {
				if (segment != currentSegment) {
					assert nextSegment == null;  // :BUG: handles trivial case only
					nextSegment = segment;
				}
			}
			return nextSegment;
		}
		
	}
	
}
