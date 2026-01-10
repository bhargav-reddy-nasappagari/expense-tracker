package com.expensetracker.controller;

import com.expensetracker.model.User;
import com.expensetracker.service.UserService;

// Logger Imports
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;

import java.io.IOException;

public class ProfileServlet extends BaseServlet {

    private static final Logger log = LoggerFactory.getLogger(ProfileServlet.class);
    private final UserService userService = new UserService();

    // GET → Display profile page
    @Override
    protected void handleAuthenticatedGet(HttpServletRequest req, HttpServletResponse resp, Long userId)
            throws ServletException, IOException {

        log.debug("Loading profile page for user {}", userId);

        HttpSession session = req.getSession();

        // 1. Move Error Message from Session to Request (Flash Attributes)
        if (session.getAttribute("error") != null) {
            req.setAttribute("error", session.getAttribute("error"));
            session.removeAttribute("error");
        }

        // 2. Move Success Message from Session to Request
        if (session.getAttribute("successMessage") != null) {
            req.setAttribute("successMessage", session.getAttribute("successMessage"));
            session.removeAttribute("successMessage");
        }

        // 3. Refresh user data to ensure latest info is displayed
        // We fetch fresh from DB because session might be stale
        User user = userService.getUserById(userId);
        req.setAttribute("user", user);

        // 4. Forward to view
        req.getRequestDispatcher("/WEB-INF/views/profile.jsp").forward(req, resp);
    }

    // POST → Handle updates (Profile Info or Password)
    @Override
    protected void handleAuthenticatedPost(HttpServletRequest req, HttpServletResponse resp, Long userId)
            throws ServletException, IOException {
        
        String action = req.getParameter("action");
        log.debug("Processing profile action '{}' for user {}", action, userId);

        try {
            if ("updateProfile".equals(action)) {
                handleUpdateProfile(req, resp, userId);
            } else if ("changePassword".equals(action)) {
                handleChangePassword(req, resp, userId);
            } else {
                log.warn("Unknown profile action '{}' for user {}", action, userId);
                resp.sendError(HttpServletResponse.SC_BAD_REQUEST, "Invalid action");
            }
        } catch (Exception e) {
            log.error("Error processing profile action '{}' for user {}: {}", action, userId, e.getMessage(), e);
            req.getSession().setAttribute("error", "An unexpected error occurred: " + e.getMessage());
            resp.sendRedirect(req.getContextPath() + "/profile");
        }
    }

    private void handleUpdateProfile(HttpServletRequest req, HttpServletResponse resp, Long userId) 
            throws IOException {
        
        // CORRECTION: UserService.updateProfile expects (userId, fullName, email, phone)
        String fullName = req.getParameter("fullName");
        String email = req.getParameter("email");
        String phone = req.getParameter("phone");

        try {
            // Update in DB
            userService.updateProfile(userId, fullName, email, phone);

            // Update Session object so header/dashboard reflects changes immediately without re-login
            // We fetch the updated object fresh from DB
            User updatedUser = userService.getUserById(userId);
            req.getSession().setAttribute("user", updatedUser);

            req.getSession().setAttribute("successMessage", "Profile updated successfully!");
            resp.sendRedirect(req.getContextPath() + "/profile");
            
            log.info("Profile updated successfully for user {}", userId);

        } catch (IllegalArgumentException e) {
            log.warn("Profile update validation failed for user {}: {}", userId, e.getMessage());
            req.getSession().setAttribute("error", e.getMessage());
            resp.sendRedirect(req.getContextPath() + "/profile");
        }
    }

    private void handleChangePassword(HttpServletRequest req, HttpServletResponse resp, Long userId) 
            throws IOException {
        
        String currentPassword = req.getParameter("currentPassword");
        String newPassword = req.getParameter("newPassword");
        String confirmPassword = req.getParameter("confirmPassword");

        try {
            // Change password via service
            userService.changePassword(userId, currentPassword, newPassword, confirmPassword);

            log.info("Password changed successfully for user {}", userId);

            // Success message
            req.getSession().setAttribute("successMessage", 
                "Password changed successfully! Please log in again with your new password.");

            // Invalidate session (force re-login for security)
            req.getSession().invalidate();

            // Redirect to login page
            resp.sendRedirect(req.getContextPath() + "/login");

        } catch (IllegalArgumentException e) {
            log.warn("Password change failed for user {}: {}", userId, e.getMessage());
            req.getSession().setAttribute("error", e.getMessage());
            // Redirect back to the password tab
            resp.sendRedirect(req.getContextPath() + "/profile?tab=password");
        }
    }
}