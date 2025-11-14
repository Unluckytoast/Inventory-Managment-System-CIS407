package config;

import java.io.File;

public class TestSQLite {
    public static void main(String[] args) {
        System.out.println("Project working dir: " + System.getProperty("user.dir"));
        System.out.println("Expected DB path: " + System.getProperty("user.dir") + File.separator + "Database" + File.separator + "schema.sqlite");
        try (var c = SQLiteDB.getConnection()) {
            System.out.println("Connection opened: " + (c != null && !c.isClosed()));
        } catch (Exception ex) {
            System.out.println("Connection failed with exception:");
            ex.printStackTrace(System.out);
        }
    }
}
