package com.expensetracker.controller;

import com.expensetracker.service.UserService;
import com.expensetracker.util.CSRFUtil;

// Logger Imports
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import java.io.IOException;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;

public class ResendVerificationServlet extends BaseServlet {

    private static final Logger log = LoggerFactory.getLogger(ResendVerificationServlet.class);
    private final UserService userService = new UserService();

    // ==================== CONFIGURATION ====================

    @Override
    protected boolean requiresAuthentication() {
        return false; // ✅ Public Endpoint: User cannot login if they need verification
    }

    @Override
    protected boolean requiresCsrfValidation() {
        return true; // ✅ Enforce CSRF check on POST
    }

    // ==================== PUBLIC HANDLERS ====================

    @Override
    protected void handlePublicPost(HttpServletRequest req, HttpServletResponse resp) 
            throws ServletException, IOException {

        // Note: CSRF validation is already handled by BaseServlet because requiresCsrfValidation() is true.

        String username = req.getParameter("username");
        log.debug("Processing verification resend request for username: {}", username);

        try {
            // Delegate logic to service
            userService.resendVerification(username);
            
            log.info("Verification email resent successfully for user: {}", username);
            
            // Redirect to Login Page with Success Message
            // We use URL encoding to ensure the message is safe in the URL
            String msg = URLEncoder.encode("Link Resent! Check your email.", StandardCharsets.UTF_8);
            resp.sendRedirect(req.getContextPath() + "/login?error=" + msg);

        } catch (Exception e) {
            log.warn("Failed to resend verification email for {}: {}", username, e.getMessage());
            
            // Redirect back with error message
            String errorMsg = URLEncoder.encode(e.getMessage(), StandardCharsets.UTF_8);
            resp.sendRedirect(req.getContextPath() + "/login?error=" + errorMsg);
        }
    }
    
    // Explicitly disabling GET for this action if it wasn't already clear
    @Override
    protected void handlePublicGet(HttpServletRequest req, HttpServletResponse resp) 
            throws ServletException, IOException {
        resp.sendError(HttpServletResponse.SC_METHOD_NOT_ALLOWED, "GET not supported for resending verification.");
    }
}