/* encoding UTF-8
 * 
 * Copyright (c) 2012-13 Arne Johannessen
 * 
 * This project and all of its individual parts may be used in accordance
 * with the terms of the 3-clause BSD licence. See LICENSE for details.
 */

package de.thaw.thesis.testbed;

import com.vividsolutions.jts.geom.Geometry;
import com.vividsolutions.jts.geom.Point;
import com.vividsolutions.jts.geom.LineString;
import org.opengis.feature.type.AttributeDescriptor;
import org.opengis.feature.simple.SimpleFeatureType;
import org.geotools.feature.simple.SimpleFeatureTypeBuilder;
import org.geotools.feature.SchemaException;
import org.geotools.data.DataUtilities;

import java.io.File;
import java.util.Collection;
import java.util.List;
import java.util.LinkedList;


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
		final Collection<LineString> lines = Testbed.getLineStrings(sourcePath);
		new ParallelismFinder(lines).analyse();
	}
	
	
	/**
	 * Run parallelism analysis of the lines read from the Shapefile using the
	 * splitting algorithm. Write output to new Shapefile.
	 */
	static void analyseTryingHarder (final String sourcePath, final String destPath) throws Exception {
		
		final ShapeReader reader = new ShapeReader();
		final Collection<LineString> originalLines = reader.readFrom(new File(sourcePath));
		
		final Splitter splitter = new Splitter(originalLines);
		splitter.split();
		
		final ParallelismFinder finder = new ParallelismFinder(splitter.lines);
//		finder.originalLines = originalLines;
		finder.analyse();
		
		final ParallelDisprover disprover = new ParallelDisprover(finder.results);
		disprover.analyse();
		
		final List<LineString> lineList = finder.resultAsLineList();
		
		final SimpleGeneraliser generaliser = new SimpleGeneraliser(lineList);
		final List<LineString> generalisedLines = generaliser.generalise();
		
		// new output: generalisation result
/*
		new ShapeWriter(destPath).writeGeometries(
				generalisedLines,
				new ShapeWriter.DefaultLineDelegate());
*/
		new Merger(generalisedLines).mergeAndWriteTo(destPath);
		
		
		// old output: line fragments with new attributes
		if (debugOutPath == null) {
			return;
		}
		Testbed.successfulLines = 0;
		Testbed.parallelLines = 0;
		new ShapeWriter(debugOutPath).writeGeometries(
				lineList,
				new ShapeWriterDelegate() {
			// this delegate enables us to include attributes with the geometry written to the output Shapefile
			
			final String PARALLEL_OSM_ID = "para_osmid";
			
			public SimpleFeatureType featureType () throws SchemaException {
				// :TRICKY: positional arguments MUST be in same order as in .attributes(Geometry)
				
				// :BUG: may not work with 64 bit IDs
				return DataUtilities.createType( "ParallelLines",
						"geometry:LineString:srid=" + Testbed.INTERNAL_EPSG_CODE()
						+ "," + ShapeReader.OSM_ID_ATTRIBUTE + ":Integer"
						+ "," + this.PARALLEL_OSM_ID + ":Integer"
						+ ",desc:String"
						+ ",reciprocal:Integer"
						+ ",in_buffer:Integer"
						+ ",no_par:Integer"
						);
			}
			
			int safeParseInt (String s) {
				try {
					return Integer.parseInt(s);
				}
				catch (NumberFormatException e) {
					return Integer.MIN_VALUE;
				}
			}
			
			public List attributes (Geometry geometry) {
				// :TRICKY: positional arguments MUST be in same order as in .featureType()
				
//				final LineMeta geometryMeta = LineMeta.getFrom(geometry);
				final LinePartMeta geometryMeta = LinePartMeta.getFrom(geometry);
				
				boolean parallelFound = geometryMeta.finderResults.parallelisms.size() > 0;
				if (! parallelFound) {
					List<Object> attributes = new LinkedList<Object>();
					attributes.add( safeParseInt(geometryMeta.toString()) );
					attributes.add( Integer.MIN_VALUE );
					attributes.add( LineMeta.description(geometry) + ": none found" );
					attributes.add( 0 );
					attributes.add( 0 );
					attributes.add( 1 );
					Testbed.successfulLines++;
					return attributes;
				}
				
				final Geometry parallel = geometryMeta.finderResults.parallelisms.first().origin;
//				final LineMeta parallelMeta = LineMeta.getFrom(parallel);
				final LinePartMeta parallelMeta = LinePartMeta.getFrom(parallel);
				
				boolean originalFound = parallelMeta.finderResults.parallelisms.size() > 0;
				if (! originalFound) {
					List<Object> attributes = new LinkedList<Object>();
					attributes.add( safeParseInt(geometryMeta.toString()) );
					attributes.add( Integer.MIN_VALUE + 1 );
					attributes.add( LineMeta.description(geometry) + ": none found 2" );
					attributes.add( 0 );
					attributes.add( 0 );
					attributes.add( 1 );
					Testbed.successfulLines++;
					return attributes;
				}
				
				final Geometry parallelParallel = parallelMeta.finderResults.parallelisms.first().origin;
				
				/* does the "likely parallel" of this LineString have _this_
				 * LineString registered as its own "likely parallel"? If so,
				 * then these two are almost definitely parallel (provided the
				 * metric used by the ParallelismFinder works okay)
				 */
//				boolean reciprocal = LineMeta.getFrom(parallelParallel) == LineMeta.getFrom(geometry);
				boolean reciprocal = false;
				Parallelism[] p = parallelMeta.finderResults.parallelisms.toArray(new Parallelism[0]);
				for (int i = 0; i < p.length; i++) {
					reciprocal |= LinePartMeta.getFrom(p[i].origin) == LinePartMeta.getFrom(geometry);
				}
				
//				if (reciprocal || ! geometryMeta.finderResults.parallelismsInBuffer) {
				if (reciprocal) {
					Testbed.parallelLines++;
					Testbed.successfulLines++;
				}
				
				List<Object> attributes = new LinkedList<Object>();
				attributes.add( safeParseInt(geometryMeta.toString()) );
				attributes.add( safeParseInt(parallelMeta.toString()) );
				attributes.add( geometryMeta.finderResults.toString() );
				attributes.add( reciprocal ? 1 : 0 );
				attributes.add( geometryMeta.finderResults.parallelismsInBuffer ? 1 : 0 );
				attributes.add( 0 );
				
				return attributes;
			}
			
		});
		
		System.out.println("Reciprocal rate: " + Testbed.parallelLines + " of " + finder.originalLines.size());
		System.out.println("Analysis success rate: " + Testbed.successfulLines + " of " + finder.originalLines.size());
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
		
//		final String in = "~aj3/Studium/Daten/nrw-road/koeln-motorways.shp";
//		final String in = "../data/shape-test/simple.shp";
//		final String out = "build/out.shp";
		
//		Testbed.analyse(in);
		Testbed.analyseTryingHarder(in, out);
//		Testbed.merge(in, out);
//		Testbed.passThrough(in, out);
		
//		new SpatialiteReader();
	}
	
}
