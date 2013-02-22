#! /bin/bash


ant build

if [ "$?" -ne 0 ]
then
	exit "$?"
fi


#IN=../data/testbed/koeln-SE-main.shp
IN=../data/testbed/koeln-SE-motorway.shp
#IN=../data/testbed/koeln-junkersdorf-main.shp
#IN=../data/shape-test/simple.shp

OUT=../data/combiner/out.shp
OUT_DEBUG1=../data/combiner/out-nodes.shp
OUT_DEBUG2=../data/combiner/out-lineparts.shp
OUT_DEBUG3=../data/combiner/out-debug.shp

java -cp build/classes:lib/geotools-8.5/* -ea:de.thaw... de.thaw.thesis.comb.cli.CombinerMain "$IN" "$OUT" "$OUT_DEBUG1" "$OUT_DEBUG2" "$OUT_DEBUG3" "$1"
