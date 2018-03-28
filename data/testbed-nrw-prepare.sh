#! /bin/bash

set -x
set -e

BBOX_PATH="$1"
BBOX_PATH_DEFAULT="../bounding-boxes"


# This script should preferably be called from the data/ directory as working
# directory, because other scripts (like software/run-testbed.sh) expect their
# input data in ../data/testbed (from their perspective). Also, the bounding
# box default path expects this working directory.


mkdir testbed-nrw
cd testbed-nrw

# use existing ZIP archive if present
if [ -e ../nrw-roads.zip ]
then
	unzip ../nrw-roads.zip
else
	curl -L -O "http://arne.johannessen.de/thesis/download/nrw-roads.zip"
	cat <<END

Contains information from OpenStreetMap, which is made available here
under the Open Database License (ODbL).
https://opendatacommons.org/licenses/odbl/1.0/

(C) OpenStreetMap contributors and Geofabrik GmbH
https://www.openstreetmap.org/copyright
https://www.geofabrik.de/data/shapefiles.html

END
	unzip nrw-roads.zip
fi

echo "ogr2ogr clipping..."

if [ "$BBOX_PATH" == "" ]
then
	BBOX_PATH="$BBOX_PATH_DEFAULT"
fi

ogr2ogr -progress -clipsrc "$BBOX_PATH/koeln-bbox.shp"  koeln.shp  roads.shp
ogr2ogr -clipsrc "$BBOX_PATH/koeln-SE-bbox.shp"  koeln-SE.shp  koeln.shp  # SE - southeast
ogr2ogr -clipsrc "$BBOX_PATH/koeln-junkersdorf-bbox.shp"  koeln-junkersdorf.shp  koeln.shp


echo "ogr2ogr filtering..."

MOTORWAY_ONLY="fclass = 'motorway'"
MOTORWAY_LINK="$MOTORWAY_ONLY OR fclass = 'motorway_link'"
MOTORWAY_TRUNK="$MOTORWAY_ONLY OR fclass = 'trunk'"
MOTORWAY_TRUNK_LINK="$MOTORWAY_LINK OR fclass = 'trunk' OR fclass = 'trunk_link'"
MOTORWAY_TRUNK_PRIMARY="$MOTORWAY_TRUNK OR fclass = 'primary'"
MOTORWAY_TRUNK_PRIMARY_LINK="$MOTORWAY_TRUNK_LINK OR fclass = 'primary' OR fclass = 'primary_link'"
CLASSIFIED_NOLINK="fclass = 'motorway' OR fclass = 'trunk' OR fclass = 'primary' OR fclass = 'secondary' OR fclass = 'tertiary'"
NO_SERVICE="fclass <> 'service'"  # expecting input with nothing but road network

# koeln
ogr2ogr -where "$CLASSIFIED_NOLINK" koeln-classfied-nolinks.shp koeln.shp
ogr2ogr -where "$MOTORWAY_TRUNK" koeln-motorway.shp koeln.shp
ogr2ogr -where "$MOTORWAY_TRUNK" koeln-SE-motorway.shp koeln-SE.shp
ogr2ogr -where "$MOTORWAY_TRUNK_PRIMARY_LINK" koeln-main.shp koeln.shp
ogr2ogr -where "$MOTORWAY_TRUNK_PRIMARY_LINK" koeln-SE-main.shp koeln-SE.shp
ogr2ogr -where "$MOTORWAY_TRUNK_PRIMARY" koeln-main-nolinks.shp koeln-main.shp
ogr2ogr -where "$MOTORWAY_TRUNK_PRIMARY" koeln-SE-main-nolinks.shp koeln-SE-main.shp
ogr2ogr -where "$NO_SERVICE" koeln-noservice.shp koeln.shp
ogr2ogr -where "$NO_SERVICE" koeln-SE-noservice.shp koeln-SE.shp
ogr2ogr -where "$MOTORWAY_TRUNK_PRIMARY" koeln-junkersdorf-main.shp koeln-junkersdorf.shp
ogr2ogr -where "$MOTORWAY_LINK" koeln-motorway-links.shp koeln-main.shp

# nrw
ogr2ogr -where "$MOTORWAY_TRUNK_PRIMARY_LINK" roads-primary-links.shp roads.shp
ogr2ogr -where "$MOTORWAY_TRUNK_PRIMARY" roads-primary.shp roads-primary-links.shp
ogr2ogr -where "$MOTORWAY_TRUNK" roads-trunk.shp roads-primary.shp
ogr2ogr -where "$MOTORWAY_ONLY" roads-motorway.shp roads-trunk.shp


rm -v roads-primary-links.* koeln.*  # remove caches that are no longer required

echo "Done."

#rm nrw-roads.zip
rm -v roads.*
