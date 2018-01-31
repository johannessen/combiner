#! /bin/bash

set -e

BBOX_PATH="../bounding-boxes"
PBFFILE="koeln-regbez-180101.osm.pbf"


# This script should preferably be called from the data/ directory as working
# directory, because other scripts (like project/run-testbed.sh) expect their
# input data in ../data/testbed (from their perspective). Also, the bounding
# box default path expects this working directory.


mkdir chapter6-rail
cd chapter6-rail

# use existing PBF file if present
if [ -e "../$PBFFILE" ]
then
	ln "../$PBFFILE" "$PBFFILE"
else
	curl -L -O "http://download.geofabrik.de/europe/germany/nordrhein-westfalen/$PBFFILE"
	cat <<END

Contains information from OpenStreetMap, which is made available here
under the Open Database License (ODbL).
https://opendatacommons.org/licenses/odbl/1.0/

(C) OpenStreetMap contributors and Geofabrik GmbH
https://www.openstreetmap.org/copyright
https://download.geofabrik.de/

END
fi

# see <https://wiki.openstreetmap.org/wiki/User:Bgirardot/How_To_Convert_osm_.pbf_files_to_Esri_Shapefiles>
cat > osmconf.ini <<END
[lines]
osm_id=yes
attributes=ref,railway,usage,service
END
ogr2ogr -oo CONFIG_FILE=osmconf.ini -lco ENCODING=UTF-8 -sql "select osm_id,ref,railway,usage,service from lines where railway is not null"  railways.shp  "$PBFFILE"

RAILWAY_TYPE_SQL="\
select osm_id,ref,concat('usage_',usage) as type from railways \
where railway = 'rail' and usage is not null and service is null \
UNION ALL \
select osm_id,ref,concat('service_',service) as type from railways \
where railway = 'rail' and usage is null and service is not null \
UNION ALL \
select osm_id,ref,concat('usage_',usage,'_service_',service) as type from railways \
where railway = 'rail' and usage is not null and service is not null \
UNION ALL \
select osm_id,ref,null as type from railways \
where railway = 'rail' and usage is null and service is null"
ogr2ogr -clipsrc "$BBOX_PATH/koeln-bbox.shp" -sql "$RAILWAY_TYPE_SQL"  koeln-rail.shp  railways.shp

ogr2ogr -where "type = 'usage_main'" koeln-main.shp koeln-rail.shp


mkdir -p ../chapter6-profiling ../testbed-nrw/
mv "$PBFFILE" "../chapter6-profiling/$PBFFILE"
cd ../chapter6-profiling

MOTORWAY_ONLY="highway = 'motorway'"
MOTORWAY_TRUNK="$MOTORWAY_ONLY OR highway = 'trunk'"
MOTORWAY_TRUNK_PRIMARY="$MOTORWAY_TRUNK OR highway = 'primary'"
#MOTORWAY_TRUNK_PRIMARY_SECONDARY="$MOTORWAY_TRUNK OR highway = 'secondary'"
MOTORWAY_TRUNK_PRIMARY_LINK="$MOTORWAY_TRUNK_PRIMARY OR highway = 'motorway_link' OR highway = 'trunk_link' OR highway = 'primary_link'"
HIGHWAY="$MOTORWAY_TRUNK_PRIMARY_LINK"

# area UTM32 = 34881 km^2
# https://download.geofabrik.de/europe/germany/nordrhein-westfalen.poly
for f in ../testbed-nrw/roads-primary.* ; do ln -s "$f" ; done

# area UTM32 = 7555.8 km^2
# https://download.geofabrik.de/europe/germany/nordrhein-westfalen/koeln-regbez.poly
cat > highway.ini <<END
[lines]
osm_id=yes
attributes=ref,highway
END
ogr2ogr -oo CONFIG_FILE=highway.ini -lco ENCODING=UTF-8 -sql "select osm_id,ref,highway from lines where $HIGHWAY"  koeln-rbz.shp  "$PBFFILE"

ogr2ogr -where "$MOTORWAY_TRUNK_PRIMARY_LINK" koeln-rbz-primary-link.shp koeln-rbz.shp
ogr2ogr -where "$MOTORWAY_TRUNK_PRIMARY" koeln-rbz-primary.shp koeln-rbz-primary-link.shp

# area UTM32 = 646.78 km^2
# bounding-boxes/koeln-bbox.shp
for f in ../testbed-nrw/koeln-main-nolinks.* ; do ln -s "$f" ; done

# area UTM32 = 161.07 km^2
# bounding-boxes/koeln-SE-bbox.shp
for f in ../testbed-nrw/koeln-SE-main-nolinks.* ; do ln -s "$f" ; done

# area UTM32 = 12.222 km^2
# bounding-boxes/koeln-junkersdorf-bbox.shp
for f in ../testbed-nrw/koeln-junkersdorf-main.* ; do ln -s "$f" ; done


rm koeln-rbz.*  # remove caches that are no longer required

echo "Done."

rm "$PBFFILE"
