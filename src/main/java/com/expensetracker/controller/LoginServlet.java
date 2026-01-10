package com.expensetracker.controller;

import com.expensetracker.dto.LoginRequest;
import com.expensetracker.model.User;
import com.expensetracker.repository.UserRepository;
import com.expensetracker.service.UserService;
import com.expensetracker.util.CSRFUtil;
import com.expensetracker.util.ConfigLoader;
import com.expensetracker.util.EmailUtil;
import com.expensetracker.util.TokenUtil;

import jakarta.servlet.ServletException;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.time.LocalDateTime;

public class LoginServlet extends BaseServlet {

    private static final Logger log = LoggerFactory.getLogger(LoginServlet.class);
    
    private final UserService userService = new UserService();
    private final UserRepository userRepository = new UserRepository();

    // ==================== CONFIGURATION ====================
    
    @Override
    protected boolean requiresAuthentication() {
        return false; // ✅ Public endpoint - no auth required
    }

    @Override
    protected boolean requiresCsrfValidation() {
        return true; // ✅ Still validate CSRF on POST
    }

    // ==================== PUBLIC HANDLERS ====================

    @Override
    protected void handlePublicGet(HttpServletRequest req, HttpServletResponse resp)
            throws ServletException, IOException {
        
        log.debug("GET /login - Checking if user already logged in");

        // 1. Check if user is ALREADY logged in
        HttpSession existingSession = req.getSession(false);
        if (existingSession != null && existingSession.getAttribute("user") != null) {
            log.debug("User already authenticated, redirecting to dashboard");
            resp.sendRedirect(req.getContextPath() + "/dashboard");
            return;
        }
        
        // 2. Create session for CSRF token
        HttpSession session = req.getSession(true);
        String csrfToken = CSRFUtil.getToken(session);
        req.setAttribute("csrfToken", csrfToken);

        log.debug("Displaying login form");
        req.getRequestDispatcher("/WEB-INF/views/login.jsp").forward(req, resp);
    }

    @Override
    protected void handlePublicPost(HttpServletRequest req, HttpServletResponse resp)
            throws ServletException, IOException {
        
        String username = req.getParameter("username");
        log.debug("POST /login - Login attempt for username: {}", username);

        // Build DTO from form
        LoginRequest loginRequest = new LoginRequest(
                username,
                req.getParameter("password")
        );

        try {
            // Attempt login
            User user = userService.login(
                    loginRequest.getUsername(),
                    loginRequest.getPassword()
            );

            log.info("Successful login for user: {} (ID: {})", user.getUsername(), user.getId());

            // ==================== SESSION REGENERATION ====================
            // SECURITY: Prevent session fixation attacks
            
            HttpSession oldSession = req.getSession(false);
            if (oldSession != null) {
                log.debug("Invalidating old session for security (session fixation prevention)");
                oldSession.invalidate();
            }

            // Create new session with new ID
            HttpSession newSession = req.getSession(true);
            newSession.setAttribute("user", user);
            
            // Generate new CSRF token
            CSRFUtil.generateToken(newSession);

            // ==================== LEGACY USER HANDLING ====================
            if (user.isLegacyUnverified()) {
                log.info("User {} is legacy unverified, flagging for verification", user.getUsername());
                newSession.setAttribute("needsVerification", true);

                // Auto-send verification email if token expired
                if (TokenUtil.isTokenExpired(user.getTokenCreatedAt())) {
                    log.info("Sending verification email to legacy user: {}", user.getEmail());
                    
                    String token = TokenUtil.generateToken();
                    String tokenHash = TokenUtil.hashToken(token);
                    
                    user.setVerificationTokenHash(tokenHash);
                    user.setTokenCreatedAt(LocalDateTime.now());
                    
                    userRepository.updateVerificationStatus(user);
                    EmailUtil.sendVerificationEmail(user.getEmail(), token);
                    
                    log.debug("Verification email sent to {}", user.getEmail());
                }
            }

            // ==================== REMEMBER ME ====================
            String rememberMe = req.getParameter("rememberMe");

            if ("on".equals(rememberMe)) {
                log.debug("Remember Me enabled for user {}", user.getUsername());
                
                String rawToken = TokenUtil.generateNewToken();
                String tokenHash = TokenUtil.hashToken(rawToken);

                int rememberMeDays = ConfigLoader.getInt("security.remember.me.days", 15);
                LocalDateTime expiresAt = LocalDateTime.now().plusDays(rememberMeDays);
                
                userRepository.updateRememberToken(user.getId(), tokenHash, expiresAt);

                Cookie rememberCookie = new Cookie("remember_token", rawToken);
                rememberCookie.setHttpOnly(true);
                rememberCookie.setPath("/");
                rememberCookie.setMaxAge(rememberMeDays * 24 * 60 * 60);
                rememberCookie.setSecure(req.isSecure());
                resp.addCookie(rememberCookie);
                
                log.debug("Remember Me cookie set for user {}, expires in {} days", 
                          user.getUsername(), rememberMeDays);
            } else {
                // Clear existing remember me cookie
                log.debug("Remember Me not checked, clearing any existing cookie");
                Cookie cookie = new Cookie("remember_token", "");
                cookie.setMaxAge(0);
                cookie.setPath("/");
                resp.addCookie(cookie);
            }

            // Success - redirect to dashboard
            log.info("Login completed successfully for user {}, redirecting to dashboard", 
                     user.getUsername());
            resp.sendRedirect(req.getContextPath() + "/dashboard");

        } catch (IllegalStateException e) {
            // Unverified email
            log.warn("Login failed for {}: Email not verified", username);
            req.setAttribute("showResend", true);
            handleLoginError(req, resp, loginRequest, e.getMessage());

        } catch (IllegalArgumentException e) {
            // Wrong credentials
            log.warn("Login failed for {}: Invalid credentials", username);
            handleLoginError(req, resp, loginRequest, "Invalid username or password");

        } catch (Exception e) {
            // Unexpected error
            log.error("Unexpected error during login for user {}: {}", username, e.getMessage(), e);
            handleLoginError(req, resp, loginRequest, "An error occurred during login. Please try again.");
        }
    }

    // ==================== HELPER METHODS ====================

    private void handleLoginError(HttpServletRequest req, HttpServletResponse resp, 
                                   LoginRequest loginRequest, String errorMessage) 
            throws ServletException, IOException {
        
        req.setAttribute("error", errorMessage);
        req.setAttribute("username", loginRequest.getUsername());
        
        // Regenerate CSRF token
        HttpSession session = req.getSession(true);
        String csrfToken = CSRFUtil.generateToken(session);
        req.setAttribute("csrfToken", csrfToken);
        
        req.getRequestDispatcher("/WEB-INF/views/login.jsp").forward(req, resp);
    }
}