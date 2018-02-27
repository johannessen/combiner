/* encoding UTF-8
 * 
 * Copyright (c) 2013 Arne Johannessen
 * 
 * This project and all of its individual parts may be used in accordance
 * with the terms of the 3-clause BSD licence. See LICENSE for details.
 */

package de.thaw.comb.io;

import de.thaw.comb.util.PlaneCoordinates;
import de.thaw.comb.util.SpatialFeature;

import java.io.File;
import java.io.PrintWriter;
import java.util.List;



/**
 * GeoJSON format text output.
 */
public final class GeoJsonWriter {
	
	// :BUG: no well-formedness checks, nor string escaping
	
	
	private final PrintWriter writer;
	
	
	
	/**
	 * @param file the output file (will be overwritten or created)
	 */
	public GeoJsonWriter (final File file) {
		try {
			writer = new PrintWriter(file);
		}
		catch (Exception e) {
			// we can't recover from problems with file I/O
			throw new RuntimeException(e);
		}
	}
	
	
	
	/**
	 * Writes data to the GeoJSON file.
	 * 
	 * @param features the geometric features to write to the output file
	 * @param helper an instance providing definitions of the type of
	 *  features and the attribute data to include with the geometry
	 */
	public void writeFeatures (final Iterable<? extends SpatialFeature> features, final WriterHelper helper) {
		try {
			writeLeadIn(helper);
			for (final SpatialFeature feature : features) {
				writeFeature(feature, helper);
			}
			writeLeadOut();
		}
		finally {
			writer.close();
		}
	}
	
	
	
	private void writeLeadIn (final WriterHelper helper) {
		writer.println("{");
		writer.println("\"type\": \"FeatureCollection\",");
		writer.print("\"crs\": { \"type\": \"name\", \"properties\": { \"name\": \"urn:ogc:def:crs:EPSG::");
		writer.print(helper.epsgCode);
		writer.println("\" } },");
		writer.println("\"features\": [");
	}
	
	
	
	private void writeFeature (final SpatialFeature feature, final WriterHelper helper) {
		writer.print("{ \"type\": \"Feature\", \"properties\": { ");
		final List attributes = helper.attributes(feature);
		for (int i = 0; i < attributes.size(); i++) {
			if (i > 0) {
				writer.println(", ");
			}
			writer.print("\"");
			writer.print(helper.schema.get(i).name);
			writer.print("\": \"");
			writer.print(attributes.get(i));
			writer.print("\"");
		}
		assert attributes.size() == helper.schema.size();
		writer.print(" }");
		
		writer.print(", \"geometry\": { \"type\": \"");
		writer.print(helper.geometryType);
		writer.print("\", \"coordinates\": [ ");
		for (final PlaneCoordinates coordinate : feature.coordinates()) {
			writer.print("[ ");
			writer.print(coordinate.easting());
			writer.print(", ");
			writer.print(coordinate.northing());
			writer.print(" ], ");
		}
		writer.println("] } },");
	}
	
	
	
	private void writeLeadOut () {
		writer.println("]");
		writer.println("}");
	}
	
}
