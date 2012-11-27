/* encoding UTF-8
 * 
 * Copyright (c) 2012 Arne Johannessen
 * 
 * This project and all of its individual parts may be used in accordance
 * with the terms of a BSD-style license. See LICENSE for details.
 */

package de.thaw.espebu;

import com.vividsolutions.jts.geom.LineString;
import com.vividsolutions.jts.io.WKTFileReader;
import com.vividsolutions.jts.io.WKTReader;

import java.io.File;
import java.util.Collection;
import java.util.Iterator;

import com.vividsolutions.jts.io.ParseException;
import java.io.IOException;


/**
 * Reads linestrings from Well-Known Text (WKT) from plain text files. The
 * input files are expected to be referenced to the internal CRS; no
 * transformation of any kind takes places in this class.
 */
final class WktReader {
	
	
	/**
	 * @param file the Well-Known Text data file
	 * @return all linestrings that could be read from <code>file</code>
	 * @see WKTFileReader#read()
	 */
	@SuppressWarnings("unchecked")
	Collection<LineString> readFrom (final File file) throws IOException, ParseException {
		final WKTFileReader fileReader = new WKTFileReader(file, new WKTReader());
		final Collection features = fileReader.read();
		
		final Iterator iterator = features.iterator();
		while (iterator.hasNext()) {
			if ( ! (iterator.next() instanceof LineString) ) {
				iterator.remove();
			}
		}
		// at this point we only have LineString instances left in the collection
		
		// yes, there's a better way to do this: com.vividsolutions.jts.geom.util.LineStringExtracter
		
		return (Collection<LineString>)features;
	}
	
}

