package com.expensetracker.controller;

import com.expensetracker.model.User;
import com.expensetracker.repository.UserRepository;
import com.expensetracker.util.CSRFUtil;
import com.expensetracker.util.TokenUtil;
import org.mindrot.jbcrypt.BCrypt;

// Logger Imports
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;

import java.io.IOException;
import java.util.Optional;

public class ResetPasswordServlet extends BaseServlet {

    private static final Logger log = LoggerFactory.getLogger(ResetPasswordServlet.class);
    private final UserRepository userRepo = new UserRepository();

    // ==================== CONFIGURATION ====================

    @Override
    protected boolean requiresAuthentication() {
        return false; // ✅ Public Endpoint (User can't be logged in yet)
    }

    @Override
    protected boolean requiresCsrfValidation() {
        return true; // ✅ Enforce CSRF check on POST
    }

    // ==================== PUBLIC HANDLERS ====================

    @Override
    protected void handlePublicGet(HttpServletRequest req, HttpServletResponse resp)
            throws ServletException, IOException {
        
        String rawToken = req.getParameter("token");
        log.debug("Processing password reset link (GET)");

        // 1. Basic Validation
        if (rawToken == null || rawToken.isBlank()) {
            log.warn("Password reset attempt with missing token");
            req.setAttribute("error", "Invalid password reset link.");
            req.getRequestDispatcher("/WEB-INF/views/login.jsp").forward(req, resp);
            return;
        }

        // 2. Verify Token against DB
        // Note: We hash the token from URL to match the hash in DB
        String tokenHash = TokenUtil.hashToken(rawToken);
        Optional<User> userOpt = userRepo.findByValidResetToken(tokenHash);

        if (userOpt.isEmpty()) {
            log.warn("Password reset attempt with invalid or expired token");
            req.setAttribute("error", "This password reset link is invalid or has expired.");
            req.getRequestDispatcher("/WEB-INF/views/login.jsp").forward(req, resp);
            return;
        }

        // 3. Token is valid. Prepare the form.
        
        // Setup Session & CSRF for the upcoming POST
        HttpSession session = req.getSession(true);
        String csrfToken = CSRFUtil.getToken(session);
        req.setAttribute("csrfToken", csrfToken);

        // Pass the raw token back to the JSP so it can be submitted with the POST
        req.setAttribute("token", rawToken); 
        
        log.debug("Token verified, displaying reset password form");
        req.getRequestDispatcher("/WEB-INF/views/reset-password.jsp").forward(req, resp);
    }

    @Override
    protected void handlePublicPost(HttpServletRequest req, HttpServletResponse resp)
            throws ServletException, IOException {

        log.debug("Processing password reset submission (POST)");

        // Note: CSRF is validated by BaseServlet automatically

        String rawToken = req.getParameter("token");
        String password = req.getParameter("password");
        String confirmPassword = req.getParameter("confirmPassword");

        // 1. Match Passwords
        if (password == null || !password.equals(confirmPassword)) {
            log.debug("Password reset failed: Passwords do not match");
            handleError(req, resp, rawToken, "Passwords do not match.");
            return;
        }

        // 2. Verify Token Again (Security Check - Prevent Replay Attacks)
        String tokenHash = TokenUtil.hashToken(rawToken);
        Optional<User> userOpt = userRepo.findByValidResetToken(tokenHash);

        if (userOpt.isEmpty()) {
            log.warn("Password reset failed: Token invalid or expired during POST");
            req.setAttribute("error", "Invalid or expired token. Please request a new link.");
            req.getRequestDispatcher("/WEB-INF/views/login.jsp").forward(req, resp);
            return;
        }

        User user = userOpt.get();

        try {
            // 3. Hash New Password
            String hashedPassword = BCrypt.hashpw(password, BCrypt.gensalt());

            // 4. Update Password in DB
            userRepo.updatePassword(user.getId(), hashedPassword);

            // 5. Invalidate the Token (Critical Security Step)
            // This ensures the link cannot be clicked again
            userRepo.updateResetToken(user.getId(), null, null);

            log.info("Password successfully reset for user {}", user.getUsername());

            // 6. Success! Send to login
            req.getSession().setAttribute("successMessage", "Password successfully reset! Please login.");
            resp.sendRedirect(req.getContextPath() + "/login");

        } catch (Exception e) {
            log.error("Error resetting password for user {}: {}", user.getUsername(), e.getMessage(), e);
            handleError(req, resp, rawToken, "An error occurred while resetting your password.");
        }
    }

    // Helper to reload form with error message and keep the token
    private void handleError(HttpServletRequest req, HttpServletResponse resp, String rawToken, String error) 
            throws ServletException, IOException {
        
        req.setAttribute("error", error);
        req.setAttribute("token", rawToken); // Keep token alive for retry
        
        // Ensure CSRF token is available for re-submission
        HttpSession session = req.getSession(true);
        req.setAttribute("csrfToken", CSRFUtil.getToken(session));

        req.getRequestDispatcher("/WEB-INF/views/reset-password.jsp").forward(req, resp);
    }
}