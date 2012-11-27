/* encoding UTF-8
 * 
 * Copyright (c) 2012 Arne Johannessen
 * 
 * This project and all of its individual parts may be used in accordance
 * with the terms of a BSD-style license. See LICENSE for details.
 */

package de.thaw.espebu;

import com.vividsolutions.jts.geom.Geometry;
import com.vividsolutions.jts.geom.Point;
import com.vividsolutions.jts.geom.LineString;
import org.opengis.feature.simple.SimpleFeatureType;
import org.geotools.feature.SchemaException;
import org.geotools.data.DataUtilities;

import java.io.File;
import java.util.Collection;
import java.util.List;
import java.util.LinkedList;


public final class Espebu {
	
	
	static int INTERNAL_EPSG_CODE () { return 32632; }  // UTM 32 U
	
	static int VERBOSITY () { return 1; }
	
	
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
	 */
	static void analyse (final String sourcePath) throws Exception {
		final Collection<LineString> lines = Espebu.getLineStrings(sourcePath);
		new Analyser(lines).analyse();
	}
	
	
	/**
	 * Run parallelism analysis of the lines read from the Shapefile using the
	 * splitting algorithm.
	 */
	static void analyseTryingHarder (final String sourcePath, final String destPath) throws Exception {
		
		final Collection<LineString> originalLines = Espebu.getLineStrings(sourcePath);
		
		final Splitter splitter = new Splitter(originalLines);
		splitter.split();
		
		final Analyser analyser = new Analyser(splitter.lines);
		analyser.originalLines = originalLines;
		analyser.analyse();
		
		new ShapeWriter(destPath).writeGeometries(
				analyser.resultAsLineList(),
				new ShapeWriterDelegate() {
			// this delegate enables us to include attributes with the geometry written to the output Shapefile
			public SimpleFeatureType featureType () throws SchemaException {
				return DataUtilities.createType( "Location",
						"location:LineString:srid=" + Espebu.INTERNAL_EPSG_CODE()
						+ ",name:String"  // attribute 1
						+ ",parallel:String"  // attribute 2
						);
			}
			public List attributes (Geometry geometry) {
				List<Object> attributes = new LinkedList<Object>();
				attributes.add( GeometryMeta.description(GeometryMeta.origin(geometry)) );
				attributes.add( GeometryMeta.description(geometry) );
				return attributes;
			}
		});
	}
	
	
	static void merge (final String sourcePath, final String destPath) throws Exception {
		final Collection<LineString> lines = Espebu.getLineStrings(sourcePath);
		new Merger(lines).mergeAndWriteTo(destPath);
	}
	
	
	static void passThrough (final String sourcePath, final String destPath) throws Exception {
		new ShapeWriter(destPath).writeLines( Espebu.getLineStrings(sourcePath) );
	}
	
	
	public static void main (String[] args) throws Throwable {
//		final String in = args.length > 0 ? args[0] : "";
//		final String out = args.length > 1 ? args[1] : "";

//		final String in = "shape/koeln-motorways.shp";
		final String in = "shape/simple.shp";
		final String out = "shape/out.shp";
		
//		Espebu.analyse(in);
		Espebu.analyseTryingHarder(in, out);
//		Espebu.merge(in, out);
//		Espebu.passThrough(in, out);
		
//		new SpatialiteReader();
	}
	
}
