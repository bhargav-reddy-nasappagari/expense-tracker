package com.expensetracker.util;

import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

/**
 * Centralized configuration loader.
 * Loads properties from config.properties and environment variables.
 * Environment variables take precedence over config file.
 */
public final class ConfigLoader {
    
    private static final Properties properties = new Properties();
    private static boolean loaded = false;

    private ConfigLoader() {
        // Utility class - prevent instantiation
    }

    /**
     * Loads configuration on first access (lazy initialization)
     */
    private static synchronized void loadIfNeeded() {
        if (loaded) return;
        
        try (InputStream is = ConfigLoader.class.getClassLoader()
                .getResourceAsStream("config.properties")) {
            
            if (is != null) {
                // config.properties exists - load it
                properties.load(is);
                System.out.println("✅ Configuration loaded from config.properties");
            } else {
                // config.properties not found - rely on environment variables only
                System.out.println("ℹ️ config.properties not found - using environment variables only");
            }
            
            loaded = true;
            
        } catch (IOException e) {
            throw new RuntimeException("Failed to load configuration", e);
        }
    }

    /**
     * Gets configuration value with environment variable override.
     * Priority: Environment Variable > config.properties > default value
     * 
     * @param key Property key (e.g., "db.url")
     * @param defaultValue Fallback if key not found
     * @return Configuration value
     */
    public static String get(String key, String defaultValue) {
        loadIfNeeded();
        
        // 1. Check environment variable (convert dots to underscores, uppercase)
        String envKey = key.replace(".", "_").toUpperCase();
        String envValue = System.getenv(envKey);
        if (envValue != null && !envValue.isBlank()) {
            return envValue;
        }
        
        // 2. Check config.properties
        String propValue = properties.getProperty(key);
        if (propValue != null && !propValue.isBlank()) {
            return propValue;
        }
        
        // 3. Return default
        return defaultValue;
    }

    /**
     * Gets configuration value (throws if not found)
     */
    public static String get(String key) {
        String value = get(key, null);
        if (value == null) {
            throw new RuntimeException("Required configuration key not found: " + key);
        }
        return value;
    }

    /**
     * Gets integer configuration value
     */
    public static int getInt(String key, int defaultValue) {
        String value = get(key, null);
        if (value == null) return defaultValue;
        
        try {
            return Integer.parseInt(value);
        } catch (NumberFormatException e) {
            System.err.println("⚠️ Invalid integer for key '" + key + "': " + value);
            return defaultValue;
        }
    }

    /**
     * Gets boolean configuration value
     */
    public static boolean getBoolean(String key, boolean defaultValue) {
        String value = get(key, null);
        if (value == null) return defaultValue;
        return Boolean.parseBoolean(value);
    }

    /**
     * Reloads configuration (useful for testing)
     */
    public static synchronized void reload() {
        properties.clear();
        loaded = false;
        loadIfNeeded();
    }
}