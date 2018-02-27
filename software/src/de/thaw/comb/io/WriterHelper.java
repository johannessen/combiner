/* encoding UTF-8
 * 
 * Copyright (c) 2013 Arne Johannessen
 * 
 * This project and all of its individual parts may be used in accordance
 * with the terms of the 3-clause BSD licence. See LICENSE for details.
 */

package de.thaw.comb.io;

import de.thaw.comb.util.SpatialFeature;

import org.opengis.feature.simple.SimpleFeatureType;
import org.opengis.feature.type.GeometryDescriptor;
import org.geotools.feature.SchemaException;
import org.geotools.data.DataUtilities;
import java.util.List;
import com.vividsolutions.jts.geom.Geometry;



/**
 * Metadata definitions for use by the file output classes. This class is meant
 * to be extended such that the {@link #defineSchema()} method configures the
 * instance's fields appropriately and the {@link #attributes(SpatialFeature)}
 * method returns the list of attribute values.
 */
public abstract class WriterHelper implements ShapeWriterDelegate {
	
	protected List<AttributeDefinition> schema = null;
	
	protected String geometryType = null;
	
	protected String typeName = null;
	
	protected int epsgCode = Projection.INTERNAL_EPSG_CODE;
	
	
	/**
	 * Calls {@link #defineSchema()}.
	 */
	public WriterHelper () {
		defineSchema();
	}
	
	
	public abstract void defineSchema () ;
	
	
	public abstract List attributes (final SpatialFeature feature) ;
	
	
	/**
	 * Returns a definition of the feature type and the attributes to be
	 * written to the Shapefile.
	 * 
	 * @param geometryDescriptor a class providing the magic constant used by
	 *  GeoTools to identify the geometry column in the Shapefile
	 * @see org.geotools.data.DataUtilities.createType
	 */
	public SimpleFeatureType featureType (final GeometryDescriptor geometryDescriptor) throws SchemaException {
		if (geometryType == null) {
			throw new IllegalStateException();
		}
		if (typeName == null) {
			typeName = Integer.toHexString(hashCode());
		}
		String typeDefinition = geometryDescriptor.getName() + ":" + geometryType + ":srid=" + epsgCode;
		for (final AttributeDefinition attribute : schema) {
			typeDefinition += "," + attribute;
		}
		return DataUtilities.createType( typeName, typeDefinition );
		
		// :BUG: may not work with 64 bit OSM node IDs
	}
	
	
	/**
	 * Returns an ordered list of the attributes of a particular feature to be
	 * added to the Shapefile. The list is expected to be in the same order as
	 * required by the {@link #featureType} definition.
	 * 
	 * @param geometry the feature the attributes of which to return
	 * @see Geometry#getUserData
	 */
	public List attributes (final Geometry geometry) {
		Object userData = geometry.getUserData();
		if ( ! (userData instanceof SpatialFeature) ) {
			throw new ClassCastException();
		}
		return attributes( (SpatialFeature)userData );
	}
	
	
	/**
	 * Name and type of an attribute.
	 */
	protected static final class AttributeDefinition {
		final String name;
		final Class type;
		public AttributeDefinition (final String name, final Class type) {
			this.name = name;
			this.type = type;
		}
		public String toString () {
			return name + ":" + type.getSimpleName();
		}
	}
	
}
