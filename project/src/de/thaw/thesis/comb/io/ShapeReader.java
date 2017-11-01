/* encoding UTF-8
 * 
 * Copyright (c) 2013 Arne Johannessen
 * 
 * This project and all of its individual parts may be used in accordance
 * with the terms of the 3-clause BSD licence. See LICENSE for details.
 */

package de.thaw.thesis.comb.io;

import de.thaw.thesis.comb.Dataset;
import de.thaw.thesis.comb.SourceNode;
import de.thaw.thesis.comb.highway.Highway;

import java.io.File;
import java.net.URI;
import java.util.Collection;
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

import com.vividsolutions.jts.geom.Coordinate;
import com.vividsolutions.jts.geom.CoordinateSequence;


/**
 * Reads linestrings from ESRI Shapefiles.
 * <p>
 * This class respects the Shapefile's Coordinate Reference System if one is
 * defined; otherwise, it's assumed that the data is unprojected and referenced
 * to WGS84 datum. All features are transformed to a target CRS specified by
 * the field {@link #targetEpsgCode}.
 */
public final class ShapeReader {
	
	private final Collection<LineString> lineStrings;
	
	private InputDataset dataset;
	
	// PolyLine shapefiles don't have node IDs, hence we invent new ones
	private long nextNodeId = -100001L;
	
	
	
	/**
	 * The EPSG code of the CRS into which features should be transformed.
	 */
	final public int targetEpsgCode = Projection.INTERNAL_EPSG_CODE;
	
	
	/**
	 * The name of the shapefile attribute to be used for feature
	 * identification.
	 */
	final static String OSM_ID_ATTRIBUTE = "osm_id";
	
	
	public ShapeReader (final File file) {
		try {
			lineStrings = readFrom(file);
		}
		catch (Exception e) {
			// we can't recover from problems with file I/O
			throw new RuntimeException(e);
		}
	}
	
	
	public int epsgCode () {
		return Projection.INTERNAL_EPSG_CODE;
	}
	
	
	private SourceNode toNode (final Coordinate coordinate) {
		// add node to repository in dataset
		final SourceNode node = new SourceNode(coordinate.x, coordinate.y, nextNodeId);
		final SourceNode nodeIntern = (SourceNode)dataset().getNode(node);
		if (node != nodeIntern) {
			nextNodeId--;
		}
		return nodeIntern;
	}
	
	
	public Dataset dataset () {
		if (dataset == null) {
			dataset = new InputDataset();  // lazy initialisation
			
			// :TODO: move this builder over to InputDataset?
			
			for (LineString lineString : lineStrings) {
				
				final Object featureObject = lineString.getUserData();
				if (! (featureObject instanceof SimpleFeature)) {
					throw new ClassCastException();
				}
				final SimpleFeature feature = (SimpleFeature)featureObject;
				
				// :HACK: diversify "simple" dataset by introducing reversed segments
				Object id = feature.getAttribute("id");
				if ("-5".equals(id) || "-1".equals(id) /*|| "-100".equals(id)*/ ) {
					Object userData = lineString.getUserData();
					lineString = (LineString)lineString.reverse();
					lineString.setUserData(userData);
				}
				
				final CoordinateSequence coordinates = lineString.getCoordinateSequence();
				if (coordinates.size() < 2) {
					continue;
				}
				
				final ShapeTagsAdapter tags = new ShapeTagsAdapter(feature);
				final ArrayList<SourceNode> nodes = new ArrayList<SourceNode>( coordinates.size() );
				for (int i = 0; i < coordinates.size(); i++) {
					nodes.add(toNode( coordinates.getCoordinate(i) ));
				}
				final Highway way = dataset.createOsmWay(tags, nodes);
				way.id = featureId(feature);
				way.mutable(false);
			}
		}
		
		return dataset;
	}
	
	
	/**
	 * Creates a number identifying a feature. For OSM data, this would
	 * be the OSM ID. However, the OSM ID (even when only considered for one
	 * feature type, e. g. ways) may not be unique in pre-processed OSM data.
	 * For example, OSM ways may have been split into two or more segments
	 * during a topology normalisation, resulting in several LineStrings with
	 * the same OSM way ID.
	 * 
	 * @return A number describing the feature such that it can be identified.
	 *  The description may not be unique.
	 */
	private long featureId (final SimpleFeature feature) {
		if (feature == null) {
			return Dataset.ID_UNKNOWN;
		}
		Object id;
		
		// try for OSM ID in Geofabrik data first (which uses the "osm_id" key)
		id = feature.getAttribute( OSM_ID_ATTRIBUTE );
		
		// the shape-test datasets use "id" for a simple number
		if (id == null) {
			id = feature.getAttribute("id");
		}
		
		// last resort: use the OpenGIS-provided feature ID string (which is
		// dependant upon the implementation of the Shapefile access)
		if (id == null) {
			id = feature.getID();
		}
		
		return Long.parseLong(id.toString());
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
		
		System.out.println("Reading content " + typeName);
		
		final SimpleFeatureSource featureSource = dataStore.getFeatureSource(typeName);
		final SimpleFeatureCollection collection = featureSource.getFeatures();
		final SimpleFeatureIterator iterator = collection.features();
		
		CoordinateReferenceSystem sourceCRS = featureSource.getSchema().getCoordinateReferenceSystem();
//		log(2, "Identified Source CRS:\n" + sourceCRS);
		if (sourceCRS == null) {
			System.out.println("No CRS definition found in source; defaulting to unprojected WGS84.");
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
//				log(2, feature.getID());
				final GeometryCollection sourceGeometry = (GeometryCollection)feature.getDefaultGeometry();
				for (int j = 0; j < sourceGeometry.getNumGeometries(); j++) {
					final Geometry singleGeometry = sourceGeometry.getGeometryN(j);
					
					// store original feature for later use in shapefile output
					singleGeometry.setUserData(feature);
					
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
			System.out.println("Features read from Shapefile: " + i);
			iterator.close();
			dataStore.dispose();
		}
		
		return result;
	}
	
}
