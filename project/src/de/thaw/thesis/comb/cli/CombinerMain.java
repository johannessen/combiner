/* encoding UTF-8
 * 
 * Copyright (c) 2013 Arne Johannessen
 * 
 * This project and all of its individual parts may be used in accordance
 * with the terms of the 3-clause BSD licence. See LICENSE for details.
 */

package de.thaw.thesis.comb.cli;

import de.thaw.thesis.comb.Combiner;
import de.thaw.thesis.comb.GeneralisedLines;
import de.thaw.thesis.comb.Line;
import de.thaw.thesis.comb.OsmDataset;
import de.thaw.thesis.comb.OsmNode;
import de.thaw.thesis.comb.OsmWay;
import de.thaw.thesis.comb.io.ShapeReader;

import de.thaw.thesis.comb.io.SQLiteWriter;
import de.thaw.thesis.comb.StatSink;

import java.io.File;
import java.util.Collection;


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
		
		final StatSink stats = new SQLiteWriter().connect("").createTables();
		
		final File inFile = new File(inPath);
		
		ShapeReader.VERBOSE = VERBOSE;
		final ShapeReader reader = new ShapeReader(inFile);
		final OsmDataset dataset = reader.osmDataset();
		dataset.stats = stats;
Combiner.printMemoryStatistics();
		
		final Combiner combiner = new Combiner(dataset, new MyAnalyser(iterations < 0));
		combiner.stats = stats;
		combiner.verbose = VERBOSE;
		combiner.run(startId);
Combiner.printMemoryStatistics();
		
		if (Math.abs(iterations) > 1) {
			combineLines2(Math.abs(iterations) - 1, combiner.gen, reader.epsgCode());
			return;
		}
		
		System.out.println("Writing results...");
		
//		new Output2(dataset, reader.epsgCode()).writeAllNodes("test.sqlite");
		
		final Output out = new Output(dataset, reader.epsgCode());
		out.verbose = VERBOSE;
		out.writeAllNodes(nodeOutPath);
		out.writeAllSegments(linePartOutPath);
//		out.writeAllFragments(linePartOutPath);
		out.writeMidPointConnectors(debugOutPath);
//		out.writeFragmentMidPointConnectors(debugOutPath);
//		out.writeCorrelationEdges(combiner.cns, debugOutPath);
//		out.writeGeneralisedLines(combiner.gen, outPath);
		out.writeAllLines(combiner.gen, outPath);
//		out.writeSimplifiedSections(combiner.gen, outPath);
Combiner.printMemoryStatistics();
System.gc();
Combiner.printMemoryStatistics();
	}
	
	
	
	void combineLines2 (final int count, final GeneralisedLines lines, final int epsgCode) {
		
		OsmDataset dataset = new OsmDataset();
		addLinesToDataset(dataset, lines.lines1());
		addLinesToDataset(dataset, lines.lines2());
		dataset.setCompleted();
		
		final Combiner combiner = new Combiner(dataset, new MyAnalyser(false));
		combiner.verbose = VERBOSE;
		combiner.run(startId);
		
		if (count > 1) {
			combineLines2(count - 1, combiner.gen, epsgCode);
			return;
		}
		
		
		System.out.println("Writing results...");
		
		final Output out = new Output(dataset, epsgCode);
		out.verbose = VERBOSE;
//		out.writeAllNodes(nodeOutPath);
//		out.writeGeneralisedLines(combiner.gen, outPath);
		out.writeAllLines(combiner.gen, outPath);
//		out.writeSimplifiedSections(combiner.gen, outPath);
	}
	
	
	
	// make generalisation result into new dataset for repetition of generalisation
	void addLinesToDataset (final OsmDataset dataset, final Collection<? extends Line> sections) {
		for (final Line section : sections) {
			final OsmWay way = dataset.createOsmWay(section.tags(), section.size());
			for (OsmNode node : section.combination()) {
				node = dataset.getNodeAtEastingNorthing(node.easting(), node.northing());
				
				way.addLast(node);
				
			}
			way.mutable(false);
		}
	}
	
	
	
	public static void main (String[] args) {
		if (args.length == 0) {
			throw new IllegalArgumentException("Arguments: inFile outFile [nodeOutFile [lineOutFile [debugOutFile [repetions]]]]");
		}
		
		CombinerMain combiner = new CombinerMain();
		combiner.inPath = args[0];
		if (args.length > 1) {
			combiner.outPath = args[1];
		}
		if (args.length > 2) {
			combiner.nodeOutPath = args[2];
		}
		if (args.length > 3) {
			combiner.linePartOutPath = args[3];
		}
		if (args.length > 4) {
			combiner.debugOutPath = args[4];
		}
		if (args.length > 5) {
			try {
				combiner.iterations = Integer.parseInt(args[5]);
			} catch (NumberFormatException e) {}
		}
		if (args.length > 6) {
			try {
				combiner.startId = Long.parseLong(args[6]);
			} catch (NumberFormatException e) {}
		}
		
		combiner.combineLines();
	}
	
}
