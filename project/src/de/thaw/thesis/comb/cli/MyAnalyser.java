/* encoding UTF-8
 * 
 * Copyright (c) 2013 Arne Johannessen
 * 
 * This project and all of its individual parts may be used in accordance
 * with the terms of the 3-clause BSD licence. See LICENSE for details.
 */

package de.thaw.thesis.comb.cli;

import de.thaw.thesis.comb.Analyser;
import de.thaw.thesis.comb.LinePart;
import de.thaw.thesis.comb.OsmTags;
import de.thaw.thesis.comb.Vector;


/**
 * This Client's Analyser. This implementation evaluates fragments on a purely
 * geometric basis; OSM tags are not taken into account in any way.
 */
final class MyAnalyser implements Analyser {
	
	private final double MAX_DISTANCE = 40.0;  // metres
	
	private final double COLLINEAR_MAX_ANGLE = 30.0 / 180.0 * Math.PI;
	
	
	private OsmTags tags (final LinePart part) {
		return part.segment().way.tags();
	}
	
	
	public boolean shouldEvaluate (final LinePart part1, final LinePart part2) {
		assert part1.segment() != part2.segment();
		
		// ignore fragments that share a node with the current segment
		// (otherwise the L/R point search in next step might get confused (?))
		// :TODO: really test this!
		if ( part1.start().equals(part2.start())
				|| part1.end().equals(part2.end())
				|| part1.start().equals(part2.end())
				|| part1.end().equals(part2.start()) ) {
			return false;
		}
		
		final Vector v1 = part1.vector();
		final Vector v2 = part2.vector().aligned(v1);
		final Vector starts;
		final Vector ends;
		if (part2.vector().isAligned(v1)) {
			starts = new Vector(part1.start(), part2.start());
			ends = new Vector(part1.end(), part2.end());
		}
		else {
			starts = new Vector(part1.start(), part2.end());
			ends = new Vector(part1.end(), part2.start());
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
		
		// only evaluate fragments of the same highway class
		// (tag results are interned => == works on Strings)
//		if ( tags(part1).get("highway") != tags(part2).get("highway") ) {
//			return false;
//		}
		
		return true;
	}
	
	
	private double evaluate (final LinePart part1, final LinePart part2, final double direction) {
		
		assert ! (part1 instanceof de.thaw.thesis.comb.LineSegment && (((de.thaw.thesis.comb.LineSegment)part1).fragments.size() > 0));
		assert ! (part2 instanceof de.thaw.thesis.comb.LineSegment && (((de.thaw.thesis.comb.LineSegment)part2).fragments.size() > 0));
		
		final Vector v1 = part1.vector();
		final Vector v2 = part2.vector().aligned(v1);
		final Vector starts;
		final Vector ends;
		if (part2.vector().isAligned(v1)) {
			starts = new Vector(part1.start(), part2.start());
			ends = new Vector(part1.end(), part2.end());
		}
		else {
			starts = new Vector(part1.start(), part2.end());
			ends = new Vector(part1.end(), part2.start());
		}
//System.err.println("Vectors: (" + part1.segment().way.id + ") " + v1 + "  /  (" + part2.segment().way.id + ") " + v2);
//System.err.println(starts + "  /  " + ends + "\n");
		
		// left / right exclusion
		// :TODO: really test this!
		if ( Math.signum(v1.relativeBearing(starts)) != direction
				|| Math.signum(v1.relativeBearing(ends)) != direction ) {
			return worstResult();
		}
		
		final double distance = starts.distance() + ends.distance();
		return distance;
	}
	
	
	public double evaluateLeft (final LinePart part1, final LinePart part2) {
		return evaluate(part1, part2, -1.0);
	}
	
	
	public double evaluateRight (final LinePart part1, final LinePart part2) {
		return evaluate(part1, part2, 1.0);
	}
	
	
	public int compare (final double value, final double bestValue) {
		return Double.compare(value, bestValue);
	}
	
	
	public double worstResult () {
		return Double.POSITIVE_INFINITY;
	}
	
}