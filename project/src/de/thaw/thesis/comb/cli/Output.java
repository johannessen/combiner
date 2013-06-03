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
import de.thaw.thesis.comb.Line;
import de.thaw.thesis.comb.LinePart;
import de.thaw.thesis.comb.LineSegment;
import de.thaw.thesis.comb.OsmDataset;
import de.thaw.thesis.comb.OsmNode;
import de.thaw.thesis.comb.OsmTags;
import de.thaw.thesis.comb.io.WriterHelper;
import de.thaw.thesis.comb.io.GeoJsonWriter;
import de.thaw.thesis.comb.io.ShapeWriter;
import de.thaw.thesis.comb.io.ShapeWriterDelegate;
import de.thaw.thesis.comb.util.SpatialFeature;

import com.vividsolutions.jts.geom.Geometry;
import com.vividsolutions.jts.simplify.DouglasPeuckerSimplifier;
import org.opengis.feature.simple.SimpleFeatureType;
import org.geotools.feature.SchemaException;
import org.geotools.data.DataUtilities;

import java.io.File;
import java.util.Arrays;
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
	
	int verbose = 0;
	
	
	
	Output (final OsmDataset dataset) {
		this.dataset = dataset;
	}
	
	
	
	private ShapeWriter writer (final String path) {
		final File file = new File(path);
		if (! file.canWrite()) {
			if (file.exists()) {
				return null;
			}
			try {
				file.createNewFile();
			}
			catch (Exception e) {
				// if this happens, the file couldn't be created
				// (This code is no good, but what the heck.)
				return null;
			}
		}
		return new ShapeWriter(file);
	}
	
	
	
	private GeoJsonWriter writerJson (final String path) {
		final File file = new File(path);
		if (! file.canWrite()) {
			if (file.exists()) {
				return null;
			}
			try {
				file.createNewFile();
			}
			catch (Exception e) {
				// if this happens, the file couldn't be created
				// (This code is no good, but what the heck.)
				return null;
			}
		}
		return new GeoJsonWriter(file);
	}
	
	
	
	private void verbose (final int verbosity, final Object message) {
		if (verbose >= verbosity) {
			System.out.println( String.valueOf(message) );
		}
	}
	
	
	void writeAllNodes (final String path) {
		final ShapeWriter writer = writer(path);
		if (writer == null) {
			verbose(1, "Skipped writeAllNodes (Writer creation failed for path: " + path + ").");
			return;
		}
		
		final LinkedList<Geometry> geometries = new LinkedList<Geometry>();
		for (final OsmNode node : dataset.allNodes()) {
			geometries.add( writer.toPoint(node) );
		}
		
		writer.writeGeometries(geometries, new ShapeWriterDelegate() {
			
			// positional arguments MUST be in same order in both methods
			public SimpleFeatureType featureType () throws SchemaException {
				// :BUG: may not work with 64 bit IDs
				return DataUtilities.createType( "AllNodes",
						"geometry:Point:srid=" + writer.epsgCode()
						+ ",id_osm:Integer"
						+ ",gen_sects:Integer"
						);
			}
			
			public List attributes (final Geometry geometry) {
				final OsmNode node = writer.toOsmNode(geometry);
				final List<Object> attributes = new LinkedList<Object>();
				attributes.add( node.id );
				attributes.add( node.generalisedSections.size() );
				return attributes;
			}
		});
		verbose(1, "Output: " + geometries.size() + " nodes.");
	}
	
	
	
	void writeAllSegments (final String path) {
		final ShapeWriter writer = writer(path);
		if (writer == null) {
			verbose(1, "Skipped writeAllSegments (Writer creation failed for path: " + path + ").");
			return;
		}
		
		final LinkedList<Geometry> geometries = new LinkedList<Geometry>();
		for (final LineSegment segment : dataset.allSegments()) {
			geometries.add( writer.toLineString(segment) );
		}
		
		writer.writeGeometries(geometries, new ShapeWriterDelegate() {
			
			// positional arguments MUST be in same order in both methods
			public SimpleFeatureType featureType () throws SchemaException {
				// :BUG: may not work with 64 bit IDs
				return DataUtilities.createType( "AllSegments",
						"geometry:LineString:srid=" + writer.epsgCode()
						+ ",id_way:Integer"
						+ ",p_left:String"
						+ ",p_right:String"
						+ ",reciprocal:Integer"
						+ ",gen:Integer"
						);
			}
			
			@SuppressWarnings("unchecked")
			private List idsFromParallels (final Collection<LineSegment> segments) {
				final List ids = new LinkedList();
				for (final LineSegment segment : segments) {
					ids.add(segment.way.id());
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
				return true;  // returns true for "no parallels"
			}
			
			public List attributes (final Geometry geometry) {
				final LineSegment segment = writer.toLineSegment(geometry);
				final List<Object> attributes = new LinkedList<Object>();
				attributes.add( segment.way.id() );
				attributes.add( idsFromParallels(segment.leftRealParallels) );
				attributes.add( idsFromParallels(segment.rightRealParallels) );
				attributes.add( reciprocal(segment) ? 1 : 0 );
				attributes.add( segment.wasGeneralised /*? 1 : 0*/ );
				return attributes;
			}
		});
		verbose(1, "Output: " + geometries.size() + " segments.");
	}
	
	
	
	void writeAllFragments (final String path) {
		final ShapeWriter writer = writer(path);
		if (writer == null) {
			verbose(1, "Skipped writeAllFragments (Writer creation failed for path: " + path + ").");
			return;
		}
		
		final LinkedList<Geometry> geometries = new LinkedList<Geometry>();
		for (final LineSegment segment : dataset.allSegments()) {
			for (final LinePart part : segment.lineParts()) {
				geometries.add( writer.toLineString(part) );
			}
		}
		
		writer.writeGeometries(geometries, new ShapeWriter.DefaultLineDelegate());
		verbose(1, "Output: " + geometries.size() + " line parts.");
	}
	
	
	
/*
	void writeSegmentOrientationAids (final String path) {
		final ShapeWriter writer = writer(path);
		if (writer == null) {
			verbose(1, "Skipped writeSegmentOrientationAids (Writer creation failed for path: " + path + ").");
			return;
		}
		
		final LinkedList<Geometry> geometries = new LinkedList<Geometry>();
		for (final LineSegment segment : dataset.allSegments()) {
			OsmNode node0 = segment.midPoint();
			OsmNode node1 = segment.end();
			geometries.add( writer.toLineString(node0, node1) );
		}
		
		writer.writeGeometries(geometries, writer.new DefaultLineDelegate());
		verbose(1, "Output: orientation aids for all " + geometries.size() + " segments.");
	}
*/
	
	
	
	void writeFragmentMidPointConnectors (final String path) {
		final ShapeWriter writer = writer(path);
		if (writer == null) {
			verbose(1, "Skipped writeFragmentMidPointConnectors (Writer creation failed for path: " + path + ").");
			return;
		}
		
		final Set<MidPointConnector> connectors = new LinkedHashSet<MidPointConnector>();
		
		for (final LinePart[] parts : dataset.parallelFragments()) {
			connectors.add(new MidPointConnector(parts[0], parts[1]));
		}
		
		final LinkedList<Geometry> geometries = new LinkedList<Geometry>();
		
		for (final MidPointConnector connector : connectors) {
			assert ! connector.s1.midPoint().equals(connector.s2.midPoint()) : connector.s1 + " / " + connector.s2;
			OsmNode node0 = connector.s1.midPoint();
			OsmNode node1 = connector.s2.midPoint();
			geometries.add( writer.toLineString(node0, node1) );
		}
		
		writer.writeGeometries(geometries, new ShapeWriter.DefaultLineDelegate());
		verbose(1, "Output: " + geometries.size() + " fragment midpoint connectors.");
	}
	
	
	
	void writeMidPointConnectors (final String path) {
		final ShapeWriter writer = writer(path);
		if (writer == null) {
			verbose(1, "Skipped writeMidPointConnectors (Writer creation failed for path: " + path + ").");
			return;
		}
		
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
//			assert ! connector.s1.midPoint().equals(connector.s2.midPoint()) : connector.s1 + " / " + connector.s2;
			OsmNode node0 = connector.s1.midPoint();
			OsmNode node1 = connector.s2.midPoint();
			geometries.add( writer.toLineString(node0, node1) );
		}
		
		writer.writeGeometries(geometries, new ShapeWriter.DefaultLineDelegate());
		verbose(1, "Output: " + geometries.size() + " midpoint connectors.");
	}
	
	
	
	private class MidPointConnector {
		final LinePart s1;
		final LinePart s2;
		MidPointConnector (final LinePart s1, final LinePart s2) {
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
		if (writer == null) {
			verbose(1, "Skipped writeCorrelationEdges (Writer creation failed for path: " + path + ").");
			return;
		}
		
		final LinkedList<Geometry> geometries = new LinkedList<Geometry>();
		for (final CorrelationEdge cn : cns) {
			OsmNode node0 = cn.start;
			OsmNode node1 = cn.end;
			geometries.add( writer.userData(writer.toLineString(node0, node1), cn) );
		}
		
		writer.writeGeometries(geometries, new ShapeWriterDelegate() {
			
			// positional arguments MUST be in same order in both methods
			public SimpleFeatureType featureType () throws SchemaException {
				return DataUtilities.createType( "AllNodes",
						"geometry:LineString:srid=" + writer.epsgCode()
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
	
	
	
	void writeAllLines (final GeneralisedLines gen, final String path) {
		final WriterHelper allLinesHelper = new WriterHelper() {
			
			// positional arguments MUST be in same order in both methods
			public void defineSchema () {
				this.geometryType = "LineString";
				this.schema = Arrays.asList(
					new AttributeDefinition("gen", String.class),
					new AttributeDefinition("highway", String.class),
					new AttributeDefinition("ref", String.class) );
			}
			
			public List attributes (final SpatialFeature feature) {
				if (feature != null && ! (feature instanceof Line)) {
					throw new AssertionError(feature.toString());
				}
				Line section = (Line)feature;
				OsmTags tags = section != null ? section.tags() : null;
				return Arrays.asList(
					section != null ? section instanceof GeneralisedSection ? "1" : "0" : "-1",
					tags != null ? tags.get("highway") : "road",
					tags != null ? tags.get("ref") : "" );
			}
			
		};
		
		if (path.endsWith(".shp")) {
			final ShapeWriter writer = writer(path);
			if (writer == null) {
				verbose(1, "Skipped writeAllLines (Shapefile writer creation failed for path: " + path + ").");
				return;
			}
			
			final LinkedList<Geometry> geometries = new LinkedList<Geometry>();
			for (final Line section : gen.lines()) {
				assert section.size() > 0;
				Geometry line = writer.toLineString( section );
				geometries.add( line );
			}
			writer.writeGeometries(geometries, allLinesHelper);
			
		}
		else if (path.endsWith(".json")) {
			final GeoJsonWriter writer = writerJson(path);
			if (writer == null) {
				verbose(1, "Skipped writeAllLines (GeoJSON writer creation failed for path: " + path + ").");
				return;
			}
			
			writer.writeFeatures(gen.lines(), allLinesHelper);
			
		}
		else {
			throw new IllegalArgumentException(".json or .shp only please");
		}
		verbose(1, "Output: " + gen.lines().size() + " lines.");
	}
	
	
	
	private List<OsmNode> toNodeList (final CorrelationEdge edge, final OsmNode genNode) {
		LinkedList<OsmNode> list = new LinkedList<OsmNode>();
		if (edge == null) {
			return list;
		}
		list.add( edge.start );
		list.add( genNode );
		list.add( edge.end );
		return list;
	}
	
	
	
	void writeSimplifiedSections (final GeneralisedLines gen, final String path) {
		final ShapeWriter writer = writer(path);
		if (writer == null) {
			verbose(1, "Skipped writeSections (Writer creation failed for path: " + path + ").");
			return;
		}
		
		final double distanceTolerance = 16.0;
		
		final LinkedList<Geometry> geometries = new LinkedList<Geometry>();
		for (final Line section : gen.lines()) {
			assert section.size() > 0;
			Geometry line = writer.toLineString( section );
			Geometry simplifiedLine = DouglasPeuckerSimplifier.simplify(line, distanceTolerance);
//			simplifiedLine.setUserData(section);
			geometries.add( simplifiedLine );
		}
/*
		for (final Line section : gen.lines2()) {
			assert section.size() > 0;
			Geometry line = writer.toLineString( section );
			Geometry simplifiedLine = DouglasPeuckerSimplifier.simplify(line, distanceTolerance);
//			simplifiedLine.setUserData(section);
			geometries.add( simplifiedLine );
		}
*/
		
		writer.writeGeometries(geometries, new ShapeWriterDelegate() {
			
			// positional arguments MUST be in same order in both methods
			public SimpleFeatureType featureType () throws SchemaException {
				return DataUtilities.createType( "AllLines",
						"geometry:LineString:srid=" + writer.epsgCode()
						+ ",gen:String"
						+ ",highway:String"
						+ ",ref:String"
						);
			}
			
			public List attributes (final Geometry geometry) {
				final List<Object> attributes = new LinkedList<Object>();
				Object userData = geometry.getUserData();
				if (userData != null && ! (userData instanceof Line)) {
					throw new AssertionError(userData.toString());
				}
				Line section = (Line)userData;
				attributes.add( section != null ? section instanceof GeneralisedSection ? "1" : "0" : "-1" );
				attributes.add( ! section.type().isUnknown() ? section.type() : "road" );
				attributes.add( section.ref() );
				return attributes;
			}
		});
		verbose(1, "Output: " + geometries.size() + " sections.");
	}
	
}
