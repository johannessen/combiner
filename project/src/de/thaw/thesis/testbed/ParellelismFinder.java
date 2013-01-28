/* encoding UTF-8
 * 
 * Copyright (c) 2012-13 Arne Johannessen
 * 
 * This project and all of its individual parts may be used in accordance
 * with the terms of the 3-clause BSD licence. See LICENSE for details.
 */

package de.thaw.thesis.testbed;

import com.vividsolutions.jts.geom.LineString;
import com.vividsolutions.jts.geom.Geometry;
import com.vividsolutions.jts.geom.Coordinate;

import com.vividsolutions.jts.geom.GeometryFactory;

import java.util.SortedSet;
import java.util.TreeSet;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.LinkedList;


/**
 * Parallelism analyser. Determines which lines are geometrically parallel to
 * other lines. Implements a brute-force approach, comparing every line to
 * every other line, disregarding any spatial relationships. Attributes are not
 * taken into account.
 * <p>
 * This implementation works best on parallel line fragments of approximately
 * equal length. The {@link Splitter} may be used to create such fragments.
 */
final class ParallelismFinder {
	
	
	/**
	 * Lines to be analysed.
	 */
	Collection<LineString> evaluationLines;
	
	
	/**
	 * Lines to be used for collating the analysis results with. The result
	 * will contain exactly one item each for every line in this collection.
	 * <p>
	 * This collection may be exactly identical to {@link #evaluationLines}, in
	 * which case there will be exactly one results item for each of the
	 * analysed lines. If OTOH you're analysing line fragments that were
	 * created by ably splitting some collection of original lines, it may be
	 * more useful to summarise the results such that there is only one results
	 * item per <em>original</em> line.
	 * <p>
	 * This field has no direct effect on the analysis itself. However, you'd
	 * want to avoid comparing any two lines that have the same origin
	 * according to the meta data. If you did, the collating would produce
	 * gargabe because most original lines would be most similar to themselves
	 * (other parts of themselves, really). That'd not be useful information.
	 * <p>
	 * Therefore the evaluation lines need to be associated with the original
	 * lines using <code>GeometryMeta</code> data, if they are not identical
	 * anyway.
	 * 
	 * @see GeometryMeta#origin(Geometry)
	 */
	Collection<LineString> originalLines;
	
	
	
	/**
	 * The analysis's results.
	 */
	Collection<ResultSet> results;
	
	
	/**
	 * The maximum (averaged) location difference at which to withdraw a
	 * particular pair of line fragments from consideration of being parallel.
	 * (This probably doesn't help us very much, if at all.)
	 */
	final double MAX_DISTANCE = 200;  // metres
//	final double MAX_DISTANCE = Double.POSITIVE_INFINITY;  // metres
	
	
	static void log (final int logLevel, final String logMessage) {
		if (logLevel <= Testbed.VERBOSITY()) {
			System.err.println(logMessage);
		}
	}
	
	
	/**
	 * Prepare to analyse the lines provided with no particular collation.
	 * Specifically, this constructor ensures that
	 * <code>this.evaluationLines == this.originalLines == lines</code>.
	 * 
	 * @see #originalLines
	 */
	ParallelismFinder (final Collection<LineString> lines) {
		this.evaluationLines = lines;
		this.originalLines = lines;
	}
	
	
	/**
	 * @return the course from <code>start</code> to <code>end</code>
	 * 
	 * @see <a href="http://en.wikipedia.org/wiki/Course_(navigation)">definition of a course</a>
	 */
	static double orientation (final Coordinate start, final Coordinate end) {
		final double orientation = Math.atan2(end.x - start.x, end.y - start.y) * 180.0 / Math.PI;
		return orientation < 0.0 ? orientation + 360.0 : orientation;
	}
	
	
	/**
	 */
	static double orientation (final LineString line) {
		return orientation(line.getStartPoint().getCoordinate(), line.getEndPoint().getCoordinate());
	}
	
	
	/**
	 * @return the (absolute) relative bearing difference
	 *  (for example, 0 for exactly parallel lines in the same direction, 180
	 *  for opposite directions, or 90 for orthogonal directions)
	 */
	static double orientationDifference (final double orientation1, final double orientation2) {
		double difference = Math.abs(orientation2 - orientation1);
		if (difference > 180.0) {
			difference = 360.0 - difference;
		}
		return difference;
	}
	
	
	/**
	 */
	static double orientationDifference (final LineString line1, final LineString line2) {
		return orientationDifference(orientation(line1), orientation(line2));
	}
	
	
	/**
	 */
	static double compareOrientation (final double orientation1, final double orientation2) {
		final double similarity = Math.abs(orientationDifference(orientation1, orientation2) - 90.0) / 90.0;
		return similarity == 0.0 ? Double.MIN_VALUE : similarity;
	}
	
	
	/**
	 */
	double compareOrientation (final LineString line1, final LineString line2) {
		return compareOrientation(orientation(line1), orientation(line2));
	}
	
	
	/**
	 * @return a distance measure representing the difference in location
	 *  between the two lines' start and end points, in map units
	 */
	double compareLocation (final LineString line1, final LineString line2original) {
		
		// reverse one of the lines before comparison if they're pointed in opposite directions
		final LineString line2;
		if (orientationDifference(line1, line2original) <= 90.0) {
			line2 = line2original;
		}
		else {
			line2 = (LineString)line2original.reverse();
		}
		
		// obtain coordinates of all start- and end-points
		final Coordinate[] startPoints = new Coordinate[2];
		final Coordinate[] endPoints = new Coordinate[2];
		startPoints[0] = line1.getStartPoint().getCoordinate();
		endPoints[0] = line1.getEndPoint().getCoordinate();
		startPoints[1] = line2.getStartPoint().getCoordinate();
		endPoints[1] = line2.getEndPoint().getCoordinate();
		
		final double[] distances = new double[2];
		distances[0] = startPoints[0].distance(startPoints[1]);
		distances[1] = endPoints[0].distance(endPoints[1]);
		log(3, "    distances: start " + distances[0] + ", end " + distances[1]);
		
//		final double arithmeticMeanDistance = (distances[0] + distances[1]) / 2.0;
//		final double distanceDifference = Math.abs(distances[0] - distances[1]);
		/*
		 * The larger of the two distances is used as base for distance
		 * evaluation because many of the respective shorter distances are
		 * exceptionally short due to the intricacies of the splitting
		 * algorithm. Since the minimum distances of parallel linestrings are
		 * capped to the value of their orthogonal distance during splitting,
		 * the larger of the two distances is by definition equal to this
		 * orthogonal distance for that fragment which is the best match, while
		 * those fragments farther away by nature have a significantly larger
		 * "larger distance".
		 */
		final double largerDifference = Math.max(distances[0], distances[1]);
		
		final double compareLocation = largerDifference;
		if (compareLocation > this.MAX_DISTANCE) {
			return Double.POSITIVE_INFINITY;
		}
		return compareLocation;
	}
	
	
	/**
	 * Compares both lines with each other and returns their estimated
	 * similarity expressed as a single floating point value.
	 * 
	 * @return a double that is the higher the more similar the two lines are
	 */
	double compareLines (final LineString line1, final LineString line2) {
		
		// the 'simple' test data set works fine with just a location comparison;
		// we can easily add comparisons of orientation or length
		
		final double orientationSimilarity = this.compareOrientation(line1, line2);
//		final double lengthSimilarity = this.compareLength(line1, line2);
		final double locationSimilarity = this.compareLocation(line1, line2);
		return 1.0 / locationSimilarity * orientationSimilarity;
//		return 1.0 / locationSimilarity;
	}
	
	
	/**
	 * Of all the lines to be evaluated, find the one that's most similar to
	 * the one provided.
	 * 
	 * @see #evaluationLines
	 * @see #originalLines
	 */
	LineString findMostSimilar (final LineString theLine) {
		log(3, "");
		log(2, "Analysing fragment: " + LinePartMeta.getFrom(theLine));
		
		// brute force: compare everything with everything else
		LineString mostSimilarLine = null;
		double maxSimilarity = Double.NEGATIVE_INFINITY;
		for (final LineString aLine : this.evaluationLines) {
			
			log(3, "  comparing with: " + LinePartMeta.getFrom(aLine));
			
			// don't compare us with ourselves
			if (theLine == aLine) {
				log(3, "    skipped (self)");
				continue;
			}
			// don't compare us with something that's got the same parent as we do
			if (LinePartMeta.origin(theLine) == LinePartMeta.origin(aLine)) {
				log(3, "    skipped (same parent)");
				continue;
			}
			
			final double similarity = this.compareLines(theLine, aLine);
			log(3, "    similarity: " + similarity);
			if (mostSimilarLine == null || similarity > maxSimilarity) {
				mostSimilarLine = aLine;
				maxSimilarity = similarity;
			}
		}
		log(2, "  most similar fragment: " + LinePartMeta.getFrom(mostSimilarLine));
		return mostSimilarLine;
	}
	
	
	/**
	 * Runs the analysis. This method attempts to find the most similar line
	 * for each of the {@link ParallelismFinder#evaluationLines}, then summarises and
	 * collates the results of that operation before writing a text
	 * representation of the collated results to the standard output.
	 */
	void analyse () {
		
		// analyse, storing pairs of similar lines (or line fragments) in a list
		final Collection<SimilarityRelation> similarities = new ArrayList<SimilarityRelation>(this.evaluationLines.size());
		for (final LineString line : this.evaluationLines) {
			similarities.add(new SimilarityRelation( line, this.findMostSimilar(line) ));
		}
		
		// summarise results, collating the list of similar pairs with the list
		// of lines originally read from the database
		this.results = new ArrayList<ResultSet>(this.originalLines.size());
		for (final LineString originalLine : this.originalLines) {
			
			final Collection<Parallelism> parallelisms = new LinkedList<Parallelism>();
			for (final SimilarityRelation relation : similarities) {
				if (LinePartMeta.origin(relation.line) == originalLine
					|| relation.line == originalLine) {  // :BUG: should be one check or the other
					parallelisms.add(new Parallelism( originalLine, relation.similarLine ));
				}
			}
			
			this.results.add(new ResultSet(originalLine, parallelisms));
		}
		
		// present results on stdout
		for (final ResultSet resultLine: this.results) {
			log(2, "> " + resultLine);
		}
		
	}
	
	
	/**
	 * Convert the results to a list of lines with metadata.
	 */
	List<LineString> resultAsLineList () {
		GeometryFactory factory = new GeometryFactory();
		List<LineString> list = new LinkedList<LineString>();
		for (final ResultSet resultLine: this.results) {
			LineString line = resultLine.line;
//			LineString line = new LineString(resultLine.line.getCoordinateSequence(), factory);
//			line.setUserData( resultLine.line.getUserData() );
			list.add(line);
		}
		return list;
	}
	
	
	
	/**
	 * Represents a pair of similar lines.
	 */
	class SimilarityRelation {
		final LineString line;
		final LineString similarLine;
		SimilarityRelation (final LineString line, final LineString similarLine) {
			this.line = line;
			this.similarLine = similarLine;
		}
	}
	
	
	
	/**
	 * Represents a relation between one single line and all the lines which
	 * are (likely) parallel to it.
	 */
	class ResultSet {
		final LineString line;
		final SortedSet<Parallelism> parallelisms;
		
		/**
		 * If a buffer analysis is run and concludes that all of this
		 * instance's parallelisms are in fact inside this line's buffer, this
		 * field is set to true.
		 * @see ParallelDisprover
		 */
		boolean parallelismsInBuffer = false;
		
		ResultSet (final LineString line, final Collection<Parallelism> parallelisms) {
			this.line = line;
			this.parallelisms = new TreeSet<Parallelism>(parallelisms);
			
			// store results for later use in shapefile output
//			final LineMeta meta = LineMeta.getFrom(line);
//			meta.finderResults = this;
			final LinePartMeta meta = LinePartMeta.getFrom(line);
			meta.finderResults = this;
		}
		
		public String toString() {
//			final String lineDescription = LineMeta.description(this.line);
			final String lineDescription = LinePartMeta.description(this.line);
			StringBuilder description = null;
			
			if (parallelisms.size() == 0) {
				return lineDescription + " has no parallels.";
			}
			
			for (final Parallelism parallelism : parallelisms) {
				if (description == null) {
					description = new StringBuilder();
					description.append(lineDescription);
					description.append(" is parallel to ");
				}
				else {
					description.append(", ");
				}
				description.append(parallelism);
			}
			return description.toString();
		}
	}
	
}

