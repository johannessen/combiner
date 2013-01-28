/* encoding UTF-8
 * 
 * Copyright (c) 2012-13 Arne Johannessen
 * 
 * This project and all of its individual parts may be used in accordance
 * with the terms of the 3-clause BSD licence. See LICENSE for details.
 */

package de.thaw.thesis.testbed;

import com.vividsolutions.jts.geom.GeometryFactory;
import com.vividsolutions.jts.geom.Geometry;
import com.vividsolutions.jts.geom.Coordinate;
import com.vividsolutions.jts.geom.Point;
import com.vividsolutions.jts.geom.LineString;
import com.vividsolutions.jts.operation.distance.DistanceOp;

import java.util.Collection;
import java.util.Iterator;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.SortedSet;
import java.util.TreeSet;


/**
 * “Ably” fragments lines such that they are more suitable for a parallelism
 * analysis. This class implements the following naïve algorithm:
 * <ol>
 * <li>
 * for a terminating point P1 of line A: find the point P2 on line B
 * that is closest to P1
 * <li>
 * split line B at point P2
 * <li>
 * repeat steps 1–2 for both terminating points of line A as P1
 * <li>
 * repeat steps 1–3 for all existing lines as B
 * <li>
 * repeat steps 1–4 for all existing lines as A
 * </ol>
 */
final class Splitter {
	
	
	/**
	 * The lines that are being worked on.
	 */
	ArrayList<LineString> lines;
	
	
	// we need this factory to create the line fragments
	private final GeometryFactory factory = new GeometryFactory();
	
	
	/**
	 * The distance under which two coordinates should be treated as equal (in
	 * map units). The numerical stability of the framework routines used by
	 * these algorithms appears to be good enough for
	 * <code>EPSILON_DISTANCE==0.0</code> to also give optimal results. Then
	 * again, coordinates that are this close really should be treated as
	 * identical anyhow.
	 */
//	final double EPSILON_DISTANCE = .00001;  // arc degrees
	final double EPSILON_DISTANCE = 2.0;  // metres
	
	
	static int VERBOSITY = Testbed.VERBOSITY();
	
	
	static void log (final int logLevel, final String logMessage) {
		if (logLevel <= VERBOSITY) {
			System.err.println(logMessage);
		}
	}
	
	
	/**
	 * Stores a deep copy of <code>lines</code> in {@link #lines}.
	 * This is necessary, of course, because the splitting algorithm modifies
	 * the lines and we want to make sure the client isn't affected by that.
	 */
	Splitter (Collection<LineString> lines) {
		this.lines = new ArrayList<LineString>(lines);
	}
	
	
	/**
	 * @return the line description combined with the suffix
	 */
	String pointName (LineString line, String suffix) {
		return LineMeta.description(line) + "-" + suffix;
	}
	
	
	/**
	 * @return the line description combined with the serial
	 */
	String pointName (LineString line, int serial) {
		return this.pointName(line, Integer.toString(serial));
	}
	
	
	/**
	 * Returns an array containing the start point and end point of a line.
	 * This method ensures that both points have <GeometryMeta> instances
	 * associated with them that identify the line as their parent.
	 */
	Point[] namedLinePoints (LineString line) {
		final Point[] points = new Point[] {line.getStartPoint(), line.getEndPoint()};
		LinePartMeta.set( points[0], line, this.pointName(line, "start") );
		LinePartMeta.set( points[1], line, this.pointName(line, "end") );
		return points;
	}
	
	
	/**
	 * Returns a Point relating to a specific Coordinate of a given LineString.
	 * This method determines whether the coordinates match one of the line's
	 * endpoints and, if that is the case, returns those points instead of
	 * creating a new point using the Factory Method. It also ensures that the
	 * returned point is associated wit the appropriate meta data.
	 * 
	 * @param newPointSerial the serial number to append to the point's name
	 *  if a new one needs to be created
	 */
	Point createPoint (Coordinate coordinate, LineString line, int newPointSerial) {
		final Point[] linePoints = namedLinePoints(line);
		for (Point linePoint: linePoints) {
			boolean isSameCoordinate = coordinate.distance( linePoint.getCoordinate() ) < EPSILON_DISTANCE;
			if (isSameCoordinate) {
				return linePoint;
			}
		}
		Point point = this.factory.createPoint(coordinate);
		LinePartMeta.set( point, line, this.pointName(line, newPointSerial) );
		return point;
	}
	
	
	/**
	 * Calculate the positions at which lines have to be split.
	 */
	Collection<SplitJob> splitJobs () {
		int k = 0;  // facilitate logging
		log(2, "\nSplit Jobs:");
		
		
		final Collection<SplitJob> jobList = new LinkedList<SplitJob>();
		
		for (final LineString line2: this.lines) {
			final Point[] line2Points = namedLinePoints(line2);
			for (final Point line2Point: line2Points) {
				
				double minDistance = 40.0;
				SplitJob minDistanceJob = null;
				
				log(3, "");
				for (final LineString line1: this.lines) {
					
					// NB: DistanceOp's 'terminateDistance' is search-terminating, not feature-terminating!
					// => terminateDistance must be zero
					DistanceOp distOp = new DistanceOp(line1, line2Point, 0.0);
					Coordinate[] nearestPoints = distOp.nearestPoints();
					
					Coordinate coordinateOnLine1NearestToLine2Point = nearestPoints[0];  // :BUG: relies on DistanceOp implementation detail of JTS version 1.12
					Point splitPoint = this.createPoint(coordinateOnLine1NearestToLine2Point, line1, k);
					
					if (distOp.distance() < minDistance && distOp.distance() > EPSILON_DISTANCE) {
						minDistanceJob = new SplitJob(line1, splitPoint);
						minDistance = distOp.distance();
					}
					
					log(3, "   line: " + LineMeta.description(line1) + " dist: " + distOp.distance() + " splitPt: " + LinePartMeta.description(splitPoint));
					log(3, "   splitPt: " + splitPoint.toString());
					
					k++;
				}
				
				if ( minDistanceJob == null ) {
					log(2, k + ": skipped (minDistance " + minDistance + ")");
					continue;
				}
				
				// at this point, we've found the nearest point on the nearest line
				
				jobList.add(minDistanceJob);
				
				
				log(2, k + ": " + minDistance + " splitPt: " + LinePartMeta.description(minDistanceJob.point) + " line2: " + LineMeta.description(line2) + " line2Pt: " + LinePartMeta.description(line2Point));
			}
//			k++;
		}
		
		return jobList;
	}
	
	
	/**
	 * Collates a list of SplitJobs with the lines which need to be split. Some
	 * lines may have to be split multiple times, and the splitting algorithm
	 * requires that the list of jobs its given is sorted by line.
	 */
	Collection<SplitJob> collate (final Collection<SplitJob> jobList) {
		
		// this is rather expensive!
		
		final SplitJob[] jobArray = jobList.toArray(new SplitJob[0]);
		final Collection<SplitJob> jobListCollated = new LinkedList<SplitJob>();
		for (int i = 0; i < jobArray.length; i++) {
			SplitJob theJob = jobArray[i];
			
			if (theJob == null) {
				continue;
			}
			
			LineString theLine = theJob.line;
			jobListCollated.add(theJob);
			jobArray[i] = null;
			
			for (int j = 0; j < jobArray.length; j++) {
				SplitJob aJob = jobArray[j];
				
				if (aJob == null) {
					continue;
				}
				if (aJob == theJob) {
					continue;  // shouldn't happen after remove(), but still...
				}
				if (aJob.line == theLine) {
					jobListCollated.add(aJob);
					jobArray[j] = null;
				}
			}
		}
		
		log(2, "\nSplit Job collation result:\nSplit Jobs:");
		final SplitJob[] collatedArray = jobListCollated.toArray(new SplitJob[0]);
		for (int i = 0; i < collatedArray.length; i++) {
			log(2, i + ": " + collatedArray[i]);
		}
		
		// we continue with the splitjobs, and nothing else;
		// this means that lines that are not to be split are dropped at this point
		// (should be solvable by further refactoring the collation algorithm,
		// we actually shoehorn those lines back in at a later stage instead)
		
		return jobListCollated;
	}
	
	
	/**
	 * Splits lines according to the list of Split Jobs provided. This
	 * algorithm works on this instance's {@link #lines} field.
	 */
	void split (final Collection<SplitJob> jobList) throws Exception {
		
		final Collection<SplitJob> jobListCollated = this.collate(jobList);
		final ArrayList<LineString> newLines = new ArrayList<LineString>();
		
		// in order to linearly progress along a line, we use a sorted set that
		// automatically yields the coordinates in their implicitly defined order
		SortedSet<Point> currentLineSplitPoints = new TreeSet<Point>();
		LineString currentLine = null;
		
		// the job list is collated, so we just run through it until we find that
		// the current job operates on a different line than the previous job;
		// at that point, we finalise the current line and begin working the next one
		for (final SplitJob theJob: jobListCollated) {
			if (currentLine == null) {
				currentLine = theJob.line;
			}
			if (currentLine != theJob.line) {
				
				// finalise;
				// actually perform the splits
				
				final Point[] splitPoints = currentLineSplitPoints.toArray(new Point[0]);
				for (int i = 0; i < splitPoints.length - 1; i++) {
					final Point p1 = splitPoints[i];
					final Point p2 = splitPoints[i+1];
					
					// integrity check
					if (p1.getCoordinate().distance(p2.getCoordinate()) < EPSILON_DISTANCE) {
						log(0, "skipping zero-length line between " + LinePartMeta.getFrom(p1) + " and " + LinePartMeta.getFrom(p2) + " (actual length: " + p1.getCoordinate().distance(p2.getCoordinate()) + ")");
						continue;
					}
					
					// create new line
					final Coordinate[] newLineCoordinates = new Coordinate[] {p1.getCoordinate(), p2.getCoordinate()};
					final LineString newLine = factory.createLineString(newLineCoordinates);
					newLines.add(newLine);
					
					// add meta data to new line
					final Geometry origin = LinePartMeta.origin(p1);
					final String description = LinePartMeta.description(p1) + "/" + LinePartMeta.description(p2);
					LinePartMeta.set( newLine, origin, description );
					
					// integrity checks
					if (origin != LinePartMeta.origin(p2)) {
						throw new RuntimeException("I refuse to be part of two different families!");
					}
					if (! (origin instanceof LineString)) {
						throw new RuntimeException("I refuse to work with strangers!");
					}
				}
				
				// reset "current line" for working on the next one
				currentLineSplitPoints = new TreeSet<Point>();
				currentLine = theJob.line;
			}
			
			// add points to list of points the current line needs to be split at
			final Point[] theLinePoints = namedLinePoints(theJob.line);
			currentLineSplitPoints.add(theLinePoints[0]);
			currentLineSplitPoints.add(theLinePoints[1]);
			currentLineSplitPoints.add(theJob.point);
		}
		
		
		// lines that have not been split aren't present in newLines yet,
		// so we need to add them
		
		log(2, "Lines that didn't need to be split: ");
		Collection<LineString> missingLines = new LinkedList<LineString>();
		lines: for (final LineString line: this.lines) {
			for (final LineString fragment: newLines) {
				if (LinePartMeta.origin(fragment) == line) {
					// line is aleady present
					continue lines;
				}
			}
			// if we reach this point, the line is not present
			
			/* We need to add a copy of the line instead of the line itself to
			 * keep the meta data straight. The ParallelismFinder expects having to deal
			 * with line _parts_. Real solution: change class structure.
			 */
			final LineString lineCopy = (LineString)factory.createGeometry(line);
			LinePartMeta.set( lineCopy, line, LineMeta.description(line) );
			missingLines.add(lineCopy);
			log(2, LineMeta.description(line));
		}
		
		log(1, "\nSplitting complete. The fragment count is: " + newLines.size());
		
		newLines.addAll(missingLines);
		
		log(1, "Number of lines that didn't need to be split: " + missingLines.size());
		log(1, "Total number of fragments in result data: " + newLines.size() + " (increased from " + this.lines.size() + " before splitting)\n");
		
		
		this.lines = newLines;
	}
	
	
	/**
	 * Runs the splitting algorithm.
	 * 
	 * @see #splitJobs()
	 * @see #split(Collection)
	 */
	void split () throws Exception {
		this.split(this.splitJobs());
	}
	
	
	
	/**
	 * Represents a relation between a line and the point at which to split it.
	 */
	class SplitJob {
		LineString line;
		Point point;
		
		SplitJob (final LineString line, final Point point) {
			this.line = line;
			this.point = point;
		}
		
		SplitJob (final LineString line, final Coordinate coordinate, final Object userData) {
			this.line = line;
			this.point = Splitter.this.factory.createPoint(coordinate);
			if (userData != null) {
				this.point.setUserData(userData);
			}
		}
		
		public String toString () {
			return "split " + LineMeta.getFrom(this.line) + " at " + LinePartMeta.getFrom(this.point);
		}
	}
	
}
