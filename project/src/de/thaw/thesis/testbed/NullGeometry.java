package de.thaw.thesis.testbed;

import com.vividsolutions.jts.geom.GeometryFactory;
import com.vividsolutions.jts.geom.LineString;
import com.vividsolutions.jts.geom.Geometry;
import com.vividsolutions.jts.geom.CoordinateFilter;
import com.vividsolutions.jts.geom.CoordinateSequenceFilter;
import com.vividsolutions.jts.geom.GeometryFilter;
import com.vividsolutions.jts.geom.CoordinateSequenceComparator;
import com.vividsolutions.jts.geom.GeometryComponentFilter;
import com.vividsolutions.jts.geom.Envelope;
import com.vividsolutions.jts.geom.Coordinate;


final class NullGeometry extends LineString {
	
	public NullGeometry () {
		super(null, new GeometryFactory());
		super.setUserData(new LinePartMeta(null, "null"));
	}
	
	public void apply (CoordinateFilter filter) {
		throw new UnsupportedOperationException();
	}
	
	public void apply (CoordinateSequenceFilter filter) {
		throw new UnsupportedOperationException();
	}
	
	public void apply (GeometryFilter filter) {
		throw new UnsupportedOperationException();
	}
	
	public void apply (GeometryComponentFilter filter) {
		throw new UnsupportedOperationException();
	}
	
	protected int compareToSameClass (Object o) {
		throw new UnsupportedOperationException();
	}
	
	protected int compareToSameClass (Object o, CoordinateSequenceComparator comp) {
		throw new UnsupportedOperationException();
	}
	
	protected Envelope computeEnvelopeInternal () {
		throw new UnsupportedOperationException();
	}
	
	public boolean equalsExact (Geometry other, double tolerance) {
		throw new UnsupportedOperationException();
	}
	
	public Geometry getBoundary () {
		throw new UnsupportedOperationException();
	}
	
	public int getBoundaryDimension () {
		throw new UnsupportedOperationException();
	}
	
	public int getDimension () {
		throw new UnsupportedOperationException();
	}
	
	public Coordinate getCoordinate () {
		throw new UnsupportedOperationException();
	}
	
	public Coordinate[] getCoordinates () {
		throw new UnsupportedOperationException();
	}
	
	public String getGeometryType () {
		throw new UnsupportedOperationException();
	}
	
	public int getNumPoints () {
		throw new UnsupportedOperationException();
	}
	
	public boolean isEmpty () {
		return super.isEmpty();
	}
	
	public void normalize () {
		throw new UnsupportedOperationException();
	}
	
	public Geometry reverse () {
		throw new UnsupportedOperationException();
	}
	
}
