package com.expensetracker.util;

import org.flywaydb.core.Flyway;

import jakarta.servlet.ServletContextEvent;
import jakarta.servlet.ServletContextListener;
import jakarta.servlet.annotation.WebListener;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Properties;

@WebListener
public class FlywayInitializer implements ServletContextListener {

    @Override
    public void contextInitialized(ServletContextEvent sce) {
        try {
            // Load database configuration from classpath
            Properties props = new Properties();
            try (InputStream is = getClass().getClassLoader().getResourceAsStream("config.properties")) {
                if (is == null) {
                    throw new RuntimeException("config.properties not found in classpath");
                }
                props.load(is);
            }

            String url = props.getProperty("db.url");
            String username = props.getProperty("db.username");
            String password = props.getProperty("db.password");

            // Configure Flyway
            Flyway flyway = Flyway.configure()
                    .dataSource(url, username, password)
                    .locations("classpath:db/migration")
                    .load();

            // Run migrations
            flyway.migrate();

            System.out.println("Flyway migrations completed successfully!");

        } catch (Exception e) {
            System.err.println("Error running Flyway migrations: " + e.getMessage());
            e.printStackTrace();
            throw new RuntimeException("Failed to initialize database", e);
        }
    }

    @Override
    public void contextDestroyed(ServletContextEvent sce) {
        // Cleanup if needed
    }
}
