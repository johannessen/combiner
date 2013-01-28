#! /bin/bash

set -x
set -e

# TODO: verify we're in correct dir!


TEMPDIR=`mktemp -d -t 'prepare-testdataXXXXXX'`

# Source:
# http://www.remote.org/frederik/tmp/nrw-roads.zip

unzip nrw-roads.zip -d "$TEMPDIR"

# bail out on unzip error
if [ "$?" -ne 0 ]
then
	rm -Rf -v "$TEMPDIR"
	exit "$?"
fi


echo "ogr2ogr clipping..."

ogr2ogr -clipsrc ../bounding-boxes/koeln-bbox.shp  koeln.shp  "$TEMPDIR/roads.shp"
ogr2ogr -clipsrc ../bounding-boxes/koeln-SE-bbox.shp  koeln-SE.shp  koeln.shp  # SE - southeast
ogr2ogr -clipsrc ../bounding-boxes/koeln-junkersdorf-bbox.shp  koeln-junkersdorf.shp  koeln.shp


echo "ogr2ogr filtering..."

MOTORWAY_ONLY="fclass = 'motorway' OR fclass = 'trunk'"
MOTORWAY_TRUNK_PRIMARY="fclass = 'motorway' OR fclass = 'motorway_link' OR fclass = 'trunk' OR fclass = 'trunk_link' OR fclass = 'primary' OR fclass = 'primary_link'"
NO_SERVICE="fclass <> 'service'"  # expecting input with nothing but road network

ogr2ogr -where "$MOTORWAY_ONLY" koeln-motorway.shp koeln.shp
ogr2ogr -where "$MOTORWAY_ONLY" koeln-SE-motorway.shp koeln-SE.shp
ogr2ogr -where "$MOTORWAY_TRUNK_PRIMARY" koeln-main.shp koeln.shp
ogr2ogr -where "$MOTORWAY_TRUNK_PRIMARY" koeln-SE-main.shp koeln-SE.shp
ogr2ogr -where "$NO_SERVICE" koeln-noservice.shp koeln.shp
ogr2ogr -where "$NO_SERVICE" koeln-SE-noservice.shp koeln-SE.shp
ogr2ogr -where "$MOTORWAY_TRUNK_PRIMARY" koeln-junkersdorf-main.shp koeln-junkersdorf.shp


echo "cleanup..."

rm -Rf -v "$TEMPDIR"
