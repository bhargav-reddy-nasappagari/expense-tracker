package com.expensetracker.repository;

import com.expensetracker.model.Expense;
import com.expensetracker.util.HikariCPDataSource;
import com.expensetracker.util.PagedResult;

import java.math.BigDecimal;
import java.sql.*;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

public class ExpenseRepository {

    // 1. Get ALL expenses for a user (ordered by date desc)
    public List<Expense> findAllByUserId(Long userId) {
        String sql = """
            SELECT * FROM expenses
            WHERE user_id = ?
            ORDER BY expense_date DESC, created_at DESC
            """;

        List<Expense> expenses = new ArrayList<>();
        try (Connection conn = HikariCPDataSource.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setLong(1, userId);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    expenses.add(mapRowToExpense(rs));
                }
            }
        } catch (SQLException e) {
            throw new RuntimeException("Error loading expenses for user: " + userId, e);
        }
        return expenses;
    }

    // 2. Find expense by ID and userId (security check)
    public Optional<Expense> findByIdAndUserId(Long expenseId, Long userId) {
        String sql = "SELECT * FROM expenses WHERE id = ? AND user_id = ?";

        try (Connection conn = HikariCPDataSource.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setLong(1, expenseId);
            ps.setLong(2, userId);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next() ? Optional.of(mapRowToExpense(rs)) : Optional.empty();
            }
        } catch (SQLException e) {
            throw new RuntimeException("Error finding expense", e);
        }
    }

    // 3. Save (INSERT) new expense – returns expense with generated ID
    public Expense save(Expense expense) {
        String sql = """
            INSERT INTO expenses (user_id, description, amount, category_id, expense_date)
            VALUES (?, ?, ?, ?, ?)
            """;

        try (Connection conn = HikariCPDataSource.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {

            ps.setLong(1, expense.getUserId());
            ps.setString(2, expense.getDescription());
            ps.setBigDecimal(3, expense.getAmount());
            ps.setInt(4, expense.getCategoryId());
            ps.setDate(5, Date.valueOf(expense.getExpenseDate()));

            ps.executeUpdate();

            try (ResultSet keys = ps.getGeneratedKeys()) {
                if (keys.next()) {
                    expense.setId(keys.getLong(1));
                }
            }
            return expense;

        } catch (SQLException e) {
            throw new RuntimeException("Failed to save expense", e);
        }
    }

    // 4. Update existing expense
    public Expense update(Expense expense) {
        String sql = """
            UPDATE expenses
            SET description = ?, amount = ?, category_id = ?, expense_date = ?, updated_at = CURRENT_TIMESTAMP
            WHERE id = ? AND user_id = ?
            """;

        try (Connection conn = HikariCPDataSource.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setString(1, expense.getDescription());
            ps.setBigDecimal(2, expense.getAmount());
            ps.setInt(3, expense.getCategoryId());
            ps.setDate(4, Date.valueOf(expense.getExpenseDate()));
            ps.setLong(5, expense.getId());
            ps.setLong(6, expense.getUserId());

            int updated = ps.executeUpdate();
            if (updated == 0) {
                throw new RuntimeException("Expense not found or you don't own it");
            }
            return expense;

        } catch (SQLException e) {
            throw new RuntimeException("Failed to update expense", e);
        }
    }

    // 5. Delete expense (only if owned by user)
    public void delete(Long expenseId, Long userId) {
        String sql = "DELETE FROM expenses WHERE id = ? AND user_id = ?";

        try (Connection conn = HikariCPDataSource.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setLong(1, expenseId);
            ps.setLong(2, userId);

            int deleted = ps.executeUpdate();
            if (deleted == 0) {
                throw new RuntimeException("Expense not found or you don't own it");
            }
        } catch (SQLException e) {
            throw new RuntimeException("Failed to delete expense", e);
        }
    }

    // 6. Get expenses by category and user
    public List<Expense> findByCategoryIdAndUserId(Integer categoryId, Long userId) {
        String sql = """
            SELECT * FROM expenses
            WHERE category_id = ? AND user_id = ?
            ORDER BY expense_date DESC
            """;

        List<Expense> expenses = new ArrayList<>();
        try (Connection conn = HikariCPDataSource.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setInt(1, categoryId);
            ps.setLong(2, userId);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    expenses.add(mapRowToExpense(rs));
                }
            }
        } catch (SQLException e) {
            throw new RuntimeException("Error loading expenses for category", e);
        }
        return expenses;
    }

    // 7. Get expenses with filters (date range, category)
    
    public List<Expense> findByUserIdAndFilters(Long userId, java.time.LocalDate from, java.time.LocalDate to, Integer categoryId, BigDecimal minAmount, BigDecimal maxAmount, String keyword) {
        StringBuilder sql = new StringBuilder("""
            SELECT * FROM expenses
            WHERE user_id = ?
            """);

        List<Object> params = new ArrayList<>();
        params.add(userId);

        if (from != null) {
            sql.append(" AND expense_date >= ?");
            params.add(Date.valueOf(from));
        }
        if (to != null) {
            sql.append(" AND expense_date <= ?");
            params.add(Date.valueOf(to));
        }
        if (categoryId != null) {
            sql.append(" AND category_id = ?");
            params.add(categoryId);
        }
        if (minAmount != null) {
            sql.append(" AND amount >= ?");
            params.add(minAmount);
        }
        if (maxAmount != null) {
            sql.append(" AND amount <= ?");
            params.add(maxAmount);
        }
        if (keyword != null && !keyword.trim().isEmpty()) {
            sql.append(" AND LOWER(description) LIKE ?");
            params.add("%" + keyword.trim().toLowerCase() + "%");
        }
        String column = "expense_date";
        String order = "DESC";
        sql.append(" ORDER BY ").append(column).append(" ").append(order);

        List<Expense> expenses = new ArrayList<>();
        try (Connection conn = HikariCPDataSource.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql.toString())) {

            for (int i = 0; i < params.size(); i++) {
                ps.setObject(i + 1, params.get(i));
            }

            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    expenses.add(mapRowToExpense(rs));
                }
            }
        } catch (SQLException e) {
            throw new RuntimeException("Error loading filtered expenses", e);
        }
        return expenses;
    }
    public BigDecimal sumAmountByCategoryAndDateRange(Long userId, Integer categoryId, LocalDate from, LocalDate to) {
        String sql = "SELECT SUM(amount) FROM expenses WHERE user_id = ? AND category_id = ? AND expense_date BETWEEN ? AND ?";
        try (Connection conn = HikariCPDataSource.getConnection();
            PreparedStatement pstmt = conn.prepareStatement(sql)) {
            
            pstmt.setLong(1, userId);
            pstmt.setInt(2, categoryId);
            pstmt.setDate(3, Date.valueOf(from));
            pstmt.setDate(4, Date.valueOf(to));
            
            try (ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) {
                    BigDecimal sum = rs.getBigDecimal(1);
                    return sum != null ? sum : BigDecimal.ZERO;
                }
            }
        } catch (SQLException e) {
            e.printStackTrace(); // Replace with logger
        }
        return BigDecimal.ZERO;
    }

    
    /**
     * Get paginated expenses with filters and sorting
     * @param userId - User ID
     * @param from - Start date (nullable)
     * @param to - End date (nullable)
     * @param categoryId - Category filter (nullable)
     * @param page - Current page (1-indexed)
     * @param pageSize - Items per page
     * @param sortBy - Column to sort by (date, amount, category, description)
     * @param sortOrder - ASC or DESC
     * @return PagedResult containing expenses and metadata
     */
    public PagedResult<Expense> findByUserIdAndFiltersPaginated(
            Long userId, 
            java.time.LocalDate from, 
            java.time.LocalDate to, 
            Integer categoryId,
            BigDecimal minAmount,
            BigDecimal maxAmount,
            String keyword,
            int page,
            int pageSize,
            String sortBy,
            String sortOrder) {
        
        // Validate page parameters
        if (page < 1) page = 1;
        if (pageSize < 1) pageSize = 20;
        if (pageSize > 100) pageSize = 100; // Max 100 items per page
        
        // Step 1: Build WHERE clause for filters
        StringBuilder whereClause = new StringBuilder("WHERE user_id = ?");
        List<Object> params = new ArrayList<>();
        params.add(userId);
        
        if (from != null) {
            whereClause.append(" AND expense_date >= ?");
            params.add(Date.valueOf(from));
        }
        if (to != null) {
            whereClause.append(" AND expense_date <= ?");
            params.add(Date.valueOf(to));
        }
        if (categoryId != null) {
            whereClause.append(" AND category_id = ?");
            params.add(categoryId);
        }
        // Amount Range Filters --
        if (minAmount != null) {
            whereClause.append(" AND amount >= ?");
            params.add(minAmount);
        }
        if (maxAmount != null) {
            whereClause.append(" AND amount <= ?");
            params.add(maxAmount);
        }

        // Keyword Search (Case Insensitive) --
        if (keyword != null && !keyword.trim().isEmpty()) {
            whereClause.append(" AND LOWER(description) LIKE ?");
            params.add("%" + keyword.trim().toLowerCase() + "%");
        }
        
        // Step 2: Get total count (for pagination metadata)
        String countSql = "SELECT COUNT(*) FROM expenses " + whereClause;
        long totalCount = 0;
        
        try (Connection conn = HikariCPDataSource.getConnection();
            PreparedStatement ps = conn.prepareStatement(countSql)) {
            
            setParameters(ps, params);
            
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    totalCount = rs.getLong(1);
                }
            }
        } catch (SQLException e) {
            throw new RuntimeException("Error counting expenses", e);
        }

        // Optimization: If count is 0, don't query data
        if (totalCount == 0) {
            return new PagedResult<>(new ArrayList<>(), page, pageSize, 0);
        }
        
        // Get paginated data WITH DYNAMIC SORT
        // Resolve safe column and order
        String safeCol = resolveSortColumn(sortBy);
        String safeOrder = resolveSortOrder(sortOrder);

        String dataSql = "SELECT * FROM expenses " + whereClause + 
                        " ORDER BY " + safeCol + " " + safeOrder + ", created_at DESC" +
                        " LIMIT ? OFFSET ?";
        
        List<Expense> expenses = new ArrayList<>();
        int offset = (page - 1) * pageSize;
        
        try (Connection conn = HikariCPDataSource.getConnection();
            PreparedStatement ps = conn.prepareStatement(dataSql)) {
            
            // Set WHERE parameters
            setParameters(ps, params);
            
            // Set LIMIT and OFFSET
            ps.setInt(params.size() + 1, pageSize);
            ps.setInt(params.size() + 2, offset);
            
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    expenses.add(mapRowToExpense(rs));
                }
            }
        } catch (SQLException e) {
            throw new RuntimeException("Error loading paginated expenses", e);
        }
        
        return new PagedResult<>(expenses, page, pageSize, totalCount);
    }


    /**
     * Count expenses by category (for checking before deletion)
     */
    public long countByCategoryId(Integer categoryId, Long userId) {
        String sql = "SELECT COUNT(*) FROM expenses WHERE category_id = ? AND user_id = ?";
        
        try (Connection conn = HikariCPDataSource.getConnection();
            PreparedStatement ps = conn.prepareStatement(sql)) {
            
            ps.setInt(1, categoryId);
            ps.setLong(2, userId);
            
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next() ? rs.getLong(1) : 0;
            }
        } catch (SQLException e) {
            throw new RuntimeException("Error counting expenses by category", e);
        }
    }

    // 11. Count total expenses for user
    public int countByUserId(Long userId) {
        String sql = "SELECT COUNT(*) FROM expenses WHERE user_id = ?";
        try (Connection conn = HikariCPDataSource.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setLong(1, userId);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return rs.getInt(1);
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return 0;
    }

    // 12. Find top expenses by amount
    public List<Expense> findTopExpensesByAmount(Long userId, LocalDate startDate, LocalDate endDate, int limit) {
        List<Expense> expenses = new ArrayList<>();
        String sql = "SELECT * FROM expenses WHERE user_id = ? AND expense_date BETWEEN ? AND ? " +
                     "ORDER BY amount DESC LIMIT ?";
        
        try (Connection conn = HikariCPDataSource.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setLong(1, userId);
            ps.setDate(2, Date.valueOf(startDate));
            ps.setDate(3, Date.valueOf(endDate));
            ps.setInt(4, limit);

            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    expenses.add(mapRowToExpense(rs));
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return expenses;
    }

    // 13. Calculate daily totals for Heatmap
    public Map<LocalDate, BigDecimal> calculateDailyTotals(Long userId, LocalDate startDate, LocalDate endDate) {
        Map<LocalDate, BigDecimal> map = new LinkedHashMap<>();
        String sql = "SELECT expense_date, SUM(amount) as daily_total " +
                     "FROM expenses WHERE user_id = ? AND expense_date BETWEEN ? AND ? " +
                     "GROUP BY expense_date ORDER BY expense_date";

        try (Connection conn = HikariCPDataSource.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setLong(1, userId);
            ps.setDate(2, Date.valueOf(startDate));
            ps.setDate(3, Date.valueOf(endDate));

            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    LocalDate date = rs.getDate("expense_date").toLocalDate();
                    BigDecimal total = rs.getBigDecimal("daily_total");
                    map.put(date, total);
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return map;
    }

    // 14. Calculate day of week distribution
    public Map<String, BigDecimal> calculateDayOfWeekDistribution(Long userId, LocalDate startDate, LocalDate endDate) {
        Map<String, BigDecimal> map = new LinkedHashMap<>();
        // MySQL syntax: DAYOFWEEK returns 1=Sunday, 2=Monday, ..., 7=Saturday
        String sql = "SELECT DAYOFWEEK(expense_date) as day_num, SUM(amount) as total " +
                     "FROM expenses WHERE user_id = ? AND expense_date BETWEEN ? AND ? " +
                     "GROUP BY DAYOFWEEK(expense_date) ORDER BY day_num";

        try (Connection conn = HikariCPDataSource.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setLong(1, userId);
            ps.setDate(2, Date.valueOf(startDate));
            ps.setDate(3, Date.valueOf(endDate));

            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    int dayNum = rs.getInt("day_num");
                    String dayName = switch(dayNum) {
                        case 1 -> "Sunday";
                        case 2 -> "Monday";
                        case 3 -> "Tuesday";
                        case 4 -> "Wednesday";
                        case 5 -> "Thursday";
                        case 6 -> "Friday";
                        case 7 -> "Saturday";
                        default -> "";
                    };
                    if (!dayName.isEmpty()) {
                        map.put(dayName, rs.getBigDecimal("total"));
                    }
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return map;
    }

    // Add this inside ExpenseRepository.java

    public LocalDate findEarliestExpenseDateByUserId(Long userId) {
        String sql = "SELECT MIN(expense_date) FROM expenses WHERE user_id = ?";
        
        try (Connection conn = HikariCPDataSource.getConnection();
            PreparedStatement ps = conn.prepareStatement(sql)) {
            
            ps.setLong(1, userId);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    java.sql.Date date = rs.getDate(1);
                    if (date != null) {
                        return date.toLocalDate();
                    }
                }
            }
        } catch (SQLException e) {
            throw new RuntimeException("Error fetching earliest expense date", e);
        }
        // Return null if no expenses exist yet
        return null; 
    }

    // ==================================================================================
    // HELPERS
    // ==================================================================================
    
    /**
     * Safely resolves the sort column using valid hardcoded safe values only.
     * Always returns a valid column name.
     */
    private String resolveSortColumn(String sortBy) {
        if (sortBy == null || sortBy.isBlank()) {
            return "expense_date";
        }
        return switch(sortBy.trim().toLowerCase()) {
            case "date" -> "expense_date";
            case "amount" -> "amount";
            case "category" -> "category_id";
            case "description" -> "description";
            default -> "expense_date";  
        };
    }

    /**
     * Strict validation for sort order.
     */
    private String resolveSortOrder(String sortOrder) {
        if (sortOrder == null || sortOrder.isBlank()) return "DESC";
        return sortOrder.equalsIgnoreCase("ASC") ? "ASC" : "DESC";
    }
    private void setParameters(PreparedStatement ps, List<Object> params) throws SQLException {
        for (int i = 0; i < params.size(); i++) {
            ps.setObject(i + 1, params.get(i));
        }
    }

    // Helper: ResultSet → Expense
    private Expense mapRowToExpense(ResultSet rs) throws SQLException {
        Expense e = new Expense();
        e.setId(rs.getLong("id"));
        e.setUserId(rs.getLong("user_id"));
        e.setDescription(rs.getString("description"));
        e.setAmount(rs.getBigDecimal("amount"));
        e.setCategoryId(rs.getInt("category_id"));
        e.setExpenseDate(rs.getDate("expense_date").toLocalDate());

        Timestamp ts = rs.getTimestamp("created_at");
        if (ts != null) e.setCreatedAt(ts.toLocalDateTime());

        ts = rs.getTimestamp("updated_at");
        if (ts != null) e.setUpdatedAt(ts.toLocalDateTime());

        return e;
    }
}

