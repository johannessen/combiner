#! /bin/bash

set -x
set -e

BBOX_PATH="$1"
BBOX_PATH_DEFAULT="../bounding-boxes"


# This script should preferably be called from the data/ directory as working
# directory, because other scripts (like project/run-testbed.sh) expect their
# input data in ../data/testbed (from their perspective). Also, the bounding
# box default path expects this working directory.


mkdir testbed-nrw
cd testbed-nrw

# use existing ZIP archive if present
if [ -e ../nrw-roads.zip ]
then
	unzip ../nrw-roads.zip
else
	curl -L -O "http://dev.thaw.de/temp/highways/nrw-roads.zip"
	unzip nrw-roads.zip
fi

echo "ogr2ogr clipping..."

if [ "$BBOX_PATH" == "" ]
then
	BBOX_PATH="$BBOX_PATH_DEFAULT"
fi

ogr2ogr -clipsrc "$BBOX_PATH/koeln-bbox.shp"  koeln.shp  roads.shp
ogr2ogr -clipsrc "$BBOX_PATH/koeln-SE-bbox.shp"  koeln-SE.shp  koeln.shp  # SE - southeast
ogr2ogr -clipsrc "$BBOX_PATH/koeln-junkersdorf-bbox.shp"  koeln-junkersdorf.shp  koeln.shp


echo "ogr2ogr filtering..."

MOTORWAY_ONLY="fclass = 'motorway' OR fclass = 'trunk'"
MOTORWAY_TRUNK_PRIMARY="fclass = 'motorway' OR fclass = 'trunk' OR fclass = 'primary'"
MOTORWAY_TRUNK_PRIMARY_LINK="$MOTORWAY_TRUNK_PRIMARY OR fclass = 'motorway_link' OR fclass = 'trunk_link' OR fclass = 'primary_link'"
CLASSIFIED_NOLINK="fclass = 'motorway' OR fclass = 'trunk' OR fclass = 'primary' OR fclass = 'secondary' OR fclass = 'tertiary'"
NO_SERVICE="fclass <> 'service'"  # expecting input with nothing but road network

ogr2ogr -where "$CLASSIFIED_NOLINK" koeln-classfied-nolinks.shp koeln.shp
ogr2ogr -where "$MOTORWAY_ONLY" koeln-motorway.shp koeln.shp
ogr2ogr -where "$MOTORWAY_ONLY" koeln-SE-motorway.shp koeln-SE.shp
ogr2ogr -where "$MOTORWAY_TRUNK_PRIMARY_LINK" koeln-main.shp koeln.shp
ogr2ogr -where "$MOTORWAY_TRUNK_PRIMARY_LINK" koeln-SE-main.shp koeln-SE.shp
ogr2ogr -where "$MOTORWAY_TRUNK_PRIMARY" koeln-main-nolinks.shp koeln-main.shp
ogr2ogr -where "$MOTORWAY_TRUNK_PRIMARY" koeln-SE-main-nolinks.shp koeln-SE-main.shp
ogr2ogr -where "$NO_SERVICE" koeln-noservice.shp koeln.shp
ogr2ogr -where "$NO_SERVICE" koeln-SE-noservice.shp koeln-SE.shp
ogr2ogr -where "$MOTORWAY_TRUNK_PRIMARY" koeln-junkersdorf-main.shp koeln-junkersdorf.shp


echo "Done."

#rm nrw-roads.zip
rm -v roads.*
