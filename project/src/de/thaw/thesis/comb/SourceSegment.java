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
	
	
	final static double PARALLEL_ANGLE_MAXIMUM = 30.0 / 180.0 * Math.PI;
	
	final static double INDEX_ENVELOPE_MARGIN = 20.0;  // metres (ideally MAX_DISTANCE/2.0)
	
	Envelope envelope;
	
	int wasGeneralised = 0;
	boolean notToBeGeneralised = false;  // avoids infinite loop in GeneralisedLines#traverse()
	
	public Line way;  // :BUG: shouldn't be public
	
	private Collection<SourceSegment> closeSegments;  // (D)
	private Collection<SourceSegment> closeParallels;  // (B)
	
	// results (real = not collinear + did match)
	public Set<SourceSegment> leftRealParallels;
	public Set<SourceSegment> rightRealParallels;
	
	
	public SourceSegment (final SourceNode start, final SourceNode end, final Line way) {
		super(start, end);
		assert way != null /* && way instanceof Highway*/;
		
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
	@Override  // return type SourceNode
	public SourceNode start () {
		assert start != null;
		return (SourceNode)start;
	}
	
	
	/**
	 * 
	 */
	@Override  // return type SourceNode
	public SourceNode end () {
		assert end != null;
		return (SourceNode)end;
	}
	
	
	public int wasGeneralised () {
		return wasGeneralised;
	}
	
	
	@Override  // return type SourceNode
	public SourceNode other (final Node node) {
		return (SourceNode)super.other(node);
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
			envelope = new Envelope(start.easting(), end.easting(), start.northing(), end.northing());
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
