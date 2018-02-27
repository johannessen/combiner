/* encoding UTF-8
 * 
 * Copyright (c) 2013 Arne Johannessen
 * 
 * This project and all of its individual parts may be used in accordance
 * with the terms of the 3-clause BSD licence. See LICENSE for details.
 */

package de.thaw.comb.highway;

import de.thaw.comb.Analyser;
import de.thaw.comb.Segment;
import de.thaw.comb.util.AttributeProvider;
import de.thaw.comb.util.SimpleVector;
import de.thaw.comb.util.Vector;


// ex MyAnalyser
/**
 * This Client's Analyser. This implementation evaluates fragments on a purely
 * geometric basis; OSM tags are not taken into account in any way.
 */
public final class HighwayAnalyser implements Analyser {
	
	private final double MAX_DISTANCE = 40.0;  // metres
	
	private final int evaluateTags;
	
	
	public HighwayAnalyser (final int evaluateTags) {
		this.evaluateTags = evaluateTags;
	}
	
	
	private AttributeProvider tags (final Segment part) {
		return part.root().way.tags();
	}
	
	
	public boolean shouldEvaluate (final Segment part1, final Segment part2) {
		assert part1.root() != part2.root();
		
/*
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
*/
		
		final Vector v1 = part1;
		final Vector v2 = part2.aligned(v1);
		final SimpleVector vStart1Start2 = new SimpleVector(part1.start(), part2.start());
		final SimpleVector vStart1End2 = new SimpleVector(part1.start(), part2.end());
		final SimpleVector vEnd1Start2 = new SimpleVector(part1.end(), part2.start());
		final SimpleVector vEnd1End2 = new SimpleVector(part1.end(), part2.end());
		final SimpleVector starts;
		final SimpleVector ends;
		if (part2.isAligned(v1)) {
			starts = vStart1Start2;
			ends = vEnd1End2;
		}
		else {
			starts = vStart1End2;
			ends = vEnd1Start2;
		}
		
		// ignore fragments whose start/end-points are far apart
		if ( starts.distance() > MAX_DISTANCE || ends.distance() > MAX_DISTANCE ) {
			return false;
		}
		
		// ignore collinear fragments
		final double angleStart1Start2 = Math.abs( v1.relativeBearing(vStart1Start2) );
		final double angleStart1End2   = Math.abs( v1.relativeBearing(vStart1End2) );
		final double angleEnd1Start2   = Math.abs( v1.relativeBearing(vEnd1Start2) );
		final double angleEnd1End2     = Math.abs( v1.relativeBearing(vEnd1End2) );
		/* If all relative bearings point either forwards or backwards, the
		 * other segment must be entirely ahead or entirely behind this one,
		 * respectively. Therefore it cannot be parallel. Relative bearings of
		 * zero are ignored because they may carry the special meaning that
		 * their vectors have zero length (i. e. that the two segments share
		 * nodes). Since no specific bearing can be determined in that case,
		 * that specific bearing is ignored for this check, but all the other
		 * bearings are still considered.
		 */
		if ( angleStart1Start2 < Vector.RIGHT_ANGLE
				&& angleStart1End2 < Vector.RIGHT_ANGLE
				&& angleEnd1Start2 < Vector.RIGHT_ANGLE
				&& angleEnd1End2 < Vector.RIGHT_ANGLE
				|| (angleStart1Start2 > Vector.RIGHT_ANGLE || angleStart1Start2 == 0.0)
				&& (angleStart1End2 > Vector.RIGHT_ANGLE || angleStart1End2 == 0.0)
				&& (angleEnd1Start2 > Vector.RIGHT_ANGLE || angleEnd1Start2 == 0.0)
				&& (angleEnd1End2 > Vector.RIGHT_ANGLE || angleEnd1End2 == 0.0) ) {
			return false;
		}
		
		// ignore fragments crossing each other
		if ( Math.signum(v1.relativeBearing(starts)) != Math.signum(v1.relativeBearing(ends)) ) {
			return false;
		}
		
		if ((evaluateTags & 0x1) > 0) {
			// only evaluate fragments of the same highway class
			// (tag results are interned => == works on Strings)
			// :BUG: fails on dual carriageways having links etc. on _both_ sides
			if ( tags(part1).get("highway") != tags(part2).get("highway") ) {
				return false;
			}
		}
		if ((evaluateTags & 0x2) > 0) {
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
		
//		assert ! (part1 instanceof de.thaw.comb.SourceSegment && (((de.thaw.comb.SourceSegment)part1).fragments.size() > 0));
//		assert ! (part2 instanceof de.thaw.comb.SourceSegment && (((de.thaw.comb.SourceSegment)part2).fragments.size() > 0));
		
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
