/* encoding UTF-8
 * 
 * Copyright (c) 2012 Arne Johannessen
 * 
 * This project and all of its individual parts may be used in accordance
 * with the terms of a BSD-style license. See LICENSE for details.
 */

package de.thaw.espebu;

import java.io.File;
import java.net.URI;
import java.util.*;
import org.geotools.data.DataStore;
import org.geotools.data.DataStoreFinder;
import org.opengis.feature.simple.SimpleFeature;
import org.geotools.data.simple.SimpleFeatureSource;
import org.geotools.data.simple.SimpleFeatureCollection;
import org.geotools.data.simple.SimpleFeatureIterator;
import org.geotools.data.simple.SimpleFeatureSource;
import com.vividsolutions.jts.geom.GeometryCollection;
import com.vividsolutions.jts.geom.Geometry;
import com.vividsolutions.jts.geom.LineString;

import org.opengis.referencing.crs.CoordinateReferenceSystem;
import org.opengis.referencing.operation.MathTransform;
import org.geotools.referencing.crs.DefaultGeographicCRS;
import org.geotools.referencing.CRS;
import org.geotools.geometry.jts.JTS;


/**
 * Reads linestrings from ESRI Shapefiles.
 * <p>
 * This class respects the Shapefile's Coordinate Reference System if one is
 * defined; otherwise, it's assumed that the data is unprojected and referenced
 * to WGS84 datum. All features are transformed to a target CRS specified by
 * the field {@link #targetEpsgCode}.
 */
final class ShapeReader {
	
	
	/**
	 * The EPSG code of the CRS into which features should be transformed.
	 */
	int targetEpsgCode = Espebu.INTERNAL_EPSG_CODE();
	
	
	static int VERBOSITY = Espebu.VERBOSITY();
	
	
	static void log (final int logLevel, final String logMessage) {
		if (logLevel <= VERBOSITY) {
			System.err.println(logMessage);
		}
	}
	
	
	/**
	 * Reads linestrings from ESRI Shapefiles. This method uses Geotools
	 * classes whose structure and naming suggests that other file formats
	 * might work as well, but none have been tested.
	 * 
	 * @param file the Shapefile
	 * @return all linestrings that could be read from <code>file</code>
	 */
	Collection<LineString> readFrom (final File file) throws Exception {
		
		final Map<String, Object> connect = new HashMap<String, Object>();
		connect.put("url", file.toURI().toURL());
		
		final DataStore dataStore = DataStoreFinder.getDataStore(connect);
		final String[] typeNames = dataStore.getTypeNames();
		final String typeName = typeNames[0];
		
		log(1, "Reading content " + typeName);
		
		final SimpleFeatureSource featureSource = dataStore.getFeatureSource(typeName);
		final SimpleFeatureCollection collection = featureSource.getFeatures();
		final SimpleFeatureIterator iterator = collection.features();
		
		CoordinateReferenceSystem sourceCRS = featureSource.getSchema().getCoordinateReferenceSystem();
		log(1, "Identified Source CRS:\n" + sourceCRS);
		if (sourceCRS == null) {
			log(0, "No CRS definition found in source; defaulting to WGS84.");
			sourceCRS = DefaultGeographicCRS.WGS84;
		}
		final CoordinateReferenceSystem projectionCRS = CRS.decode("EPSG:" + this.targetEpsgCode);
		final MathTransform transform = CRS.findMathTransform(sourceCRS, projectionCRS, true);
		
		final Collection<LineString> result = new LinkedList<LineString>();
		
		int i = 0;
		try {
			while (iterator.hasNext()) {
				i++;
				final SimpleFeature feature = iterator.next();
				log(2, feature.getID() + " " + feature.getAttribute("id").toString());
				final GeometryCollection sourceGeometry = (GeometryCollection)feature.getDefaultGeometry();  // possible bug
				for (int j = 0; j < sourceGeometry.getNumGeometries(); j++) {
					final Geometry singleGeometry = sourceGeometry.getGeometryN(j);
					singleGeometry.setUserData(feature.getID());
					if (singleGeometry instanceof LineString) {
						final Geometry projectedGeometry = JTS.transform(singleGeometry, transform);
						result.add((LineString)projectedGeometry);
					}
				}
			}
		}
		finally {
			log(2, "Fetaures: " + i);
			iterator.close();
		}
		log(1, "Done.");
		
		return result;
	}
	
	
/*
	public static void main (final String[] args) throws Exception {
		final File file = new File(args[0]);
		new ShapeReader().readFrom(file);
	}
*/
}