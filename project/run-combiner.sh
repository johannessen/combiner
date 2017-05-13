#! /bin/bash

# halt on error
set -e


ant build


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

# GeoTools (the georeferencing code in particular) gets peeved when certain
# unused classes are on the classpath. To avoid the need to manually delete
# those JARs, we can put only those JARs we actually use on the classpath.
# As a result, this runtime classpath differs from the compiler classpath.
# see also:
# <http://docs.geotools.org/stable/userguide/faq.html#q-what-jars-does-gt-referencing-need>
# <http://article.gmane.org/gmane.comp.gis.geotools2.user/15863>

time java \
		-cp build/classes:lib/*:lib/geotools-17.0/core-0.26.jar:lib/geotools-17.0/gt-api-17.0.jar:lib/geotools-17.0/gt-data-17.0.jar:lib/geotools-17.0/gt-epsg-hsql-17.0.jar:lib/geotools-17.0/gt-main-17.0.jar:lib/geotools-17.0/gt-metadata-17.0.jar:lib/geotools-17.0/gt-opengis-17.0.jar:lib/geotools-17.0/gt-referencing-17.0.jar:lib/geotools-17.0/gt-shapefile-17.0.jar:lib/geotools-17.0/hsqldb-2.3.0.jar:lib/geotools-17.0/jsr-275-1.0-beta-2.jar:lib/geotools-17.0/jts-1.13.jar:lib/geotools-17.0/sqlite-jdbc-3.14.2.1.jar \
		-ea:de.thaw... \
		-Djava.awt.headless=true \
		-verbose:gc -XX:+PrintGCDetails -XX:+PrintGCTimeStamps \
		-Xms256m -Xmx2048m -XX:+UseParallelGC -XX:+UseParallelOldGC \
		de.thaw.thesis.comb.cli.CombinerMain \
		--input "$IN" \
		--output "$OUT" \
		--out-nodes "$OUT_DEBUG1" \
		--out-lineparts "$OUT_DEBUG2" \
		--out-debug "$OUT_DEBUG3" \
		$@

# -verbose:gc -XX:+PrintGCDetails -XX:+PrintGCTimeStamps
# -Xms10999m -Xmx100999m -XX:+UseParallelGC -XX:+UseParallelOldGC

# The Shapefile output directly from Java via Geotools seems problematic.
# Writing a GeoJSON file instead which can then be converted to Shapefile is
# an attempt to circumvent those issues, hopefully improving scalability.
rm -f ../data/combiner/out.shp ../data/combiner/out.shx ../data/combiner/out.dbf ../data/combiner/out.prj 
ogr2ogr -f "ESRI Shapefile" ../data/combiner/out.shp ../data/combiner/out.json
rm ../data/combiner/out.json
