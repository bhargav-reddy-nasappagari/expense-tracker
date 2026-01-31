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
            
            // Read from ENVIRONMENT VARIABLES (set by docker-compose)
            String url = System.getenv("DB_URL");
            String username = System.getenv("DB_USERNAME");
            String password = System.getenv("DB_PASSWORD");

            // Fallback to config.properties if env vars not set (for local dev)
            if (url == null) {
                Properties props = new Properties();
                try (InputStream is = getClass().getClassLoader()
                        .getResourceAsStream("config.properties")) {
                    props.load(is);
                }
                url = props.getProperty("db.url");
                username = props.getProperty("db.username");
                password = props.getProperty("db.password");
            }

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
