/* encoding UTF-8
 * 
 * Copyright (c) 2012 Arne Johannessen
 * 
 * This project and all of its individual parts may be used in accordance
 * with the terms of a BSD-style license. See LICENSE for details.
 */

package de.thaw.espebu;

import org.opengis.feature.simple.SimpleFeatureType;
import org.geotools.feature.SchemaException;
import java.util.List;
import com.vividsolutions.jts.geom.Geometry;


/*
 * Defines feature type and feature attributes of a <code>ShapeWriter</code>.
 * An instance of an implementing class may be passed to a
 * <code>ShapeWriter</code>, which will then delegate any decisions about what
 * type of features to create a Shapefile for and which attributes that
 * Shapefile should have back to this instance.
 */
interface ShapeWriterDelegate {
	
	
	/**
	 * Returns a definition of the feature type and the attributes to be
	 * written to the Shapefile.
	 * 
	 * @see org.geotools.data.DataUtilities.createType
	 */
	SimpleFeatureType featureType () throws SchemaException ;
	
	
	/**
	 * Returns an ordered list of the attributes of a particular feature to be
	 * added to the Shapefile. The list is expected to be in the same order as
	 * required by the {@link #featureType} definition.
	 * 
	 * @param geometry the feature the attributes of which to return
	 * @see GeometryMeta
	 * @see Geometry#getUserData
	 */
	List attributes (Geometry geometry) ;
	
}