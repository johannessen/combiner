/* encoding UTF-8
 * 
 * Copyright (c) 2013 Arne Johannessen
 * 
 * This project and all of its individual parts may be used in accordance
 * with the terms of the 3-clause BSD licence. See LICENSE for details.
 */

package de.thaw.comb.cli;

import de.thaw.comb.Combiner;
import de.thaw.comb.Dataset;
import de.thaw.comb.Line;
import de.thaw.comb.Node;
import de.thaw.comb.ResultLine;
import de.thaw.comb.SourceNode;
import de.thaw.comb.highway.Highway;
import de.thaw.comb.highway.HighwayAnalyser;
import de.thaw.comb.io.InputDataset;
import de.thaw.comb.io.ShapeReader;

import java.io.File;
import java.util.Collection;

import org.kohsuke.args4j.CmdLineParser;
import org.kohsuke.args4j.CmdLineException;


/**
 * This Client's main class (executable).
 */
public final class CombinerMain {
	
	String inPath = null;
	String outPath = "out.shp";
	String nodeOutPath = "";
	String linePartOutPath = "";
	String debugOutPath = "";
	long startId = 0;
	
	// options (see cli.Options)
	int verbose;
	boolean cleanup;
	int evaluateTags;  // affects first analyser only
	int iterations;
	
	Dataset dataset;
	Combiner combiner;
	
	
	CombinerMain () {
	}
	
	
	
	void combineLines () {
		verbose(2, "Java maximum available memory: " + Runtime.getRuntime().maxMemory() / 1024L + " KB.");
		if (inPath == null || outPath == null) {
			throw new IllegalStateException("set input/output file paths first");
		}
		if (! inPath.endsWith(".shp")) {
			throw new IllegalArgumentException(".shp input only");
		}
		
		final File inFile = new File(inPath);
		
		final ShapeReader reader = new ShapeReader(inFile);
		dataset = reader.dataset();
		
		combiner = new Combiner(dataset, new HighwayAnalyser(evaluateTags));
//		combiner.printMemoryStatistics();
		combiner.verbose = verbose;
		combiner.cleanup(cleanup);
		combiner.run();
//		combiner.printMemoryStatistics();
		
		if (Math.abs(iterations) > 1) {
			combineLinesAgain(Math.abs(iterations) - 1);
		}
		
		System.out.println("Writing results...");
		
//		new Output2(dataset, reader.epsgCode()).writeAllNodes("test.sqlite");
		
		final Output out = new Output(dataset);
		out.verbose = verbose;
		out.writeAllLines(combiner.lines(), outPath);
		out.writeAllNodes(combiner.lines(), nodeOutPath);
		out.writeAllSegments(linePartOutPath);
//		out.writeAllFragments(linePartOutPath);
//		out.writeMidPointConnectors(debugOutPath);
//		out.writeFragmentMidPointConnectors(debugOutPath);
		out.writeNodeMatches(combiner.cns, debugOutPath);
		
		if (verbose >= 2) {
			combiner.printMemoryStatistics();
			System.gc();
			combiner.printMemoryStatistics();
		}
	}
	
	
	
	void combineLinesAgain (final int count) {
		
		// make generalisation result into new dataset for repetition of generalisation
		final InputDataset inputDataset = new InputDataset();
		for (final Line line : combiner.lines()) {
			final Highway way = inputDataset.createOsmWay(line);
			way.mutable(false);
		}
		dataset = inputDataset;
		
		combiner = new Combiner(dataset, new HighwayAnalyser(0));
		combiner.verbose = verbose;
		combiner.cleanup(cleanup);
		combiner.run();
		
		if (count > 1) {
			combineLinesAgain(count - 1);
		}
	}
	
	
	
	private void verbose (final int verbosity, final Object message) {
		if (verbose >= verbosity) {
			System.out.println( String.valueOf(message) );
		}
	}
	
	
	
	public static void main (String[] args) {
		Options options = new Options();
		CmdLineParser parser = new CmdLineParser(options);
		try {
			parser.parseArgument(args);
		}
		catch (CmdLineException e) {
			System.err.println(e.getMessage());
			parser.printUsage(System.err);
			System.exit(2);
		}
		
		final int evaluateTagsDefault = 0x1;  // only used with no multiple iterations
		
		CombinerMain combiner = new CombinerMain();
		combiner.inPath = options.input;
		combiner.outPath = options.output;
		combiner.nodeOutPath = options.outNodes;
		combiner.linePartOutPath = options.outLineParts;
		combiner.debugOutPath = options.outDebug;
		combiner.iterations = options.iterations;
		combiner.verbose = options.verbose ? 2 : 1;
		combiner.cleanup = ! options.noCleanup;
		combiner.evaluateTags = options.tags != Options.SENSIBLE_DEFAULT_INT ? options.tags : options.iterations > 1 ? 0 : evaluateTagsDefault;
		combiner.startId = options.startId;
		
		combiner.combineLines();
	}
	
}
