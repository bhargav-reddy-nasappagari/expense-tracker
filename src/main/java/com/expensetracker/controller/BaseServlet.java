package com.expensetracker.controller;

import com.expensetracker.service.AuthService;
import com.expensetracker.util.CSRFUtil;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import java.io.IOException;

public abstract class BaseServlet extends HttpServlet {

    private static final Logger log = LoggerFactory.getLogger(BaseServlet.class);

    // ==================== CONFIGURATION OVERRIDES ====================
    
    /**
     * Override this to make endpoints public (no authentication required)
     * Default: true (requires authentication)
     */
    protected boolean requiresAuthentication() {
        return true;
    }

    /**
     * Override this to skip CSRF validation on POST requests
     * Default: true (validates CSRF)
     */
    protected boolean requiresCsrfValidation() {
        return true;
    }

    // ==================== CHILD IMPLEMENTATIONS ====================
    
    /**
     * Handle GET requests with authenticated user
     * Only called if requiresAuthentication() = true
     */
    protected void handleAuthenticatedGet(HttpServletRequest req, HttpServletResponse resp, Long userId) 
            throws ServletException, IOException {
        resp.sendError(HttpServletResponse.SC_METHOD_NOT_ALLOWED);
    }

    /**
     * Handle POST requests with authenticated user
     * Only called if requiresAuthentication() = true
     */
    protected void handleAuthenticatedPost(HttpServletRequest req, HttpServletResponse resp, Long userId) 
            throws ServletException, IOException {
        resp.sendError(HttpServletResponse.SC_METHOD_NOT_ALLOWED);
    }

    /**
     * Handle public GET requests (no authentication)
     * Only called if requiresAuthentication() = false
     */
    protected void handlePublicGet(HttpServletRequest req, HttpServletResponse resp) 
            throws ServletException, IOException {
        resp.sendError(HttpServletResponse.SC_METHOD_NOT_ALLOWED);
    }

    /**
     * Handle public POST requests (no authentication)
     * Only called if requiresAuthentication() = false
     */
    protected void handlePublicPost(HttpServletRequest req, HttpServletResponse resp) 
            throws ServletException, IOException {
        resp.sendError(HttpServletResponse.SC_METHOD_NOT_ALLOWED);
    }

    // ==================== ERROR HANDLER ====================
    
    protected void handleError(HttpServletRequest req, HttpServletResponse resp, Exception e) 
            throws ServletException, IOException {
        log.error("Error handling request: {}", e.getMessage(), e);

        String errorMsg = (e.getMessage() != null) ? e.getMessage() : "An unexpected error occurred";
        req.setAttribute("error", errorMsg);
        req.setAttribute("exception", e); // Keep for debugging
        
        req.getRequestDispatcher("/WEB-INF/views/error.jsp").forward(req, resp);
    }

    // ==================== FINAL METHODS ====================
    
    @Override
    protected final void doGet(HttpServletRequest req, HttpServletResponse resp) 
            throws ServletException, IOException {
        try {
            if (requiresAuthentication()) {
                // AUTHENTICATED ENDPOINT
                AuthService.requireLogin(req, resp);
                if(resp.isCommitted()) return;
                Long userId = AuthService.getCurrentUserId(req.getSession());
                handleAuthenticatedGet(req, resp, userId);
            } else {
                // PUBLIC ENDPOINT
                handlePublicGet(req, resp);
            }
        } catch (RuntimeException e) {
            // CRITICAL: Don't catch redirect exceptions
            if (resp.isCommitted()) {
                // Response already sent (redirect happened)
                return;
            }
            if (e.getMessage() != null && e.getMessage().contains("not logged in")) {
                // This should never happen if requiresAuthentication() is configured correctly
                return;
            }
            handleError(req, resp, e);
        }
    }

    @Override
    protected final void doPost(HttpServletRequest req, HttpServletResponse resp) 
            throws ServletException, IOException {
        try {

            if (requiresAuthentication()) {
                // AUTHENTICATED ENDPOINT
                AuthService.requireLogin(req, resp);
                if(resp.isCommitted()) return;
                Long userId = AuthService.getCurrentUserId(req.getSession());
                // CSRF Check (Global)
                if (requiresCsrfValidation() && !CSRFUtil.validateTokenOrError(req, resp)) {
                    return; // Already sent error response
                }

                handleAuthenticatedPost(req, resp, userId);
            } else {
                // PUBLIC ENDPOINT
                // Public endpoints still need CSRF for POST
                if (requiresCsrfValidation() && !CSRFUtil.validateTokenOrError(req, resp)) {
                    return;
                }
                handlePublicPost(req, resp);
            }
        } catch (RuntimeException e) {
            if (resp.isCommitted()) return;
            handleError(req, resp, e);
        }
    }
}