/* encoding UTF-8
 * 
 * Copyright (c) 2013 Arne Johannessen
 * 
 * This project and all of its individual parts may be used in accordance
 * with the terms of the 3-clause BSD licence. See LICENSE for details.
 */

package de.thaw.thesis.testbed;

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
final class LinePartMeta extends GeometryMeta {
	
	
	/**
	 * 
	 */
	ParallelismFinder.ResultSet finderResults = null;
	
	
	/**
	 */
	LinePartMeta (final Geometry parent, final String description) {
		super(parent, description);
	}
	
	
	/**
	 * Creates a new <code>LinePartMeta</code> association.
	 * 
	 * @param geometry the geometry with which to associate the new meta data
	 * @param parent the feature from which <code>geometry</code> was derived
	 * @param description a description of <code>geometry</code>
	 * @see GeometryMeta#set(Geometry,Geometry,String)
	 */
	static void set (final Geometry geometry, final Geometry parent, final String description) {
//		throw new UnsupportedOperationException();
		geometry.setUserData( new LinePartMeta(parent, description) );
	}
	
	
	/**
	 * Retrieves the <code>LinePartMeta</code> instance associated with the
	 * passed geometry. If no user data is associated with it, or if the user
	 * data associated with the geometry is a String, a new
	 * <code>GeometryMeta</code> instance is created, associated and returned,
	 * ready to use. The String (if any) is used as the {@link #description} of
	 * the new <code>GeometryMeta</code> instance.
	 * 
	 * @throws ClassCastException if <code>geometry.getUserData()</code> is
	 *  not a <code>LinePartMeta</code> instance
	 * @throws NullPointerException if <code>geometry.getUserData()</code> is
	 *  <code>null</code>
	 * @see GeometryMeta#getFrom(Geometry)
	 */
	static LinePartMeta getFrom (final Geometry geometry) {
		Object userData = geometry.getUserData();
		if (userData == null) {
			throw new NullPointerException("No LinePartMeta associated with the geometry that was provided");
		}
		if (! (userData instanceof LinePartMeta)) {
			throw new ClassCastException("Geometry userData '" + geometry.getUserData().getClass() + "' is not LinePartMeta");
		}
		return (LinePartMeta)userData;
	}
	
	
	/**
	 * Recursively retrieves the highest-order non-null {@link #parent} of the
	 * passed geometry's <code>LinePartMeta</code> instance.
	 */
	static Geometry origin (final Geometry geometry) {
		final Geometry parent = LinePartMeta.getFrom(geometry).parent;
		final Object userData = parent.getUserData();
		if (userData == null || userData instanceof LineMeta) {
			return parent;
		}
		return LinePartMeta.origin(parent);
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
		return LinePartMeta.getFrom(geometry).description();
	}
	
}
