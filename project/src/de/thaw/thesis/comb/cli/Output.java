/* encoding UTF-8
 * 
 * Copyright (c) 2013 Arne Johannessen
 * 
 * This project and all of its individual parts may be used in accordance
 * with the terms of the 3-clause BSD licence. See LICENSE for details.
 */

package de.thaw.thesis.comb.cli;

import de.thaw.thesis.comb.CorrelationEdge;
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



/**
 * This Client's output helper class. Writes results to shapefiles.
 */
final class Output {
	
	final OsmDataset dataset;
	
	final int epsgCode;
	
	int verbose = 0;
	
	
	
	Output (final OsmDataset dataset, final int dataEpsgCode) {
		this.dataset = dataset;
		this.epsgCode = dataEpsgCode;
	}
	
	
	
	private ShapeWriter writer (final String path) {
		return new ShapeWriter( new File(path), epsgCode );
	}
	
	
	
	private void verbose (final int verbosity, final Object message) {
		if (verbose >= verbosity) {
			System.out.println( String.valueOf(message) );
		}
	}
	
	
	void writeAllNodes (final String path) {
		final ShapeWriter writer = writer(path);
		
		final LinkedList<Geometry> geometries = new LinkedList<Geometry>();
		for (final OsmNode node : dataset.allNodes()) {
			geometries.add( writer.toPoint(node) );
		}
		
		writer.writeGeometries(geometries, new ShapeWriterDelegate() {
			
			// positional arguments MUST be in same order in both methods
			public SimpleFeatureType featureType () throws SchemaException {
				// :BUG: may not work with 64 bit IDs
				return DataUtilities.createType( "AllNodes",
						"geometry:Point:srid=" + epsgCode
						+ ",id_osm:Integer"
						);
			}
			
			public List attributes (final Geometry geometry) {
				final OsmNode node = writer.toOsmNode(geometry);
				final List<Object> attributes = new LinkedList<Object>();
				attributes.add( node.id );
				return attributes;
			}
		});
		verbose(1, "Output: " + geometries.size() + " nodes.");
	}
	
	
	
	void writeAllSegments (final String path) {
		final ShapeWriter writer = writer(path);
		
		final LinkedList<Geometry> geometries = new LinkedList<Geometry>();
		for (final LineSegment segment : dataset.allSegments()) {
			geometries.add( writer.toLineString(segment) );
		}
		
		writer.writeGeometries(geometries, new ShapeWriterDelegate() {
			
			// positional arguments MUST be in same order in both methods
			public SimpleFeatureType featureType () throws SchemaException {
				// :BUG: may not work with 64 bit IDs
				return DataUtilities.createType( "AllSegments",
						"geometry:LineString:srid=" + epsgCode
						+ ",id_way:Integer"
						+ ",p_left:String"
						+ ",p_right:String"
						+ ",reciprocal:Integer"
						);
			}
			
			@SuppressWarnings("unchecked")
			private List idsFromParallels (final Collection<LineSegment> segments) {
				final List ids = new LinkedList();
				for (final LineSegment segment : segments) {
					ids.add(segment.way.id);
				}
				return ids;
			}
			
			public boolean reciprocal (final LineSegment segment) {  // :BUG: ignores L/R !
				for (LineSegment parallel : segment.leftRealParallels) {
					if ( ! parallel.leftRealParallels.contains(segment)
							&& ! parallel.rightRealParallels.contains(segment)) {
						return false;
					}
				}
				for (LineSegment parallel : segment.rightRealParallels) {
					if ( ! parallel.leftRealParallels.contains(segment)
							&& ! parallel.rightRealParallels.contains(segment)) {
						return false;
					}
				}
				return true;
			}
			
			public List attributes (final Geometry geometry) {
				final LineSegment segment = writer.toLineSegment(geometry);
				final List<Object> attributes = new LinkedList<Object>();
				attributes.add( segment.way.id );
				attributes.add( idsFromParallels(segment.leftRealParallels) );
				attributes.add( idsFromParallels(segment.rightRealParallels) );
				attributes.add( reciprocal(segment) ? 1 : 0 );
				return attributes;
			}
		});
		verbose(1, "Output: " + geometries.size() + " segments.");
	}
	
	
	
	void writeAllFragments (final String path) {
		final ShapeWriter writer = writer(path);
		
		final LinkedList<Geometry> geometries = new LinkedList<Geometry>();
		for (final LineSegment segment : dataset.allSegments()) {
			for (final LinePart part : segment.lineParts()) {
				geometries.add( writer.toLineString(part) );
			}
		}
		
		writer.writeGeometries(geometries, writer.new DefaultLineDelegate());
		verbose(1, "Output: " + geometries.size() + " line parts.");
	}
	
	
	
	void writeSegmentOrientationAids (final String path) {
		final ShapeWriter writer = writer(path);
		
		final LinkedList<Geometry> geometries = new LinkedList<Geometry>();
		for (final LineSegment segment : dataset.allSegments()) {
			OsmNode node0 = segment.midPoint();
			OsmNode node1 = segment.end();
			geometries.add( writer.toLineString(node0, node1) );
		}
		
		writer.writeGeometries(geometries, writer.new DefaultLineDelegate());
		verbose(1, "Output: orientation aids for all " + geometries.size() + " segments.");
	}
	
	
	
	void writeMidPointConnectors (final String path) {
		final ShapeWriter writer = writer(path);
		
		final Set<MidPointConnector> connectors = new LinkedHashSet<MidPointConnector>();
		
		for (final LineSegment segment : dataset.allSegments()) {
			for (final LineSegment parallel : segment.leftRealParallels) {
				assert segment != parallel : parallel;
				connectors.add(new MidPointConnector(segment, parallel));
			}
			for (final LineSegment parallel : segment.rightRealParallels) {
				assert segment != parallel : parallel;
				connectors.add(new MidPointConnector(segment, parallel));
			}
		}
		
		final LinkedList<Geometry> geometries = new LinkedList<Geometry>();
		
		for (final MidPointConnector connector : connectors) {
			assert ! connector.s1.midPoint().equals(connector.s2.midPoint()) : connector.s1 + " / " + connector.s2;
			OsmNode node0 = connector.s1.midPoint();
			OsmNode node1 = connector.s2.midPoint();
			geometries.add( writer.toLineString(node0, node1) );
		}
		
		writer.writeGeometries(geometries, writer.new DefaultLineDelegate());
		verbose(1, "Output: " + geometries.size() + " midpoint connectors.");
	}
	
	
	
	private class MidPointConnector {
		final LineSegment s1;
		final LineSegment s2;
		MidPointConnector (final LineSegment s1, final LineSegment s2) {
			if (s1.compareTo(s2) > 0) {
				this.s1 = s1;
				this.s2 = s2;
			}
			else {
				this.s1 = s2;
				this.s2 = s1;
			}
		}
		public boolean equals (Object that) {
			if (! (that instanceof MidPointConnector)) {
				return false;
			}
			return this.s1 == ((MidPointConnector)that).s1 && this.s2 == ((MidPointConnector)that).s2;
		}
		public int hashCode () {
			int hashCode = 17;
			hashCode = hashCode * 37 + s1.hashCode();
			hashCode = hashCode * 37 + s2.hashCode();
			return hashCode;
		}
	}
	
	
	
	void writeCorrelationEdges (final Collection<CorrelationEdge> cns, final String path) {
		final ShapeWriter writer = writer(path);
		
		final LinkedList<Geometry> geometries = new LinkedList<Geometry>();
		for (final CorrelationEdge cn : cns) {
			OsmNode node0 = cn.node0;
			OsmNode node1 = cn.node1;
			geometries.add( writer.userData(writer.toLineString(node0, node1), cn) );
		}
		
		writer.writeGeometries(geometries, new ShapeWriterDelegate() {
			
			// positional arguments MUST be in same order in both methods
			public SimpleFeatureType featureType () throws SchemaException {
				return DataUtilities.createType( "AllNodes",
						"geometry:LineString:srid=" + epsgCode
						+ ",desc:String"
						);
			}
			
			public List attributes (final Geometry geometry) {
				final List<Object> attributes = new LinkedList<Object>();
				attributes.add( geometry.getUserData().toString() );
				return attributes;
			}
		});
		verbose(1, "Output: " + geometries.size() + " correlation edges.");
	}
	
	
	
	void writeGeneralisedLines (final Collection<Collection<OsmNode>> gen, final String path) {
		final ShapeWriter writer = writer(path);
		
		final LinkedList<Geometry> geometries = new LinkedList<Geometry>();
		for (final Collection<OsmNode> nodeList : gen) {
			geometries.add( writer.toLineString(nodeList) );
		}
		
		writer.writeGeometries(geometries, writer.new DefaultLineDelegate());
		verbose(1, "Output: " + geometries.size() + " generalised linestring" + (geometries.size() == 1 ? "." : "s."));
	}
	
}