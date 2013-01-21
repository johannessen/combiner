/* encoding UTF-8
 * 
 * Copyright (c) 2012-13 Arne Johannessen
 * 
 * This project and all of its individual parts may be used in accordance
 * with the terms of the 3-clause BSD licence. See LICENSE for details.
 */

package de.thaw.thesis.testbed;

import com.vividsolutions.jts.geom.LineString;
import com.vividsolutions.jts.operation.linemerge.LineMerger;

import java.util.Collection;


/**
 * Merge continuing line fragments into a single line.
 */
final class Merger {
	
	final Collection<LineString> lines;
	
	
	Merger (Collection<LineString> lines) {
		this.lines = lines;
	}
	
	
	@SuppressWarnings("unchecked")
	void mergeAndWriteTo (final String newPath) throws Exception {
		System.out.println(this.lines.size());
		
		final LineMerger lineMerger = new LineMerger();
		lineMerger.add(this.lines);
		final Collection<LineString> mergedLines = lineMerger.getMergedLineStrings();
		
		System.out.println("Lines formed: " + mergedLines.size());
//		System.out.println(mergedLines);
		
		new ShapeWriter(newPath).writeLines(mergedLines);
	}
	
}
