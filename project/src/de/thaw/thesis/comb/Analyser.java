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
	 * PARALLEL angle = SourceSegment.PARALLEL_ANGLE_MAXIMUM / HighwayAnalyser.COLLINEAR_MAX_ANGLE
	 * PARALLEL distance = HighwayAnalyser.MAX_DISTANCE
	 * DISTANZ = HighwayAnalyser.evaluate()
	 * the tag combination routines in *Section
	 */
	
	
	/**
	 * 
	 */
	boolean shouldEvaluate (Segment currentPart, Segment otherPart) ;
	
	
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
