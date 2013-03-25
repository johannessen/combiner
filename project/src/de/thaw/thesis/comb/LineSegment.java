/* encoding UTF-8
 * 
 * Copyright (c) 2013 Arne Johannessen
 * 
 * This project and all of its individual parts may be used in accordance
 * with the terms of the 3-clause BSD licence. See LICENSE for details.
 */

package de.thaw.thesis.comb;

import de.thaw.thesis.comb.util.OneItemList;

import com.vividsolutions.jts.geom.Envelope;

import java.util.Collection;
import java.util.LinkedHashSet;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.Set;


/**
 * A <code>LinePart</code> implementation representing segments as read from
 * the source data. May be split into two or more <code>LineFragment</code>s.
 */
public final class LineSegment extends AbstractLinePart implements LinePart {
	
	// :TODO: rework structure to better fit the Composite pattern
	
	
	final static double PARALLEL_ANGLE_MAXIMUM = 15.0 / 180.0 * Math.PI;
	
	final static double INDEX_ENVELOPE_MARGIN = 40.0;  // metres
	
	Envelope envelope;
	
	boolean wasCorrelated = false;
//	public boolean wasGeneralised = false;
	public int wasGeneralised = 0;
	public boolean notToBeGeneralised = false;
	
	// LineSegment
	public OsmWay way;
	public Collection<LineFragment> fragments;  // (A)
	
	private Collection<LineSegment> closeSegments;  // (D)
	private Collection<LineSegment> closeParallels;  // (B)
	
	// results (real = not collinear + did match)
	public Set<LineSegment> leftRealParallels;
	public Set<LineSegment> rightRealParallels;
	
	
	LineSegment (final OsmNode start, final OsmNode end, final OsmWay way) {
		super(start, end);
		assert way != null;
		
		this.way = way;
		this.fragments = new LinkedList<LineFragment>();
		
		leftRealParallels = new LinkedHashSet<LineSegment>();
		rightRealParallels = new LinkedHashSet<LineSegment>();
	}
	
	
	/**
	 * 
	 */
	public LineSegment segment () {
		return this;
	}
	
	
	/**
	 * 
	 */
	public Collection<? extends LinePart> lineParts () {
		if (fragments.size() > 0) {
			return fragments;
		}
		return new OneItemList<LineSegment>(this);
	}
	
	
	/**
	 * 
	 */
	public Collection<LineSegment> closeParallels () {
		// filter close parallels from close segment list
		// :TODO: check if filtering is necessary here after regionalisation
		
		if (closeParallels == null) {
			assert closeSegments != null;
			
			closeParallels = new LinkedList<LineSegment>();
			for (final LineSegment segment : closeSegments) {
				
				final Vector v1 = segment.vector();
				final Vector v2 = vector().aligned(v1);
				final double angleDifference = Math.abs( v1.relativeBearing(v2) );
				
				if (angleDifference > PARALLEL_ANGLE_MAXIMUM) {
					continue;
				}
				
				closeParallels.add(segment);
			}
		}
		
		return closeParallels;
	}
	
	
	void setCloseSegments (final Collection<LineSegment> closeSegments) {
		this.closeSegments = closeSegments;
	}
	
	
	// for spatial index (from JTS)
	Envelope envelope () {
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
		for (LinePart part : lineParts()) {
			part.analyse(visitor);
		}
	}
	
}
