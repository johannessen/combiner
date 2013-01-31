/* encoding UTF-8
 * 
 * Copyright (c) 2012-13 Arne Johannessen
 * 
 * This project and all of its individual parts may be used in accordance
 * with the terms of the 3-clause BSD licence. See LICENSE for details.
 */

package de.thaw.thesis.testbed;

import com.vividsolutions.jts.geom.LineString;

import java.io.File;
import java.util.Collection;


public final class Testbed {
	
	
	static int INTERNAL_EPSG_CODE () { return 32632; }  // UTM 32 U
	
	static int VERBOSITY () { return 1; }
	
	static int successfulLines = Integer.MIN_VALUE;
	static int parallelLines = Integer.MIN_VALUE;
	
	static String debugOutPath = null;
	
	
	static Collection<LineString> getLineStrings (final String path) throws Exception {
		final File file = new File(path);
		if (path.endsWith(".shp")) {
			return new ShapeReader().readFrom(file);
		}
		else if (path.endsWith(".wkt")) {
			return new WktReader().readFrom(file);
		}
		else {
			throw new IllegalArgumentException(".wkt or .shp only please");
		}
	}
	
	
	/**
	 * Run parallelism analysis of *exactly* the lines read from the Shapefile.
	 * No output other than debug messages inside the ParallelismFinder.
	 */
	static void analyse (final String sourcePath) throws Exception {
		
		// may no longer work correctly, given changes in ParallelismFinder
		
		final Collection<LineString> lines = Testbed.getLineStrings(sourcePath);
		new ParallelismFinder(lines).analyse();
	}
	
	
	/**
	 * Run parallelism analysis of the lines read from the Shapefile using the
	 * splitting algorithm. Write output to new Shapefile.
	 */
	static void analyseTryingHarder (final String sourcePath, final String destPath) throws Exception {
		
		final ShapeReader reader = new ShapeReader();
		final Collection<LineString> lines = reader.readFrom(new File(sourcePath));
		
		final Analyser analyser = new Analyser(lines);
		analyser.analyse();
		analyser.outputResults(sourcePath, destPath, debugOutPath);
	}
	
	
	static void merge (final String sourcePath, final String destPath) throws Exception {
		final Collection<LineString> lines = Testbed.getLineStrings(sourcePath);
		new Merger(lines).mergeAndWriteTo(destPath);
	}
	
	
	static void passThrough (final String sourcePath, final String destPath) throws Exception {
		new ShapeWriter(destPath).writeLines( Testbed.getLineStrings(sourcePath) );
	}
	
	
	public static void main (String[] args) throws Throwable {
		final String in = args.length > 0 ? args[0] : null;
		final String out = args.length > 1 ? args[1] : null;
		if (args.length > 2) {
			Testbed.debugOutPath = args[2];
		}
		
//		Testbed.analyse(in);
		Testbed.analyseTryingHarder(in, out);
//		Testbed.merge(in, out);
//		Testbed.passThrough(in, out);
		
//		new SpatialiteReader();
	}
	
}
