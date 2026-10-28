package dao;

import database.DBConnection;
import model.UserAccount;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

public class UserDAO {

    public boolean create(String username, String email, String passwordHash)
            throws SQLException, ClassNotFoundException {
        String sql = "INSERT INTO users (username, email, password_hash) "
                + "VALUES (?, ?, ?)";

        try (Connection con = DBConnection.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setString(1, username);
            ps.setString(2, email);
            ps.setString(3, passwordHash);
            ps.executeUpdate();
            return true;
        } catch (SQLException e) {
            if (e.getSQLState() != null
                    && e.getSQLState().startsWith("23")) {
                return false;
            }
            throw e;
        }
    }

    public UserAccount findByUsernameOrEmail(String identifier)
            throws SQLException, ClassNotFoundException {
        String sql = "SELECT user_id, username, email, password_hash "
                + "FROM users WHERE username = ? OR email = ? LIMIT 1";

        try (Connection con = DBConnection.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setString(1, identifier);
            ps.setString(2, identifier);

            try (ResultSet rs = ps.executeQuery()) {
                if (!rs.next()) {
                    return null;
                }
                return new UserAccount(
                        rs.getLong("user_id"),
                        rs.getString("username"),
                        rs.getString("email"),
                        rs.getString("password_hash")
                );
            }
        }
    }
}
