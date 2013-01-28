/* encoding UTF-8
 * 
 * Copyright (c) 2013 Arne Johannessen
 * 
 * This project and all of its individual parts may be used in accordance
 * with the terms of the 3-clause BSD licence. See LICENSE for details.
 */

package de.thaw.thesis.testbed;

import com.vividsolutions.jts.geom.Geometry;

import org.opengis.feature.simple.SimpleFeature;



/**
 * User Data class for JTS geometries.
 * <p>
 * JTS's <code>Geometry</code> objects may have a user data object associated
 * with them. This package uses the geometry user data to keep tabs on some of
 * the original attributes of the source data, and to keep a reference to the
 * particular source feature the <code>Geometry</code> was derived from during
 * processing.
 * <p>
 * For example, if the start- and end-points of a line are retrieved, these are
 * associated with a new <code>LineMeta</code> instance whose
 * {@link #parent} is set to the line. If the line is split into several
 * fragments, each of those is associated with a new <code>LineMeta</code>
 * instance whose {@link #parent} is set to the object representing the line
 * before it was split. Clients may then use {@link #origin(Geometry)} to
 * retrieve the original geometry. The {@link Analyser} and {@link Splitter}
 * classes make some use of this.
 */
final class LineMeta {
	
	
	/**
	 * A reference to the original feature object as read from a data source.
	 */
	final SimpleFeature feature;
	
	
	/**
	 * A feature related to <em>this</em> feature in some way. For example,
	 * this field may be used to identify a feature parallel to this one. The
	 * semantics of this field are entirely dependant upon the client; this
	 * class makes no assumptions about them.
	 */
//	Geometry related = null;
	
	
	/**
	 * 
	 */
	ParallelismFinder.ResultSet finderResults = null;
	
	
	/**
	 */
	LineMeta (final SimpleFeature feature) {
		this.feature = feature;
	}
	
	
	/**
	 * Retrieves the <code>LineMeta</code> instance associated with the
	 * passed geometry. If no user data is associated with it, or if the user
	 * data associated with the geometry is not a geometry meta object, a new
	 * run-time exception is thrown. If a <code>LinePartMeta</code> instance
	 * is passed, the <code>LineMeta</code> instance of its parent is returned.
	 * 
	 * @throws NullPointerException if <code>geometry.getUserData()</code> is
	 *  <code>null</code>
	 * @throws ClassCastException if <code>geometry.getUserData()</code> is
	 *  neither a <code>LineMeta</code> instance
	 *  nor a <code>LinePartMeta</code> instance
	 *  nor <code>null</code>
	 * @see Geometry#getUserData()
	 */
	static LineMeta getFrom (final Geometry geometry) {
		Object userData = geometry.getUserData();
		if (userData == null) {
			throw new NullPointerException("No LineMeta associated with the geometry that was provided");
		}
//		if (userData instanceof LinePartMeta) {
//			return LineMeta.getFrom( LinePartMeta.origin(geometry) );
//		}
		if (! (userData instanceof LineMeta)) {
			throw new ClassCastException("Geometry userData '" + geometry.getUserData().getClass() + "' is not LineMeta");
		}
		return (LineMeta)userData;
	}
	
	
	/**
	 * Creates a short string identifying a feature. For OSM data, this would
	 * be the OSM ID. However, the OSM ID (even when only considered for one
	 * feature type, e. g. ways) may not be unique in pre-processed OSM data.
	 * For example, OSM ways may have been split into two or more segments
	 * during a topology normalisation, resulting in several LineStrings with
	 * the same OSM way ID.
	 * 
	 * @return A string describing the feature such that it can be identified.
	 *  The description may not be unique.
	 */
	String featureId () {
		if (this.feature == null) {
			return "null";
		}
		Object id;
		
		// try for OSM ID in Geofabrik data first (which uses the "osm_id" key)
		id = feature.getAttribute(ShapeReader.OSM_ID_ATTRIBUTE);
		if (id != null) {
			return id.toString();
		}
		
		// the shape-test datasets use "id" for a simple number
		id = feature.getAttribute("id");
		if (id != null) {
			return id.toString();
		}
		
		// last resort: use the OpenGIS-provided feature ID string (which is
		// dependant upon the implementation of the Shapefile access)
		return feature.getID();
	}
	
	
	/**
	 * Calls {@link #description()} on the passed geometry's
	 * <code>LineMeta</code> instance, and returns the result.
	 * <p>
	 * Calling this method will always yield a printable String.
	 * 
	 * @see #getFrom(Geometry)
	 */
	static String description (final Geometry geometry) {
		
//		if (geometry.getUserData() instanceof LinePartMeta) {
//			return LinePartMeta.getFrom(geometry).description();
//		}
		
		return LineMeta.getFrom(geometry).featureId();
	}
	
	
	/**
	 * Returns a printable String representation of the {@link #description}.
	 * 
	 * @return {@link #description} or <code>"null"</code>, iff
	 *  <code>{@link #description} == null</code>
	 */
	String description () {
		return this.featureId();
	}
	
	
	/**
	 * @return {@link #description()}
	 */
	public String toString () {
		return this.featureId();
	}
	
}
