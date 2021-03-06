/* encoding UTF-8
 * 
 * Copyright (c) 2013 Arne Johannessen
 * 
 * This project and all of its individual parts may be used in accordance
 * with the terms of the 3-clause BSD licence. See LICENSE for details.
 */

package de.thaw.comb.io;

import de.thaw.comb.Line;
import de.thaw.comb.Node;
import de.thaw.comb.Segment;
import de.thaw.comb.SourceSegment;

import de.thaw.comb.util.PlaneCoordinates;

import com.vividsolutions.jts.geom.Coordinate;
import com.vividsolutions.jts.geom.GeometryFactory;

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
import org.opengis.feature.type.GeometryDescriptor;
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
public final class ShapeWriter {
	
	final GeometryFactory factory;
	
	/**
	 * GeoTools provides a GeometryDescriptor interface to retrieve the magic
	 * constant that defines the geometry column in spatial data stores. Since
	 * this constant evidently isn't stable across GeoTools versions, we need
	 * to retrieve it each time the ShapeWriter is used. This field stores a
	 * local copy for use by object methods.
	 */
	private GeometryDescriptor geometryDescriptor;
	
	/**
	 * The object defining the feature type and the attributes of the
	 * geometries to be written.
	 */
	private ShapeWriterDelegate delegate;
	
	/**
	 * The output Shapefile (will be overwritten or created).
	 */
	final File file;
	
	/**
	 * The EPSG code of the CRS that the features provided are referred to.
	 */
	final static int sourceEpsgCode = Projection.INTERNAL_EPSG_CODE;
	
	
	
	/**
	 * @param file the output Shapefile (will be overwritten or created)
	 */
	public ShapeWriter (final File file) {
		this.file = file;
		this.factory = new GeometryFactory();
	}
	
	
	
	/**
	 * Writes data to the Shapefile {@link #file}.
	 * 
	 * @param geometries the geometric features to write to the output file
	 * @param delegate an instance providing definitions of the type of
	 *  features and the attribute data to include with the geometry
	 */
	public void writeGeometries (final Collection<? extends Geometry> geometries, final ShapeWriterDelegate delegate) {
		this.delegate = delegate;
		try {
			final ShapefileDataStore dataStore = this.createDataStore(this.file);
			final List<SimpleFeature> features = this.simpleFeaturesFromGeometries(geometries);
			writeFeaturesToDataStore(features, dataStore);
		}
		catch (Exception e) {
			// we can't recover from problems with file I/O
			throw new RuntimeException(e);
		}
	}
	
	
	
	/**
	 * Gives the EPSG code of the projection to be used for the output files.
	 */
	public int epsgCode () {
		return sourceEpsgCode;
	}
	
	
	
	public Geometry userData (final Geometry geometry, final Object userData) {
		geometry.setUserData(userData);
		return geometry;
	}
	
	
	
	public Point toPoint (final PlaneCoordinates node) {
		final Point point = factory.createPoint(toCoordinate(node));
		point.setUserData(node);
		return point;
	}
	
	
	
	public Coordinate toCoordinate (final PlaneCoordinates node) {
		return new Coordinate(node.easting(), node.northing());
	}
	
	
	
	public Node toNode (final Geometry point) {
		Object userData = point.getUserData();
		if ( ! (userData instanceof Node) ) {
			throw new ClassCastException();
		}
		return (Node)userData;
	}
	
	
	
	public LineString toLineString (final Segment part) {
		final LineString lineString = toLineString(part.start(), part.end());
		lineString.setUserData(part);
		return lineString;
	}
	
	
	
	public LineString toLineString (final Node node1, final Node node2) {
		final Coordinate[] coordinates = new Coordinate[2];
		coordinates[0] = toCoordinate(node1);
		coordinates[1] = toCoordinate(node2);
		return factory.createLineString(coordinates);
	}
	
	
	
	public LineString toLineString (final Line line) {
		assert line.size() > 0;
		final Coordinate[] coordinates = new Coordinate[line.size() + 1];
		int i = 0;
		for (final Node node : line.coordinates()) {
			coordinates[i] = toCoordinate(node);
			i++;
		}
		final LineString lineString = factory.createLineString(coordinates);
		lineString.setUserData(line);
		return lineString;
	}
	
	
	
	public SourceSegment toSegment (final Geometry lineString) {
		Object userData = lineString.getUserData();
		if ( ! (userData instanceof SourceSegment) ) {
			throw new ClassCastException();
		}
		return (SourceSegment)userData;
	}
	
	
	
	/**
	 * Converts JTS geometries to Geotools simple features.
	 */
	List<SimpleFeature> simpleFeaturesFromGeometries (final Collection<? extends Geometry> geometries) throws SchemaException {
		final SimpleFeatureBuilder featureBuilder = new SimpleFeatureBuilder(this.delegate.featureType(geometryDescriptor));
		final List<SimpleFeature> features = new ArrayList<SimpleFeature>(geometries.size());
		for (final Geometry geometry: geometries) {
		
			featureBuilder.add(geometry);
			List attributes = this.delegate.attributes(geometry);
//			int i = 0;
			for (Object attribute : attributes) {
/*
				System.err.println(i + ": " + attribute);
				i++;
				try {
*/
					featureBuilder.add(attribute);
/*
				}
				catch (Exception e) {
					// huh?
				}
*/
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
		geometryDescriptor = dataStore.getSchema().getGeometryDescriptor();
		dataStore.createSchema(this.delegate.featureType(geometryDescriptor));
		
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
		final SimpleFeatureCollection collection = new ListFeatureCollection(this.delegate.featureType(geometryDescriptor), features);
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
	 * Writes linestrings to the Shapefile {@link #newFile}.
	 * 
	 * @see DefaultLineDelegate
	 */
/*
	public void writeLines (final Collection<LineString> lines) throws Exception {
		this.writeGeometries(lines, new DefaultLineDelegate());
	}
*/
	
	
	
	/**
	 * Writes points to the Shapefile {@link #newFile}.
	 * 
	 * @see DefaultPointDelegate
	 */
/*
	public void writePoints (final Collection<Point> points) throws Exception {
		this.writeGeometries(points, new DefaultPointDelegate());
	}
*/
	
	
	
	/**
	 * A <code>ShapeWriterDelegate</code> defining no attributes by default.
	 */
	public static abstract class DefaultDelegate implements ShapeWriterDelegate {
		
		/**
		 * Returns an empty attribute list.
		 * 
		 * @return {@link Collections#EMPTY_LIST}
		 */
		public List attributes (final Geometry geometry) {
			return Collections.EMPTY_LIST;
		}
		
	}
	
	
	
	/**
	 * A <code>ShapeWriterDelegate</code> defining an output of
	 * <code>LineString</code>s without attributes.
	 */
	public static class DefaultLineDelegate extends DefaultDelegate {
		public SimpleFeatureType featureType (final GeometryDescriptor geometryDescriptor) throws SchemaException {
			return DataUtilities.createType("Location", geometryDescriptor.getName() + ":LineString:srid=" + sourceEpsgCode);
		}
	}
	
	
	
	/**
	 * A <code>ShapeWriterDelegate</code> defining an output of
	 * <code>Point</code>s without attributes.
	 */
	public static class DefaultPointDelegate extends DefaultDelegate {
		public SimpleFeatureType featureType (final GeometryDescriptor geometryDescriptor) throws SchemaException {
			return DataUtilities.createType("Location", geometryDescriptor.getName() + ":Point:srid=" + sourceEpsgCode);
		}
	}
	
}
