/* encoding UTF-8
 * 
 * Copyright (c) 2013 Arne Johannessen
 * 
 * This project and all of its individual parts may be used in accordance
 * with the terms of the 3-clause BSD licence. See LICENSE for details.
 */

package de.thaw.thesis.comb;


/**
 * The interface through which OSM tags are retrieved from a tag store.
 */
public interface OsmTags {
	
	
	/**
	 * 
	 */
	static final String NO_VALUE = "";
	
	
	/**
	 * 
	 */
	String get (String key) ;
	
}



// HighwayType } neue Klassen mit Builder
// HighwayREf  }
// ^--- Leerzeichen vereinheitlichen
// ^--- static (?) method joinRef, um statistisch basiert den richtigen ref von mehreren ways zu ermitteln
// ^ singleton für null etc.
// ^ interning?

// test-driven design

/*
=> jeder way muss ein typ- und ein ref-objekt haben
=> erzeugen durch factory method o. ä., beim builden des ways
=> (später) interning möglich

=> erst leeres gerüst schreiben
=> dann tests schreiben
=> dann code schreiben
*/
