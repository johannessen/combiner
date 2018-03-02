/* encoding UTF-8
 * 
 * Copyright (c) 2013 Arne Johannessen
 * 
 * This project and all of its individual parts may be used in accordance
 * with the terms of the 3-clause BSD licence. See LICENSE for details.
 */

package de.thaw.comb;

//import de.thaw.comb.io.StatSink;
import de.thaw.comb.util.MutableIterator2;

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
 * <code>run()</code> to run the generalisation by merging / collapsing.
 */
public final class Combiner {
	
	private final Dataset dataset;
	
	private final Analyser analyser;
	private GeneralisedLines lines;
	
	// :DEBUG: debugging output
	public Collection<NodeMatch> cns;
	
	public int verbose = 0;
	private boolean cleanup = true;
	
	
	
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
		final long startTime = System.currentTimeMillis();
		
		regionaliseSegments(/* allSegements */);
		splitSegments(/* ways */);
		analyseSegments(analyser);
		
		NodeGraph graph = correlateNodes();
		lines = generaliseLines(graph);
		
		printStats(startTime);
	}
	
	
	
	private void printStats (final long startTime) {
		final long endTime = System.currentTimeMillis();
		printMemoryStatistics();
		
		int concatLength = 0, genLength = 0;
		for (final ResultLine line : lines.lines()) {
			if (line instanceof ConcatenatedSection) {
				concatLength += line.length();
			}
			else if (line instanceof GeneralisedSection) {
				genLength += line.length();
			}
		}
		String percent = String.format("%.1f", 100f * genLength / (concatLength + genLength));
		percent = percent.equals("0.0") && genLength > 0 ? "0.1" : percent;  // avoid "0%" if generalisations did take place
		int km = (concatLength + genLength) / 1000;
		verbose(1, "Road network length: " + km + " km (generalised: " + (genLength / 1000) + " km or " + percent + " %, other: " + (km - genLength / 1000) + " km)");
		
		int fragments = 0, nodeMatches = 0, matchedNodes = 0;
		for (final SourceSegment segment : dataset.allSegments()) {
			for (final Segment fragment : segment) {
				fragments++;
			}
		}
		for (final Node node : dataset.allNodes()) {
			if (node instanceof SourceNode) {
				if ( ((SourceNode)node).matches().size() > 0 ) {
					nodeMatches += ((SourceNode)node).matches().size();
					matchedNodes++;
				}
			}
		}
		String psi = String.format("%.2f", (double) nodeMatches / matchedNodes);
		verbose(1, "Segments after splitting |S'| = " + fragments + ", matches per node ψ = " + psi + " on average.");
		
		verbose(1, "Processing time: " + (endTime - startTime) + " ms");
	}
	
	
	
	/**
	 * Retrieve the generalisation result.
	 * @throws IllegalStateException if the Combiner hasn't been run yet
	 * @see GeneralisedLines#lines()
	 */
	public Collection<ResultLine> lines () {
		if (lines == null) {
			throw new IllegalStateException("Call run() first");
		}
		return lines.lines();
	}
	
	
	
	NodeGraph correlateNodes () {
		NodeGraph matches = new NodeGraph(dataset);
		verbose(1, "Node Matching done.");
		cns = matches.matches();  // :DEBUG: debugging output
		return matches;
	}
	
	
	
	GeneralisedLines generaliseLines (final NodeGraph graph) {
		GeneralisedLines lines = new GeneralisedLines();
		lines.traverse(graph);
		lines.concatUncombinedLines(dataset);
		if (cleanup) {
			lines.cleanup();
		}
		
		// stats
		int totalCount = dataset.allSegments().size();
		int concatCount = 0;
		for (final ResultLine line : lines.lines()) {
			if (line instanceof ConcatenatedSection) {
				concatCount += line.size();
			}
		}
		String percent = String.format("%.1f", 100.0 * (totalCount - concatCount) / totalCount);
		percent = percent.equals("0.0") && totalCount - concatCount > 0 ? "0.1" : percent;  // avoid "0%" if generalisations did take place
		verbose(1, "Done; generalised " + (totalCount - concatCount) + " of " + totalCount + " segments (" + percent + " %), leaving " + concatCount);
		return lines;
	}
	
	
	
	void analyseSegments (Analyser visitor) {
		for (final SourceSegment segment : dataset.allSegments()) {
			segment.analyseLineParts(visitor);
		}
		verbose(1, "Analysis done.");
		printMemoryStatistics();
	}
	
	
	
	// NAHESEGMENTE, see chapter 4.3.1
	@SuppressWarnings("unchecked")
	private void regionaliseSegments () {
		Collection<SourceSegment> allSegments = dataset.allSegments();
		verbose(2, "Total segment count: " + allSegments.size());
		
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
		verbose(1, "Regionalisation done; " + (i > 0 ? "on average " + c / i + " close segments (" + p / i + " aligned)." : "no segments exist in source data."));
		printMemoryStatistics();
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
		printMemoryStatistics();
	}
	
	
	
	private SplitQueueIterator createSplitQueue () {
		
		// creates queue of split bases, initialised with all segments
		
		final SplitQueueIterator iterator = new SplitQueueIterator();
		
		// :BUG: expensive; write our own Queue implementation and work directly on its Entry objects
		for (final Line way : dataset.ways()) {
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
	
	
	
	public boolean cleanup () {
		return cleanup;
	}
	
	
	
	public void cleanup (final boolean cleanup) {
		this.cleanup = cleanup;
	}
	
	
	
	private void verbose (final int verbosity, final Object message) {
		if (verbose >= verbosity) {
			System.out.println( String.valueOf(message) );
		}
	}
	
	
	
	public void printMemoryStatistics () {
		if (verbose < 2) {
			return;
		}
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
