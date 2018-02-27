/* encoding UTF-8
 * 
 * Copyright (c) 2012-13 Arne Johannessen
 * 
 * This project and all of its individual parts may be used in accordance
 * with the terms of the 3-clause BSD licence. See LICENSE for details.
 */

package de.thaw.comb.io;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.Statement;
import java.sql.SQLException;
import java.io.FileNotFoundException;

import org.sqlite.SQLiteJDBCLoader;



/**
 * SQLite database output of non-spatial data. Limited support as of yet;
 * intended for statistics.
 */
public class SQLiteWriter implements StatSink {
	
	Connection connection;
	
	
	public SQLiteWriter () {
		try {
//			System.setProperty("sqlite.purejava", "true");
			Class.forName("org.sqlite.JDBC");
			System.err.println(String.format("SQLite driver running in %s mode", SQLiteJDBCLoader.isNativeMode() ? "native" : "pure-java"));
		}
		catch (Exception e) {
			throw new RuntimeException(e);
		}
	}
	
	
	public SQLiteWriter connect (final String path) {
		try {
			connection = DriverManager.getConnection("jdbc:sqlite:" + path);
		}
		catch (SQLException t) {
			throw new RuntimeException(t);
		}
		finally {
			try {
				connection.close();
			}
			catch (SQLException t) {
				throw new RuntimeException(t);
			}
		}
		return this;
	}
	
	
/*
	private int update (final String query, final Statement statement) {
		try {
			return statement.executeUpdate(query);
		}
		catch (SQLException t) {
			throw new RuntimeException(t);
		}
		finally {
			try {
				statement.close();
			}
			catch (SQLException t) {
				throw new RuntimeException(t);
			}
			finally {
				try {
					connection.close();
				}
				catch (SQLException t) {
					throw new RuntimeException(t);
				}
			}
			return -1;
		}
	}
*/
	
	
	public int update (final String query) {
		Statement statement = null;
		try {
			statement = connection.createStatement();
			return statement.executeUpdate(query);
		}
		catch (SQLException t) {
			throw new RuntimeException(t);
		}
		finally {
			try {
				statement.close();
			}
			catch (SQLException t) {
				throw new RuntimeException(t);
			}
			finally {
				try {
					connection.close();
				}
				catch (SQLException t) {
					throw new RuntimeException(t);
				}
			}
			return -1;
		}
	}
	
	
	public SQLiteWriter createTables () {
		update("DROP TABLE IF EXISTS test;");
		update("CREATE TABLE test (id numeric);");
		return this;
	}
	
}
