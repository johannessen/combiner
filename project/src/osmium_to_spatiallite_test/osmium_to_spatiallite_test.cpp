/*

  Kauneberget  http://www.openstreetmap.org/?mlat=58.73&mlon=8.54&zoom=14
  is a station on the disused
  Treungenbanen  http://no.wikipedia.org/wiki/Treungenbanen#Linjekart


  This is an example tool that converts OSM data to some output format
  like Spatialite or Shapefiles using the OGR library.

*/

/*

kauneberget, aj3

This file is based on an Osmium example (https://github.com/joto/osmium).
Copyright 2012 Jochen Topf <jochen@topf.org> and others (see README).

Osmium is free software: you can redistribute it and/or modify it under the
terms of the GNU Lesser General Public License or (at your option) the GNU
General Public License as published by the Free Software Foundation, either
version 3 of the Licenses, or (at your option) any later version.

Osmium is distributed in the hope that it will be useful, but WITHOUT ANY
WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A
PARTICULAR PURPOSE. See the GNU Lesser General Public License and the GNU
General Public License for more details.

You should have received a copy of the Licenses along with Osmium. If not, see
<http://www.gnu.org/licenses/>.

*/

#include <iostream>
#include <getopt.h>

#include <ogr_api.h>
#include <ogrsf_frmts.h>

#define OSMIUM_WITH_PBF_INPUT
#define OSMIUM_WITH_XML_INPUT

#include <osmium.hpp>
#include <osmium/storage/byid/sparse_table.hpp>
#include <osmium/storage/byid/mmap_file.hpp>
#include <osmium/handler/coordinates_for_ways.hpp>
#include <osmium/geometry/point.hpp>
#include <osmium/geometry/ogr.hpp>

typedef Osmium::Storage::ById::SparseTable<Osmium::OSM::Position> storage_sparsetable_t;
typedef Osmium::Storage::ById::MmapFile<Osmium::OSM::Position> storage_mmap_t;
typedef Osmium::Handler::CoordinatesForWays<storage_sparsetable_t, storage_mmap_t> cfw_handler_t;

class MyOGRHandler : public Osmium::Handler::Base {

    OGRDataSource* m_data_source;
    OGRLayer* m_layer_linestring;
    OGRLayer* m_layer_linestring2;

    storage_sparsetable_t store_pos;
    storage_mmap_t store_neg;
    cfw_handler_t* handler_cfw;
	
	int motorwayWayCount;
	int railwayWayCount;

public:

    MyOGRHandler(const std::string& driver_name, const std::string& filename, const bool countOnly) {
        handler_cfw = new cfw_handler_t(store_pos, store_neg);
		
        OGRRegisterAll();
		
		// (i assmue NULL is the default value anyway, but still...)
		m_data_source = NULL;
		m_layer_linestring = NULL;
		m_layer_linestring2 = NULL;
		
		
		// initialise counters
		motorwayWayCount = 0;
		railwayWayCount = 0;
		
		// the rest of this constructor deals with setting up the output database only;
		// if we don't want to get any output besides the way counts, we're done here
		if (countOnly) {
			return;
		}
		
		
        OGRSFDriver* driver = OGRSFDriverRegistrar::GetRegistrar()->GetDriverByName(driver_name.c_str());
        if (driver == NULL) {
            std::cerr << driver_name << " driver not available.\n";
            exit(1);
        }

        CPLSetConfigOption("OGR_SQLITE_SYNCHRONOUS", "FALSE");
        const char* options[] = { "SPATIALITE=TRUE", NULL };
        m_data_source = driver->CreateDataSource(filename.c_str(), const_cast<char**>(options));
        if (m_data_source == NULL) {
            std::cerr << "Creation of output file failed.\n";
            exit(1);
        }

        OGRSpatialReference sparef;
        sparef.SetWellKnownGeogCS("WGS84");
		
		
		// motorway layer
		
        m_layer_linestring = m_data_source->CreateLayer("motorways", &sparef, wkbLineString, NULL);
        if (m_layer_linestring == NULL) {
            std::cerr << "Layer creation failed.\n";
            exit(1);
        }

        OGRFieldDefn layer_linestring_field_id("id", OFTInteger);
        layer_linestring_field_id.SetWidth(10);

        if (m_layer_linestring->CreateField(&layer_linestring_field_id) != OGRERR_NONE) {
            std::cerr << "Creating id field failed.\n";
            exit(1);
        }

//        OGRFieldDefn layer_linestring_field_type("type", OFTString);
//        layer_linestring_field_type.SetWidth(30);
        OGRFieldDefn layer_linestring_field_type("type", OFTInteger);
        layer_linestring_field_type.SetWidth(5);

        if (m_layer_linestring->CreateField(&layer_linestring_field_type) != OGRERR_NONE) {
            std::cerr << "Creating type field failed.\n";
            exit(1);
        }
		
		
		// railway layer
		
        m_layer_linestring2 = m_data_source->CreateLayer("railways", &sparef, wkbLineString, NULL);
        if (m_layer_linestring2 == NULL) {
            std::cerr << "Layer creation failed.\n";
            exit(1);
        }

        OGRFieldDefn layer_linestring_field_id2("id", OFTInteger);
        layer_linestring_field_id.SetWidth(10);

        if (m_layer_linestring2->CreateField(&layer_linestring_field_id2) != OGRERR_NONE) {
            std::cerr << "Creating id field failed.\n";
            exit(1);
        }
		
        OGRFieldDefn layer_linestring_field_type2("service", OFTString);
        layer_linestring_field_type2.SetWidth(30);

        if (m_layer_linestring2->CreateField(&layer_linestring_field_type2) != OGRERR_NONE) {
            std::cerr << "Creating type field failed.\n";
            exit(1);
        }
		
        OGRFieldDefn layer_linestring_field_type3("usage", OFTString);
        layer_linestring_field_type3.SetWidth(30);

        if (m_layer_linestring2->CreateField(&layer_linestring_field_type3) != OGRERR_NONE) {
            std::cerr << "Creating type field failed.\n";
            exit(1);
        }
		
    }
	
	
	
    ~MyOGRHandler() {
        if (m_data_source == NULL) {
			OGRDataSource::DestroyDataSource(m_data_source);
		}
        delete handler_cfw;
        OGRCleanupAll();
    }

    void init(Osmium::OSM::Meta& meta) {
        handler_cfw->init(meta);
    }

    void node(const shared_ptr<Osmium::OSM::Node const>& node) {
        handler_cfw->node(node);
    }

    void after_nodes() {
        std::cerr << "Memory used for node coordinates storage (approximate):\n  for positive IDs: "
                  << store_pos.used_memory() / (1024 * 1024)
                  << " MiB\n  for negative IDs: "
                  << store_neg.used_memory() / (1024 * 1024)
                  << " MiB\n";
        handler_cfw->after_nodes();
    }
	
	
	
    void way(const shared_ptr<Osmium::OSM::Way>& way) {
        handler_cfw->way(way);
        const char* highway = way->tags().get_value_by_key("highway");
        const char* railway = way->tags().get_value_by_key("railway");
        const char* usage = way->tags().get_value_by_key("usage");
        const char* service = way->tags().get_value_by_key("service");
		
		// determine way type
		//  (is this really the only way to do string comparison?! prolly not...)
		//  (do we need to release this memory...?)
        
		// layer 1: only motorways, trunks and their links
		const char* motorwayLiteral = "motorway";
        const char* motorwayLinkLiteral = "motorway_link";
        const char* trunkLiteral = "trunk";
        const char* trunkLinkLinkLiteral = "trunk_link";
		const int isMotorway = highway && ! strcmp(highway, motorwayLiteral);
		const int isMotorwayLink = highway && ! strcmp(highway, motorwayLinkLiteral);
		const int isTrunk = highway && ! strcmp(highway, trunkLiteral);
		const int isTrunkLink = highway && ! strcmp(highway, trunkLinkLinkLiteral);
		
		// layer 2: only main line railways
        const char* railwayLiteral = "rail";
        const char* spurLiteral = "spur";
        const char* yardLiteral = "yard";
		int isRailway = railway && ! strcmp(railway, railwayLiteral);
		isRailway &= ! service || strcmp(service, spurLiteral) && strcmp(service, yardLiteral);
		
		// obtain a reference to the database layer for this way type
		OGRLayer* layer = NULL;
		if ( isMotorway || isMotorwayLink || isTrunk || isTrunkLink ) {
			motorwayWayCount++;
			layer = m_layer_linestring;
		}
		else if ( isRailway ) {
			railwayWayCount++;
			layer = m_layer_linestring2;
		}
		
		// write ways to database layer;
		// if we're in count-only mode, those layers were never created, therefore layer == NULL
		if (layer) {
            try {
                Osmium::Geometry::LineString linestring(*way);

                OGRFeature* feature = OGRFeature::CreateFeature(layer->GetLayerDefn());
                OGRLineString* ogrlinestring = Osmium::Geometry::create_ogr_geometry(linestring);
                feature->SetGeometry(ogrlinestring);
                feature->SetField("id", way->id());
                if (isMotorway || isMotorwayLink || isTrunk || isTrunkLink) {
					feature->SetField("type",
							isMotorway ? 1 :
							isMotorwayLink ? 2 :
							isTrunk ? 3 :
							isTrunkLink ? 4 :
							0 );
				}
				if (isRailway && usage) {
					feature->SetField("usage", usage);
				}
				if (isRailway && service) {
					feature->SetField("service", service);
				}

                if (layer->CreateFeature(feature) != OGRERR_NONE) {
                    std::cerr << "Failed to create feature.\n";
                    exit(1);
                }

                OGRFeature::DestroyFeature(feature);
                delete ogrlinestring;
            } catch (Osmium::Geometry::IllegalGeometry) {
                std::cerr << "Ignoring illegal geometry for way " << way->id() << ".\n";
            }
        }
    }
	
	

    void after_ways() {
        std::cerr << "Motorway Ways: "
                  << motorwayWayCount
                  << "\nRailway Ways: "
                  << railwayWayCount
                  << "\n";
    }

};

/* ================================================== */

void print_help() {
    std::cout << "kauneberget [OPTIONS] [INFILE [OUTFILE]]\n\n" \
              << "If INFILE is not given stdin is assumed.\n" \
              << "If OUTFILE is not given no output is generated (except for total way count).\n" \
              << "\nOptions:\n" \
              << "  -h, --help           This help message\n" \
              << "  -d, --debug          Enable debugging output\n" \
              << "  -f, --format=FORMAT  Output OGR format (Default: 'SQLite')\n";
}

int main(int argc, char* argv[]) {
    static struct option long_options[] = {
        {"debug",  no_argument, 0, 'd'},
        {"help",   no_argument, 0, 'h'},
        {"format", required_argument, 0, 'f'},
        {0, 0, 0, 0}
    };

    bool debug = false;
	bool countOnly = false;

    std::string output_format("SQLite");

    while (true) {
        int c = getopt_long(argc, argv, "dhf:", long_options, 0);
        if (c == -1) {
            break;
        }

        switch (c) {
            case 'd':
                debug = true;
                break;
            case 'h':
                print_help();
                exit(0);
            case 'f':
                output_format = optarg;
                break;
            default:
                exit(1);
        }
    }

    std::string input_filename;
    std::string output_filename("ogr_out");
    int remaining_args = argc - optind;
    if (remaining_args > 2) {
        std::cerr << "Usage: " << argv[0] << " [OPTIONS] [INFILE [OUTFILE]]" << std::endl;
        exit(1);
    } else if (remaining_args == 2) {
        input_filename =  argv[optind];
        output_filename = argv[optind+1];
    } else if (remaining_args == 1) {
        input_filename =  argv[optind];
		countOnly = true;
    } else {
        input_filename = "-";
    }

    Osmium::OSMFile infile(input_filename);
    MyOGRHandler handler(output_format, output_filename, countOnly);
    Osmium::Input::read(infile, handler);

    google::protobuf::ShutdownProtobufLibrary();
}

