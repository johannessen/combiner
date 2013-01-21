#------------------------------------------------------------------------------
#
#  osmium_to_spatiallite_test
#
#------------------------------------------------------------------------------

CXX = g++
#CXX = clang++

CXXFLAGS = -g
#CXXFLAGS = -O3 -g

CXXFLAGS += -Wall -Wextra -Wdisabled-optimization -pedantic -Wctor-dtor-privacy -Wnon-virtual-dtor -Woverloaded-virtual -Wsign-promo -Wno-long-long

CXXFLAGS_GEOS    = $(shell geos-config --cflags)
CXXFLAGS_LIBXML2 = $(shell xml2-config --cflags)
CXXFLAGS_OGR     = $(shell gdal-config --cflags)

CXXFLAGS += -D_LARGEFILE_SOURCE -D_FILE_OFFSET_BITS=64
CXXFLAGS += -I../include

# remove this if you do not want debugging to be compiled in
CXXFLAGS += -DOSMIUM_WITH_DEBUG

LIB_EXPAT  = -lexpat
LIB_GD     = -lgd -lz -lm
LIB_GEOS   = $(shell geos-config --libs)
LIB_OGR    = $(shell gdal-config --libs)
LIB_PBF    = -lz -lpthread -lprotobuf-lite -losmpbf
LIB_SHAPE  = -lshp $(LIB_GEOS)
LIB_SQLITE = -lsqlite3
LIB_XML2   = $(shell xml2-config --libs)

LDFLAGS = $(LIB_EXPAT) $(LIB_PBF)

PROGRAMS = \
    kauneberget

.PHONY: all clean

all: $(PROGRAMS)

osmium_to_spatiallite_test: osmium_to_spatiallite_test.cpp
	$(CXX) $(CXXFLAGS) $(CXXFLAGS_OGR) -o $@ $< $(LDFLAGS) $(LIB_OGR)

clean:
	rm -f *.o core $(PROGRAMS)

