/* encoding UTF-8
 * 
 * Copyright (c) 2013 Arne Johannessen
 * 
 * This project and all of its individual parts may be used in accordance
 * with the terms of the 3-clause BSD licence. See LICENSE for details.
 */

package de.thaw.thesis.comb.cli;

import de.thaw.thesis.comb.CorrelationEdge;
import de.thaw.thesis.comb.GeneralisedLines;
import de.thaw.thesis.comb.GeneralisedSection;
import de.thaw.thesis.comb.LinePart;
import de.thaw.thesis.comb.LineSegment;
import de.thaw.thesis.comb.OsmDataset;
import de.thaw.thesis.comb.OsmNode;
import de.thaw.thesis.comb.io.ShapeWriter;
import de.thaw.thesis.comb.io.ShapeWriterDelegate;

import com.vividsolutions.jts.geom.Geometry;
import org.opengis.feature.simple.SimpleFeatureType;
import org.geotools.feature.SchemaException;
import org.geotools.data.DataUtilities;

import java.io.File;
import java.util.Collection;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.LinkedList;
import java.util.Set;

import de.thaw.thesis.comb.io.SpatialiteWriter;



/**
 * 
 */
final class Output2 {
	
	final OsmDataset dataset;
	
	final int epsgCode;
	
	int verbose = 0;
	
	
	
	Output2 (final OsmDataset dataset, final int dataEpsgCode) {
		this.dataset = dataset;
		this.epsgCode = dataEpsgCode;
	}
	
	
	
	private void verbose (final int verbosity, final Object message) {
		if (verbose >= verbosity) {
			System.out.println( String.valueOf(message) );
		}
	}
	
	
	void writeAllNodes (final String path) {
		try {
			new SpatialiteWriter(path).writePoints(dataset.allNodes());
		}
		catch (Exception e) {
			throw new RuntimeException(e);
		}
		verbose(1, "Output: " + dataset.allNodes().size() + " nodes to spatialite.");
	}
	
}
