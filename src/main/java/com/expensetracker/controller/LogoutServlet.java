package com.expensetracker.controller;

import com.expensetracker.model.User;
import com.expensetracker.repository.UserRepository;

// Logger Imports
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;

import java.io.IOException;

public class LogoutServlet extends BaseServlet {

    private static final Logger log = LoggerFactory.getLogger(LogoutServlet.class);
    private final UserRepository userRepository = new UserRepository();

    // ==================== CONFIGURATION ====================

    @Override
    protected boolean requiresAuthentication() {
        return false; // ✅ Public: Always allow cleanup code to run
    }

    // ==================== PUBLIC HANDLERS ====================

    @Override
    protected void handlePublicGet(HttpServletRequest req, HttpServletResponse resp)
            throws IOException {
        performLogout(req, resp);
    }

    @Override
    protected void handlePublicPost(HttpServletRequest req, HttpServletResponse resp)
            throws IOException {
        performLogout(req, resp);
    }

    // ==================== LOGIC ====================

    private void performLogout(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        log.debug("Processing logout request");

        // 1. Get existing session without creating a new one
        HttpSession session = req.getSession(false);
        
        if (session != null) {
            // A. Retrieve User to clear DB token
            User user = (User) session.getAttribute("user");
            
            if (user != null) {
                log.info("Logging out user: {}", user.getUsername());
                // Clear the token in the database so it can't be used again
                userRepository.updateRememberToken(user.getId(), null, null);
            } else {
                log.debug("Session found, but no user attribute present");
            }

            // B. Invalidate the session
            session.invalidate();
            log.debug("Session invalidated");
        } else {
            log.debug("No active session found during logout");
        }

        // 2. Kill the "Remember Me" Cookie in the browser
        // We do this regardless of session state to ensure clean client state
        Cookie cookie = new Cookie("remember_token", "");
        cookie.setMaxAge(0); // 0 = Delete immediately
        cookie.setPath("/"); // Must match the path used when creating it
        resp.addCookie(cookie);

        // 3. Set cache control headers to prevent browser caching
        resp.setHeader("Cache-Control", "no-cache, no-store, must-revalidate");
        resp.setHeader("Pragma", "no-cache");
        resp.setHeader("Expires", "0");

        // 4. Redirect to login page
        resp.sendRedirect(req.getContextPath() + "/login?loggedout=true");
    }
}