/* encoding UTF-8
 * 
 * Copyright (c) 2013 Arne Johannessen
 * 
 * This project and all of its individual parts may be used in accordance
 * with the terms of the 3-clause BSD licence. See LICENSE for details.
 */

package de.thaw.thesis.comb.io;

import de.thaw.thesis.comb.OsmDataset;
import de.thaw.thesis.comb.OsmNode;
import de.thaw.thesis.comb.OsmWay;

import de.thaw.thesis.testbed.LineMeta;
//import de.thaw.thesis.testbed.ShapeReader;  // would redefine own name, have to use fully qualified name instead

import com.vividsolutions.jts.geom.Coordinate;
import com.vividsolutions.jts.geom.CoordinateSequence;
import com.vividsolutions.jts.geom.LineString;
import org.opengis.feature.simple.SimpleFeature;

import java.io.File;
import java.util.Collection;


/**
 * Adapter to the Testbed's ShapeReader class.
 * @see de.thaw.thesis.testbed.ShapeReader
 */
public final class ShapeReader {
	
	private final int epsgCode;
	
	private final Collection<LineString> lineStrings;
	
	private OsmDataset dataset;
	
	// these shapefiles don't have node IDs, hence we invent new ones
	private long nextNodeId = -100001L;
	
	public static int VERBOSE = 0;
	
	
	private OsmDataset dataset () {
		if (dataset == null) {
			dataset = new OsmDataset();
		}
		return dataset;
	}
	
	
	public ShapeReader (final File file) {
		de.thaw.thesis.testbed.ShapeReader.VERBOSITY = VERBOSE;
		final de.thaw.thesis.testbed.ShapeReader reader = new de.thaw.thesis.testbed.ShapeReader();
		try {
			lineStrings = reader.readFrom(file);
			epsgCode = reader.targetEpsgCode;
		}
		catch (Exception e) {
			// we can't recover from problems with file I/O
			throw new RuntimeException(e);
		}
	}
	
	
	public int epsgCode () {
		return epsgCode;
	}
	
	
	private OsmNode toNode (final Coordinate coordinate) {
		// add node to repository in dataset
		OsmNode node = dataset().getNodeAtEastingNorthing(coordinate.x, coordinate.y);
		node.id = nextNodeId--;
		return node;
	}
	
	
	public OsmDataset osmDataset () {
		final OsmDataset dataset = dataset();
		
		for (LineString lineString : lineStrings) {
			// :HACK: diversify "simple" dataset by introducing reversed segments
			Object id = LineMeta.getFrom(lineString).feature().getAttribute("id");
			if ("-5".equals(id) || "-1".equals(id) /*|| "-100".equals(id)*/ ) {
				Object userData = lineString.getUserData();
				lineString = (LineString)lineString.reverse();
				lineString.setUserData(userData);
			}
			
			final CoordinateSequence coordinates = lineString.getCoordinateSequence();
			if (coordinates.size() < 2) {
				continue;
			}
			
			final SimpleFeature feature = LineMeta.getFrom(lineString).feature();
			final ShapeTagsAdapter tags = new ShapeTagsAdapter(feature);
			final OsmWay way = dataset.createOsmWay(tags, coordinates.size() - 1);
			way.id = featureId(feature);
			
			for (int i = 0; i < coordinates.size(); i++) {
				final OsmNode node = toNode( coordinates.getCoordinate(i) );
				
				way.addLast(node);
			}
			
			way.mutable(false);
		}
		dataset.setCompleted();
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
			return OsmDataset.ID_UNKNOWN;
		}
		Object id;
		
		// try for OSM ID in Geofabrik data first (which uses the "osm_id" key)
		id = feature.getAttribute("osm_id");
		
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
	
}
