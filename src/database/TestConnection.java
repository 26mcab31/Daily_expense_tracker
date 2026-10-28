
package database;

import java.sql.Connection;

public class TestConnection {

    public static void main(String[] args) {
        try (Connection connection = DBConnection.getConnection()) {
            if (connection.isValid(5)) {
                System.out.println("Database Connected Successfully!");
            }
        } catch (Exception e) {
            System.out.println("Database connection failed!");
            System.out.println(e.getMessage());
        }
    }
}
