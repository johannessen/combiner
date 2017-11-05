/* encoding UTF-8
 * 
 * Copyright (c) 2013 Arne Johannessen
 * 
 * This project and all of its individual parts may be used in accordance
 * with the terms of the 3-clause BSD licence. See LICENSE for details.
 */

package de.thaw.thesis.comb.highway;

import de.thaw.thesis.comb.Analyser;
import de.thaw.thesis.comb.Segment;
import de.thaw.thesis.comb.util.AttributeProvider;
import de.thaw.thesis.comb.util.SimpleVector;
import de.thaw.thesis.comb.util.Vector;


// ex MyAnalyser
/**
 * This Client's Analyser. This implementation evaluates fragments on a purely
 * geometric basis; OSM tags are not taken into account in any way.
 */
public final class HighwayAnalyser implements Analyser {
	
	private final double MAX_DISTANCE = 40.0;  // metres
	
	private final double COLLINEAR_MAX_ANGLE = 30.0 / 180.0 * Math.PI;
	
	private final boolean evaluateTags;
	
	
	public HighwayAnalyser (final boolean evaluateTags) {
		this.evaluateTags = evaluateTags;
	}
	
	
	private AttributeProvider tags (final Segment part) {
		return part.root().way.tags();
	}
	
	
	public boolean shouldEvaluate (final Segment part1, final Segment part2) {
		assert part1.root() != part2.root();
		
		// ignore fragments that share a node with the current segment
		// (otherwise the L/R point search in next step might get confused (?))
		// :TODO: really test this!
		//        (initial testing indicates this may not any impact on trivial
		//         datasets, but may screw up those that have branches etc.)
		if ( part1.start().equals(part2.start())
				|| part1.end().equals(part2.end())
				|| part1.start().equals(part2.end())
				|| part1.end().equals(part2.start()) ) {
//			return false;
		}
		
		final Vector v1 = part1;
		final Vector v2 = part2.aligned(v1);
		final SimpleVector starts;
		final SimpleVector ends;
		if (part2.isAligned(v1)) {
			starts = new SimpleVector(part1.start(), part2.start());
			ends = new SimpleVector(part1.end(), part2.end());
		}
		else {
			starts = new SimpleVector(part1.start(), part2.end());
			ends = new SimpleVector(part1.end(), part2.start());
		}
		
		// ignore fragments whose start/end-points are far apart
		if ( starts.distance() > MAX_DISTANCE || ends.distance() > MAX_DISTANCE ) {
			return false;
		}
		
		// ignore collinear fragments
		if ( Math.abs( v1.relativeBearing(starts.aligned(v1)) ) < COLLINEAR_MAX_ANGLE
				&& Math.abs( v1.relativeBearing(ends.aligned(v1)) ) < COLLINEAR_MAX_ANGLE
				&& Math.abs( v2.relativeBearing(starts.aligned(v2)) ) < COLLINEAR_MAX_ANGLE
				&& Math.abs( v2.relativeBearing(ends.aligned(v2)) ) < COLLINEAR_MAX_ANGLE ) {
			return false;
		}
		
		// ignore fragments crossing each other
		// :TODO: really test this!
		if ( Math.signum(v1.relativeBearing(starts)) != Math.signum(v1.relativeBearing(ends)) ) {
			return false;
		}
		
		if (evaluateTags) {
			// only evaluate fragments of the same highway class
			// (tag results are interned => == works on Strings)
			// :BUG: fails on dual carriageways having links etc. on _both_ sides
			if ( tags(part1).get("highway") != tags(part2).get("highway") ) {
				return false;
			}
			if ( tags(part1).get("ref") != tags(part2).get("ref") ) {
				return false;
			}
		}
		
		return true;
	}
	
	
	/**
	 * {@inheritDoc}<p>
	 * 
	 * This implementation calculates the <em>sum</em> of the distance of the
	 * two start points and the two endpoints. If the segments are not aligned
	 * the same way, one of them is reversed for this calculation.
	 * 
	 * @throws NullPointerException if <code>s1</code> or <code>s2</code> are
	 *  <code>null</code>
	 */
	public double distance (final Segment s1, final Segment s2) {
		return evaluate(s1, s2, Double.NaN);
	}
	
	
	private double evaluate (final Segment part1, final Segment part2, final double direction) {
		
//		assert ! (part1 instanceof de.thaw.thesis.comb.SourceSegment && (((de.thaw.thesis.comb.SourceSegment)part1).fragments.size() > 0));
//		assert ! (part2 instanceof de.thaw.thesis.comb.SourceSegment && (((de.thaw.thesis.comb.SourceSegment)part2).fragments.size() > 0));
		
		final Vector v1 = part1;
//		final Vector v2 = part2.aligned(v1);
		final SimpleVector starts;
		final SimpleVector ends;
		if (part2.isAligned(v1)) {
			starts = new SimpleVector(part1.start(), part2.start());
			ends = new SimpleVector(part1.end(), part2.end());
		}
		else {
			starts = new SimpleVector(part1.start(), part2.end());
			ends = new SimpleVector(part1.end(), part2.start());
		}
/*
if ((part1.root().way.id == 19975724L || part2.root().way.id == 19975724L || part1.root().way.id == 105062275L || part2.root().way.id == 105062275L) && (part1.root().way.id == 105062281L || part2.root().way.id == 105062281L)) {
System.err.println("Vectors: (" + part1.root().way.id + ") " + v1 + "  /  (" + part2.root().way.id + ") " + v2);
System.err.println(starts + "  /  " + ends + "\n");
}
*/
		
		// left / right exclusion
		// :TODO: really test this! -- seems to work fine though, based on practical results
		if ( ! Double.isNaN(direction) && (
				Math.signum(v1.relativeBearing(starts)) != direction
				|| Math.signum(v1.relativeBearing(ends)) != direction )) {
			return worstResult();
		}
		
		final double distance = starts.distance() + ends.distance();
//		final double distance = Math.max(starts.distance(), ends.distance());
		return distance;
	}
	
	
	public double evaluateLeft (final Segment part1, final Segment part2) {
		return evaluate(part1, part2, -1.0);
	}
	
	
	public double evaluateRight (final Segment part1, final Segment part2) {
		return evaluate(part1, part2, 1.0);
	}
	
	
	public int compare (final double value, final double bestValue) {
		return Double.compare(value, bestValue);
	}
	
	
	public double worstResult () {
		return Double.POSITIVE_INFINITY;
	}
	
}
