/* encoding UTF-8
 * 
 * Copyright (c) 2013 Arne Johannessen
 * 
 * This project and all of its individual parts may be used in accordance
 * with the terms of the 3-clause BSD licence. See LICENSE for details.
 */

package de.thaw.thesis.comb.io;

import de.thaw.thesis.comb.Line;
import de.thaw.thesis.comb.util.PlaneCoordinate;
import de.thaw.thesis.comb.util.SpatialFeature;

import com.vividsolutions.jts.geom.Coordinate;
import com.vividsolutions.jts.geom.Geometry;
import com.vividsolutions.jts.geom.GeometryFactory;
import com.vividsolutions.jts.geom.LineString;
import com.vividsolutions.jts.geom.Point;
import org.opengis.feature.simple.SimpleFeatureType;
import org.geotools.feature.SchemaException;
import org.geotools.data.DataUtilities;

import java.io.File;
import java.util.Collection;

////////

import com.vividsolutions.jts.geom.LineString;
import com.vividsolutions.jts.geom.Point;
import com.vividsolutions.jts.geom.Geometry;

import org.opengis.feature.simple.SimpleFeature;
import com.vividsolutions.jts.io.WKTFileReader;
import com.vividsolutions.jts.io.WKTReader;

import org.geotools.feature.simple.SimpleFeatureBuilder;
import org.opengis.feature.simple.SimpleFeature;

import org.opengis.feature.simple.SimpleFeatureType;
import org.geotools.data.shapefile.ShapefileDataStoreFactory;
import org.geotools.data.shapefile.ShapefileDataStore;
import org.opengis.referencing.crs.CoordinateReferenceSystem;
import org.geotools.referencing.CRS;
import org.geotools.data.DataUtilities;

import org.geotools.data.Transaction;
import org.geotools.data.DefaultTransaction;
import org.geotools.data.simple.SimpleFeatureSource;
import org.geotools.data.simple.SimpleFeatureStore;
import org.geotools.data.simple.SimpleFeatureCollection;
import org.geotools.data.collection.ListFeatureCollection;

import org.geotools.data.DataStore;

import java.io.File;
import java.io.PrintWriter;
import java.util.Collections;
import java.util.Collection;
import java.util.Map;
import java.util.List;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;

import java.io.IOException;
import org.geotools.feature.SchemaException;


/**
 * KML format text output (Keyhole / Google Earth). NB: <code>ogr2ogr</code> can't
 * seem to read the attributes in KML files, even though it can <em>write</em>
 * those same attributes just fine. This issue limits the usefulness of this
 * class.
 */
public final class KmlWriter {
	
	// :BUG: XML writing through string prints
	// :BUG: no well-formedness checks, nor string escaping
	
	private static final String charset = "UTF-8";
	
	/**
	 * The output Shapefile (will be overwritten or created).
	 */
	final PrintWriter writer;
	
	/**
	 * The EPSG code of the CRS that the features provided are referred to.
	 */
	final static int sourceEpsgCode = Projection.INTERNAL_EPSG_CODE;
	
	/**
	 * The EPSG code of the CRS that the features provided are referred to.
	 */
	private final static int outputEpsgCode = sourceEpsgCode;
	
	
	
	/**
	 * @param file the output Shapefile (will be overwritten or created)
	 */
	public KmlWriter (final File file) {
		try {
			writer = new PrintWriter(file, charset);
		}
		catch (Exception e) {
			// we can't recover from problems with file I/O
			throw new RuntimeException(e);
		}
	}
	
	
	
	/**
	 * Writes data to the Shapefile.
	 * 
	 * @param features the geometric features to write to the output file
	 * @param attributeWriter an instance providing definitions of the type of
	 *  features and the attribute data to include with the geometry
	 */
	public void writeGeometries (final Iterable<? extends SpatialFeature> features, final WriterHelper attributeWriter) {
		writeKmlLeadIn(attributeWriter);
		for (final SpatialFeature feature : features) {
			writeKmlPlacemark(feature, attributeWriter);
		}
		writeKmlLeadOut();
		writer.close();
	}
	
	
	private void writeKmlLeadIn (final WriterHelper attributeWriter) {
		writer.println("<?xml version='1.0' encoding='" + charset + "' ?>");
		writer.println("<kml xmlns='http://www.opengis.net/kml/2.2'>");
		writer.println("<Document><Folder><name>out</name>");
		writer.println("<Schema name='out' id='out'>");
		writer.println("  <SimpleField name='Name' type='string'></SimpleField>");
		writer.println("  <SimpleField name='Description' type='string'></SimpleField>");
		for (final WriterHelper.AttributeDefinition attribute : attributeWriter.schema) {
			writer.print("  <SimpleField name='");
			writer.print(attribute.name);
			writer.print("' type='");
			writer.print(attribute.type.getSimpleName());
			writer.println("'></SimpleField>");
		}
		writer.println("</Schema>");
	}
	
	
	private void writeKmlPlacemark (final SpatialFeature feature, final WriterHelper attributeWriter) {
		writer.println("<Placemark>");
		
		writer.println("  <ExtendedData><SchemaData schemaUrl='#out'>");
		final List attributes = attributeWriter.attributes(feature);
		for (int i = 0; i < attributes.size(); i++) {
			writer.print("    <SimpleData name='");
			writer.print(attributeWriter.schema.get(i).name);
			writer.print("'>");
			writer.print(attributes.get(i));
			writer.println("</SimpleData>");
		}
		assert attributes.size() == attributeWriter.schema.size();
		writer.println("  </SchemaData></ExtendedData>");
		
		writer.print("  <");
		writer.print(attributeWriter.geometryType);
		writer.print("><coordinates>");
		for (final PlaneCoordinate coordinate : feature.coordinates()) {
			writer.print(coordinate.easting());
			writer.print(',');
			writer.print(coordinate.northing());
			writer.print(' ');
		}
		writer.print("</coordinates></");
		writer.print(attributeWriter.geometryType);
		writer.println(">");
		
		writer.println("</Placemark>");
	}
	
	
	private void writeKmlLeadOut () {
		writer.println("</Folder></Document></kml>");
	}
	
	
	
	/**
	 * Gives the EPSG code of the projection to be used for the output files.
	 */
	public int epsgCode () {
		return outputEpsgCode;
	}
	
}
