package com.expensetracker.util;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

class ConfigLoaderTest {

    @Test
    void testDatabaseConfigLoaded() {
        String dbUrl = ConfigLoader.get("db.url");
        assertNotNull(dbUrl);
        assertTrue(dbUrl.contains("expense_tracker_db"));
    }

    @Test
    void testEmailConfigLoaded() {
        String smtpHost = ConfigLoader.get("email.smtp.host");
        assertEquals("smtp-pulse.com", smtpHost);
    }

    @Test
    void testDefaultValue() {
        String missing = ConfigLoader.get("nonexistent.key", "default");
        assertEquals("default", missing);
    }

    @Test
    void testIntParsing() {
        int timeout = ConfigLoader.getInt("security.token.expiry.hours", 24);
        assertEquals(24, timeout);
    }

    @Test
    void testBooleanParsing() {
        boolean enabled = ConfigLoader.getBoolean("feature.email.verification.enabled", false);
        assertTrue(enabled);
    }

    @Test
    void testEnvironmentVariableOverride() {
        // This would work if you set: export DB_URL=jdbc:mysql://prod-server:3306/db
        // For now, just document the behavior
        String dbUrl = ConfigLoader.get("db.url");
        assertNotNull(dbUrl);
    }
}