/* encoding UTF-8
 * 
 * Copyright (c) 2013 Arne Johannessen
 * 
 * This project and all of its individual parts may be used in accordance
 * with the terms of the 3-clause BSD licence. See LICENSE for details.
 */

package de.thaw.thesis.testbed;

import java.util.Collection;
import java.util.LinkedList;

import com.vividsolutions.jts.geom.Geometry;
import com.vividsolutions.jts.operation.buffer.BufferOp;
import com.vividsolutions.jts.operation.buffer.BufferParameters;


/**
 * Parallelism analyser. Determines which pairs of lines that are
 * <em>suspected</em> to be parallel to each other are in fact not.
 * <p>
 * Works by creating a buffer zone on both sides of the one line of the pair
 * and checking wheter the other line intersects that buffer. Since this class
 * depends on the earlier application of the <code>Analyser</code> class to
 * find these likely pairs in the first place, it needs to handle the
 * <code>Analyser</code>'s way of marking multiple lines as possible parallels.
 * Therefore the internals of this class depend heavily on those of the
 * <code>Analyser</code>.
 * @see Analayser
 */
final class ParallelDisprover {
	
	
	/**
	 * The input lines for this analysis.
	 */
	final Collection<Analyser.ResultSet> analyserResults;
	
	
	/**
	 * Whether or not to remove from the result set those parallelisms which
	 * this class determines to be not actually parallel to the line under
	 * consideration.
	 * <p>
	 * Directly removing the line from the results has side-effects
	 * for the client and slightly changes the end result. In
	 * particular, it may lead to the result collections having
	 * inconsistent states, which unchecked would lead to
	 * NoSuchElementExceptions. The client needs to check for that.
	 */
	boolean removeDisproven = false;
	
	
	/**
	 * "Buffer distance".
	 * @see BufferOp#getResultGeometry(double)
	 */
	double bufferDistance = 40.0;
	
	
	/**
	 * @param analyserResults a collection containing the results of the
	 *  initial parallelism analysis.
	 */
	ParallelDisprover (Collection<Analyser.ResultSet> analyserResults) {
		this.analyserResults = analyserResults;
	}
	
	
	/**
	 * Runs the analysis. This method attempts to disprove possible parallels
	 * found by the <code>Analyser</code> by checking their geometric
	 * location. If the lines identfied as possible parallels are not on either
	 * side of the line, but rather behind it or ahead of it, it means that
	 * there apparently is no parallel line and the <code>Analyser</code>'s
	 * result is wrong. The result sets are then marked as such by means
	 * of{@link Analyser.ResultSet#parallismsInBuffer} and also removed from
	 * the result collection iff {@link #remvoeDisproven} is <code>true</code>.
	 */
	void analyse () {
		
		/* The buffer caps need to be flat (yielding a rectangular polygon) to
		 * prevent the analysis from considering e. g. lines which are the
		 * natural continuation of the line under consideration. This method
		 * intends to specifically filter those lines, so this detail is a
		 * crucial one.
		 */
		final BufferParameters params = new BufferParameters(0, BufferParameters.CAP_FLAT);
		
		for (final Analyser.ResultSet result : analyserResults) {
			
			// collect all disproven parallelisms
			final LinkedList<Analyser.Parallelism> disproven = new LinkedList<Analyser.Parallelism>();
			
			// we assume that the other lines are in fact all parallel, then check if that's true
			result.parallelismsInBuffer = true;
			
			for (final Analyser.Parallelism parallelism : result.parallelisms) {
				
				final Geometry theLine = result.line;
				final Geometry aLine = parallelism.origin;
				
				final BufferOp operation = new BufferOp(theLine, params);
				final Geometry buffer = operation.getResultGeometry(this.bufferDistance);
				
				/* If any of the lines identified as possible parallels do not
				 * intersect the original line's buffer, this is a problem case.
				 */
//				final boolean disjoint = buffer.disjoint(aLine);
				
				/* The BufferOp sometimes diagnoses a line touching (ending on)
				 * the rim of the buffer as being an intersection, but that's
				 * no good. Checking whether the intersection actually has more
				 * than just one point works for us.
				 */
				Geometry intersection = buffer.intersection(aLine);
				final boolean intersectionEmpty = intersection.getNumPoints() < 2;
				
				result.parallelismsInBuffer &= ! intersectionEmpty;
				
				// remove the disproven parallelism from the result set
				if (intersectionEmpty && this.removeDisproven) {
					disproven.add(parallelism);
				}
			}
			if (this.removeDisproven) {
				result.parallelisms.removeAll(disproven);
			}
		}
		
	}
	
	
/*
	// simple disprover
	
	void analyse () {
		final BufferParameters params = new BufferParameters(0, BufferParameters.CAP_FLAT);
		
		for (final Analyser.ResultSet result : analyserResults) {
			
			result.parallelismsInBuffer = true;
			
			for (final Analyser.Parallelism parallelism : result.parallelisms) {
				
				final LineString theLine = result.line;
				final Geometry aLine = parallelism.origin;
				
				final BufferOp operation = new BufferOp(theLine, params);
				final Geometry buffer = operation.getResultGeometry(30.0);
				final boolean disjoint = buffer.disjoint(aLine);
				
				result.parallelismsInBuffer &= ! disjoint;
				
			}
		}
		
	}
*/
}
