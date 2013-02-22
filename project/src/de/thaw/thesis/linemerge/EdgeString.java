/* The de.thaw.thesis.linemerge package is based on the JTS 1.13 library,
 * with changes by Arne Johannessen, 2013.
 */

/*
 * The JTS Topology Suite is a collection of Java classes that
 * implement the fundamental operations required to validate a given
 * geo-spatial data set to a known topological specification.
 *
 * Copyright (C) 2001 Vivid Solutions
 *
 * This library is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation; either
 * version 2.1 of the License, or (at your option) any later version.
 *
 * This library is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with this library; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307  USA
 *
 * For more information, contact:
 *
 *     Vivid Solutions
 *     Suite #1A
 *     2328 Government Street
 *     Victoria BC  V8T 5G5
 *     Canada
 *
 *     (250)385-6040
 *     www.vividsolutions.com
 */
package de.thaw.thesis.linemerge;

import com.vividsolutions.jts.geom.*;
import com.vividsolutions.jts.geom.Coordinate;
import com.vividsolutions.jts.geom.CoordinateList;
import com.vividsolutions.jts.geom.GeometryFactory;
import com.vividsolutions.jts.geom.LineString;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

/**
 * A sequence of {@link LineMergeDirectedEdge}s forming one of the lines that will
 * be output by the line-merging process.
 *
 * @version 1.7
 */
public class EdgeString {
  private GeometryFactory factory;
  private List directedEdges = new ArrayList();
  
  private List userDataObjects = null;
  private LineString lineString = null;
  
  /**
   * Constructs an EdgeString with the given factory used to convert this EdgeString
   * to a LineString
   */
  public EdgeString(GeometryFactory factory) {
    this.factory = factory;
  }
  
  /**
   * Adds a directed edge which is known to form part of this line.
   */
  @SuppressWarnings("unchecked")
  public void add(LineMergeDirectedEdge directedEdge) {
    directedEdges.add(directedEdge);
  }
  
  @SuppressWarnings("unchecked")
  private Coordinate[] getCoordinates() {
    int forwardDirectedEdges = 0;
    int reverseDirectedEdges = 0;
    
    userDataObjects = new ArrayList(directedEdges.size());
    
    CoordinateList coordinateList = new CoordinateList();
    for (Iterator i = directedEdges.iterator(); i.hasNext();) {
      LineMergeDirectedEdge directedEdge = (LineMergeDirectedEdge) i.next();
      if (directedEdge.getEdgeDirection()) {
        forwardDirectedEdges++;
      }
      else {
        reverseDirectedEdges++;
      }
      
      final LineString line = ((LineMergeEdge) directedEdge.getEdge()).getLine();
      userDataObjects.add(line.getUserData());
      
      coordinateList.add(line.getCoordinates(), false, directedEdge.getEdgeDirection());
    }
    
    final Coordinate[] coordinates;
    coordinates = coordinateList.toCoordinateArray();
    if (reverseDirectedEdges > forwardDirectedEdges) {
      CoordinateArrays.reverse(coordinates);
    }
    
    return coordinates;
  }
  
  /**
   * Converts this EdgeString into a LineString.
   */
  public LineString toLineString() {
    if (lineString == null) {
      lineString = factory.createLineString(getCoordinates());
      lineString.setUserData(userDataObjects);
    }
    return lineString;
  }
}
