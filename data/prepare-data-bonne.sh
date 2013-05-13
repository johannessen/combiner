#! /bin/bash

set -x
set -e

mkdir temp
cd temp

curl -L -O "$1"
unzip *.zip

mv roads.* ..
cd ..
rm -Rf temp

MTP_NOLINKS="type = 'motorway' OR type = 'trunk' OR type = 'primary'"
CLASSIFIED_NOLINKS="$MTP_NOLINKS OR type = 'secondary' OR type = 'tertiary'"
NETWORK_NOLINKS="$CLASSIFIED_NOLINKS OR type = 'unclassified'"
NOLINKS="$NETWORK_NOLINKS OR type = 'residential' OR type = 'service'"

ogr2ogr -where "$NOLINKS" roads-nolinks.shp roads.shp
ogr2ogr -where "$NETWORK_NOLINKS" roads-network-nolinks.shp roads-nolinks.shp
ogr2ogr -where "$CLASSIFIED_NOLINKS" roads-classified-nolinks.shp roads-network-nolinks.shp
ogr2ogr -where "$MTP_NOLINKS" roads-mtp-nolinks.shp roads-classified-nolinks.shp
