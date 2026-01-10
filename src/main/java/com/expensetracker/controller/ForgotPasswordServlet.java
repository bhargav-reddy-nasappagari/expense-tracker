package com.expensetracker.controller;

import com.expensetracker.model.User;
import com.expensetracker.repository.UserRepository;
import com.expensetracker.util.ConfigLoader;
import com.expensetracker.util.EmailUtil;
import com.expensetracker.util.TokenUtil;

import jakarta.servlet.ServletException;
import jakarta.servlet.http.*;
import java.io.IOException;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.Optional;
import java.util.Random;
import java.util.concurrent.ConcurrentHashMap;

public class ForgotPasswordServlet extends HttpServlet {

    private final UserRepository userRepo = new UserRepository();

    // Track recent requests to prevent duplicates
    private static final ConcurrentHashMap<String, Long> recentRequests = new ConcurrentHashMap<>();
    private static final long DUPLICATE_THRESHOLD_MS = 
    ConfigLoader.getInt("security.forgot.password.cooldown.seconds", 5) * 1000L;

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        // Generate CSRF token for the form
        HttpSession session = req.getSession(true);
        String csrfToken = com.expensetracker.util.CSRFUtil.getToken(session);
        req.setAttribute("csrfToken", csrfToken);

        req.getRequestDispatcher("/WEB-INF/views/forgot-password.jsp").forward(req, resp);
    }

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        
        // Validate CSRF
        if (!com.expensetracker.util.CSRFUtil.validateTokenOrError(req, resp)) {
            return;
        }

        String email = req.getParameter("email");
        
        if (email == null || email.trim().isEmpty()) {
            req.setAttribute("error", "Email is required.");
            req.getRequestDispatcher("/WEB-INF/views/forgot-password.jsp").forward(req, resp);
            return;
        }
        email = email.trim().toLowerCase();
        
        // ✅ CHECK: Prevent duplicate requests within 5 seconds
        String requestKey = email;
        Long lastRequestTime = recentRequests.get(requestKey);
        long currentTime = System.currentTimeMillis();
        
        if (lastRequestTime != null && (currentTime - lastRequestTime) < DUPLICATE_THRESHOLD_MS) {
            
            // Still show success message to user (don't reveal that we detected duplicate)
            req.setAttribute("message", "If an account with that email exists, we have sent a password reset link.");
            req.getRequestDispatcher("/WEB-INF/views/forgot-password.jsp").forward(req, resp);
            return;
        }
        
        // ✅ TRACK: Record this request
        recentRequests.put(requestKey, currentTime);
        
        // ✅ CLEANUP: Remove old entries (older than 10 seconds)
        recentRequests.entrySet().removeIf(entry -> 
            currentTime - entry.getValue() > 10000
        );

        // 1. Check if user exists
        Optional<User> userOpt = userRepo.findByEmailIgnoreCase(email);

        if (userOpt.isPresent()) {
            User user = userOpt.get();

            // 2. Generate secure token & Hash it
            String rawToken = TokenUtil.generateNewToken(); 
            String tokenHash = TokenUtil.hashToken(rawToken);

            // 3. Save to DB - expiresAt is passed as null leaving it fot MySQL to handle
            userRepo.updateResetToken(user.getId(), tokenHash, null);

            // 4. Build the link
            String appUrl = req.getScheme() + "://" + req.getServerName();
            if (req.getServerPort() != 80 && req.getServerPort() != 443) {
                appUrl += ":" + req.getServerPort();
            }
            appUrl += req.getContextPath();
            
            String resetLink = appUrl + "/reset-password?token=" + rawToken;

            System.out.println("Generated reset link: " + resetLink);

            // 6. Send Email
            EmailUtil.sendResetEmail(email, resetLink);

        }

        // ALWAYS send email (to a honeypot) even for non-existent users
        if (userOpt.isEmpty()) {
            try {
                Thread.sleep(200 + new Random().nextInt(300)); // 200-500ms random delay
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                // Proceed gracefully — security timing protection is partially lost, but request continues
            }
        }

        // 7. Always show success message (Security: User Enumeration Prevention)
        req.setAttribute("message", "If an account with that email exists, we have sent a password reset link.");
        req.getRequestDispatcher("/WEB-INF/views/forgot-password.jsp").forward(req, resp);
    }
}