/* encoding UTF-8
 * 
 * Copyright (c) 2013 Arne Johannessen
 * 
 * This project and all of its individual parts may be used in accordance
 * with the terms of the 3-clause BSD licence. See LICENSE for details.
 */

package de.thaw.comb.cli;

import org.kohsuke.args4j.Option;
import org.kohsuke.args4j.Argument;


// see http://args4j.kohsuke.org/

public class Options {
	
	/**
	 * Signals clients that they should determine a sensible default for this
	 * value based on other options given.
	 */
	final static int SENSIBLE_DEFAULT_INT = -1;
	
	
	@Option(name="--input",usage="input",required=true)
	public String input = null;
	
	@Option(name="--output",usage="output")
	public String output = "out.shp";
	
	@Option(name="--out-nodes",usage="nodes")
	public String outNodes = "";
	
	@Option(name="--out-lineparts",usage="lineparts")
	public String outLineParts = "";
	
	@Option(name="--out-debug",usage="debug")
	public String outDebug = "";
	
	@Option(name="--iterations",usage="iterations count")
	public int iterations = 1;
	
	@Option(name="--verbose",usage="verbose")
	public boolean verbose = false;
	
	@Option(name="--no-cleanup",usage="no cleanup")
	public boolean noCleanup = false;
	
	@Option(name="--tags",usage="evaluateTags (bitmask: 0x1=highway, 0x2=ref)")
	public int tags = SENSIBLE_DEFAULT_INT;
	
	@Option(name="--start-id")
	public long startId = 0;
	
}
