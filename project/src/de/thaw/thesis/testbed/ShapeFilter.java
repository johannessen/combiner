/* encoding UTF-8
 * 
 * Copyright (c) 2012-13 Arne Johannessen
 * 
 * This project and all of its individual parts may be used in accordance
 * with the terms of the 3-clause BSD licence. See LICENSE for details.
 */

package de.thaw.thesis.testbed;

import java.io.File;
import java.net.URI;
import java.util.*;
import org.geotools.data.DataStore;
import org.geotools.data.DataStoreFinder;
import org.opengis.feature.simple.SimpleFeature;
import org.geotools.data.simple.SimpleFeatureSource;
import org.geotools.data.simple.SimpleFeatureCollection;
import org.geotools.data.simple.SimpleFeatureIterator;
import org.geotools.data.simple.SimpleFeatureSource;
import com.vividsolutions.jts.geom.GeometryCollection;
import com.vividsolutions.jts.geom.Geometry;
import com.vividsolutions.jts.geom.LineString;

final class ShapeFilter {
	
	ShapeFilter () throws Exception {
//		final File file = new File("a.shp/motorways.shp");
		final File file = new File("nrw-road/road.shp");
		this.read(file);
	}
	
	@SuppressWarnings("unchecked")
	void read (final File file) throws Exception {
		
		Map<String, Object> connect = new HashMap<String, Object>();
		connect.put("url", file.toURI().toURL());
		
		DataStore dataStore = DataStoreFinder.getDataStore(connect);
		String[] typeNames = dataStore.getTypeNames();
		String typeName = typeNames[0];
		
		System.out.println("Reading content " + typeName);
		
		SimpleFeatureSource featureSource = dataStore.getFeatureSource(typeName);
		SimpleFeatureCollection collection = featureSource.getFeatures();
		SimpleFeatureIterator iterator = collection.features();
		
		Collection<LineString> result = new LinkedList<LineString>();
		
		int i = 0;
		try {
			while (iterator.hasNext()) {
				i++;
				SimpleFeature feature = iterator.next();
				GeometryCollection sourceGeometry = (GeometryCollection)feature.getDefaultGeometry();  // possible bug
				for (int j = 0; j < sourceGeometry.getNumGeometries(); j++) {
					Geometry singleGeometry = sourceGeometry.getGeometryN(j);
					if (singleGeometry instanceof LineString) {
						result.add((LineString)singleGeometry);
					}
				}
			}
		}
		finally {
			System.out.println("Fetaures read: " + i);
			iterator.close();
		}
		System.out.println("Done reading.");
		
		
		
		
		
		
		File newFile = new File("b.shp");
		
		org.geotools.data.shapefile.ShapefileDataStoreFactory dataStoreFactory = new org.geotools.data.shapefile.ShapefileDataStoreFactory();
		Map<String, java.io.Serializable> params = new HashMap<String, java.io.Serializable>();
		params.put("url", newFile.toURI().toURL());
		params.put("create spatial index", Boolean.TRUE);
		org.geotools.data.shapefile.ShapefileDataStore newDataStore = (org.geotools.data.shapefile.ShapefileDataStore) dataStoreFactory.createNewDataStore(params);
/**/		final org.opengis.feature.simple.SimpleFeatureType TYPE = org.geotools.data.DataUtilities.createType("Location",
				"location:Point:srid=4326," + // <- the geometry attribute: Point type
						"name:String," + // <- a String attribute
						"number:Integer" // a number attribute
		);
		newDataStore.createSchema(TYPE);
		newDataStore.forceSchemaCRS(org.geotools.referencing.crs.DefaultGeographicCRS.WGS84);
		
		org.geotools.data.Transaction transaction = new org.geotools.data.DefaultTransaction("create");
		String typeName2 = newDataStore.getTypeNames()[0];
		SimpleFeatureSource featureSource2 = newDataStore.getFeatureSource(typeName2);
		if (featureSource2 instanceof org.geotools.data.simple.SimpleFeatureStore) {
			org.geotools.data.simple.SimpleFeatureStore featureStore2 = (org.geotools.data.simple.SimpleFeatureStore) featureSource2;
			SimpleFeatureCollection collection2 = new org.geotools.data.collection.ListFeatureCollection(TYPE, (List)result);
			featureStore2.setTransaction(transaction);
			try {
				featureStore2.addFeatures(collection);
				transaction.commit();
			}
			catch (Exception problem) {
				problem.printStackTrace();
				transaction.rollback();
			}
			finally {
				transaction.close();
			}
		}
		else {
			System.out.println(typeName + " does not support read/write access");
		}
		
		
		
		
		
		
/*
		org.opengis.feature.type.FeatureType ft = source.getSchema();
		
		FilterFactory ff = FilterFactory.createFilterFactory();
		LiteralExpression literal200 = ff.createLiteralExpression(200.0);
		LiteralExpression literal100 = ff.createLiteralExpression(100.0);
		AttributeExpression diExpression = ff.createAttributeExpression(ft, "DI");
		BetweenFilter betweenFilter = ff.createBetweenFilter();
		betweenFilter.addLeftValue(literal100);
		betweenFilter.addMiddleValue(diExpression);
		betweenFilter.addRightValue(literal200);
		FeatureResults fsFilteredShape = source.getFeatures(betweenFilter);
		
		org.geotools.data.FeatureReader filteredReader = collection.reader();
		
		DataStore newShapefileDataStore = new ShapefileDataStore(new File("b.shp").toURI().toURL());
		newShapefileDataStore.createSchema(ft);
		FeatureSource newFeatureSource = newShapefileDataStore.getFeatureSource(name);
		FeatureStore newFeatureStore = (FeatureStore)newFeatureSource;
		Transaction t = newFeatureStore.getTransaction();
		newFeatureStore.addFeatures(filteredReader);
		t.commit();
		t.close();
*/
		
//		return result;
	}
	
	public static void main (final String[] argv) throws Exception {
		new ShapeFilter();
	}
}

