package com.expensetracker.util;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import java.io.IOException;
import java.util.UUID;

public class CSRFUtil {
    
    private static final String CSRF_TOKEN_SESSION_ATTR = "csrfToken";
    private static final String CSRF_TOKEN_PARAM = "csrfToken";

    /**
     * Generate and store CSRF token in session.
     * Call this on GET requests for forms.
     */
    public static String generateToken(HttpSession session) {
        String token = UUID.randomUUID().toString();
        session.setAttribute(CSRF_TOKEN_SESSION_ATTR, token);
        return token;
    }

    /**
     * Get existing token from session (or generate if missing).
     */
    public static String getToken(HttpSession session) {
        String token = (String) session.getAttribute(CSRF_TOKEN_SESSION_ATTR);
        if (token == null) {
            token = generateToken(session);
        }
        return token;
    }

    /**
     * Validate CSRF token from request.
     */
    public static boolean validateToken(HttpServletRequest request) {
        HttpSession session = request.getSession(false);
        if (session == null) {
            return false; // No session = invalid
        }
        
        String sessionToken = (String) session.getAttribute(CSRF_TOKEN_SESSION_ATTR);
        String requestToken = request.getParameter(CSRF_TOKEN_PARAM);
        
        // Both must exist and match
        return sessionToken != null 
            && requestToken != null 
            && sessionToken.equals(requestToken);
    }

    /**
     * Validate token and send error if invalid.
     * Call this at the start of every doPost.
     */
    public static boolean validateTokenOrError(HttpServletRequest request, HttpServletResponse response) 
            throws IOException {
        if (!validateToken(request)) {
            response.sendError(HttpServletResponse.SC_FORBIDDEN, 
                "Security Check Failed: Invalid or missing CSRF token. Please refresh the page.");
            return false;
        }
        return true;
    }
}