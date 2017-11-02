/* encoding UTF-8
 * 
 * Copyright (c) 2013 Arne Johannessen
 * 
 * This project and all of its individual parts may be used in accordance
 * with the terms of the 3-clause BSD licence. See LICENSE for details.
 */

package de.thaw.thesis.comb.cli;

import de.thaw.thesis.comb.Combiner;
import de.thaw.thesis.comb.Dataset;
import de.thaw.thesis.comb.GeneralisedLines;
import de.thaw.thesis.comb.Line;
import de.thaw.thesis.comb.Node;
import de.thaw.thesis.comb.SourceNode;
import de.thaw.thesis.comb.highway.Highway;
import de.thaw.thesis.comb.highway.HighwayAnalyser;
import de.thaw.thesis.comb.io.InputDataset;
import de.thaw.thesis.comb.io.ShapeReader;
import de.thaw.thesis.comb.io.SQLiteWriter;

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
	
	int VERBOSE = 1;
	
	// negative value: first analyser uses tags
	int iterations = 0;
	
	
	
	CombinerMain () {
	}
	
	
	
	void combineLines () {
System.err.println("Java maximum available memory: " + Runtime.getRuntime().maxMemory() / 1024L + " KB.");
		if (inPath == null || outPath == null) {
			throw new IllegalStateException("set input/output file paths first");
		}
		if (! inPath.endsWith(".shp")) {
			throw new IllegalArgumentException(".shp input only");
		}
		
//		final StatSink stats = new SQLiteWriter().connect("").createTables();
		
		final File inFile = new File(inPath);
		
		final ShapeReader reader = new ShapeReader(inFile);
		final Dataset dataset = reader.dataset();
//		dataset.stats = stats;
Combiner.printMemoryStatistics();
		
		final Combiner combiner = new Combiner(dataset, new HighwayAnalyser(iterations < 0));
//		combiner.stats = stats;
		combiner.verbose = VERBOSE;
		combiner.run();
Combiner.printMemoryStatistics();
		
		if (Math.abs(iterations) > 1) {
			combineLines2(Math.abs(iterations) - 1, combiner.gen);
			return;
		}
		
		System.out.println("Writing results...");
		
//		new Output2(dataset, reader.epsgCode()).writeAllNodes("test.sqlite");
		
		final Output out = new Output(dataset);
		out.verbose = VERBOSE;
		out.writeAllNodes(nodeOutPath);
		out.writeAllSegments(linePartOutPath);
//		out.writeAllFragments(linePartOutPath);
		out.writeMidPointConnectors(debugOutPath);
//		out.writeFragmentMidPointConnectors(debugOutPath);
//		out.writeNodeMatches(combiner.cns, debugOutPath);
//		out.writeGeneralisedLines(combiner.gen, outPath);
		out.writeAllLines(combiner.gen, outPath);
//		out.writeSimplifiedSections(combiner.gen, outPath);
Combiner.printMemoryStatistics();
System.gc();
Combiner.printMemoryStatistics();
	}
	
	
	
	void combineLines2 (final int count, final GeneralisedLines lines) {
		
		InputDataset dataset = new InputDataset();
		addLinesToDataset(dataset, lines.lines());
		
		final Combiner combiner = new Combiner(dataset, new HighwayAnalyser(false));
		combiner.verbose = VERBOSE;
		combiner.run();
		
		if (count > 1) {
			combineLines2(count - 1, combiner.gen);
			return;
		}
		
		
		System.out.println("Writing results...");
		
		final Output out = new Output(dataset);
		out.verbose = VERBOSE;
//		out.writeAllNodes(nodeOutPath);
//		out.writeGeneralisedLines(combiner.gen, outPath);
		out.writeAllLines(combiner.gen, outPath);
//		out.writeSimplifiedSections(combiner.gen, outPath);
	}
	
	
	
	// make generalisation result into new dataset for repetition of generalisation
	void addLinesToDataset (final InputDataset dataset, final Collection<? extends Line> sections) {
		for (final Line section : sections) {
			final Highway way = dataset.createOsmWay(section.tags(), section.size());
			for (Node node : section.coordinates()) {
				node = dataset.getNodeAtEastingNorthing(node.easting(), node.northing());
				
//				way.addLast(node);
				way.addLast((SourceNode)node);  // untested
				
			}
			way.mutable(false);
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
			return;
		}
		
		CombinerMain combiner = new CombinerMain();
		combiner.inPath = options.input;
		combiner.outPath = options.output;
		combiner.nodeOutPath = options.outNodes;
		combiner.linePartOutPath = options.outLineParts;
		combiner.debugOutPath = options.outDebug;
		combiner.iterations = options.iterations;
		combiner.startId = options.startId;
		
		combiner.combineLines();
	}
	
}
