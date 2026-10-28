
package database;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Properties;

public class DBConnection {

    private static final String CONFIG_ENVIRONMENT_VARIABLE =
            "EXPENSE_DB_CONFIG";

    public static Connection getConnection()
            throws SQLException, ClassNotFoundException {
        Class.forName("com.mysql.cj.jdbc.Driver");
        Properties properties = loadProperties();
        String url = requiredProperty(properties, "db.url");
        String username = requiredProperty(properties, "db.username");
        String password = requiredProperty(properties, "db.password");
        return DriverManager.getConnection(url, username, password);
    }

    private static Properties loadProperties() throws SQLException {
        String configPath = System.getenv(CONFIG_ENVIRONMENT_VARIABLE);
        if (configPath == null || configPath.trim().isEmpty()) {
            throw new SQLException("Database configuration is not available.");
        }

        Path path = Paths.get(configPath);
        Properties properties = new Properties();
        try (InputStream input = Files.newInputStream(path)) {
            properties.load(input);
        } catch (IOException | RuntimeException e) {
            throw new SQLException(
                    "Unable to load external database configuration.", e);
        }
        return properties;
    }

    private static String requiredProperty(Properties properties, String key)
            throws SQLException {
        String value = properties.getProperty(key);
        if (value == null || value.trim().isEmpty()) {
            throw new SQLException(
                    "Required database configuration is missing: " + key);
        }
        return value.trim();
    }

    public static void main(String[] args) {
        try (Connection connection = getConnection()) {
            if (connection.isValid(5)) {
                System.out.println("Database Connected Successfully!");
            }
        } catch (SQLException | ClassNotFoundException e) {
            System.out.println("Database connection failed!");
            System.err.println("Check the external database configuration.");
        }
    }
}
