package com.expensetracker.controller;

import com.expensetracker.dto.RegisterRequest;
import com.expensetracker.model.User;
import com.expensetracker.repository.UserRepository;
import com.expensetracker.service.UserService;
import com.expensetracker.util.CSRFUtil;
import com.expensetracker.util.EmailUtil;
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

public class RegisterServlet extends BaseServlet {

    private static final Logger log = LoggerFactory.getLogger(RegisterServlet.class);
    
    private final UserService userService = new UserService();
    private final UserRepository userRepo = new UserRepository();

    // ==================== CONFIGURATION ====================

    @Override
    protected boolean requiresAuthentication() {
        return false; // ✅ Public Endpoint
    }

    @Override
    protected boolean requiresCsrfValidation() {
        return true; // ✅ Enforce CSRF check on POST
    }

    // ==================== PUBLIC HANDLERS ====================

    @Override
    protected void handlePublicGet(HttpServletRequest req, HttpServletResponse resp)
            throws ServletException, IOException {

        log.debug("Serving registration page");

        // Create a session if it doesn't exist to hold the CSRF token
        HttpSession session = req.getSession(true);
        String csrfToken = CSRFUtil.getToken(session);

        // Set CSRF Token in request scope so that JSP can access it
        req.setAttribute("csrfToken", csrfToken);

        // Show the registration form
        req.getRequestDispatcher("/WEB-INF/views/register.jsp").forward(req, resp);
    }

    @Override
    protected void handlePublicPost(HttpServletRequest req, HttpServletResponse resp)
            throws ServletException, IOException {

        log.debug("Processing registration request");

        // Note: CSRF validation is already handled by BaseServlet because requiresCsrfValidation() is true.

        // 1. Read and build DTO from form parameters
        RegisterRequest request = new RegisterRequest(
                req.getParameter("username"),
                req.getParameter("password"),
                req.getParameter("fullName"),
                req.getParameter("email"),
                req.getParameter("phone")
        );

        try {
            // 2. Delegate to service — all validation + transaction happens here
            User user = userService.register(
                            request.getUsername(),
                            request.getPassword(),
                            request.getFullName(),
                            request.getEmail(),
                            request.getPhone()
                        );
            
            log.info("User registered successfully: {}", user.getUsername());

            // ================= NEW VERIFICATION LOGIC =================

            // 3. Generate & Hash Token
            String token = TokenUtil.generateToken();      // Raw UUID (for email)
            String tokenHash = TokenUtil.hashToken(token); // SHA-256 Hex (for DB)

            // 4. Update User with Verification Details
            user.setVerificationTokenHash(tokenHash);
            user.setTokenCreatedAt(LocalDateTime.now());
            user.setEmailVerified(false);
            user.setLegacyUnverified(false); // New users are NOT legacy

            // 5. Save Updates to DB
            userRepo.updateVerificationStatus(user);

            // 6. Send Verification Email (Async)
            EmailUtil.sendVerificationEmail(user.getEmail(), token);
            
            log.info("Verification email sent to {}", user.getEmail());

            // 7. Forward to "Pending" Page
            // We use setAttribute because we are forwarding internally
            req.setAttribute("email", user.getEmail());
            req.getRequestDispatcher("/WEB-INF/views/verification-pending.jsp").forward(req, resp);

        } catch (IllegalArgumentException e) {
            // Business Error (e.g., Username taken)
            log.warn("Registration failed: {}", e.getMessage());
            handleError(req, resp, request, e.getMessage());
        } catch (Exception e) {
            // System Error
            log.error("Unexpected error during registration: {}", e.getMessage(), e);
            handleError(req, resp, request, "An unexpected error occurred. Please try again.");
        }
    }

    // Helper to reduce code duplication in error handling
    private void handleError(HttpServletRequest req, HttpServletResponse resp, RegisterRequest request, String errorMsg) 
            throws ServletException, IOException {
        
        req.setAttribute("error", errorMsg);
        req.setAttribute("registerRequest", request); // Keep user input
        
        // Regenerate CSRF token so they can try again without refresh
        HttpSession session = req.getSession(true);
        String csrfToken = CSRFUtil.generateToken(session);
        req.setAttribute("csrfToken", csrfToken);
        
        req.getRequestDispatcher("/WEB-INF/views/register.jsp").forward(req, resp);
    }
}