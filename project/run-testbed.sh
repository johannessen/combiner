#! /bin/bash


ant build

if [ "$?" -ne 0 ]
then
	exit "$?"
fi


#IN=../data/testbed/koeln-SE-main.shp
#IN=../data/testbed/koeln-SE-motorway.shp
IN=../data/testbed/koeln-junkersdorf-main.shp
#IN=../data/shape-test/simple.shp

OUT=../data/testbed/out.shp
OUT_DEBUG=../data/testbed/out-debug.shp

java -cp build/classes:lib/geotools-8.5/* de.thaw.thesis.testbed.Testbed "$IN" "$OUT" "$OUT_DEBUG"





# Apache Ant was used for execution as well as compilation. The reason for this
# is the unfortunate inability of java(1) to accept multiple JAR files on the
# CLI with the use of wildcards; java(1) insists on having each and every JAR
# file enumerated in one single -classpath argument. This issue is supposed to
# have been solved with Java 1.6, but I can't get it to work that way. And
# ant(1) works fine. (Cue entrance for the Java bashers.)
