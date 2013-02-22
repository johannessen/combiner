/* encoding UTF-8
 * 
 * Copyright (c) 2012-13 Arne Johannessen
 * 
 * This project and all of its individual parts may be used in accordance
 * with the terms of the 3-clause BSD licence. See LICENSE for details.
 */

package de.thaw.thesis.testbed;

import java.io.File;
import java.net.URI;
import java.util.*;
import org.geotools.data.DataStore;
import org.geotools.data.DataStoreFinder;
import org.opengis.feature.simple.SimpleFeature;
import org.opengis.feature.simple.SimpleFeatureType;
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
public final class ShapeReader {
	
	
	/**
	 * The EPSG code of the CRS into which features should be transformed.
	 */
	final public int targetEpsgCode = Testbed.INTERNAL_EPSG_CODE();
	
	
	/**
	 * The name of the shapefile attribute to be used for feature
	 * identification.
	 */
	final static String OSM_ID_ATTRIBUTE = "osm_id";
	
	
	/**
	 */
//	SimpleFeatureType featureType = null;
	
	
	public static int VERBOSITY = Testbed.VERBOSITY();
	
	
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
	public Collection<LineString> readFrom (final File file) throws Exception {
		
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
		log(2, "Identified Source CRS:\n" + sourceCRS);
		if (sourceCRS == null) {
			log(0, "No CRS definition found in source; defaulting to unprojected WGS84.");
			sourceCRS = DefaultGeographicCRS.WGS84;
		}
		final CoordinateReferenceSystem projectionCRS = CRS.decode("EPSG:" + this.targetEpsgCode);
		final MathTransform transform = CRS.findMathTransform(sourceCRS, projectionCRS);
		
		final Collection<LineString> result = new LinkedList<LineString>();
		
		SimpleFeature logFeature = null;
		int i = 0;
		try {
			while (iterator.hasNext()) {
				i++;
				final SimpleFeature feature = iterator.next();
				log(2, feature.getID());
				final GeometryCollection sourceGeometry = (GeometryCollection)feature.getDefaultGeometry();
				for (int j = 0; j < sourceGeometry.getNumGeometries(); j++) {
					final Geometry singleGeometry = sourceGeometry.getGeometryN(j);
					
					// store original feature for later use in shapefile output
					singleGeometry.setUserData(new LineMeta(feature));
					
					if (singleGeometry instanceof LineString) {
						final Geometry projectedGeometry = JTS.transform(singleGeometry, transform);
						result.add((LineString)projectedGeometry);
					}
				}
//				this.featureType = feature.getType();  // :TODO: better get this from ShapefileDataStore
				
				logFeature = feature;
			}
			
/*
			// :DEBUG: log feature type input
			log(2, "FeatureType properties for '" + logFeature.getType().getGeometryDescriptor().getLocalName() + "':");
			for (org.opengis.feature.Property p: logFeature.getProperties()) {
				log(2, p.getName() + " = " + p.getValue());
			}
*/
		}
		finally {
			log(1, "Features read from Shapefile: " + i);
			iterator.close();
		}
		
		return result;
	}
	
	
/*
	public static void main (final String[] args) throws Exception {
		final File file = new File(args[0]);
		new ShapeReader().readFrom(file);
	}
*/
}
