/* encoding UTF-8
 * 
 * Copyright (c) 2013 Arne Johannessen
 * 
 * This project and all of its individual parts may be used in accordance
 * with the terms of the 3-clause BSD licence. See LICENSE for details.
 */

package de.thaw.thesis.comb.cli;

import de.thaw.thesis.comb.Combiner;
import de.thaw.thesis.comb.OsmDataset;
import de.thaw.thesis.comb.io.ShapeReader;

import java.io.File;


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
	
	
	
	CombinerMain () {
	}
	
	
	
	void combineLines () {
		if (inPath == null || outPath == null) {
			throw new IllegalStateException("set input/output file paths first");
		}
		if (! inPath.endsWith(".shp")) {
			throw new IllegalArgumentException(".shp input only");
		}
		
		final File inFile = new File(inPath);
		
		ShapeReader.VERBOSE = VERBOSE;
		final ShapeReader reader = new ShapeReader(inFile);
		final OsmDataset dataset = reader.osmDataset();
		
		final Combiner combiner = new Combiner(dataset, new MyAnalyser());
		combiner.verbose = VERBOSE;
		combiner.run(startId);
		
		System.out.println("Writing results...");
		final Output out = new Output(dataset, reader.epsgCode());
		out.verbose = VERBOSE;
		out.writeAllNodes(nodeOutPath);
		out.writeAllSegments(linePartOutPath);
//		out.writeAllFragments(linePartOutPath);
//		out.writeMidPointConnectors(debugOutPath);
//		out.writeFragmentMidPointConnectors(debugOutPath);
		out.writeCorrelationEdges(combiner.cns, debugOutPath);
		out.writeGeneralisedLines(combiner.gen, outPath);
	}
	
	
	
	public static void main (String[] args) {
		if (args.length == 0) {
			throw new IllegalArgumentException("Arguments: inFile outFile [nodeOutFile [lineOutFile [debugOutFile]]]");
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
				combiner.startId = Long.parseLong(args[5]);
			} catch (NumberFormatException e) {}
		}
		
		combiner.combineLines();
	}
	
}
