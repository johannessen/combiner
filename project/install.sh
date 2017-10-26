#! /bin/bash

# Usage:
# bash -c "$(curl -fsSL uri://host.example/install.sh)" dirname
# bash -c "$(cat path/to/install.sh)" dirname repository

# halt on error
set -e

TESTDATA_URI="http://dev.thaw.de/temp/highways/nrw-roads.zip"
ARGS4J_URI="http://search.maven.org/remotecontent?filepath=args4j/args4j/2.33/args4j-2.33.jar"
GEOTOOLS_URI="https://downloads.sourceforge.net/project/geotools/GeoTools%2018%20Releases/18.0/geotools-18.0-bin.zip"
#TESTNG_URI="https://web.archive.org/web/20121113133417/http://testng.org/testng-6.8.zip"
TESTNG_URI="http://dev.thaw.de/temp/highways/testng-6.8.zip"
# TestNG: no other version than 6.8 tested
# (Currently there is trouble with certain TestNG releases; the one from the Internet Archive is the only one I know to be good, containing all required classes. To avoid DOSing the Internet Archive's servers, I put up a copy on dev.thaw.de.)

# see if there are local copies of the files to be downloaded, to prevent needless strain on the network
if [[ "`whoami`" == "aj" && "`hostname`" == "Pentland" ]]
then
	TESTDATA_URI="file:///Users/aj/Studium/git/data/testbed-nrw/nrw-roads.zip"
	ARGS4J_URI="file:///Users/aj/Studium/git/project/lib/args4j-2.33.jar"
	GEOTOOLS_URI="file:///Users/aj/Studium/Daten/Attic/sw/geotools-18.0-bin.zip"
	TESTNG_URI="file:///Users/aj/Studium/Daten/Attic/sw/testng-6.8.zip"
fi

REPOSITORY="https://github.com/johannessen/combiner"
if [[ -n "$1" ]] ; then REPOSITORY="$1" ; fi



# clone into current dir, which only works if it is empty
echo "Cloning repository '$REPOSITORY'..."
git clone --recurse-submodules "$REPOSITORY" "$0"
cd "$0"

# install dependencies
# The class path consists of the lib directory and any _immediate_ subdirectories.
cd project/lib

echo "$ARGS4J_URI"
curl -OL "$ARGS4J_URI"

echo "$GEOTOOLS_URI"
curl -OL "$GEOTOOLS_URI"
unzip geotools-18.0-bin.zip

echo "$TESTNG_URI"
curl -OL "$TESTNG_URI"
unzip testng-6.8.zip
mv testng-6.8/testng-6.8.jar .
rm -Rf testng-6.8

# cleanup
rm -f geotools-18.0-bin.zip testng-6.8.zip
cd ../..

# install test dataset(s):
cd data
echo "$TESTDATA_URI"
curl -OL "$TESTDATA_URI"
./testbed-nrw-prepare.sh
rm -f nrw-roads.zip
cd ..

# build and test
cd project
ant clean build doc test
