package com.expensetracker.util;

import org.junit.jupiter.api.Test;
import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.*;

class TokenUtilTest {

    @Test
    void testGenerateTokenReturnsValidUuidFormat() {
        String token = TokenUtil.generateToken();
        assertNotNull(token);
        // Regex for UUID v4
        assertTrue(token.matches("^[a-f0-9]{8}-[a-f0-9]{4}-[a-f0-9]{4}-[a-f0-9]{4}-[a-f0-9]{12}$"), 
            "Token should match UUID format");
    }

    @Test
    void testGenerateNewTokenReturnsBase64String() {
        String token = TokenUtil.generateNewToken();
        assertNotNull(token);
        assertTrue(token.length() > 20, "New token should have sufficient entropy/length");
    }

    @Test
    void testHashTokenConsistency() {
        String rawToken = "secure-token-123";
        String hash1 = TokenUtil.hashToken(rawToken);
        String hash2 = TokenUtil.hashToken(rawToken);

        assertEquals(hash1, hash2, "Hashing the same token twice should produce identical results");
        assertEquals(64, hash1.length(), "SHA-256 hash should be 64 characters long (hex)");
    }

    @Test
    void testHashTokenUniqueness() {
        String hash1 = TokenUtil.hashToken("token-a");
        String hash2 = TokenUtil.hashToken("token-b");
        assertNotEquals(hash1, hash2, "Different tokens must produce different hashes");
    }

    @Test
    void testIsTokenExpiredChecks24HourWindow() {
        LocalDateTime now = LocalDateTime.now();
        
        assertFalse(TokenUtil.isTokenExpired(now.minusHours(23)), "Token < 24h old should NOT be expired");
        assertTrue(TokenUtil.isTokenExpired(now.minusHours(25)), "Token > 24h old SHOULD be expired");
    }

    @Test
    void testIsResetTokenExpiredChecks1HourWindow() {
        LocalDateTime now = LocalDateTime.now();
        
        assertFalse(TokenUtil.isResetTokenExpired(now.minusMinutes(59)), "Reset token < 1h old should NOT be expired");
        assertTrue(TokenUtil.isResetTokenExpired(now.minusMinutes(61)), "Reset token > 1h old SHOULD be expired");
    }
}