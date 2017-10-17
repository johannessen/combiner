/* encoding UTF-8
 * 
 * Copyright (c) 2013 Arne Johannessen
 * 
 * This project and all of its individual parts may be used in accordance
 * with the terms of the 3-clause BSD licence. See LICENSE for details.
 */

package de.thaw.thesis.comb;

import de.thaw.thesis.comb.util.Vector;

import com.vividsolutions.jts.geom.Envelope;

import java.util.Collection;
import java.util.LinkedHashSet;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.Set;


// ex LineSegment
/**
 * A <code>Segment</code> implementation representing segments as read from
 * the source data.
 * @see AbstractSegment
 */
public final class SourceSegment extends AbstractSegment {
	
	
	final static double PARALLEL_ANGLE_MAXIMUM = 15.0 / 180.0 * Math.PI;
	
	final static double INDEX_ENVELOPE_MARGIN = 40.0;  // metres
	
	Envelope envelope;
	
	boolean wasCorrelated = false;
//	public boolean wasGeneralised = false;
	public int wasGeneralised = 0;
	public boolean notToBeGeneralised = false;
	
	public Line way;
	
	private Collection<SourceSegment> closeSegments;  // (D)
	private Collection<SourceSegment> closeParallels;  // (B)
	
	// results (real = not collinear + did match)
	public Set<SourceSegment> leftRealParallels;
	public Set<SourceSegment> rightRealParallels;
	
	
	SourceSegment (final OsmNode start, final OsmNode end, final Line way) {
		super(start, end);
		assert way != null /* && way instanceof OsmWay*/;
		
		this.way = way;
		leftRealParallels = new LinkedHashSet<SourceSegment>();
		rightRealParallels = new LinkedHashSet<SourceSegment>();
	}
	
	
	protected AbstractSegment parent () {
		return null;
	}
	
	
	/**
	 * 
	 */
	public Collection<SourceSegment> closeParallels () {
		// filter close parallels from close segment list
		// :TODO: check if filtering is necessary here after regionalisation
		
		if (closeParallels == null) {
			assert closeSegments != null;
			
			closeParallels = new LinkedList<SourceSegment>();
			for (final SourceSegment segment : closeSegments) {
				
				final Vector v1 = segment;
				final Vector v2 = aligned(v1);
				final double angleDifference = Math.abs( v1.relativeBearing(v2) );
				
				if (angleDifference > PARALLEL_ANGLE_MAXIMUM) {
					continue;  // this IS significant
				}
				
				closeParallels.add(segment);
			}
		}
		
		return closeParallels;
	}
	
	
	void setCloseSegments (final Collection<SourceSegment> closeSegments) {
		this.closeSegments = closeSegments;
	}
	
	
	// for spatial index (from JTS)
	Envelope envelope () {
		// HÜLLE, see chapter 4.3.1
		if (envelope == null) {
			envelope = new Envelope(start.e, end.e, start.n, end.n);
			envelope.expandBy(INDEX_ENVELOPE_MARGIN);
		}
		return envelope;
	}
	
	
	/**
	 * 
	 */
	public void analyseLineParts (final Analyser visitor) {
		// the fragments are the ones that need to be analysed
		for (Segment fragment : this) {
			fragment.analyse(visitor);
		}
	}
	
}
