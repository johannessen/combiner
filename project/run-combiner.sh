#! /bin/bash


ant build

if [ "$?" -ne 0 ]
then
	exit "$?"
fi


IN=../data/testbed-nrw/koeln-main-nolinks.shp
#IN=../data/testbed-nrw/koeln-SE-main.shp
#IN=../data/testbed-nrw/koeln-SE-motorway.shp
#IN=../data/testbed-nrw/koeln-junkersdorf-main.shp
#IN=../data/shape-test/simple3.shp

#IN=../data/quads/de_1x2-mtp/baden-wuerttemberg/n48.5-e9.shp

OUT=../data/combiner/out.shp
OUT_DEBUG1=../data/combiner/out-nodes.shp
OUT_DEBUG2=../data/combiner/out-lineparts.shp
OUT_DEBUG3=../data/combiner/out-debug.shp

java -cp build/classes:lib/geotools-9.0/* -ea:de.thaw... -Xms256m -Xmx2048m \
		de.thaw.thesis.comb.cli.CombinerMain \
		"$IN" "$OUT" "$OUT_DEBUG1" "$OUT_DEBUG2" "$OUT_DEBUG3" "$1"
