/* encoding UTF-8
 * 
 * Copyright (c) 2012 Arne Johannessen
 * 
 * This project and all of its individual parts may be used in accordance
 * with the terms of a BSD-style license. See LICENSE for details.
 */

package de.thaw.espebu;

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



/**
 * Writes geometry features to ESRI Shapefiles.
 * <p>
 * This class defines the Shapefile's Coordinate Reference System to be the one
 * specified by the field {@link #sourceEpsgCode}. Coordinate transformation
 * is not performed.
 */
final class ShapeWriter {
	
	
	/**
	 * The EPSG code of the CRS that the features provided are referred to.
	 */
	int sourceEpsgCode = Espebu.INTERNAL_EPSG_CODE();
	
	
	/**
	 * The output Shapefile (will be overwritten or created).
	 */
	File newFile;
	
	
	/**
	 * The object defining the feature type and the attributes of the
	 * geometries to be written.
	 */
	ShapeWriterDelegate delegate = null;
	
	
	/**
	 * @param newPath the path to the output Shapefile (will be overwritten or created)
	 */
	ShapeWriter (final String newPath) {
		this.newFile = new File(newPath);
	}
	
	
	/**
	 * Converts JTS geometries to Geotools simple features.
	 */
	List<SimpleFeature> simpleFeaturesFromGeometries (final Collection<? extends Geometry> geometries) throws SchemaException {
		final SimpleFeatureBuilder featureBuilder = new SimpleFeatureBuilder(this.delegate.featureType());
		final List<SimpleFeature> features = new ArrayList<SimpleFeature>(geometries.size());
		for (final Geometry geometry: geometries) {
		
			featureBuilder.add(geometry);
			List attributes = this.delegate.attributes(geometry);
			for (Object attribute : attributes) {
				featureBuilder.add(attribute);
			}
			
			final SimpleFeature feature = featureBuilder.buildFeature(null);
			features.add(feature);
		}
		return features;
	}
	
	
	/**
	 */
	ShapefileDataStore createDataStore (final File file) throws Exception {
		final Map<String, Serializable> params = new HashMap<String, Serializable>();
		params.put("url", file.toURI().toURL());
		params.put("create spatial index", Boolean.TRUE);
		
		final ShapefileDataStoreFactory factory = new ShapefileDataStoreFactory();
		final ShapefileDataStore dataStore = (ShapefileDataStore)factory.createNewDataStore(params);
		dataStore.createSchema(this.delegate.featureType());
		
		final CoordinateReferenceSystem projectionCRS = CRS.decode("EPSG:" + this.sourceEpsgCode);
		dataStore.forceSchemaCRS(projectionCRS);
		
		return dataStore;
	}
	
	
	/**
	 */
	void writeFeaturesToDataStore (final List<SimpleFeature> features, final DataStore dataStore) throws Exception {
		final Transaction transaction = new DefaultTransaction("create");
		final String typeName = dataStore.getTypeNames()[0];
		final SimpleFeatureSource featureSource = dataStore.getFeatureSource(typeName);
		if ( ! (featureSource instanceof SimpleFeatureStore) ) {
			throw new IOException();
		}
		final SimpleFeatureStore featureStore = (SimpleFeatureStore) featureSource;
		final SimpleFeatureCollection collection = new ListFeatureCollection(this.delegate.featureType(), features);
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
	
	
	/**
	 * Writes data to the Shapefile {@link #newFile}.
	 * 
	 * @param geometries the geometric features to write to the output file
	 * @param delegate an instance providing definitions of the type of
	 *  features and the attribute data to include with the geometry
	 */
	void writeGeometries (Collection<? extends Geometry> geometries, final ShapeWriterDelegate delegate) throws Exception {
		this.delegate = delegate;
		final List<SimpleFeature> features = this.simpleFeaturesFromGeometries(geometries);
		final ShapefileDataStore dataStore = this.createDataStore(this.newFile);
		this.writeFeaturesToDataStore(features, dataStore);
	}
	
	
	/**
	 * Writes linestrings to the Shapefile {@link #newFile}.
	 * 
	 * @see DefaultLineDelegate
	 */
	void writeLines (Collection<LineString> lines) throws Exception {
		this.writeGeometries(lines, new DefaultLineDelegate());
	}
	
	
	/**
	 * Writes points to the Shapefile {@link #newFile}.
	 * 
	 * @see DefaultPointDelegate
	 */
	void writePoints (Collection<Point> points) throws Exception {
		this.writeGeometries(points, new DefaultPointDelegate());
	}
	
	
	/**
	 * A <code>ShapeWriterDelegate</code> defining no attributes by default.
	 */
	abstract class DefaultDelegate implements ShapeWriterDelegate {
		
		/**
		 * Returns an empty attribute list.
		 * 
		 * @return {@link Collections#EMPTY_LIST}
		 */
		public List attributes (Geometry geometry) {
			return Collections.EMPTY_LIST;
		}
		
	}
	
	
	/**
	 * A <code>ShapeWriterDelegate</code> defining an output of
	 * <code>LineString</code>s without attributes.
	 */
	class DefaultLineDelegate extends DefaultDelegate {
		public SimpleFeatureType featureType () throws SchemaException {
			return DataUtilities.createType("Location", "location:LineString:srid=" + ShapeWriter.this.sourceEpsgCode);
		}
	}
	
	
	/**
	 * A <code>ShapeWriterDelegate</code> defining an output of
	 * <code>Point</code>s without attributes.
	 */
	class DefaultPointDelegate extends DefaultDelegate {
		public SimpleFeatureType featureType () throws SchemaException {
			return DataUtilities.createType("Location", "location:Point:srid=" + ShapeWriter.this.sourceEpsgCode);
		}
	}
	
	
	/**
	 * @param args {input file, output file}
	 */
	public static void main (String[] args) throws Throwable {
		Collection<LineString> lines = Espebu.getLineStrings(args[0]);
		Collection<Point> points = new ArrayList<Point>();
		for (final LineString line: lines) {
			points.add(line.getStartPoint());
		}
		new ShapeWriter(args[1]).writePoints(points);
	}
	
}

