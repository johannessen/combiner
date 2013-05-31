#! /bin/bash

set -x
set -e

if true
then
	mkdir temp
	cd temp
	
	curl -L -O "$1"
	unzip *.zip
	
	mv roads.* ..
	cd ..
	rm -Rf temp
fi

MTP_WITHLINKS="type = 'motorway' OR type = 'trunk' OR type = 'primary' OR type = 'motorway_link' OR type = 'trunk_link' OR type = 'primary_link'"
CLASSIFIED_WITHLINKS="$MTP_WITHLINKS OR type = 'secondary' OR type = 'tertiary' OR type = 'secondary_link' OR type = 'tertiary_link'"
NETWORK_WITHLINKS="$CLASSIFIED_WITHLINKS OR type = 'unclassified' OR type = 'unclassified_link'"

ogr2ogr -where "$NETWORK_WITHLINKS" roads-network.shp roads.shp
ogr2ogr -where "$CLASSIFIED_WITHLINKS" roads-classified.shp roads-network.shp
ogr2ogr -where "$MTP_WITHLINKS" roads-mtp.shp roads-classified.shp

MTP_NOLINKS="type = 'motorway' OR type = 'trunk' OR type = 'primary'"
CLASSIFIED_NOLINKS="$MTP_NOLINKS OR type = 'secondary' OR type = 'tertiary'"
NETWORK_NOLINKS="$CLASSIFIED_NOLINKS OR type = 'unclassified'"
NOLINKS="$NETWORK_NOLINKS OR type = 'residential' OR type = 'service'"

ogr2ogr -where "$NOLINKS" roads-nolinks.shp roads.shp
ogr2ogr -where "$NETWORK_NOLINKS" roads-network-nolinks.shp roads-nolinks.shp
ogr2ogr -where "$CLASSIFIED_NOLINKS" roads-classified-nolinks.shp roads-network-nolinks.shp
ogr2ogr -where "$MTP_NOLINKS" roads-mtp-nolinks.shp roads-classified-nolinks.shp
