/* encoding UTF-8
 * 
 * Copyright (c) 2013 Arne Johannessen
 * 
 * This project and all of its individual parts may be used in accordance
 * with the terms of the 3-clause BSD licence. See LICENSE for details.
 */

package de.thaw.thesis.comb;

import de.thaw.thesis.comb.util.PlaneCoordinate;
import de.thaw.thesis.comb.util.SimpleVector;
import de.thaw.thesis.comb.util.Vector;

import java.util.Collection;
import java.util.Collections;
import java.util.LinkedList;
import java.util.Set;
import java.util.TreeSet;


/**
 * A node with a known relationship to the source data. In particular, the
 * input and output data of this project has such a relationship:
 * <code>SourceSegment</code>s are defined by <code>SourceNode</code>
 * instances, and the combined lines resulting from the generalisation are not
 * <em>yet</em> in the source data, but represent parts of it.
 * @see SourceSegment
 * @see GeneralisedNode
 */
public class SourceNode extends AbstractNode {
	
	private final long id;
	// not all AbstractNodes exist in the OSM planet (e. g. splitPts, Frederik's shapefile)
	
	private final Set<SourceSegment> connectingSegments = new TreeSet<SourceSegment>();
	
	private final Collection<GeneralisedSection> generalisedSections = new LinkedList<GeneralisedSection>();
	
	private final Collection<CorrelationEdge> edges = new LinkedList<CorrelationEdge>();
	
//	final Collection<SectionInterface> allSections = new LinkedList<SectionInterface>();
	
	
	/**
	 * Creates a new node with the position and id provided.
	 * @param e easting
	 * @param n northing
	 * @param id A number identifying this node in some context (not
	 *  necessarily uniquely)
	 */
	public SourceNode (final double e, final double n, final long id) {
		super(e, n);
		this.id = id;
	}
	
	
	public long id () {
		return id;
	}
	
	
	public Set<SourceSegment> connectingSegments () {
		return Collections.<SourceSegment>unmodifiableSet( connectingSegments );
	}
	
	
	public void addSegment (final SourceSegment segment) {
		connectingSegments.add(segment);
	}
	
	
	public Collection<GeneralisedSection> generalisedSections () {
		return Collections.<GeneralisedSection>unmodifiableCollection( generalisedSections );
	}
	
	
	public void addGeneralisedSection (final GeneralisedSection section) {
		generalisedSections.add(section);
	}
	
	
	public Collection<CorrelationEdge> edges () {
		return Collections.<CorrelationEdge>unmodifiableCollection( edges );
	}
	
	
	public void addEdge (final CorrelationEdge edge) {
		edges.add(edge);
	}
	
}
