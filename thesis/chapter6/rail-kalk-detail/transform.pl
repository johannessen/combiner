#! /usr/bin/env perl

use strict;
use 5.016;

use File::Basename qw(dirname);
use File::Slurper;
use Geo::JSON;
use Geo::JSON::CRS;  # required for loading files that contain a CRS def ... this seems like a bug in Geo::JSON
use Geometry::AffineTransform;
#use Data::Dumper;

my $data = "../../../data";
my $software = "../../../software";
my $format = "geojson";
my $output_file = "out.$format";
my $nodes_file = "out-nodes.$format";
my $matches_file = "out-debug.$format";
my $input_file = "out-lineparts.$format";


chdir dirname $0;

$ENV{IN} = "$data/chapter6-rail/koeln-main.shp";
system "$software/run-combiner.sh", qw(--debug --tags), 0x2;


sub ogr2ogr {
	system qw(ogr2ogr
		-lco SIGNIFICANT_FIGURES=9
		-f GeoJSON
	), @_;
}

my @clip = qw(-spat 7.013 50.927 7.018 50.934 -spat_srs EPSG:4326);

ogr2ogr @clip, qw(-select gen,ref), $output_file, "$data/combiner/out.shp";
ogr2ogr @clip, $nodes_file, "$data/combiner/out-nodes.shp";
ogr2ogr @clip, $matches_file, "$data/combiner/out-debug.shp";
ogr2ogr @clip, $input_file, "$data/combiner/out-lineparts.shp";


sub affine_transform_geojson {
	my ($affine, $file) = @_;
	my $json = Geo::JSON->from_json( File::Slurper::read_text($file) );
	if ($json->{crs} && $json->{crs}->{name} eq "urn:ogc:def:crs:OGC:1.3:CRS84") {
		die "Affine transformation is not supported for WGS84; use a projected CRS like UTM";
	}
	foreach my $feature ( @{$json->{features}} ) {
		my $coordinates = $feature->{geometry}->{coordinates};
		if (ref $coordinates->[0] eq "ARRAY") {
			foreach my $point ( @$coordinates ) {
				($point->[0], $point->[1]) = $affine->transform($point->[0], $point->[1]);
			}
		}
		else {
			($coordinates->[0], $coordinates->[1]) = $affine->transform($coordinates->[0], $coordinates->[1]);
		}
	}
	$json->{bbox} = $json->compute_bbox;
	File::Slurper::write_text $file, $json->to_json;
}

# scale perpendicular to an axis defined by a position and bearing
my @point = (360591, 5643957);  # UTM 32
my $axis = 16;  # degrees CW
my $t = Geometry::AffineTransform->new();
$t->translate(map {-$_} @point);  #tx, ty
$t->rotate(+$axis);  # degrees CCW
$t->translate(0, +$point[1]);  #tx, ty
$t->scale(2, 1);  # sx, sy
$t->translate(0, -$point[1]);  #tx, ty
$t->rotate(-$axis);  # degrees CCW
$t->translate(@point);  #tx, ty
#print Dumper $t->matrix;

#Geo::JSON->codec->canonical(1)->pretty;  # debug only due to file size
affine_transform_geojson $t, $output_file;
affine_transform_geojson $t, $nodes_file;
affine_transform_geojson $t, $matches_file;
affine_transform_geojson $t, $input_file;


exit 0;
