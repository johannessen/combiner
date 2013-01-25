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
	 * No output other than debug messages inside the Analyser.
	 */
	static void analyse (final String sourcePath) throws Exception {
		final Collection<LineString> lines = Testbed.getLineStrings(sourcePath);
		new Analyser(lines).analyse();
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
		
		final Analyser analyser = new Analyser(splitter.lines);
		analyser.originalLines = originalLines;
		analyser.analyse();
		
		final ParallelDisprover disprover = new ParallelDisprover(analyser.results);
		disprover.analyse();
		
		
		Testbed.successfulLines = 0;
		new ShapeWriter(destPath).writeGeometries(
				analyser.resultAsLineList(),
				new ShapeWriterDelegate() {
			// this delegate enables us to include attributes with the geometry written to the output Shapefile
			
			final String PARALLEL_OSM_ID = "para_osmid";
			
			public SimpleFeatureType featureType () throws SchemaException {
				// :TRICKY: positional arguments MUST be in same order as in .attributes(Geometry)
				
/*
				// :BUG: throws varying errors indicating trouble with property count and order; not traceable
				
				final SimpleFeatureTypeBuilder builder = new SimpleFeatureTypeBuilder();
				builder.init(reader.featureType);
//				builder.remove("the_geom");
//				builder.add("geometry", LineString.class, Testbed.INTERNAL_EPSG_CODE());  // :BUG: simpleFeaturesFromGeometries writes geometry first, needs adjustment
				builder.setName("ParallelLines");
//				builder.add(this.PARALLEL_OSM_ID, Long.class);
				builder.setDefaultGeometry("geometry");
				final SimpleFeatureType type = builder.buildFeatureType();
				return type;
*/
				
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
			
			public List attributes (Geometry geometry) {
				// :TRICKY: positional arguments MUST be in same order as in .featureType()
				
				final LineMeta geometryMeta = LineMeta.getFrom(geometry);
				
				boolean parallelFound = geometryMeta.analyserResults.parallelisms.size() > 0;
				if (! parallelFound) {
					List<Object> attributes = new LinkedList<Object>();
					attributes.add( geometryMeta.toString() );
					attributes.add( -1 );
					attributes.add( "none found" );
					attributes.add( 0 );
					attributes.add( 0 );
					attributes.add( 1 );
					Testbed.successfulLines++;
					return attributes;
				}
				
				final Geometry parallel = geometryMeta.analyserResults.parallelisms.first().origin;
				final LineMeta parallelMeta = LineMeta.getFrom(parallel);
				
				boolean originalFound = parallelMeta.analyserResults.parallelisms.size() > 0;
				if (! originalFound) {
					List<Object> attributes = new LinkedList<Object>();
					attributes.add( geometryMeta.toString() );
					attributes.add( -2 );
					attributes.add( "none found 2" );
					attributes.add( 0 );
					attributes.add( 0 );
					attributes.add( 1 );
					Testbed.successfulLines++;
					return attributes;
				}
				
				final Geometry parallelParallel = parallelMeta.analyserResults.parallelisms.first().origin;
				
				/* does the "likely parallel" of this LineString have _this_
				 * LineString registered as its own "likely parallel"? If so,
				 * then these two are almost definitely parallel (provided the
				 * metric used by the Analyser works okay)
				 */
//				boolean reciprocal = LineMeta.getFrom(parallelParallel) == LineMeta.getFrom(geometry);
				boolean reciprocal = false;
				Analyser.Parallelism[] p = parallelMeta.analyserResults.parallelisms.toArray(new Analyser.Parallelism[0]);
				for (int i = 0; i < p.length; i++) {
					reciprocal |= LineMeta.getFrom(p[i].origin) == LineMeta.getFrom(geometry);
				}
				
//				if (reciprocal || ! geometryMeta.analyserResults.parallelismsInBuffer) {
				if (reciprocal) {
					Testbed.successfulLines++;
				}
				
/*
				// copy attributes from source file
				
				// :BUG: throws varying errors indicating trouble with property count and order; not traceable
				
				// OpenGIS uses Java Collections in its interface, and collections are unordered
				// but the actual collections used all seem to be Lists (which are ordered collections)
				
				final List<Object> attributes = new LinkedList<Object>();
				try {
					for (final AttributeDescriptor attribute: this.featureType().getAttributeDescriptors()) {
						final String attributeName = attribute.getLocalName();
//						if (attributeName == "geometry") {
//							continue;
//						}
						if (attributeName == this.PARALLEL_OSM_ID) {
							final Object osmId = parallelOriginMeta.feature.getAttribute(ShapeReader.OSM_ID_ATTRIBUTE);
							attributes.add( osmId );
							continue;
						}
						attributes.add( geometryOriginMeta.feature.getAttribute(attributeName) );
					}
				}
				catch (SchemaException exception) {
					throw new RuntimeException(exception);
				}
*/
				
				List<Object> attributes = new LinkedList<Object>();
				attributes.add( geometryMeta.toString() );
				attributes.add( parallelMeta.toString() );
				attributes.add( geometryMeta.analyserResults.toString() );
				attributes.add( reciprocal ? 1 : 0 );
				attributes.add( geometryMeta.analyserResults.parallelismsInBuffer ? 1 : 0 );
				attributes.add( 0 );
				
				return attributes;
			}
			
		});
		
		System.out.println("Success rate: " + Testbed.successfulLines + " of " + originalLines.size());
	}
	
	
	static void merge (final String sourcePath, final String destPath) throws Exception {
		final Collection<LineString> lines = Testbed.getLineStrings(sourcePath);
		new Merger(lines).mergeAndWriteTo(destPath);
	}
	
	
	static void passThrough (final String sourcePath, final String destPath) throws Exception {
		new ShapeWriter(destPath).writeLines( Testbed.getLineStrings(sourcePath) );
	}
	
	
	public static void main (String[] args) throws Throwable {
		final String in = args.length > 0 ? args[0] : "";
		final String out = args.length > 1 ? args[1] : "";
		
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
