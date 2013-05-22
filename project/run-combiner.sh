#! /bin/bash


ant build

if [ "$?" -ne 0 ]
then
	exit "$?"
fi


IN=../data/testbed-nrw/koeln-classfied-nolinks.shp
#IN=../data/testbed-nrw/koeln-main-nolinks.shp
#IN=../data/testbed-nrw/koeln-motorway.shp
#IN=../data/testbed-nrw/koeln-SE-main.shp
#IN=../data/testbed-nrw/koeln-SE-motorway.shp
#IN=../data/testbed-nrw/koeln-junkersdorf-main.shp
#IN=../data/shape-test/simple3.shp

#IN=../data/quads/de_1x2-mtp/baden-wuerttemberg/n48.5-e9.shp
#IN=../data/quads/de_1x2-classified/baden-wuerttemberg/n48.5-e9.shp

OUT=../data/combiner/out.json
#OUT_DEBUG1=../data/combiner/out-nodes.shp
#OUT_DEBUG2=../data/combiner/out-lineparts.shp
#OUT_DEBUG3=../data/combiner/out-debug.shp

time java -cp build/classes:lib/geotools-9.0/* -ea:de.thaw... \
		-verbose:gc -XX:+PrintGCDetails -XX:+PrintGCTimeStamps \
		-Xms256m -Xmx2048m -XX:+UseParallelGC -XX:+UseParallelOldGC \
		de.thaw.thesis.comb.cli.CombinerMain \
		"$IN" "$OUT" "$OUT_DEBUG1" "$OUT_DEBUG2" "$OUT_DEBUG3" "$1"

# -verbose:gc -XX:+PrintGCDetails -XX:+PrintGCTimeStamps
# -Xms10999m -Xmx100999m -XX:+UseParallelGC -XX:+UseParallelOldGC

# The Shapefile output directly from Java via Geotools seems problematic.
# Writing a GeoJSON file instead which can then be converted to Shapefile is
# an attempt to circumvent those issues, hopefully improving scalability.
rm -f ../data/combiner/out.shp ../data/combiner/out.shx ../data/combiner/out.dbf ../data/combiner/out.prj 
ogr2ogr -f "ESRI Shapefile" ../data/combiner/out.shp ../data/combiner/out.json
rm ../data/combiner/out.json
