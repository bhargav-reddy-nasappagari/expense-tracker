package com.expensetracker.util;

import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;
import java.sql.Connection;
import java.sql.SQLException;

public class HikariCPDataSource {

    // ✅ FIXED: Changed from 'final' to allow lazy initialization
    private static HikariDataSource dataSource = null;

    // ✅ FIXED: Removed static block - now loads on first use instead of class loading
    private static synchronized HikariDataSource getDataSource() {
        if (dataSource == null) {
            HikariConfig config = new HikariConfig();
            
            // Use ConfigLoader with defaults for flexibility
            config.setJdbcUrl(ConfigLoader.get("db.url"));
            config.setUsername(ConfigLoader.get("db.username"));
            config.setPassword(ConfigLoader.get("db.password", ""));
            
            // Allow driver to be configured (useful for H2 in tests vs MySQL in prod)
            String driver = ConfigLoader.get("db.driver", "com.mysql.cj.jdbc.Driver");
            config.setDriverClassName(driver);

            // Pool configuration (also made configurable)
            config.setMaximumPoolSize(ConfigLoader.getInt("db.pool.size", 10));
            config.setMinimumIdle(ConfigLoader.getInt("db.pool.min.idle", 5));
            config.setConnectionTimeout(ConfigLoader.getInt("db.connection.timeout.ms", 30000));
            config.setIdleTimeout(ConfigLoader.getInt("db.idle.timeout.ms", 600000));
            config.setMaxLifetime(ConfigLoader.getInt("db.max.lifetime.ms", 1800000));

            dataSource = new HikariDataSource(config);
            System.out.println("✅ HikariCP DataSource initialized successfully");
        }
        return dataSource;
    }

    public static Connection getConnection() throws SQLException {
        return getDataSource().getConnection();
    }

    public static void close() {
        if (dataSource != null && !dataSource.isClosed()) {
            dataSource.close();
            dataSource = null;
            System.out.println("✅ HikariCP DataSource closed");
        }
    }
}