#! /bin/bash


runcomb () {

IN=.."$1"

OUT=../data/combiner/out.json

time java -cp build/classes:lib/geotools-9.0/* -ea:de.thaw... \
		-verbose:gc -XX:+PrintGCDetails -XX:+PrintGCTimeStamps \
		-Xms10999m -Xmx100999m -XX:+UseParallelGC -XX:+UseParallelOldGC \
		de.thaw.thesis.comb.cli.CombinerMain \
		"$IN" "$OUT" "" "" "" "" 2>&1

# -verbose:gc -XX:+PrintGCDetails -XX:+PrintGCTimeStamps -Xms10999m -Xmx100999m

echo ; echo ; echo
date
echo ; echo ; echo

}



ant build

date
echo ; echo ; echo

runcomb /../data/de/roads-mtp-nolinks.shp | tee ../../log/de-roads-mtp-nolinks.log
runcomb /../data/de/roads-classified-nolinks.shp | tee ../../log/de-roads-classified-nolinks.log
runcomb /../data/de/roads-network-nolinks.shp | tee ../../log/de-roads-network-nolinks.log

runcomb /../data/de/roads-nolinks.shp | tee ../../log/de-roads-nolinks.log
runcomb /../data/bw/roads-network-nolinks.shp | tee ../../log/de_bw-roads-network-nolinks.log

runcomb /../data/gb/roads-network-nolinks.shp | tee ../../log/gb-roads-network-nolinks.log
runcomb /../data/it/roads-network-nolinks.shp | tee ../../log/it-roads-network-nolinks.log
runcomb /../data/nl/roads-network-nolinks.shp | tee ../../log/nl-roads-network-nolinks.log
runcomb /../data/de-nw/roads-network-nolinks.shp | tee ../../log/de_nw-roads-network-nolinks.log

runcomb /../data/de/roads-mtp.shp | tee ../../log/de-roads-mtp.log
runcomb /../data/de/roads-classified.shp | tee ../../log/de-roads-classified.log
runcomb /../data/de/roads-network.shp | tee ../../log/de-roads-network.log
