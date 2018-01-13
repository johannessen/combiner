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

echo "Done."

rm "$PBFFILE"
