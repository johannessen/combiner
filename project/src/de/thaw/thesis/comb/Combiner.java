/* encoding UTF-8
 * 
 * Copyright (c) 2013 Arne Johannessen
 * 
 * This project and all of its individual parts may be used in accordance
 * with the terms of the 3-clause BSD licence. See LICENSE for details.
 */

package de.thaw.thesis.comb;

import de.thaw.thesis.comb.util.MutableIterator2;

import com.vividsolutions.jts.index.SpatialIndex;
import com.vividsolutions.jts.index.strtree.STRtree;
import com.vividsolutions.jts.geom.Envelope;

import java.util.Collection;
import java.util.Collections;
//import java.util.LinkedHashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;
//import java.util.TreeSet;



/**
 * The Combiner's main class / Façade. Implements the main top-level
 * generalisation algorithm. After properly initialising the
 * <code>Dataset</code> and the parallelism <code>Analyser</code>, call
 * <code>run()</code> to run the generalisation by combination.
 */
public final class Combiner {
	
	private final Dataset dataset;
	
	private final Analyser analyser;
	
	// :DEBUG: debugging output
	public Collection<CorrelationEdge> cns;
	public GeneralisedLines gen;
	
	public int verbose = 0;
	
	public StatSink stats = null;
	
	
	
	/**
	 * 
	 */
	public Combiner (final Dataset dataset, final Analyser analyser) {
		this.dataset = dataset;
		this.analyser = analyser;
	}
	
	
	
	/**
	 * 
	 */
	public void run () {
		long startTime = System.currentTimeMillis();
		
		regionaliseSegments(/* allSegements */);
Combiner.printMemoryStatistics();
		splitSegments(/* ways */);
Combiner.printMemoryStatistics();
		analyseSegments(analyser);
Combiner.printMemoryStatistics();
		
		CorrelationGraph graph = correlateNodes();
		cns = graph.edges();  // :DEBUG: debugging output
		
		GeneralisedLines lines = new GeneralisedLines();
		lines.traverse(graph);
		gen = lines;  // :DEBUG: debugging output
Combiner.printMemoryStatistics();
		
		lines.cleanup(dataset);
		
		verbose(1, "Done.");
		verbose(1, "Processing time: " + (System.currentTimeMillis() - startTime) + " ms");
	}
	
	
	
	CorrelationGraph correlateNodes () {
		CorrelationGraph correlations = new CorrelationGraph(dataset);
		verbose(1, "Node Correlation done.");
		return correlations;
	}
	
	
	
	void analyseSegments (Analyser visitor) {
		for (final SourceSegment segment : dataset.allSegments()) {
			segment.analyseLineParts(visitor);
		}
		verbose(1, "Analysis done.");
	}
	
	
	
	// NAHESEGMENTE, see chapter 4.3.1
	@SuppressWarnings("unchecked")
	private void regionaliseSegments () {
		Collection<SourceSegment> allSegments = dataset.allSegments();
		verbose(1, "Total segment count: " + allSegments.size());
		
		final SpatialIndex index = new STRtree();
		for (final SourceSegment segment : allSegments) {
			final Envelope envelope = segment.envelope();
			assert ! envelope.isNull();
			index.insert(envelope, segment);
		}
		
		// stats
		int i = 0;
		int c = 0;
		int p = 0;
		
		// the first call to AbstractSTRtree.query builds (packs) the tree
		for (final SourceSegment segment : allSegments) {
			final List<SourceSegment> closeSegments = index.query(segment.envelope());
			closeSegments.remove(segment);  // keep segments from being compared with themselves down the road
			segment.setCloseSegments(closeSegments);
			
			// stats
			i++;
			c += closeSegments.size();
			p += segment.closeParallels().size();
		}
		verbose(1, "Regionalisation done; " + (i > 0 ? "on average " + c / i + " close segments, " + p / i + " close false parallels." : "no segments exist in source data."));
	}
	
	
	
	void splitSegments () {
		// SPLITTEN, see chapter 4.3.1
		
		final SplitQueueIterator iterator = createSplitQueue();  // "S'"
		while (iterator.hasNext()) {
			Segment base = iterator.next();  // "s"
			
			SplitQueueListener sink = iterator;
			base.splitCloseParallels(sink);
		}
		verbose(1, "Splitting done.");
	}
	
	
	
	private SplitQueueIterator createSplitQueue () {
		
		// creates queue of split bases, initialised with all segments
		
		final SplitQueueIterator iterator = new SplitQueueIterator();
		
		// :BUG: expensive; write our own Queue implementation and work directly on its Entry objects
		for (final OsmWay way : dataset.ways()) {
			// we only have segments at the beginning, which means this is sufficient
			// fragments are added later as they are created, see MutableIterator#add()
			iterator.add(Collections.<Segment>unmodifiableCollection( way ));
		}
		
		return iterator;
	}
	
	
	
	final private class SplitQueueIterator extends MutableIterator2<Segment> implements SplitQueueListener {
		
		SplitQueueIterator () {
			super();
		}
		
		public void didSplit (final List<Segment> fragments, final Node splitNode) {
			
			// enqueue new fragments as split bases
			super.add(fragments);
		}
		
	}
	
	
	
	private void verbose (final int verbosity, final Object message) {
		if (verbose >= verbosity) {
			System.out.println( String.valueOf(message) );
		}
	}
	
	
	
	public static void printMemoryStatistics () {
		long total = Runtime.getRuntime().totalMemory() / 1024L;
		long used = (Runtime.getRuntime().totalMemory() - Runtime.getRuntime().freeMemory()) / 1024L;
		System.err.println("Java using " + used + " KB of " + total + " KB.");
		try {
			Process p = new ProcessBuilder("ps", "au").redirectErrorStream(true).start();
			java.io.BufferedReader stdInput = new java.io.BufferedReader(new java.io.InputStreamReader(p.getInputStream()));
			String line = null;
			while ((line = stdInput.readLine()) != null) {
				if (line.contains("java")) {
					System.err.println("\t" + line.split("java")[0]);
				}
			}
		}
		catch (Exception e) {
			// ignore
		}
	}
	
}
