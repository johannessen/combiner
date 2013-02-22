/* encoding UTF-8
 * 
 * Copyright (c) 2013 Arne Johannessen
 * 
 * This project and all of its individual parts may be used in accordance
 * with the terms of the 3-clause BSD licence. See LICENSE for details.
 */

package de.thaw.thesis.comb.io;

import de.thaw.thesis.comb.LinePart;
import de.thaw.thesis.comb.LineSegment;
import de.thaw.thesis.comb.OsmDataset;
import de.thaw.thesis.comb.OsmNode;

import de.thaw.thesis.testbed.ShapeWriterDelegate;
import de.thaw.thesis.testbed.ShapeWriter.DefaultDelegate;
//import de.thaw.thesis.testbed.ShapeWriter;  // would redefine own name, have to use fully qualified name instead

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


/**
 * Adapter to the Testbed's ShapeWriter class.
 * @see de.thaw.thesis.testbed.ShapeWriter
 */
public final class ShapeWriter {
	
	final String path;
	
	final int epsgCode;
	
	final GeometryFactory factory;
	
	
	public ShapeWriter (final File file, final int epsgCode) {
		this.path = file.getPath();
		this.epsgCode = epsgCode;
		this.factory = new GeometryFactory();
	}
	
	
	public void writeGeometries (final Collection<? extends Geometry> geometries, final ShapeWriterDelegate delegate) {
		final de.thaw.thesis.testbed.ShapeWriter writer = new de.thaw.thesis.testbed.ShapeWriter(path);
		try {
			writer.writeGeometries(geometries, delegate);
		}
		catch (Exception e) {
			// we can't recover from problems with file I/O
			throw new RuntimeException(e);
		}
	}
	
	
	public Geometry userData (final Geometry geometry, final Object userData) {
		geometry.setUserData(userData);
		return geometry;
	}
	
	
	public Point toPoint (final OsmNode node) {
			final Point point = factory.createPoint(toCoordinate(node));
			point.setUserData(node);
			return point;
	}
	
	
	public Coordinate toCoordinate (final OsmNode node) {
		return new Coordinate(node.easting(), node.northing());
	}
	
	
	public OsmNode toOsmNode (final Geometry point) {
		Object userData = point.getUserData();
		if ( ! (userData instanceof OsmNode) ) {
			throw new ClassCastException();
		}
		return (OsmNode)userData;
	}
	
	
	public LineString toLineString (final LinePart part) {
		final LineString lineString = toLineString(part.start(), part.end());
		lineString.setUserData(part);
		return lineString;
	}
	
	
	public LineString toLineString (final OsmNode node1, final OsmNode node2) {
		final Coordinate[] coordinates = new Coordinate[2];
		coordinates[0] = toCoordinate(node1);
		coordinates[1] = toCoordinate(node2);
		return factory.createLineString(coordinates);
	}
	
	
	public LineString toLineString (final Collection<OsmNode> nodeList) {
		final Coordinate[] coordinates = new Coordinate[nodeList.size()];
		int i = 0;
		for (final OsmNode node : nodeList) {
			coordinates[i] = toCoordinate(node);
			i++;
		}
		return factory.createLineString(coordinates);
	}
	
	
	public LineSegment toLineSegment (final Geometry lineString) {
		Object userData = lineString.getUserData();
		if ( ! (userData instanceof LineSegment) ) {
			throw new ClassCastException();
		}
		return (LineSegment)userData;
	}
	
	
	/**
	 * A <code>ShapeWriterDelegate</code> defining an output of
	 * <code>LineString</code>s without attributes.
	 */
	public class DefaultLineDelegate extends DefaultDelegate {
		public SimpleFeatureType featureType () throws SchemaException {
			return DataUtilities.createType("Location", "location:LineString:srid=" + epsgCode);
		}
	}
	
	
	/**
	 * A <code>ShapeWriterDelegate</code> defining an output of
	 * <code>Point</code>s without attributes.
	 */
	public class DefaultPointDelegate extends DefaultDelegate {
		public SimpleFeatureType featureType () throws SchemaException {
			return DataUtilities.createType("Location", "location:Point:srid=" + epsgCode);
		}
	}
	
}
