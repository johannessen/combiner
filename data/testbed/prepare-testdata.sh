#! /bin/bash


# TODO: verify we're in correct dir!


TEMPDIR=`mktemp -d -t 'prepare-testdata'`

# Source:
# http://www.remote.org/frederik/tmp/nrw-roads.zip

unzip nrw-roads.zip -d "$TEMPDIR"

# bail out on unzip error
if [ "$?" -ne 0 ]
then
	exit "$?"
fi


echo "ogr2ogr clipping..."

ogr2ogr -clipsrc ../bounding-boxes/koeln-bbox.shp  koeln.shp  "$TEMPDIR/roads.shp"

# southeast
ogr2ogr -clipsrc ../bounding-boxes/koeln-SE-bbox.shp  koeln-SE.shp  koeln.shp


echo "ogr2ogr filtering..."

ogr2ogr -where "fclass = 'motorway'" koeln-motorway.shp koeln.shp
ogr2ogr -where "fclass = 'motorway'" koeln-SE-motorway.shp koeln-SE.shp


echo "cleanup..."

rm -Rf -v "$TEMPDIR"
