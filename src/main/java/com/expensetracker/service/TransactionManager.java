package com.expensetracker.service;

import com.expensetracker.util.HikariCPDataSource;
import java.sql.Connection;
import java.sql.SQLException;

public final class TransactionManager {

    private TransactionManager() {}

    public static void executeInTransaction(RunnableWithException task) {
        Connection conn = null;
        try {
            conn = HikariCPDataSource.getConnection();
            conn.setAutoCommit(false);
            task.run(conn);
            conn.commit();
        } catch (Exception e) {
            if (conn != null) {
                try { conn.rollback(); } catch (SQLException ex) { /* ignore */ }
            }
            throw e instanceof RuntimeException ? (RuntimeException) e : new RuntimeException(e);
        } finally {
            if (conn != null) {
                try { conn.setAutoCommit(true); conn.close(); } catch (SQLException e) { /* ignore */ }
            }
        }
    }

    @FunctionalInterface
    public interface RunnableWithException {
        void run(Connection conn) throws Exception;
    }
}