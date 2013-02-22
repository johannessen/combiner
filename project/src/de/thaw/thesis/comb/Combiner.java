/* encoding UTF-8
 * 
 * Copyright (c) 2013 Arne Johannessen
 * 
 * This project and all of its individual parts may be used in accordance
 * with the terms of the 3-clause BSD licence. See LICENSE for details.
 */

package de.thaw.thesis.comb;

import de.thaw.thesis.comb.util.MutableIterator;

import com.vividsolutions.jts.index.SpatialIndex;
import com.vividsolutions.jts.index.strtree.STRtree;
import com.vividsolutions.jts.geom.Envelope;

import java.util.Collection;
//import java.util.LinkedHashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;
//import java.util.TreeSet;



/**
 * The Combiner's main class / Façade. Implements the main top-level
 * generalisation algorithm. After properly initialising the
 * <code>OsmDataset</code> and the parallelism <code>Analyser</code>, call
 * <code>run()</code> to run the generalisation by combination.
 */
public final class Combiner {
	
	private final OsmDataset dataset;
	
	private final Analyser analyser;
	
	// :DEBUG: debugging output
	public Set<CorrelationEdge> cns;
	public Collection<Collection<OsmNode>> gen;
	
	public int verbose = 0;
	
	
	
	/**
	 * 
	 */
	public Combiner (final OsmDataset dataset, final Analyser analyser) {
		dataset.setCompleted();  // no data modifications beyond this point
		this.dataset = dataset;
		this.analyser = analyser;
	}
	
	
	
	/**
	 * 
	 */
	public void run (final long startId) {
		splitSegments();
		analyseSegments(analyser);
		
		CorrelationGraph graph = correlateNodes();
		cns = graph.edges;  // :DEBUG: debugging output
		generaliseSegments(graph, startId);
		gen = graph.generalisedLines;
		// :TODO: ... magic
		
		verbose(1, "Done.");
	}
	
	
	
	void generaliseSegments (final CorrelationGraph graph, final long startId) {
		
		LineSegment start = graph.findStart(startId);
		if (start != null) {
			graph.traverse(start);
		}
		
	}
	
	
	
	CorrelationGraph correlateNodes () {
		CorrelationGraph correlations = new CorrelationGraph(dataset);
		verbose(1, "Node Correlation done.");
		return correlations;
	}
	
	
	
	void analyseSegments (Analyser visitor) {
		for (final LineSegment segment : dataset.allSegments()) {
			segment.analyseLineParts(visitor);
		}
		verbose(1, "Analysis done.");
	}
	
	
	
	@SuppressWarnings("unchecked")
	private void regionaliseSegments () {
		List<LineSegment> allSegments = dataset.allSegments();
		verbose(1, "Total segment count: " + allSegments.size());
		
		final SpatialIndex index = new STRtree();
		for (final LineSegment segment : allSegments) {
			final Envelope envelope = segment.envelope();
			assert ! envelope.isNull();
			index.insert(envelope, segment);
		}
		
		// stats
		int i = 0;
		int c = 0;
		int p = 0;
		
		for (final LineSegment segment : allSegments) {
			final List<LineSegment> closeSegments = index.query(segment.envelope());
			closeSegments.remove(segment);  // keep segments from being compared with themselves down the road
			segment.setCloseSegments(closeSegments);
			
			// stats
			i++;
			c += closeSegments.size();
			p += segment.closeParallels().size();
		}
		verbose(1, "Regionalisation done; on average " + c / i + " close segments, " + p / i + " close false parallels.");
	}
	
	
	
	void splitSegments () {
		regionaliseSegments();
		
		final SplitQueueIterator iterator = createSplitQueue();
		while (iterator.hasNext()) {
			LinePart base = iterator.next();
			
			SplitQueueListener sink = iterator;
			base.splitCloseParallels(sink);
		}
		verbose(1, "Splitting done.");
	}
	
	
	
	private SplitQueueIterator createSplitQueue () {
		
		// creates queue of split bases, initialised with all segments
		
		final SplitQueueIterator iterator = new SplitQueueIterator();
		
		// :BUG: expensive; write our own Queue implementation and work directly on its Entry objects
		for (final OsmWay way : dataset.ways) {
			// we only have segments at the beginning, which means this is sufficient
			// fragments are added later as they are created, see MutableIterator#add()
			iterator.addAll(way.segments());
		}
		
		return iterator;
	}
	
	
	
	final private class SplitQueueIterator extends MutableIterator<LinePart> implements SplitQueueListener {
		
		SplitQueueIterator () {
			super();
		}
		
		public void didSplit (final LineFragment newLine1, final LineFragment newLine2, final OsmNode splitNode) {
			
			// enqueue new fragments as split bases
			super.add(newLine1);
			super.add(newLine2);
			
			final boolean didAdd;
			didAdd = dataset.addNode(splitNode);
			
			// if the node was already present, there shouldn't have been a split in the first place!
			// :BUG: this logic predictably breaks down if fuzzy matching is utilised
			assert didAdd;
		}
		
	}
	
	
	
	private void verbose (final int verbosity, final Object message) {
		if (verbose >= verbosity) {
			System.out.println( String.valueOf(message) );
		}
	}
	
}
