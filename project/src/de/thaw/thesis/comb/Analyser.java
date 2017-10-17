/* encoding UTF-8
 * 
 * Copyright (c) 2013 Arne Johannessen
 * 
 * This project and all of its individual parts may be used in accordance
 * with the terms of the 3-clause BSD licence. See LICENSE for details.
 */

package de.thaw.thesis.comb;


/**
 * Evaluation functions, which analyse <code>Segment</code>s for parallelisms.
 */
public interface Analyser {
	
	
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
