package de.thaw.espebu;

import com.vividsolutions.jts.geom.LineString;
import com.vividsolutions.jts.geom.Geometry;
import com.vividsolutions.jts.geom.Coordinate;
//import com.vividsolutions.jts.io.ParseException;
import com.vividsolutions.jts.io.WKTFileReader;
import com.vividsolutions.jts.io.WKTReader;
//import com.vividsolutions.jts.operation.linemerge.LineMerger;

import java.io.File;
//import java.io.FileReader;
//import java.io.IOException;

import java.util.Collection;
import java.util.Iterator;
import java.util.Properties;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.sql.Statement;

/*****************************************************/
/* these imports ensure the JDBC driver is available */
/*****************************************************/
//import org.sqlite.SQLiteConfig;
//import org.sqlite.SQLiteJDBCLoader;

/* see also:
 * http://www.xerial.org/trac/Xerial/wiki/SQLiteJDBC
 * http://www.gaia-gis.it/spatialite-2.4.0-4/splite-jdbc.html
 * http://sourceforge.net/apps/mediawiki/jump-pilot/?title=OpenJUMP_with_SpatialLite
 */



/**
 * A Spatialite database reader. This class crashes the Java VM on loading the
 * Spatialite extension while accessing SQLite through the JDBC. At least,
 * that's what happens with Java 1.6.0_37 under Mac OS X 10.6.8 on a Core 2 Duo
 * processor (OS X 10.6 is not supported by Oracle's Java 7 offering for the
 * Mac, but documentation says Java 6 ought to be enough). I've tried all
 * combinations of versions of the JDBC driver, SQLite and Spatialite that I
 * could easily get my hands on, to no avail.
 * @deprecated Causes JVM crashes.
 */
@Deprecated
final class SpatialiteReader {
	
	SpatialiteReader () throws Throwable {
//		System.setProperty("sqlite.purejava", "true");
		Class.forName("org.sqlite.JDBC");
//		System.out.println(String.format("running in %s mode", SQLiteJDBCLoader.isNativeMode() ? "native" : "pure-java"));
		
		Properties properties = new Properties();
/*
		SQLiteConfig config = new SQLiteConfig();
		config.enableLoadExtension(true);
		properties = config.toProperties();
*/
		
		Connection connection = DriverManager.getConnection("jdbc:sqlite:test.sqlite", properties);
		Statement statement = connection.createStatement();
//		statement.setQueryTimeout(30);
		try {
			statement.execute("SELECT load_extension('libspatialite.2.dylib')");  // segfault
//			statement.execute("SELECT load_extension('/usr/local/Cellar/libspatialite/3.0.1/lib/libspatialite.2.dylib')");
//			statement.execute("SELECT load_extension('/Library/Frameworks/SQLite3.framework/Versions/3/SQLite3')");  // supposed to include libspatialite
		}
		catch (Throwable t) {
			throw t;
		}
/*
		ResultSet rs = statement.executeQuery("select astext(geometry) from motorways");
		while(rs.next()) {
			ResultSetMetaData md = rs.getMetaData();
			int s = md.getColumnCount();
			for (int i = 1; i <= s; i++) {
				Object o = rs.getObject(i);
				System.out.print(o + ", ");
			}
			System.out.println();
		}
*/
		connection.close();
	}
	
/*	Collection getData() throws Exception {
		Collection lines = new ArrayList();
		lines.add(read("LINESTRING (365085.756828781915829 5645997.095345852896571,365052.151363758486696 5645991.072187417186797)"));
		lines.add(read("LINESTRING (363807.539624206954613 5646142.298014994710684,363560.014399671868887 5646207.471485083922744)"));

		return lines;
	}

	Geometry read(String lineWKT) throws Exception {
		return new WKTReader().read(lineWKT);
	}
*/
	
}

