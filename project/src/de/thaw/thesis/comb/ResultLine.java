/* encoding UTF-8
 * 
 * Copyright (c) 2017 Arne Johannessen
 * 
 * This project and all of its individual parts may be used in accordance
 * with the terms of the 3-clause BSD licence. See LICENSE for details.
 */

package de.thaw.thesis.comb;

import de.thaw.thesis.comb.highway.HighwayRef;
import de.thaw.thesis.comb.util.AttributeProvider;
import de.thaw.thesis.comb.util.Vector;

import java.util.AbstractSequentialList;
import java.util.Collections;
import java.util.Collection;
import java.util.LinkedList;
import java.util.ListIterator;



/**
 * A contiguous line in the Combiner's result.
 */
public abstract class ResultLine extends AbstractLine {
	
	
	protected boolean valid = false;
	
	
	protected ResultLine () {
	}
	
	
	/**
	 * @return <code>Dataset.ID_NONEXISTENT</code>
	 */
	public long id () {
		return Dataset.ID_NONEXISTENT;
	}
	
	
	public boolean valid () {
		return valid;
	}
	
	
/* 
	void filterShortSection () ;  // abstract method
*/	
	
	
	/**
	 * The geometric length of this line. Calculated by summing the length of
	 * each segment.
	 * @return the length in internal map units
	 */
	double length () {
		double length = 0.0;
		for (final Vector segment : this) {
			length += segment.distance();
		}
		return length;
	}
	
	
	/**
	 * Fix topology after generalisation. Moves nodes such that line ends are
	 * no longer dangling after generalisation, but end on the same lines they
	 * ended on before generalisation. This is necessary because nodes are
	 * implemented as immutable by this project. <p>
	 * 
	 * Note that this method is possibly a misnomer: The nodes relocated are
	 * those that were previously <em>affected</em> by a generalisation process
	 * insofar as some of the lines that were using these nodes have been
	 * generalised. Nodes <em>created</em> in the generalisation process are
	 * not relocated.
	 */
	protected abstract void relocateGeneralisedNodes () ;
	
	
	/**
	 * Provides attributes to users of the Combiner's result.
	 */
	protected static class Tags implements AttributeProvider {
		
		private final String highway;
		
		private final String ref;
		
		protected Tags (final String highway, final String ref) {
			this.highway = highway != null ? highway.intern() : AttributeProvider.NO_VALUE;
			this.ref = (ref != null && ref.length() > 0) ? ref.intern() : AttributeProvider.NO_VALUE;
		}
		
		public String get (final String key) {
			if (key.equals("highway")) {
				return highway;
			}
			if (key.equals("ref")) {
				return ref;
			}
			return AttributeProvider.NO_VALUE;
		}
		
	}
	
}


