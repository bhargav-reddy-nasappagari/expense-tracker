package com.expensetracker.repository;

import com.expensetracker.model.Category;
import com.expensetracker.util.HikariCPDataSource;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class CategoryRepository {

    // 1. Get ALL categories for a user (ordered by name)
    public List<Category> findAllByUserId(Long userId) {
        String sql = """
            SELECT * FROM categories
            WHERE user_id = ?
            ORDER BY is_default DESC, name ASC
            """;

        List<Category> categories = new ArrayList<>();
        try (Connection conn = HikariCPDataSource.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setLong(1, userId);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    categories.add(mapRowToCategory(rs));
                }
            }
        } catch (SQLException e) {
            throw new RuntimeException("Error loading categories for user: " + userId, e);
        }
        return categories;
    }

    // 2. Find category by userId + name (case-insensitive) – for duplicate check
    public Optional<Category> findByUserIdAndNameIgnoreCase(Long userId, String name) {
        String sql = "SELECT * FROM categories WHERE user_id = ? AND LOWER(name) = LOWER(?)";

        try (Connection conn = HikariCPDataSource.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setLong(1, userId);
            ps.setString(2, name);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next() ? Optional.of(mapRowToCategory(rs)) : Optional.empty();
            }
        } catch (SQLException e) {
            throw new RuntimeException("Error checking category name", e);
        }
    }

    // 3. Save (INSERT) new category – returns category with generated ID
    public Category save(Category category) {
        String sql = """
            INSERT INTO categories (name, user_id, is_default)
            VALUES (?, ?, ?)
            """;

        try (Connection conn = HikariCPDataSource.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {

            ps.setString(1, category.getName());
            ps.setLong(2, category.getUserId());
            ps.setBoolean(3, category.isDefaultCategory());

            ps.executeUpdate();

            try (ResultSet keys = ps.getGeneratedKeys()) {
                if (keys.next()) {
                    category.setId(keys.getInt(1));
                }
            }
            return category;

        } catch (SQLException e) {
            if ("23000".equals(e.getSQLState()) && e.getMessage().contains("uk_user_category_name")) {
                throw new RuntimeException("Category '" + category.getName() + "' already exists");
            }
            throw new RuntimeException("Failed to save category", e);
        }
    }

    // 4. Delete category (only if not default and no expenses use it – we'll check in service)
    public void delete(Category category) {
        String sql = "DELETE FROM categories WHERE id = ? AND user_id = ?";

        try (Connection conn = HikariCPDataSource.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setInt(1, category.getId());
            ps.setLong(2, category.getUserId());

            int deleted = ps.executeUpdate();
            if (deleted == 0) {
                throw new RuntimeException("Category not found or you don't own it");
            }
        } catch (SQLException e) {
            throw new RuntimeException("Failed to delete category", e);
        }
    }

    //5. Rename category - updates the existing name with new name (only user defined)
    public void update(Category category) {
        String sql = """
            UPDATE categories
            SET name = ?, updated_at = CURRENT_TIMESTAMP
            WHERE id = ? AND user_id = ?
            """;

        try (Connection conn = HikariCPDataSource.getConnection();
            PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setString(1, category.getName());
            ps.setInt(2, category.getId());
            ps.setLong(3, category.getUserId());

            if (ps.executeUpdate() == 0) {
                throw new RuntimeException("Update failed — category not found or not owned");
            }

        } catch (SQLException e) {
            throw new RuntimeException("Failed to update category", e);
        }
    }


    // 6. Security check: does this category belong to this user?
    public boolean existsByIdAndUserId(Integer categoryId, Long userId) {
        String sql = "SELECT 1 FROM categories WHERE id = ? AND user_id = ?";

        try (Connection conn = HikariCPDataSource.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setInt(1, categoryId);
            ps.setLong(2, userId);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next();
            }
        } catch (SQLException e) {
            throw new RuntimeException("Error checking category ownership", e);
        }
    }

    // Helper: ResultSet → Category
    private Category mapRowToCategory(ResultSet rs) throws SQLException {
        Category c = new Category();
        c.setId(rs.getInt("id"));
        c.setName(rs.getString("name"));
        c.setUserId(rs.getLong("user_id"));
        c.setDefaultCategory(rs.getBoolean("is_default"));

        Timestamp ts = rs.getTimestamp("created_at");
        if (ts != null) c.setCreatedAt(ts.toLocalDateTime());

        ts = rs.getTimestamp("updated_at");
        if (ts != null) c.setUpdatedAt(ts.toLocalDateTime());

        return c;
    }
}
