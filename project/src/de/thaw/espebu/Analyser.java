/* encoding UTF-8
 * 
 * Copyright (c) 2012 Arne Johannessen
 * 
 * This project and all of its individual parts may be used in accordance
 * with the terms of a BSD-style license. See LICENSE for details.
 */

package de.thaw.espebu;

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
final class Analyser {
	
	
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
	
	// :TODO: we can prolly use this together with the Merger to get some interesting results...
	
	
	
	/**
	 * The analysis's results.
	 */
	Collection<ResultSet> results;
	
	
	/**
	 * Prepare to analyse the lines provided with no particular collation.
	 * Specifically, this constructor ensures that
	 * <code>this.evaluationLines == this.originalLines == lines</code>.
	 * 
	 * @see #originalLines
	 */
	Analyser (final Collection<LineString> lines) {
		this.evaluationLines = lines;
		this.originalLines = lines;
	}
	
	
	/**
	 * @return the course from <code>start</code> to <code>end</code>
	 * 
	 * @see <a href="http://en.wikipedia.org/wiki/Course_(navigation)">definition of a course</a>
	 */
	double orientation (final Coordinate start, final Coordinate end) {
		final double orientation = Math.atan2(end.x - start.x, end.y - start.y) * 180.0 / Math.PI;
		return orientation < 0.0 ? orientation + 360.0 : orientation;
	}
	
	
	/**
	 */
	double orientation (final LineString line) {
		return this.orientation(line.getStartPoint().getCoordinate(), line.getEndPoint().getCoordinate());
	}
	
	
	/**
	 * @return the (absolute) relative bearing difference
	 *  (for example, 0 for exactly parallel lines in the same direction, 180
	 *  for opposite directions, or 90 for orthogonal directions)
	 */
	double orientationDifference (final double orientation1, final double orientation2) {
		double difference = Math.abs(orientation2 - orientation1);
		if (difference > 180.0) {
			difference = 360.0 - difference;
		}
		return difference;
	}
	
	
	/**
	 */
	double orientationDifference (final LineString line1, final LineString line2) {
		return this.orientationDifference(this.orientation(line1), this.orientation(line2));
	}
	
	
	/**
	 */
	double compareOrientation (final double orientation1, final double orientation2) {
		final double similarity = Math.abs(this.orientationDifference(orientation1, orientation2) - 90.0) / 90.0;
		return similarity == 0.0 ? Double.MIN_VALUE : similarity;
	}
	
	
	/**
	 */
	double compareOrientation (final LineString line1, final LineString line2) {
		return this.compareOrientation(this.orientation(line1), this.orientation(line2));
	}
	
	
	/**
	 * @return the average distance of the two lines' start and end points, in
	 *  map units
	 */
	double compareLocation (final LineString line1, final LineString line2original) {
		
		// reverse one of the lines before comparison if they're pointed in opposite directions
		final LineString line2;
		if (this.orientationDifference(line1, line2original) <= 90.0) {
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
		
		return (distances[0] + distances[1]) / 2.0;
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
		
//		final double orientationSimilarity = this.compareOrientation(line1, line2);
//		final double lengthSimilarity = this.compareLength(line1, line2);
		final double locationSimilarity = this.compareLocation(line1, line2);
		return 1.0 / locationSimilarity;
	}
	
	
	/**
	 * Of all the lines to be evaluated, find the one that's most similar to
	 * the one provided.
	 * 
	 * @see #evaluationLines
	 * @see #originalLines
	 */
	LineString findMostSimilar (final LineString theLine) {
		
		// brute force: compare everything with everything else
		LineString mostSimilarLine = null;
		double maxSimilarity = Double.NEGATIVE_INFINITY;
		for (final LineString aLine : this.evaluationLines) {
			
			// don't compare us with ourselves
			if (theLine == aLine) {
				continue;
			}
			// don't compare us with something that's got the same parent as we do
			if (GeometryMeta.origin(theLine) == GeometryMeta.origin(aLine)) {
				continue;
			}
			
			final double similarity = compareLines(theLine, aLine);
			if (mostSimilarLine == null || similarity > maxSimilarity) {
				mostSimilarLine = aLine;
				maxSimilarity = similarity;
			}
		}
		return mostSimilarLine;
	}
	
	
	/**
	 * Runs the analysis. This method attempts to find the most similar line
	 * for each of the {@link Analyser#evaluationLines}, then summarises and
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
				if (GeometryMeta.origin(relation.line) == originalLine) {
					parallelisms.add(new Parallelism( originalLine, relation.similarLine ));
				}
			}
			
			this.results.add(new ResultSet(originalLine, parallelisms));
		}
		
		// present results on stdout
		for (final ResultSet resultLine: this.results) {
			System.out.println("> " + resultLine);
		}
		
	}
	
	
	/**
	 * Convert the results to a list of lines with metadata.
	 */
	List<LineString> resultAsLineList () {
		List<LineString> list = new LinkedList<LineString>();
		for (final ResultSet resultLine: this.results) {
			LineString clone = new LineString(resultLine.line.getCoordinateSequence(), new GeometryFactory());
			GeometryMeta.set(clone, resultLine.line, resultLine.parallelisms.first().toString());
			list.add(clone);
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
	 * Represents a relation between a line and a (likely) parallel line,
	 * including the percentage to which the two lines are (likely) parallel.
	 */
	class Parallelism implements Comparable<Parallelism> {
		final Geometry origin;
		final double overlap;
		
		Parallelism (final LineString baseLine, final LineString fragment) {
			this.origin = GeometryMeta.origin(fragment);
			this.overlap = fragment.getLength() / baseLine.getLength();
		}
		
		public String toString () {
			return GeometryMeta.description(this.origin) + " (" + Math.max(1, Math.round((float)this.overlap * 100f)) + "%)";
		}
		
		// we want instances to be easily sortable so that in a list of parallelisms,
		// the one with the highest degree of overlap comes up first
		public int compareTo (final Parallelism that) {
			if (this.equals(that)) {
				/* We can't really represent this case completely correctly,
				 * but need to handle it somewhat gracefully at least.
				 * The Comparable contract requires every compareTo
				 * implementation to be invertible, i. e.
				 * <code>x.compareTo(y) == -y.compareTo(x)</code>.
				 * We are unable to satsify this requirement well given our
				 * data structure, but since we only use it to sort some
				 * percentages into a sort of sensible order to ease reading
				 * comprehension, it doesn't matter if compareTo yields
				 * confusing results in our particular case.
				 */
				final int hashDifference = this.objectHashCode() - that.objectHashCode();
				return hashDifference != 0 ? hashDifference : Integer.MAX_VALUE;
			}
			return -1 * Double.compare(this.overlap, that.overlap);
		}
		
		// if we need to implement compareTo, we also need to override equals (by contract terms)
		public boolean equals (final Object object) {
			if (! (object instanceof Parallelism)) {
				return false;
			}
			final Parallelism that = (Parallelism)object;
			return this.overlap == that.overlap && this.origin == that.origin;
		}
		
		// if we need to override equals, we also need to override hashCode (by contract terms)
		public int hashCode () {
			return (int)(overlap * (double)(Integer.MAX_VALUE - Integer.MIN_VALUE));
		}
		
		private int objectHashCode () {
			// this would be the original hash code as returned by the
			// java.lang.Object implementation; usually this relates to the
			// object's logical location in memory
			return super.hashCode();
		}
	}
	
	
	
	/**
	 * Represents a relation between one single line and all the lines which
	 * are (likely) parallel to it.
	 */
	class ResultSet {
		final LineString line;
		final SortedSet<Parallelism> parallelisms;
		
		ResultSet (final LineString line, final Collection<Parallelism> parallelisms) {
			this.line = line;
			this.parallelisms = new TreeSet<Parallelism>(parallelisms);
		}
		
		public String toString() {
			StringBuilder description = null;
			for (final Parallelism parallelism : parallelisms) {
				if (description == null) {
					description = new StringBuilder();
					description.append(GeometryMeta.description(this.line));
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

