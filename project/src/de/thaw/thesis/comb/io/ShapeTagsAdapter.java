/* encoding UTF-8
 * 
 * Copyright (c) 2013 Arne Johannessen
 * 
 * This project and all of its individual parts may be used in accordance
 * with the terms of the 3-clause BSD licence. See LICENSE for details.
 */

package de.thaw.thesis.comb.io;

import de.thaw.thesis.comb.OsmTags;

import org.opengis.feature.simple.SimpleFeature;


/**
 * Adapter to the attributes provided by the <code>ShapeReader</code> as OSM tags.
 */
final class ShapeTagsAdapter implements OsmTags {
	
	final SimpleFeature feature;
	
	
	ShapeTagsAdapter (final SimpleFeature feature) {
		this.feature = feature;
	}
	
	
	private String rewriteKey (final String osmKey) {
		if (osmKey == "highway") {
			return "fclass";
		}
		if (osmKey == "highway") {
			return "fclass";
		}
		return osmKey;
	}
	
	
	private String rewriteValue (final String osmKey, final String value) {
		if (osmKey == "oneway" || osmKey == "bridge" || osmKey == "tunnel") {
			// boolean key
			if (value == "T") {
				return "yes";
			}
			if (value == "F") {
				return "no";
			}
		}
		if (osmKey == "highway") {
			if (osmKey == "primary") {
				return "x3_primary";
			}
			if (osmKey == "primary_link") {
				return "x3_primary_link";
			}
			if (osmKey == "secondary") {
				return "x4_secondary";
			}
			if (osmKey == "secondary_link") {
				return "x4_secondary_link";
			}
			if (osmKey == "tertiary") {
				return "x5_tertiary";
			}
			if (osmKey == "tertiary_link") {
				return "x5_tertiary_link";
			}
			if (osmKey == "unclassified") {
				return "x6_unclassified";
			}
			if (osmKey == "unclassified_link") {
				return "x6_unclassified_link";
			}
			if (osmKey == "residential") {
				return "x7_residential";
			}
			if (osmKey == "residential_link") {
				return "x7_residential_link";
			}
		}
		return value;
	}
	
	
	public String get (final String key) {
		Object attrValue = feature.getAttribute(rewriteKey(key.intern()));
		if (attrValue == null) {
			return OsmTags.NO_VALUE;
		}
		String stringValue = rewriteValue(key, attrValue.toString().intern());
		return stringValue.intern();
	}
	
}
