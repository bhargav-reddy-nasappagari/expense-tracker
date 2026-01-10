package com.expensetracker.util;

import org.apache.commons.codec.digest.DigestUtils;
import java.security.SecureRandom;
import java.time.LocalDateTime;
import java.util.Base64;
import java.util.UUID;

public class TokenUtil {

    private static final SecureRandom secureRandom = new SecureRandom();

    public static String generateNewToken() {
        byte[] randomBytes = new byte[32];
        secureRandom.nextBytes(randomBytes);
        return Base64.getUrlEncoder().withoutPadding().encodeToString(randomBytes);
    }

    public static String generateToken() {
        return UUID.randomUUID().toString();
    }

    public static String hashToken(String token) {
        return DigestUtils.sha256Hex(token);
    }

    /**
     * Checks if verification token has expired (uses config for expiry time)
     */
    public static boolean isTokenExpired(LocalDateTime tokenCreatedAt) {
        if (tokenCreatedAt == null) return true;
        
        int expiryHours = ConfigLoader.getInt("security.token.expiry.hours", 24);
        return tokenCreatedAt.isBefore(LocalDateTime.now().minusHours(expiryHours));
    }

    /**
     * Checks if password reset token has expired
     */
    public static boolean isResetTokenExpired(LocalDateTime tokenCreatedAt) {
        if (tokenCreatedAt == null) return true;
        
        int expiryHours = ConfigLoader.getInt("security.password.reset.hours", 1);
        return tokenCreatedAt.isBefore(LocalDateTime.now().minusHours(expiryHours));
    }

    /**
     * Checks if remember-me token has expired
     */
    public static boolean isRememberMeExpired(LocalDateTime expiresAt) {
        if (expiresAt == null) return true;
        return expiresAt.isBefore(LocalDateTime.now());
    }
}