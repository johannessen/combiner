/* encoding UTF-8
 * 
 * Copyright (c) 2012-13 Arne Johannessen
 * 
 * This project and all of its individual parts may be used in accordance
 * with the terms of the 3-clause BSD licence. See LICENSE for details.
 */

package de.thaw.thesis.comb.io;


/*
 * Defines feature type and feature attributes of a <code>ShapeWriter</code>.
 * An instance of an implementing class may be passed to a
 * <code>ShapeWriter</code>, which will then delegate any decisions about what
 * type of features to create a Shapefile for and which attributes that
 * Shapefile should have back to this instance.
 */
public interface ShapeWriterDelegate extends de.thaw.thesis.testbed.ShapeWriterDelegate {
}
