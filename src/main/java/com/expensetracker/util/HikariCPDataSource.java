package com.expensetracker.util;

import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;
import java.sql.Connection;
import java.sql.SQLException;

public class HikariCPDataSource {

    private static final HikariDataSource dataSource;

    static {
        HikariConfig config = new HikariConfig();
        
        // Use ConfigLoader instead of direct file reading
        config.setJdbcUrl(ConfigLoader.get("db.url"));
        config.setUsername(ConfigLoader.get("db.username"));
        config.setPassword(ConfigLoader.get("db.password"));
        config.setDriverClassName("com.mysql.cj.jdbc.Driver");

        // Pool configuration
        config.setMaximumPoolSize(10);
        config.setMinimumIdle(5);
        config.setConnectionTimeout(30000);
        config.setIdleTimeout(600000);
        config.setMaxLifetime(1800000);

        dataSource = new HikariDataSource(config);
    }

    public static Connection getConnection() throws SQLException {
        return dataSource.getConnection();
    }

    public static void close() {
        if (dataSource != null) {
            dataSource.close();
        }
    }
}