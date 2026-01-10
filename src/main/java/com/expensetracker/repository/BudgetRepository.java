package com.expensetracker.repository;

import com.expensetracker.model.Budget;
import com.expensetracker.util.HikariCPDataSource; // Assuming utility class exists

import java.math.BigDecimal;
import java.sql.*;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class BudgetRepository {

    public Budget save(Budget budget) {
        String sql = "INSERT INTO budgets (user_id, category_id, amount, period_start, period_end, is_recurring, is_active) " +
                     "VALUES (?, ?, ?, ?, ?, ?, ?)";
        
        try (Connection conn = HikariCPDataSource.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            
            pstmt.setLong(1, budget.getUserId());
            pstmt.setInt(2, budget.getCategoryId());
            pstmt.setBigDecimal(3, budget.getAmount());
            pstmt.setDate(4, Date.valueOf(budget.getPeriodStart()));
            pstmt.setDate(5, budget.getPeriodEnd() != null ? Date.valueOf(budget.getPeriodEnd()) : null);
            pstmt.setBoolean(6, budget.isRecurring());
            pstmt.setBoolean(7, budget.isActive());
            
            int affectedRows = pstmt.executeUpdate();
            
            if (affectedRows == 0) {
                throw new SQLException("Creating budget failed, no rows affected.");
            }

            try (ResultSet generatedKeys = pstmt.getGeneratedKeys()) {
                if (generatedKeys.next()) {
                    budget.setId(generatedKeys.getLong(1));
                } else {
                    throw new SQLException("Creating budget failed, no ID obtained.");
                }
            }
            return budget;
        } catch (SQLException e) {
            throw new RuntimeException("Error saving budget", e);
        }
    }

    public Optional<Budget> findById(Long id) {
        String sql = "SELECT b.*, c.name as category_name " +
                     "FROM budgets b " +
                     "JOIN categories c ON b.category_id = c.id " +
                     "WHERE b.id = ?";
                     
        try (Connection conn = HikariCPDataSource.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            
            pstmt.setLong(1, id);
            try (ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) {
                    Budget budget = mapRowToBudget(rs);
                    // Manually map the joined column that isn't in the base table
                    budget.setCategoryName(rs.getString("category_name")); 
                    return Optional.of(budget);
                }
            }
        } catch (SQLException e) {
            throw new RuntimeException("Error finding budget by ID", e);
        }
        return Optional.empty();
    }

    public List<Budget> findActiveByUserId(Long userId) {
        // Joins with categories to get category name for display
        String sql = "SELECT b.*, c.name as category_name " +
                     "FROM budgets b " +
                     "JOIN categories c ON b.category_id = c.id " +
                     "WHERE b.user_id = ? AND b.is_active = TRUE " +
                     "ORDER BY c.name ASC";
                     
        List<Budget> budgets = new ArrayList<>();
        try (Connection conn = HikariCPDataSource.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            
            pstmt.setLong(1, userId);
            try (ResultSet rs = pstmt.executeQuery()) {
                while (rs.next()) {
                    Budget budget = mapRowToBudget(rs);
                    budget.setCategoryName(rs.getString("category_name"));
                    budgets.add(budget);
                }
            }
        } catch (SQLException e) {
            throw new RuntimeException("Error listing active budgets", e);
        }
        return budgets;
    }

    public boolean existsByUserCategoryAndPeriod(Long userId, Integer categoryId, LocalDate periodStart) {
        String sql = "SELECT COUNT(*) FROM budgets WHERE user_id = ? AND category_id = ? AND period_start = ? AND is_active = TRUE";
        try (Connection conn = HikariCPDataSource.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            
            pstmt.setLong(1, userId);
            pstmt.setInt(2, categoryId);
            pstmt.setDate(3, Date.valueOf(periodStart));
            
            try (ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) {
                    return rs.getInt(1) > 0;
                }
            }
        } catch (SQLException e) {
            throw new RuntimeException("Error checking budget existence", e);
        }
        return false;
    }

    public void update(Budget budget) {
        String sql = "UPDATE budgets SET amount = ?, period_end = ?, is_recurring = ?, updated_at = CURRENT_TIMESTAMP " +
                     "WHERE id = ? AND user_id = ?";
        
        try (Connection conn = HikariCPDataSource.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            
            pstmt.setBigDecimal(1, budget.getAmount());
            pstmt.setDate(2, budget.getPeriodEnd() != null ? Date.valueOf(budget.getPeriodEnd()) : null);
            pstmt.setBoolean(3, budget.isRecurring());
            pstmt.setLong(4, budget.getId());
            pstmt.setLong(5, budget.getUserId());
            
            int affectedRows = pstmt.executeUpdate();
            if (affectedRows == 0) {
                throw new RuntimeException("Budget not found or access denied");
            }
        } catch (SQLException e) {
            throw new RuntimeException("Error updating budget", e);
        }
    }

    public void softDelete(Long budgetId, Long userId) {
        String sql = "UPDATE budgets SET is_active = FALSE WHERE id = ? AND user_id = ?";
        try (Connection conn = HikariCPDataSource.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            
            pstmt.setLong(1, budgetId);
            pstmt.setLong(2, userId);
            pstmt.executeUpdate();
        } catch (SQLException e) {
            throw new RuntimeException("Error deleting budget", e);
        }
    }

    private Budget mapRowToBudget(ResultSet rs) throws SQLException {
        Budget budget = new Budget();
        budget.setId(rs.getLong("id"));
        budget.setUserId(rs.getLong("user_id"));
        budget.setCategoryId(rs.getInt("category_id"));
        budget.setAmount(rs.getBigDecimal("amount"));
        budget.setPeriodStart(rs.getDate("period_start").toLocalDate());
        
        Date periodEnd = rs.getDate("period_end");
        if (periodEnd != null) {
            budget.setPeriodEnd(periodEnd.toLocalDate());
        }
        
        budget.setRecurring(rs.getBoolean("is_recurring"));
        budget.setActive(rs.getBoolean("is_active"));
        budget.setCreatedAt(rs.getTimestamp("created_at").toLocalDateTime());
        
        Timestamp updatedAt = rs.getTimestamp("updated_at");
        if (updatedAt != null) {
            budget.setUpdatedAt(updatedAt.toLocalDateTime());
        }
        return budget;
    }
}