/* encoding UTF-8
 * 
 * Copyright (c) 2013 Arne Johannessen
 * 
 * This project and all of its individual parts may be used in accordance
 * with the terms of the 3-clause BSD licence. See LICENSE for details.
 */

package de.thaw.comb.cli;

import de.thaw.comb.ConcatenatedSection;
import de.thaw.comb.Dataset;
import de.thaw.comb.GeneralisedNode;
import de.thaw.comb.GeneralisedSection;
import de.thaw.comb.Line;
import de.thaw.comb.Node;
import de.thaw.comb.NodeMatch;
import de.thaw.comb.ResultLine;
import de.thaw.comb.Segment;
import de.thaw.comb.SourceNode;
import de.thaw.comb.SourceSegment;
import de.thaw.comb.io.WriterHelper;
import de.thaw.comb.io.GeoJsonWriter;
import de.thaw.comb.io.ShapeWriter;
import de.thaw.comb.io.ShapeWriterDelegate;
import de.thaw.comb.util.AttributeProvider;
import de.thaw.comb.util.SpatialFeature;

import com.vividsolutions.jts.geom.Geometry;
import com.vividsolutions.jts.simplify.DouglasPeuckerSimplifier;
import org.opengis.feature.simple.SimpleFeatureType;
import org.opengis.feature.type.GeometryDescriptor;
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
	
	final Dataset dataset;
	
	int verbose = 0;
	
	
	
	Output (final Dataset dataset) {
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
	
	
	void writeAllNodes (final Collection<ResultLine> lines, final String path) {
		final ShapeWriter writer = writer(path);
		if (writer == null) {
			verbose(2, "Skipped writeAllNodes (Writer creation failed for path: " + path + ").");
			return;
		}
		
		final LinkedList<Geometry> geometries = new LinkedList<Geometry>();
		for (final Node node : dataset.allNodes()) {
			geometries.add( writer.toPoint(node) );
		}
		for (final ResultLine line : lines) {
			if (line instanceof ConcatenatedSection) {
				continue;
			}
			for (final Node node : line.coordinates()) {
				geometries.add( writer.toPoint(node) );
			}
		}
		
		writer.writeGeometries(geometries, new ShapeWriterDelegate() {
			
			// positional arguments MUST be in same order in both methods
			public SimpleFeatureType featureType (final GeometryDescriptor geometryDescriptor) throws SchemaException {
				// :BUG: may not work with 64 bit IDs
				return DataUtilities.createType( "AllNodes",
						geometryDescriptor.getName() + ":Point:srid=" + writer.epsgCode()
						+ ",id_osm:Integer"
						+ ",gen_sects:Integer"
						);
			}
			
			public List attributes (final Geometry geometry) {
				final Node node = writer.toNode(geometry);
				final List<Object> attributes = new LinkedList<Object>();
				attributes.add( node instanceof GeneralisedNode ? -2L : node.id() );
				if (node instanceof SourceNode) {
					attributes.add( ((SourceNode)node).generalisedSections().size() );
				}
				return attributes;
			}
		});
		verbose(1, "Output: " + geometries.size() + " nodes.");
	}
	
	
	
	void writeAllSegments (final String path) {
		final ShapeWriter writer = writer(path);
		if (writer == null) {
			verbose(2, "Skipped writeAllSegments (Writer creation failed for path: " + path + ").");
			return;
		}
		
		final LinkedList<Geometry> geometries = new LinkedList<Geometry>();
		for (final SourceSegment segment : dataset.allSegments()) {
			geometries.add( writer.toLineString(segment) );
		}
		
		writer.writeGeometries(geometries, new ShapeWriterDelegate() {
			
			// positional arguments MUST be in same order in both methods
			public SimpleFeatureType featureType (final GeometryDescriptor geometryDescriptor) throws SchemaException {
				// :BUG: may not work with 64 bit IDs
				return DataUtilities.createType( "AllSegments",
						geometryDescriptor.getName() + ":LineString:srid=" + writer.epsgCode()
						+ ",id_way:Integer"
						+ ",p_left:String"
						+ ",p_right:String"
						+ ",reciprocal:Integer"
						+ ",gen:Integer"
						);
			}
			
			@SuppressWarnings("unchecked")
			private List idsFromParallels (final Collection<SourceSegment> segments) {
				final List ids = new LinkedList();
				for (final SourceSegment segment : segments) {
					ids.add(segment.way.id());
				}
				return ids;
			}
			
			public boolean reciprocal (final SourceSegment segment) {  // :BUG: ignores L/R !
				for (SourceSegment parallel : segment.leftRealParallels) {
					if ( ! parallel.leftRealParallels.contains(segment)
							&& ! parallel.rightRealParallels.contains(segment)) {
						return false;
					}
				}
				for (SourceSegment parallel : segment.rightRealParallels) {
					if ( ! parallel.leftRealParallels.contains(segment)
							&& ! parallel.rightRealParallels.contains(segment)) {
						return false;
					}
				}
				return true;  // returns true for "no parallels"
			}
			
			public List attributes (final Geometry geometry) {
				final SourceSegment segment = writer.toSegment(geometry);
				final List<Object> attributes = new LinkedList<Object>();
				attributes.add( segment.way.id() );
				attributes.add( idsFromParallels(segment.leftRealParallels) );
				attributes.add( idsFromParallels(segment.rightRealParallels) );
				attributes.add( reciprocal(segment) ? 1 : 0 );
				attributes.add( segment.wasGeneralised() /*? 1 : 0*/ );
				return attributes;
			}
		});
		verbose(1, "Output: " + geometries.size() + " segments.");
	}
	
	
	
	void writeAllFragments (final String path) {
		final ShapeWriter writer = writer(path);
		if (writer == null) {
			verbose(2, "Skipped writeAllFragments (Writer creation failed for path: " + path + ").");
			return;
		}
		
		final LinkedList<Geometry> geometries = new LinkedList<Geometry>();
		for (final SourceSegment segment : dataset.allSegments()) {
			for (final Segment fragment : segment) {
				geometries.add( writer.toLineString(fragment) );
			}
		}
		
		writer.writeGeometries(geometries, new ShapeWriter.DefaultLineDelegate());
		verbose(1, "Output: " + geometries.size() + " line parts.");
	}
	
	
	
/*
	void writeSegmentOrientationAids (final String path) {
		final ShapeWriter writer = writer(path);
		if (writer == null) {
			verbose(2, "Skipped writeSegmentOrientationAids (Writer creation failed for path: " + path + ").");
			return;
		}
		
		final LinkedList<Geometry> geometries = new LinkedList<Geometry>();
		for (final SourceSegment segment : dataset.allSegments()) {
			Node node0 = segment.midPoint();
			Node node1 = segment.end();
			geometries.add( writer.toLineString(node0, node1) );
		}
		
		writer.writeGeometries(geometries, writer.new DefaultLineDelegate());
		verbose(1, "Output: orientation aids for all " + geometries.size() + " segments.");
	}
*/
	
	
	
	void writeFragmentMidPointConnectors (final String path) {
		final ShapeWriter writer = writer(path);
		if (writer == null) {
			verbose(2, "Skipped writeFragmentMidPointConnectors (Writer creation failed for path: " + path + ").");
			return;
		}
		
		final Set<MidPointConnector> connectors = new LinkedHashSet<MidPointConnector>();
		
		for (final Segment[] parts : dataset.parallelFragments()) {
			connectors.add(new MidPointConnector(parts[0], parts[1]));
		}
		
		final LinkedList<Geometry> geometries = new LinkedList<Geometry>();
		
		for (final MidPointConnector connector : connectors) {
			assert ! connector.s1.midPoint().equals(connector.s2.midPoint()) : connector.s1 + " / " + connector.s2;
			Node node0 = connector.s1.midPoint();
			Node node1 = connector.s2.midPoint();
			geometries.add( writer.toLineString(node0, node1) );
		}
		
		writer.writeGeometries(geometries, new ShapeWriter.DefaultLineDelegate());
		verbose(1, "Output: " + geometries.size() + " fragment midpoint connectors.");
	}
	
	
	
	void writeMidPointConnectors (final String path) {
		final ShapeWriter writer = writer(path);
		if (writer == null) {
			verbose(2, "Skipped writeMidPointConnectors (Writer creation failed for path: " + path + ").");
			return;
		}
		
		final Set<MidPointConnector> connectors = new LinkedHashSet<MidPointConnector>();
		
		for (final SourceSegment segment : dataset.allSegments()) {
			for (final SourceSegment parallel : segment.leftRealParallels) {
				assert segment != parallel : parallel;
				connectors.add(new MidPointConnector(segment, parallel));
			}
			for (final SourceSegment parallel : segment.rightRealParallels) {
				assert segment != parallel : parallel;
				connectors.add(new MidPointConnector(segment, parallel));
			}
		}
		
		final LinkedList<Geometry> geometries = new LinkedList<Geometry>();
		
		for (final MidPointConnector connector : connectors) {
//			assert ! connector.s1.midPoint().equals(connector.s2.midPoint()) : connector.s1 + " / " + connector.s2;
			Node node0 = connector.s1.midPoint();
			Node node1 = connector.s2.midPoint();
			geometries.add( writer.toLineString(node0, node1) );
		}
		
		writer.writeGeometries(geometries, new ShapeWriter.DefaultLineDelegate());
		verbose(1, "Output: " + geometries.size() + " midpoint connectors.");
	}
	
	
	
	private class MidPointConnector {
		final Segment s1;
		final Segment s2;
		MidPointConnector (final Segment s1, final Segment s2) {
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
	
	
	
	void writeNodeMatches (final Collection<NodeMatch> cns, final String path) {
		final ShapeWriter writer = writer(path);
		if (writer == null) {
			verbose(2, "Skipped writeNodeMatches (Writer creation failed for path: " + path + ").");
			return;
		}
		
		final LinkedList<Geometry> geometries = new LinkedList<Geometry>();
		for (final NodeMatch cn : cns) {
			Node node0 = cn.node0();
			Node node1 = cn.node1();
			geometries.add( writer.userData(writer.toLineString(node0, node1), cn) );
		}
		
		writer.writeGeometries(geometries, new ShapeWriterDelegate() {
			
			// positional arguments MUST be in same order in both methods
			public SimpleFeatureType featureType (final GeometryDescriptor geometryDescriptor) throws SchemaException {
				return DataUtilities.createType( "AllNodes",
						geometryDescriptor.getName() + ":LineString:srid=" + writer.epsgCode()
						+ ",desc:String"
						);
			}
			
			public List attributes (final Geometry geometry) {
				final List<Object> attributes = new LinkedList<Object>();
				attributes.add( geometry.getUserData().toString() );
				return attributes;
			}
		});
		verbose(1, "Output: " + geometries.size() + " node matches.");
	}
	
	
	
	void writeAllLines (final Collection<ResultLine> lines, final String path) {
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
				AttributeProvider tags = section != null ? section.tags() : null;
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
			for (final Line section : lines) {
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
			
			writer.writeFeatures(lines, allLinesHelper);
			
		}
		else {
			throw new IllegalArgumentException(".json or .shp only please");
		}
		verbose(1, "Output: " + lines.size() + " lines.");
	}
	
	
	
/*
	private List<Node> toNodeList (final NodeMatch match, final Node genNode) {
		LinkedList<Node> list = new LinkedList<Node>();
		if (match == null) {
			return list;
		}
		list.add( match.node0() );
		list.add( genNode );
		list.add( match.node1() );
		return list;
	}
	
	
	
	void writeSimplifiedSections (final Collection<ResultLine> lines, final String path) {
		final ShapeWriter writer = writer(path);
		if (writer == null) {
			verbose(2, "Skipped writeSections (Writer creation failed for path: " + path + ").");
			return;
		}
		
		final double distanceTolerance = 16.0;
		
		final LinkedList<Geometry> geometries = new LinkedList<Geometry>();
		for (final Line section : lines) {
			assert section.size() > 0;
			Geometry line = writer.toLineString( section );
			Geometry simplifiedLine = DouglasPeuckerSimplifier.simplify(line, distanceTolerance);
//			simplifiedLine.setUserData(section);
			geometries.add( simplifiedLine );
		}
		
		writer.writeGeometries(geometries, new ShapeWriterDelegate() {
			
			// positional arguments MUST be in same order in both methods
			public SimpleFeatureType featureType (final GeometryDescriptor geometryDescriptor) throws SchemaException {
				return DataUtilities.createType( "AllLines",
						geometryDescriptor.getName() + ":LineString:srid=" + writer.epsgCode()
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
*/
	
}
