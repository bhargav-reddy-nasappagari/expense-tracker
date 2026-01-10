package com.expensetracker.controller;

import com.expensetracker.model.User;
import com.expensetracker.repository.UserRepository;
import com.expensetracker.util.TokenUtil;

// Logger Imports
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;

import java.io.IOException;
import java.time.LocalDateTime;
import java.util.Optional;

public class VerifyEmailServlet extends BaseServlet {

    private static final Logger log = LoggerFactory.getLogger(VerifyEmailServlet.class);
    private final UserRepository userRepo = new UserRepository();

    // ==================== CONFIGURATION ====================

    @Override
    protected boolean requiresAuthentication() {
        return false; // ✅ Public Endpoint (Link accessed via email)
    }

    // ==================== PUBLIC HANDLERS ====================

    @Override
    protected void handlePublicGet(HttpServletRequest req, HttpServletResponse resp) 
            throws ServletException, IOException {

        String token = req.getParameter("token");
        log.debug("Processing email verification request");

        // 1. Basic Validation
        if (token == null || token.isBlank()) {
            log.warn("Email verification failed: Missing token");
            resp.sendRedirect(req.getContextPath() + "/login?error=Invalid+Link");
            return;
        }

        // 2. Hash the token to look it up (Security Best Practice)
        String tokenHash = TokenUtil.hashToken(token);
        Optional<User> userOpt = userRepo.findByTokenHash(tokenHash);

        if (userOpt.isEmpty()) {
            log.warn("Email verification failed: Token not found or invalid");
            resp.sendRedirect(req.getContextPath() + "/login?error=Invalid+or+Expired+Token");
            return;
        }

        User user = userOpt.get();

        // 3. Check Expiry (24 hours)
        LocalDateTime expiryTime = user.getTokenCreatedAt().plusHours(24);
        if (LocalDateTime.now().isAfter(expiryTime)) {
            log.warn("Email verification failed: Token expired for user {}", user.getUsername());
            resp.sendRedirect(req.getContextPath() + "/login?error=Token+Expired");
            return;
        }

        // 4. Success: Update User Status in DB
        try {
            user.setEmailVerified(true);
            user.setVerificationTokenHash(null); // Clear used token
            user.setTokenCreatedAt(null);
            user.setLegacyUnverified(false); // They are definitely real now
            
            userRepo.updateVerificationStatus(user);
            log.info("User verified successfully: {}", user.getUsername());

            // 5. Update Active Session (if user is currently logged in)
            updateActiveSessionIfExists(req, user);

            // 6. Redirect to Login with success
            resp.sendRedirect(req.getContextPath() + "/login?verified=true");

        } catch (Exception e) {
            log.error("Error verifying email for user {}: {}", user.getUsername(), e.getMessage(), e);
            resp.sendRedirect(req.getContextPath() + "/login?error=Server+Error");
        }
    }

    /**
     * If the user is already logged in on this browser (e.g. in another tab),
     * update their session immediately so they don't have to relogin to remove restrictions.
     */
    private void updateActiveSessionIfExists(HttpServletRequest req, User verifiedUser) {
        HttpSession session = req.getSession(false);
        if (session != null) {
            User sessionUser = (User) session.getAttribute("user");
            
            // Check if the logged-in user matches the one we just verified
            if (sessionUser != null && sessionUser.getId().equals(verifiedUser.getId())) {
                
                // Clear the restriction flag
                session.removeAttribute("needsVerification");
                
                // Update the User object in session with verified status
                sessionUser.setEmailVerified(true);
                sessionUser.setLegacyUnverified(false);
                session.setAttribute("user", sessionUser);
                
                log.debug("Active session updated for user {} (verification restrictions removed)", verifiedUser.getUsername());
            }
        }
    }
}