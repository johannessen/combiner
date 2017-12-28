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
	
	// for debugging only
	private final Collection<GeneralisedSection> generalisedSections = new LinkedList<GeneralisedSection>();
	
	// for use by ConcatenatedSection#relocateGeneralisedNodes()
	private final Collection<NodeMatch> matches = new LinkedList<NodeMatch>();
	
//	final Collection<ResultLine> allSections = new LinkedList<ResultLine>();
	
	
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
	
	
	/**
	 * Creates a new node as an exact copy of another node. Useful if the
	 * pre-existing node is not a <code>SourceNode</code>, but a
	 * <code>SourceNode</code> is required.
	 * @param node the node to by copied
	 */
	public SourceNode (final Node node) {
		this( node.easting(), node.northing(), node.id() );
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
	
	
	public Collection<NodeMatch> matches () {
		return Collections.<NodeMatch>unmodifiableCollection( matches );
	}
	
	
	public void addMatch (final NodeMatch match) {
		matches.add(match);
	}
	
}
