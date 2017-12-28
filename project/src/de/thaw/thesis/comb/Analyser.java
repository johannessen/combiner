/* encoding UTF-8
 * 
 * Copyright (c) 2013 Arne Johannessen
 * 
 * This project and all of its individual parts may be used in accordance
 * with the terms of the 3-clause BSD licence. See LICENSE for details.
 */

package de.thaw.thesis.comb;


/**
 * Provides special-cased knowledge about generalising segments by combining.
 * Clients must provide implementations of this interface that are tuned to
 * the specific situation in which the generalisation is to be applied (for
 * example, multi-track rail lines or dual carriageways). This interface is
 * primarily used during the analysis for parallelisms.
 */
public interface Analyser {
	
	
	/* Yet to be included here as methods:
	 * µ = AbstractSegment.MIN_FRAGMENT_LENGTH
	 * η = SourceSegment.INDEX_ENVELOPE_MARGIN
	 * PARALLEL angle = SourceSegment.PARALLEL_ANGLE_MAXIMUM
	 * PARALLEL distance = HighwayAnalyser.MAX_DISTANCE
	 * the tag combination routines in *Section
	 */
	
	
	/**
	 * 
	 */
	boolean shouldEvaluate (Segment currentPart, Segment otherPart) ;
	
	
	/**
	 * The geometric distance between two segments (D<small>ISTANZ</small>).
	 * How the distance is determined may depend upon the use case. For
	 * example, in one context the distance between two segments might be
	 * best defined as the distance of their mid points, whereas other contexts
	 * may require it to be defined as the average distance of their respective
	 * endpoints.
	 * 
	 * @return an ordinal scaled value representing the distance between the
	 *  two segments <code>s1</code> and <code>s2</code>
	 * @throws NullPointerException if <code>s1</code> or <code>s2</code> are
	 *  <code>null</code>
	 */
	double distance (Segment s1, Segment s2) ;
	
	
	/**
	 * 
	 */
	double evaluateLeft (Segment currentPart, Segment otherPart) ;
	
	
	/**
	 * 
	 */
	double evaluateRight (Segment currentPart, Segment otherPart) ;
	
	
	/**
	 * 
	 */
	int compare (double value, double bestValue) ;
	
	
	/**
	 * 
	 */
	double worstResult () ;
	
}
