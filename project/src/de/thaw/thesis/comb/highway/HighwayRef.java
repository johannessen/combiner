/* encoding UTF-8
 * 
 * Copyright (c) 2013 Arne Johannessen
 * 
 * This project and all of its individual parts may be used in accordance
 * with the terms of the 3-clause BSD licence. See LICENSE for details.
 */

package de.thaw.thesis.comb.highway;


// :TODO:
// ^--- Leerzeichen vereinheitlichen
// ^--- static (?) method joinRef, um statistisch basiert den richtigen ref von mehreren ways zu ermitteln

public class HighwayRef implements Comparable<HighwayRef> {
	
	private static final String NO_VALUE = "";
	
	private final String ref;
	
	
	private HighwayRef (final String ref) {
		this.ref = ref;
	}
	
	
	public static HighwayRef valueOf (final String name) {
		return new HighwayRef(name.intern());
		/* Memory cost ist still more of a problem for us than time cost. Since
		 * there is most likely a high number of ways with the same ref, string
		 * interning will have a measurable impact on memory use. However, we
		 * don't want to make any guarantees; interning should preferably be an
		 * implementation detail (for now).
		 */
	}
	
	
	public boolean isEmpty () {
		return ref.length() == 0;
	}
	
	
	// we don't really care about the order; we just want it to be well-defined
	public int compareTo (HighwayRef other) {
		return this.ref.compareTo( other.ref );
	}
	
	
	public boolean equals (Object object) {
		if (object instanceof HighwayRef) {
			return ref.equals( ((HighwayRef)object).ref );
		}
		if (object != null) {
			return object.equals(ref);
		}
		return false;
	}
	
	
	public int hashCode () {
		return ref.hashCode();
	}
	
	
	public String toString () {
		return ref;
	}
	
}
