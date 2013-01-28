/* encoding UTF-8
 * 
 * Copyright (c) 2013 Arne Johannessen
 * 
 * This project and all of its individual parts may be used in accordance
 * with the terms of the 3-clause BSD licence. See LICENSE for details.
 */

package de.thaw.thesis.testbed;

import com.vividsolutions.jts.geom.Coordinate;
import com.vividsolutions.jts.geom.GeometryFactory;
import com.vividsolutions.jts.geom.LineString;
import com.vividsolutions.jts.geom.MultiLineString;
import com.vividsolutions.jts.geom.impl.CoordinateArraySequence;
import com.vividsolutions.jts.operation.union.UnaryUnionOp;

import java.util.List;
import java.util.ArrayList;
import java.util.LinkedList;


/**
 * 
 */
final class SimpleGeneraliser {
	
	
	/**
	 * The input lines for this generalisation.
	 */
	final ArrayList<LineString> lines;
	
	
	final GeometryFactory factory = new GeometryFactory();
	
	
	// :DEBUG: create lines connecting the fragments to each other
	boolean debugReferenceLineCreation = false;
	
	
	/**
	 * 
	 */
	boolean preventDuplicates = true;
	
	
	/**
	 * 
	 */
	SimpleGeneraliser (final List<LineString> lines) {
		this.lines = new ArrayList<LineString>(lines);
	}
	
	
	/**
	 * Converts a GeometryCollection to a Java Collection.
	 */
	static ArrayList<LineString> toList (MultiLineString geometryCollection) {
		// :TODO: convert to use a GeometryCollectionIterator
		final int length = geometryCollection.getNumGeometries();
		final ArrayList<LineString> list = new ArrayList<LineString>();
		for (int i = 0; i < length; i++) {
			list.add( (LineString) geometryCollection.getGeometryN(i) );
		}
		return list;
	}
	
	
	/**
	 * 
	 */
	LineString mostParallel (final LineString line) {
		// implementation copied from Testbed's ShapeWriterDelegate
		
		final LinePartMeta geometryMeta = LinePartMeta.getFrom(line);
		
		boolean parallelFound = geometryMeta.finderResults.parallelisms.size() > 0;
		if (! parallelFound) {
			return null;
		}
		
		final LineString parallel = (LineString)geometryMeta.finderResults.parallelisms.first().origin;
		final LinePartMeta parallelMeta = LinePartMeta.getFrom(parallel);
		
		boolean originalFound = parallelMeta.finderResults.parallelisms.size() > 0;
		if (! originalFound) {
			return null;
		}
		
		final LineString parallelParallel = (LineString)parallelMeta.finderResults.parallelisms.first().origin;
		
		/* does the "likely parallel" of this LineString have _this_
		 * LineString registered as its own "likely parallel"? If so,
		 * then these two are almost definitely parallel (provided the
		 * metric used by the Analyser works okay)
		 */
//		boolean reciprocal = LineMeta.getFrom(parallelParallel) == LineMeta.getFrom(line);
		boolean reciprocal = false;
		Parallelism[] p = parallelMeta.finderResults.parallelisms.toArray(new Parallelism[0]);
		for (int i = 0; i < p.length; i++) {
			reciprocal |= LinePartMeta.getFrom(p[i].origin) == LinePartMeta.getFrom(line);
		}
		
		if (! reciprocal) {
			return null;
		}
		
		return parallel;
	}
	
	
	/**
	 * 
	 */
	Coordinate[][] linePoints (final LineString line, final LineString parallel) {
		// based on ParallelismFinder.compareLocation
		
		// reverse one of the lines before comparison if they're pointed in opposite directions
		final LineString line2;
		if (ParallelismFinder.orientationDifference(line, parallel) <= 90.0) {
			line2 = parallel;
		}
		else {
			line2 = (LineString)parallel.reverse();
		}
		
		// obtain coordinates of all start- and end-points
		final Coordinate[][] linePoints = new Coordinate[2][2];
		linePoints[0][0] = line.getStartPoint().getCoordinate();
		linePoints[0][1] = line2.getStartPoint().getCoordinate();
		linePoints[1][0] = line.getEndPoint().getCoordinate();
		linePoints[1][1] = line2.getEndPoint().getCoordinate();
		
		return linePoints;
	}
	
	
	/**
	 * 
	 */
	Coordinate midPoint (final Coordinate[] points) {
		return new Coordinate( (points[0].x + points[1].x) / 2.0, (points[0].y + points[1].y) / 2.0 );
	}
	
	
	/**
	 * 
	 */
	LineString generalisedLine (final LineString line, final LineString parallel) {
		
		final Coordinate[][] linePoints = this.linePoints(line, parallel);
		final Coordinate[] newLinePoints = new Coordinate[2];
		
		newLinePoints[0] = this.midPoint(linePoints[0]);
		newLinePoints[1] = this.midPoint(linePoints[1]);
		
		if (this.debugReferenceLineCreation) {
			// connect line mid points instead of line ends' midpoints
			newLinePoints[0] = this.midPoint(new Coordinate[]{ linePoints[0][0], linePoints[1][0] });
			newLinePoints[1] = this.midPoint(new Coordinate[]{ linePoints[0][1], linePoints[1][1] });
		}
		
		LineString newLine = new LineString(new CoordinateArraySequence(newLinePoints), this.factory);
		
		return newLine;
	}
	
	
	/**
	 * 
	 */
	List<LineString> generalise () {
		
		final LinkedList<LineString> newLines = new LinkedList<LineString>();
		
		/* We need to iterate ourselves to prevent the Collection framework's
		 * fail-fast behaviour on concurrent modifications during iterations
		 * (if duplicates are removed). Because we've chosen the List
		 * implementation for this.lines to be an ArrayList, random access
		 * happens in constant time.
		 */
		final ArrayList<LineString> lines = this.lines;
		for (int i = 0; i < lines.size(); i++) {
			final LineString line = lines.get(i);
			
			if (line == null) {
				// this line was identified and removed as another line's parallel earlier
				continue;
			}
			
			final LineString parallel = this.mostParallel(line);
			if (parallel == null) {
				// no parallel could be identfied
				continue;  // skip this line completely
			}
			
			if (this.preventDuplicates) {
				lines.set(lines.indexOf(parallel), null);
			}
			newLines.add( this.generalisedLine(line, parallel) );
		}
		
		// normalise result
		if (this.preventDuplicates) {
			// :TODO: .normalize() on a newly constructed GeometryCollection may be enough
			// -- but would it really be faster?
			MultiLineString filtered = (MultiLineString) UnaryUnionOp.union(newLines);
			return SimpleGeneraliser.toList(filtered);
		}
		
		return newLines;
	}
	
}
