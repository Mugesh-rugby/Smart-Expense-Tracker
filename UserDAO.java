package database;

import model.User;
import utils.PasswordUtil;

import java.sql.*;
import java.util.Optional;

/**
 * UserDAO - Data Access Object for user authentication and management.
 * Uses PreparedStatements exclusively to prevent SQL injection.
 */
public class UserDAO {

    private Connection conn;

    public UserDAO() throws SQLException {
        this.conn = DBConnection.getInstance().getConnection();
    }

    // ── Authentication ─────────────────────────────────────────────────────

    /**
     * Validates credentials and returns the authenticated user, or empty.
     */
    public Optional<User> authenticate(String username, String plainPassword) throws SQLException {
        String sql = "SELECT id, username, email, password_hash, full_name, created_at, is_active "
                   + "FROM users WHERE username = ? AND is_active = 1";
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, username.trim().toLowerCase());
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    String storedHash = rs.getString("password_hash");
                    if (PasswordUtil.verifyPassword(plainPassword, storedHash)) {
                        return Optional.of(mapUser(rs));
                    }
                }
            }
        }
        return Optional.empty();
    }

    // ── Registration ───────────────────────────────────────────────────────

    /**
     * Creates a new user account.
     *
     * @return the generated user id
     * @throws SQLException if username/email already exists
     */
    public int registerUser(User user, String plainPassword) throws SQLException {
        String hash = PasswordUtil.hashPassword(plainPassword);
        String sql  = "INSERT INTO users (username, email, password_hash, full_name) VALUES (?,?,?,?)";
        try (PreparedStatement ps = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            ps.setString(1, user.getUsername().trim().toLowerCase());
            ps.setString(2, user.getEmail().trim().toLowerCase());
            ps.setString(3, hash);
            ps.setString(4, user.getFullName().trim());
            ps.executeUpdate();
            try (ResultSet keys = ps.getGeneratedKeys()) {
                if (keys.next()) return keys.getInt(1);
            }
        }
        throw new SQLException("User registration failed – no ID generated.");
    }

    // ── Lookup helpers ─────────────────────────────────────────────────────

    public boolean usernameExists(String username) throws SQLException {
        String sql = "SELECT 1 FROM users WHERE username = ?";
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, username.trim().toLowerCase());
            try (ResultSet rs = ps.executeQuery()) { return rs.next(); }
        }
    }

    public boolean emailExists(String email) throws SQLException {
        String sql = "SELECT 1 FROM users WHERE email = ?";
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, email.trim().toLowerCase());
            try (ResultSet rs = ps.executeQuery()) { return rs.next(); }
        }
    }

    public Optional<User> findByEmail(String email) throws SQLException {
        String sql = "SELECT id, username, email, password_hash, full_name, created_at, is_active "
                   + "FROM users WHERE email = ?";
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, email.trim().toLowerCase());
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) return Optional.of(mapUser(rs));
            }
        }
        return Optional.empty();
    }

    // ── Update ─────────────────────────────────────────────────────────────

    public boolean updatePassword(int userId, String newPlainPassword) throws SQLException {
        String hash = PasswordUtil.hashPassword(newPlainPassword);
        String sql  = "UPDATE users SET password_hash = ? WHERE id = ?";
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, hash);
            ps.setInt(2, userId);
            return ps.executeUpdate() > 0;
        }
    }

    public boolean updateProfile(int userId, String fullName, String email) throws SQLException {
        String sql = "UPDATE users SET full_name = ?, email = ? WHERE id = ?";
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, fullName.trim());
            ps.setString(2, email.trim().toLowerCase());
            ps.setInt(3, userId);
            return ps.executeUpdate() > 0;
        }
    }

    // ── Mapping ────────────────────────────────────────────────────────────

    private User mapUser(ResultSet rs) throws SQLException {
        User u = new User();
        u.setId(rs.getInt("id"));
        u.setUsername(rs.getString("username"));
        u.setEmail(rs.getString("email"));
        u.setPasswordHash(rs.getString("password_hash"));
        u.setFullName(rs.getString("full_name"));
        Timestamp ts = rs.getTimestamp("created_at");
        if (ts != null) u.setCreatedAt(ts.toLocalDateTime());
        u.setActive(rs.getBoolean("is_active"));
        return u;
    }
}
