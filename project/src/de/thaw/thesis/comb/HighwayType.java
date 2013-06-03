/* encoding UTF-8
 * 
 * Copyright (c) 2013 Arne Johannessen
 * 
 * This project and all of its individual parts may be used in accordance
 * with the terms of the 3-clause BSD licence. See LICENSE for details.
 */

package de.thaw.thesis.comb;


final class HighwayType implements Comparable<HighwayType> {
	
	private final String name;
	
	private final int level;
	
	private static final HighwayType DEFAULT;
	
	private static final HighwayType[] TYPES;
	
	static {
		String[] typeNames = new String[]{
				"motorway",
//				"motorway_link",
				"trunk",
//				"trunk_link",
				"primary",
//				"primary_link",
				"secondary",
//				"secondary_link",
				"tertiary",
//				"tertiary_link",
				"unclassified" ,
/*
				"unclassified_link",
				"residential",
				"residential_link",
				"service",
				"road",
				"track",
				"cycleway",
				"bridleway",
				"footway",
				"path",
*/
				"" };
		
		HighwayType[] types = new HighwayType[typeNames.length];
		for (int i = 0; i < types.length; i++) {
			types[i] = new HighwayType(typeNames[i], i);
		}
		
		DEFAULT = types[types.length - 1];
		
		TYPES = types;
	}
	
	
	private HighwayType (final String name, final int level) {
		this.name = name;
		this.level = level;
	}
	
	
	static HighwayType valueOf (final String name) {
		final String nameIntern = name.intern();
		
		for (int i = 0; i < TYPES.length; i++) {
			if (TYPES[i].name == nameIntern) {
				return TYPES[i];
			}
		}
		
		// type doesn't exist (in our pre-defined list): return "" instance
		return DEFAULT;
	}
	
	
	String name () {
		return name;
	}
	
	
	public String toString () {
		return name == "" ? "[unknown highway type]" : name;
	}
	
	
	public int compareTo (HighwayType other) {
		return other.level - this.level;
	}
	
	
	public boolean equals (HighwayType other) {
		return this == other;
	}
	
	// :TODO: hashCode
	
}
