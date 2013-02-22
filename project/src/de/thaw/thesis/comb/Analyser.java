/* encoding UTF-8
 * 
 * Copyright (c) 2013 Arne Johannessen
 * 
 * This project and all of its individual parts may be used in accordance
 * with the terms of the 3-clause BSD licence. See LICENSE for details.
 */

package de.thaw.thesis.comb;


/**
 * Evaluation functions, which analyse <code>LinePart</code>s for parallelisms.
 */
public interface Analyser {
	
	
	/**
	 * 
	 */
	boolean shouldEvaluate (LinePart currentPart, LinePart otherPart) ;
	
	
	/**
	 * 
	 */
	double evaluateLeft (LinePart currentPart, LinePart otherPart) ;
	
	
	/**
	 * 
	 */
	double evaluateRight (LinePart currentPart, LinePart otherPart) ;
	
	
	/**
	 * 
	 */
	int compare (double value, double bestValue) ;
	
	
	/**
	 * 
	 */
	double worstResult () ;
	
}
