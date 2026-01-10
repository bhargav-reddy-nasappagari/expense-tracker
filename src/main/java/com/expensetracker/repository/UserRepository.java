package com.expensetracker.repository;

import com.expensetracker.model.User;
import com.expensetracker.util.ConfigLoader;
import com.expensetracker.util.HikariCPDataSource;

import java.sql.*;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.Optional;

public class UserRepository {

    // 1. Find by username (case-insensitive)
    public Optional<User> findByUsernameIgnoreCase(String username) {
        String sql = "SELECT * FROM users WHERE LOWER(username) = LOWER(?)";
        try (Connection conn = HikariCPDataSource.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setString(1, username);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next() ? Optional.of(mapRowToUser(rs)) : Optional.empty();
            }
        } catch (SQLException e) {
            throw new RuntimeException("Error finding user by username: " + username, e);
        }
    }

    // 2. Find by email (case-insensitive)
    public Optional<User> findByEmailIgnoreCase(String email) {
        if (email == null || email.isBlank()) return Optional.empty();

        String sql = "SELECT * FROM users WHERE LOWER(email) = LOWER(?)";
        try (Connection conn = HikariCPDataSource.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setString(1, email);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next() ? Optional.of(mapRowToUser(rs)) : Optional.empty();
            }
        } catch (SQLException e) {
            throw new RuntimeException("Error finding user by email: " + email, e);
        }
    }

    // 3. Save new user + return with generated ID
    public User save(User user) {
        String sql = """
            INSERT INTO users (username, password, full_name, email, phone, email_verified, legacy_unverified)
            VALUES (?, ?, ?, ?, ?, ?, ?)
            """;

        try (Connection conn = HikariCPDataSource.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {

            ps.setString(1, user.getUsername().toLowerCase());
            ps.setString(2, user.getPassword());
            ps.setString(3, user.getFullName());
            ps.setString(4, user.getEmail());
            ps.setString(5, user.getPhone());

            // 1. Not verified yet
            ps.setBoolean(6, false); 
            // 2. Not legacy (they signed up AFTER the feature launch)
            ps.setBoolean(7, false);

            int affected = ps.executeUpdate();
            if (affected == 0) throw new RuntimeException("Failed to insert user");

            try (ResultSet keys = ps.getGeneratedKeys()) {
                if (keys.next()) {
                    user.setId(keys.getLong(1));
                } else {
                    throw new RuntimeException("No ID generated for new user");
                }
            }
            return user;

        } catch (SQLException e) {
            if ("23000".equals(e.getSQLState())) {
                String msg = e.getMessage().toLowerCase();
                if (msg.contains("uk_username")) {
                    throw new RuntimeException("Username already taken");
                }
                if (msg.contains("uk_email")) {
                    throw new RuntimeException("Email already registered");
                }
            }
            throw new RuntimeException("Database error saving user", e);
        }
    }

    // 4. Find by ID
    public Optional<User> findById(Long id) {
        String sql = "SELECT * FROM users WHERE id = ?";
        try (Connection conn = HikariCPDataSource.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setLong(1, id);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next() ? Optional.of(mapRowToUser(rs)) : Optional.empty();
            }
        } catch (SQLException e) {
            throw new RuntimeException("Error finding user by ID: " + id, e);
        }
    }

    // 4.5 NEW: Find by Verification Token Hash (For clicking the email link)
    public Optional<User> findByTokenHash(String tokenHash) {
        String sql = "SELECT * FROM users WHERE verification_token_hash = ?";
        
        try (Connection conn = HikariCPDataSource.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            
            ps.setString(1, tokenHash);
            
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next() ? Optional.of(mapRowToUser(rs)) : Optional.empty();
            }
        } catch (SQLException e) {
            throw new RuntimeException("Error finding user by verification token", e);
        }
    }

    /**
     * 5. Update Remember Me Token
     * Sets the token hash and expiration. Pass nulls to clear them (logout).
     */
    public void updateRememberToken(Long userId, String tokenHash, LocalDateTime expiresAt) {
        String sql = "UPDATE users SET remember_token = ?, remember_expires_at = ? WHERE id = ?";

        try (Connection conn = HikariCPDataSource.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            // 1. Set Token Hash (or null)
            ps.setString(1, tokenHash);

            // 2. Set Expiry (or null)
            if (expiresAt != null) {
                ps.setTimestamp(2, Timestamp.valueOf(expiresAt));
            } else {
                ps.setNull(2, Types.TIMESTAMP);
            }

            // 3. Set ID
            ps.setLong(3, userId);

            ps.executeUpdate();

        } catch (SQLException e) {
            throw new RuntimeException("Failed to update remember token for user ID: " + userId, e);
        }
    }

    /**
     * 6. Find by Valid Remember Token
     * Checks if token matches AND if it has not expired yet.
     */
    public Optional<User> findByRememberToken(String tokenHash) {
        // The SQL handles the expiry check for us -> fast & efficient
        String sql = "SELECT * FROM users WHERE remember_token = ? AND remember_expires_at > CURRENT_TIMESTAMP";

        try (Connection conn = HikariCPDataSource.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setString(1, tokenHash);

            try (ResultSet rs = ps.executeQuery()) {
                return rs.next() ? Optional.of(mapRowToUser(rs)) : Optional.empty();
            }
        } catch (SQLException e) {
            throw new RuntimeException("Error finding user by remember token", e);
        }
    }

    /**
     * 7. Update Reset Token (Forgot Password)
     * Sets the token hash and expiration. Pass nulls to clear them (after successful reset).
     */
    public void updateResetToken(Long userId, String tokenHash, LocalDateTime expiresAt) {
        String sql = "UPDATE users SET reset_token = ?, reset_expires_at = ? WHERE id = ?";
        
        try (Connection conn = HikariCPDataSource.getConnection();
            PreparedStatement ps = conn.prepareStatement(sql)) {
            
            ps.setString(1, tokenHash);
            
            // ✅ ALWAYS compute expiry in application layer
            LocalDateTime expiry = (expiresAt != null) 
                ? expiresAt 
                : LocalDateTime.now().plusHours(
                    ConfigLoader.getInt("security.password.reset.hours", 1)
                );
            ps.setTimestamp(2, Timestamp.valueOf(expiry));
            ps.setLong(3, userId);
            
            ps.executeUpdate();
        } catch (SQLException e) {
            throw new RuntimeException("Failed to update reset token", e);
        }
    }

    /**
     * 8. Find by Valid Reset Token
     * Checks if token matches AND if it has not expired yet.
     */
    // UserRepository.java - findByValidResetToken()
    public Optional<User> findByValidResetToken(String tokenHash) {
        String sql = "SELECT * FROM users WHERE reset_token = ? AND reset_expires_at > UTC_TIMESTAMP()";

        try (Connection conn = HikariCPDataSource.getConnection();
            PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setString(1, tokenHash);

            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return Optional.of(mapRowToUser(rs));
                }
            }

            return Optional.empty();

        } catch (SQLException e) {
            throw new RuntimeException("Error finding user by reset token", e);
        }
    }

    // 9. NEW: Update Verification Status
    // Updates only the verification-related fields for efficiency
    public void updateVerificationStatus(User user) {
        String sql = """
            UPDATE users 
            SET email_verified = ?, 
                verification_token_hash = ?, 
                token_created_at = ?,
                legacy_unverified = ?,
                updated_at = CURRENT_TIMESTAMP
            WHERE id = ?
            """;
            
        try (Connection conn = HikariCPDataSource.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            
            ps.setBoolean(1, user.isEmailVerified());
            ps.setString(2, user.getVerificationTokenHash());
            
            if (user.getTokenCreatedAt() != null) {
                ps.setTimestamp(3, Timestamp.valueOf(user.getTokenCreatedAt()));
            } else {
                ps.setTimestamp(3, null);
            }
            
            ps.setBoolean(4, user.isLegacyUnverified());
            ps.setLong(5, user.getId());
            
            int updated = ps.executeUpdate();
            if (updated == 0) {
                throw new RuntimeException("User not found for verification update: " + user.getId());
            }
        } catch (SQLException e) {
            throw new RuntimeException("Failed to update verification status", e);
        }
    }

    /**
     * Update user profile information (name, email, phone)
     * Does NOT update password - use updatePassword() for that
     */
    public void updateProfile(User user) {
        String sql = """
            UPDATE users
            SET full_name = ?, email = ?, phone = ?, updated_at = CURRENT_TIMESTAMP
            WHERE id = ?
            """;

        try (Connection conn = HikariCPDataSource.getConnection();
            PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setString(1, user.getFullName());
            ps.setString(2, user.getEmail());
            ps.setString(3, user.getPhone());
            ps.setLong(4, user.getId());

            int updated = ps.executeUpdate();
            if (updated == 0) {
                throw new RuntimeException("User not found: " + user.getId());
            }

        } catch (SQLException e) {
            // Handle unique constraint violations
            if ("23000".equals(e.getSQLState())) {
                String msg = e.getMessage().toLowerCase();
                if (msg.contains("uk_email")) {
                    throw new RuntimeException("Email already in use by another account");
                }
            }
            throw new RuntimeException("Failed to update user profile", e);
        }
    }

    /**
     * Update user password (separate method for security)
     * Assumes password is already hashed with BCrypt
     */
    public void updatePassword(Long userId, String hashedPassword) {
        String sql = """
            UPDATE users
            SET password = ?, updated_at = CURRENT_TIMESTAMP
            WHERE id = ?
            """;

        try (Connection conn = HikariCPDataSource.getConnection();
            PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setString(1, hashedPassword);
            ps.setLong(2, userId);

            int updated = ps.executeUpdate();
            if (updated == 0) {
                throw new RuntimeException("User not found: " + userId);
            }

        } catch (SQLException e) {
            throw new RuntimeException("Failed to update password", e);
        }
    }

    /**
     * Check if email is already used by another user
     * Used for validation when updating profile
     */
    public boolean isEmailTakenByAnotherUser(String email, Long excludeUserId) {
        if (email == null || email.isBlank()) return false;

        String sql = "SELECT COUNT(*) FROM users WHERE LOWER(email) = LOWER(?) AND id != ?";
        
        try (Connection conn = HikariCPDataSource.getConnection();
            PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setString(1, email);
            ps.setLong(2, excludeUserId);

            try (ResultSet rs = ps.executeQuery()) {
                return rs.next() && rs.getInt(1) > 0;
            }
        } catch (SQLException e) {
            throw new RuntimeException("Error checking email availability", e);
        }
    }

    // Private helper: ResultSet → User
    private User mapRowToUser(ResultSet rs) throws SQLException {
        User user = new User();
        user.setId(rs.getLong("id"));
        user.setUsername(rs.getString("username"));
        user.setPassword(rs.getString("password"));
        user.setFullName(rs.getString("full_name"));
        user.setEmail(rs.getString("email"));
        user.setPhone(rs.getString("phone"));

        Timestamp ts = rs.getTimestamp("created_at");
        if (ts != null) user.setCreatedAt(ts.toLocalDateTime());

        ts = rs.getTimestamp("updated_at");
        if (ts != null) user.setUpdatedAt(ts.toLocalDateTime());

        //Remember Me Token
        user.setRememberToken(rs.getString("remember_token"));
        
        Timestamp exp = rs.getTimestamp("remember_expires_at");
        if (exp != null) user.setRememberExpiresAt(exp.toLocalDateTime());

        // Reset Token 
        user.setResetToken(rs.getString("reset_token"));
        Timestamp resetExp = rs.getTimestamp("reset_expires_at");
        if (resetExp != null) user.setResetExpiresAt(resetExp.toLocalDateTime());

        user.setEmailVerified(rs.getBoolean("email_verified"));
        user.setVerificationTokenHash(rs.getString("verification_token_hash"));
        
        Timestamp tokenTs = rs.getTimestamp("token_created_at");
        if (tokenTs != null) {
            user.setTokenCreatedAt(tokenTs.toLocalDateTime());
        }
        
        user.setLegacyUnverified(rs.getBoolean("legacy_unverified"));

        return user;
    }
}
