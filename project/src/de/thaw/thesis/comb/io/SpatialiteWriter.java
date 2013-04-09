/* encoding UTF-8
 * 
 * Copyright (c) 2012-13 Arne Johannessen
 * 
 * This project and all of its individual parts may be used in accordance
 * with the terms of the 3-clause BSD licence. See LICENSE for details.
 */

package de.thaw.thesis.comb.io;

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
import java.util.Collections;
import java.util.Collection;
import java.util.Map;
import java.util.List;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.io.Serializable;

import java.io.IOException;
import org.geotools.feature.SchemaException;

import org.geotools.jdbc.JDBCDataStore;
import org.geotools.jdbc.JDBCDataStoreFactory;
import org.geotools.data.spatialite.SpatiaLiteDataStoreFactory;
import de.thaw.thesis.comb.OsmNode;



/**
 * 
 */
public class SpatialiteWriter {
	
	
	String newPath;
	
	
	public SpatialiteWriter (final String newPath) {
		this.newPath = newPath;
	}
	
	
	DataStore createDataStore (final String filename) throws Exception {
		
		final Map<String, Serializable> params = new HashMap<String, Serializable>();
		params.put(JDBCDataStoreFactory.SCHEMA.toString(), "sqlite");
		params.put(JDBCDataStoreFactory.DATABASE.toString(), filename);
//		params.put(, file.toURI().toURL());
		
		final SpatiaLiteDataStoreFactory factory = new SpatiaLiteDataStoreFactory();
//System.out.println(factory.getJDBCUrl(params));
		Object o = factory.createDataSource(params);
		System.out.println(o);
		System.out.println(o.getClass());
		final DataStore dataStore = factory.createNewDataStore(params);
//		dataStore.createSchema(this.delegate.featureType());
		
//		final CoordinateReferenceSystem projectionCRS = CRS.decode("EPSG:" + this.sourceEpsgCode);
//		dataStore.forceSchemaCRS(projectionCRS);
		
		return dataStore;
	}
	
	
	public void writePoints (final Collection<OsmNode> points) throws Exception {
		SimpleFeatureType type = DataUtilities.createType("Location", "location:Point:srid=32632");
		final SimpleFeatureBuilder featureBuilder = new SimpleFeatureBuilder(type);
		final List<SimpleFeature> features = new ArrayList<SimpleFeature>(points.size());
		for (final OsmNode point: points) {
			
			featureBuilder.add(point);
			final SimpleFeature feature = featureBuilder.buildFeature(null);
			features.add(feature);
		}
		
		final DataStore dataStore = createDataStore(newPath);
//		writeFeaturesToDataStore(features, dataStore);
		final Transaction transaction = new DefaultTransaction("create");
		final String typeName = dataStore.getTypeNames()[0];
		final SimpleFeatureSource featureSource = dataStore.getFeatureSource(typeName);
		if ( ! (featureSource instanceof SimpleFeatureStore) ) {
			throw new IOException();
		}
		final SimpleFeatureStore featureStore = (SimpleFeatureStore) featureSource;
		final SimpleFeatureCollection collection = new ListFeatureCollection(type, features);
		featureStore.setTransaction(transaction);
		try {
			featureStore.addFeatures(collection);
			transaction.commit();
		}
		catch (Exception e) {
			e.printStackTrace();
			transaction.rollback();
		}
		finally {
			transaction.close();
		}
	}
	
}

