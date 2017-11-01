/* encoding UTF-8
 * 
 * Copyright (c) 2017 Arne Johannessen
 * 
 * This project and all of its individual parts may be used in accordance
 * with the terms of the 3-clause BSD licence. See LICENSE for details.
 */

package de.thaw.thesis.comb;


/**
 * A node defining a line string generalised by combination. This type inherits
 * everything from SourceNode because GeneralisedSections are AbstractLines,
 * which expect to be made out of SourceNodes. The only purpose of this class
 * is to avoid having to instantiate new objects called "source" at the end of
 * the process. That said, doing so would be less counter-intuitive as it may
 * seem, first and foremost because the output is expected to be in the same
 * data <em>format</em> as the input, but also because there actually are use
 * cases for re-using the output data for another run of the generalisation
 * algorithm, for example for instances of more than two parallel lines.
 * @see GeneralisedSection
 * @see NodeMatch#midPoint()
 */
public final class GeneralisedNode extends SourceNode {
	
	
	/**
	 * Creates a new generalised node at the position provided. The ID is set
	 * to a value defining a feature that doesn't yet exist in the source
	 * dataset.
	 * @param e easting
	 * @param n northing
	 */
	public GeneralisedNode (final double e, final double n) {
		super(e, n, Dataset.ID_NONEXISTENT);
	}
	
}
