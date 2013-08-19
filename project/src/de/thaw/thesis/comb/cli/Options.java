/* encoding UTF-8
 * 
 * Copyright (c) 2013 Arne Johannessen
 * 
 * This project and all of its individual parts may be used in accordance
 * with the terms of the 3-clause BSD licence. See LICENSE for details.
 */

package de.thaw.thesis.comb.cli;

import org.kohsuke.args4j.Option;
import org.kohsuke.args4j.Argument;


// see http://args4j.kohsuke.org/

public class Options {
	
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
	
	@Option(name="--iterations",usage="iterations")
	public int iterations = 0;
	
	@Option(name="--start-id")
	public long startId = 0;
	
}
