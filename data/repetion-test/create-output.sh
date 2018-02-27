#! /bin/bash

# set working dir to this directory


run-combiner () {
	
	IN="$1"
	OUT="$3"
	LINES_OUT="$4"
	ITERATIONS="$2"
	java -cp ../../software/build/classes:../../software/lib/geotools-9.0/* -ea:de.thaw... -Xms256m -Xmx2048m \
		de.thaw.comb.cli.CombinerMain \
		"$IN" "$OUT" "" "$LINES_OUT" "" "$ITERATIONS"
	
}



IN=../testbed-nrw/koeln-classfied-nolinks.shp
#IN=../data/testbed-nrw/koeln-main-nolinks.shp
#IN=../data/testbed-nrw/koeln-motorway.shp
#IN=../data/testbed-nrw/koeln-SE-main.shp
#IN=../data/testbed-nrw/koeln-SE-motorway.shp
#IN=../data/testbed-nrw/koeln-junkersdorf-main.shp
#IN=../data/shape-test/simple3.shp

#IN=../data/quads/de_1x2-mtp/baden-wuerttemberg/n48.5-e9.shp
#IN=../data/quads/de_1x2-classified/baden-wuerttemberg/n48.5-e9.shp



run-combiner "$IN" 1 old-output.shp in-original.shp

run-combiner "$IN" 3 out-notags.shp

run-combiner "$IN" -3 out-withtags.shp
