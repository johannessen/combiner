/* encoding UTF-8
 * 
 * Copyright (c) 2012 Arne Johannessen
 * 
 * This project and all of its individual parts may be used in accordance
 * with the terms of a BSD-style license. See LICENSE for details.
 */

package de.thaw.espebu;

import com.vividsolutions.jts.geom.Geometry;



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
 * associated with a new <code>GeometryMeta</code> instance whose
 * {@link #parent} is set to the line. If the line is split into several
 * fragments, each of those is associated with a new <code>GeometryMeta</code>
 * instance whose {@link #parent} is set to the object representing the line
 * before it was split. Clients may then use {@link #origin(Geometry)} to
 * retrieve the original geometry. The {@link Analyser} and {@link Splitter}
 * classes make some use of this.
 */
final class GeometryMeta {
	
	
	/**
	 * The feature from which <em>this</em> feature was derived (if any).
	 */
	final Geometry parent;
	
	
	/**
	 * A textual description of this feature and/or some of its attributes.
	 */
	final String description;
	
	
//	int index = -1;
	
	
	/**
	 */
	GeometryMeta (final Geometry parent, final String description) {
		this.parent = parent;
		this.description = description;
	}
	
	
	/**
	 * Creates a new <code>GeometryMeta</code> association.
	 * 
	 * @param geometry the geometry with which to associate the new meta data
	 * @param parent the feature from which <code>geometry</code> was derived
	 * @param description a description of <code>geometry</code>
	 * @see Geometry#setUserData(Object)
	 */
	static void set (final Geometry geometry, final Geometry parent, final String description) {
		geometry.setUserData( new GeometryMeta(parent, description) );
	}
	
	
	/**
	 * Retrieves the <code>GeometryMeta</code> instance associated with the
	 * passed geometry. If no user data is associated with it, or if the user
	 * data associated with the geometry is a String, a new
	 * <code>GeometryMeta</code> instance is created, associated and returned,
	 * ready to use. The String (if any) is used as the {@link #description} of
	 * the new <code>GeometryMeta</code> instance.
	 * 
	 * @throws ClassCastException if <code>geometry.getUserData()</code> is
	 *  neither a <code>GeometryMeta</code> instance
	 *  nor a <code>String</code>
	 *  nor <code>null</code>
	 * @see Geometry#getUserData()
	 */
	static GeometryMeta getFrom (final Geometry geometry) {
		Object userData = geometry.getUserData();
		if (userData == null) {
			System.err.println("initialising Geometry userData in GeometryMeta.getFrom");
			userData = new GeometryMeta(null, null);
			geometry.setUserData(userData);
		}
		if (userData instanceof String) {
			userData = new GeometryMeta(null, (String)userData);
			geometry.setUserData(userData);
		}
		if (! (userData instanceof GeometryMeta)) {
			throw new ClassCastException("Geometry userData '" + geometry.getUserData().getClass() + "' is not GeometryMeta");
		}
		return (GeometryMeta)userData;
	}
	
	
	/**
	 * Recursively retrieves the highest-order non-null {@link #parent} of the
	 * passed geometry's <code>GeometryMeta</code> instance.
	 */
	static Geometry origin (final Geometry geometry) {
		final Geometry parent = GeometryMeta.getFrom(geometry).parent;
		if (parent == null) {
			return geometry;
		}
		return GeometryMeta.origin(parent);
	}
	
	
	/**
	 * Calls {@link #description()} on the passed geometry's
	 * <code>GeometryMeta</code> instance, and returns the result.
	 * <p>
	 * Calling this method will always yield a printable String.
	 * 
	 * @see #getFrom(Geometry)
	 */
	static String description (final Geometry geometry) {
		return GeometryMeta.getFrom(geometry).description();
	}
	
	
	/**
	 * Returns a printable String representation of the {@link #description}.
	 * 
	 * @return {@link #description} or <code>"null"</code>, iff
	 *  <code>{@link #description} == null</code>
	 */
	String description () {
		if (this.description == null) {
			return "null";
		}
		return this.description;
	}
	
	
	/**
	 * @return {@link #description()}
	 */
	public String toString () {
		return this.description();
	}
	
}
