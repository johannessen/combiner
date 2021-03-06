#! /bin/bash

# halt on error
set -e

if [ "$1" == "--debug" ]
then
	DEBUG_OUT=1
	VERBOSE="--verbose"
	DEBUG_VM="-verbose:gc -XX:+PrintGCDetails -XX:+PrintGCTimeStamps"
	shift
fi


DIR=`pwd`
cd `dirname "$0"`

ant build


IN_DEFAULT=../data/testbed-nrw/koeln-classfied-nolinks.shp
#IN_DEFAULT=../data/testbed-nrw/koeln-main-nolinks.shp
#IN_DEFAULT=../data/testbed-nrw/koeln-motorway.shp
#IN_DEFAULT=../data/testbed-nrw/koeln-SE-main.shp
#IN_DEFAULT=../data/testbed-nrw/koeln-SE-motorway.shp
#IN_DEFAULT=../data/testbed-nrw/koeln-junkersdorf-main.shp
#IN_DEFAULT=../data/shape-test/simple3.shp
#IN_DEFAULT=../data/chapter6-rail/koeln-main.shp
#IN_DEFAULT=../data/chapter6-profiling/koeln-rbz-primary-link.shp
#IN_DEFAULT=../data/testbed-nrw/roads-motorway.shp

#IN_DEFAULT=../data/quads/de_1x2-mtp/baden-wuerttemberg/n48.5-e9.shp
#IN_DEFAULT=../data/quads/de_1x2-classified/baden-wuerttemberg/n48.5-e9.shp

IN="${IN:=$IN_DEFAULT}"
if [ ! -f "$IN" -a -f "$DIR/$IN" ]
then
	IN="$DIR/$IN"
fi

OUT=../data/combiner/out.json
if [ -n "$DEBUG_OUT" ]
then
	OUT_DEBUG1=../data/combiner/out-nodes.shp
	OUT_DEBUG2=../data/combiner/out-lineparts.shp
	OUT_DEBUG3=../data/combiner/out-debug.shp
fi

# GeoTools (the georeferencing code in particular) gets peeved when certain
# unused classes are on the classpath. To avoid the need to manually delete
# those JARs, we can put only those JARs we actually use on the classpath.
# As a result, this runtime classpath differs from the compiler classpath.
# see also:
# <http://docs.geotools.org/stable/userguide/faq.html#q-what-jars-does-gt-referencing-need>
# <http://article.gmane.org/gmane.comp.gis.geotools2.user/15863>

CLASSPATH=`perl -e 'print join ":", qw(
	build/classes
	lib/*
	lib/geotools-18.2/core-0.26.jar
	lib/geotools-18.2/gt-api-18.2.jar
	lib/geotools-18.2/gt-data-18.2.jar
	lib/geotools-18.2/gt-epsg-hsql-18.2.jar
	lib/geotools-18.2/gt-main-18.2.jar
	lib/geotools-18.2/gt-metadata-18.2.jar
	lib/geotools-18.2/gt-opengis-18.2.jar
	lib/geotools-18.2/gt-referencing-18.2.jar
	lib/geotools-18.2/gt-shapefile-18.2.jar
	lib/geotools-18.2/hsqldb-2.3.0.jar
	lib/geotools-18.2/jsr-275-1.0-beta-2.jar
	lib/geotools-18.2/jts-core-1.14.0.jar
	lib/geotools-18.2/sqlite-jdbc-3.20.0.jar
)'`

time java \
		-cp $CLASSPATH \
		-ea:de.thaw... \
		-Djava.awt.headless=true \
		$DEBUG_VM \
		de.thaw.comb.cli.CombinerMain \
		--input "$IN" \
		--output "$OUT" \
		--out-nodes "$OUT_DEBUG1" \
		--out-lineparts "$OUT_DEBUG2" \
		--out-debug "$OUT_DEBUG3" \
		$VERBOSE \
		$@

# examples for pass-through options:
#		--no-cleanup
#		--tags 2
#		--iterations 3

# VM options to debug/optimise Java memory use:
#		-Xms256m -Xmx2048m -XX:+UseParallelGC -XX:+UseParallelOldGC \

# The Shapefile output directly from Java via Geotools seems problematic.
# Writing a GeoJSON file instead which can then be converted to Shapefile is
# an attempt to circumvent those issues, hopefully improving scalability.
rm -f ../data/combiner/out.shp ../data/combiner/out.shx ../data/combiner/out.dbf ../data/combiner/out.prj 
ogr2ogr -f "ESRI Shapefile" ../data/combiner/out.shp ../data/combiner/out.json
rm ../data/combiner/out.json
