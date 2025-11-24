package config;

import java.io.File;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

public class SQLiteDB {
	// Path to the SQLite file: <project-root>/Database/schema.sqlite
	private static final String DB_PATH = System.getProperty("user.dir")
			+ File.separator + "Database" + File.separator + "schema.sqlite";

	// Returns a new Connection to the SQLite database.
	public static Connection getConnection() throws SQLException {
		String url = "jdbc:sqlite:" + DB_PATH;
		return DriverManager.getConnection(url);
	}

	// Simple check helper. Call from a main/test to verify connectivity.
	public static boolean testConnection() {
		try (Connection c = getConnection()) {
			return c != null && !c.isClosed();
		} catch (SQLException ex) {
			return false;
		}
	}
}
