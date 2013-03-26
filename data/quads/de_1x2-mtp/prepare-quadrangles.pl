#! /usr/bin/env perl
# $Id: prepare-quadrangles.pl 2013-02-22 aj3 $

use strict;
use warnings;

our $VERSION = 0.01;

use File::Path qw();
use List::Util qw();
#use LWP::Simple qw();

my $DOWNLOAD = 1;

sub run;
sub get;
sub ogr2ogr;
sub clean;
sub sieve;


#my @DE_bounds = (6, 47.5, 15, 54.5);
my @DE_bounds = (5, 47.5, 15, 54.5);

#run 'bremen', 8, 52.5, 10, 54;
#run 'bremen', 8, 52, 10, 54;
#run 'nordrhein-westfalen', @DE_bounds;
#run 'niedersachsen', @DE_bounds;
#run 'schleswig-holstein', @DE_bounds;
#run 'mecklenburg-vorpommern', @DE_bounds;
#run 'hamburg', @DE_bounds;
#run 'brandenburg', @DE_bounds;
#run 'berlin', @DE_bounds;
#run 'hessen', @DE_bounds;
#run 'thueringen', @DE_bounds;
#run 'sachsen-anhalt', @DE_bounds;
#run 'sachsen', @DE_bounds;
#run 'rheinland-pfalz', @DE_bounds;
run 'baden-wuerttemberg', @DE_bounds;
#run 'bayern', @DE_bounds;
#run 'saarland', @DE_bounds;


sub run {
	my @params = @_;
	my $filename = shift;
	my $startLon = shift;
	my $startLat = shift;
	my $endLon = shift;
	my $endLat = shift;
	
	get $filename;
	
	my $highwaysQuery = "type='motorway' OR type='trunk' OR type='primary'";
	
#	my $latStep = .5;
#	my $lonStep = 1;
	my $latStep = 1;
	my $lonStep = 2;
	for (my $lat = $startLat; $lat <= $endLat; $lat += $latStep) {
		for (my $lon = $startLon; $lon <= $endLon; $lon += $lonStep) {
			ogr2ogr $filename,
					('roads.shp' => 'n' . $lat . '-e' . $lon . '.shp'),
					'-where', $highwaysQuery, '-spat', $lon, $lat, $lon + $lonStep, $lat + $latStep;
		}
	}
	
	clean $filename;
	
	sieve @params;
}


sub get {
	my $filename = shift;
	
	my $shapefilename = $filename . '.shp';
	my $zipfilename = $shapefilename . '.zip';
	my $serverpath = '/openstreetmap/europe/germany/' . $zipfilename;
	
	if ($DOWNLOAD) {
		my $uri = 'http://download.geofabrik.de' . $serverpath;
		print "Downloading $zipfilename...\n";
		system('curl', '-L', '-O', $uri) == 0 or die "DL $uri failed";
#		LWP::Simple::mirror($uri, $zipfilename);
	}
	
	unless (-d $shapefilename) {
		File::Path::make_path $shapefilename or die "creating dir $shapefilename failed";
	}
	system('unzip', '-u', '-d', $shapefilename, $zipfilename, 'roads.*') == 0 or die "unzip $zipfilename failed";
	if ($DOWNLOAD) {
#		unlink $zipfilename or die "unlink $zipfilename failed";
	}
	
	if (-d $filename) {
		File::Path::remove_tree $filename;
	}
	File::Path::make_path $filename or die "creating dir $filename failed";
}


sub ogr2ogr {
	my $filename = shift;
	my $from = shift;
	my $to = shift;
	my @options = @_;
	
	my $exit = system('ogr2ogr', @options, "${filename}/$to", "${filename}.shp/$from");
	if ($exit != 0) {
		die "'ogr2ogr " . join(' ', @options) . " $to $from' failed";
	}
}


sub clean {
	my $filename = shift;
	
	File::Path::remove_tree $filename . '.shp';
}


sub sieve {
	my $filename = shift;
	my $startLon = shift;
	my $startLat = shift;
	my $endLon = shift;
	my $endLat = shift;
	
#	my $latStep = .5;
#	my $lonStep = 1;
	my $latStep = 1;
	my $lonStep = 2;
	for (my $lat = $startLat; $lat <= $endLat; $lat += $latStep) {
		for (my $lon = $startLon; $lon <= $endLon; $lon += $lonStep) {
			
			my $shapefile = $filename . '/n' . $lat . '-e' . $lon;
			my $maxFileSize = List::Util::max(
					-e ($shapefile . '.dbf') ? -s ($shapefile . '.dbf') : 0,
					-e ($shapefile . '.prj') ? -s ($shapefile . '.prj') : 0,
					-e ($shapefile . '.shp') ? -s ($shapefile . '.shp') : 0,
					-e ($shapefile . '.shx') ? -s ($shapefile . '.shx') : 0,
					0);
			
			if ($maxFileSize < 500) {
				unlink $shapefile . '.dbf';
				unlink $shapefile . '.prj';
				unlink $shapefile . '.shp';
				unlink $shapefile . '.shx';
			}
		}
	}
}


1;

__END__
