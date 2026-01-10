package com.expensetracker.filter;

import com.expensetracker.model.User;
import com.expensetracker.repository.UserRepository;
import jakarta.servlet.*;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import java.io.IOException;

public class VerificationFilter implements Filter {

    private final UserRepository userRepo = new UserRepository();

    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain)
            throws IOException, ServletException {
        
        HttpServletRequest req = (HttpServletRequest) request;
        HttpServletResponse resp = (HttpServletResponse) response;
        HttpSession session = req.getSession(false);

        // Only check if user is logged in
        if (session != null && session.getAttribute("user") != null) {
            User sessionUser = (User) session.getAttribute("user");
            
            // ═══════════════════════════════════════════════════════════
            // ✅ FIX: RE-CHECK DB STATUS (Handles Post-Verification Case)
            // ═══════════════════════════════════════════════════════════
            User freshUser = userRepo.findById(sessionUser.getId()).orElse(null);
            
            if (freshUser != null) {
                boolean isVerified = freshUser.isEmailVerified();
                boolean isLegacy = freshUser.isLegacyUnverified();
                
                // If DB says verified, clear the flag
                if (isVerified || !isLegacy) {
                    session.removeAttribute("needsVerification");
                    
                    // Also update session user object to avoid future lookups
                    sessionUser.setEmailVerified(isVerified);
                    sessionUser.setLegacyUnverified(isLegacy);
                    session.setAttribute("user", sessionUser);
                    
                    // Allow request
                    chain.doFilter(request, response);
                    return;
                }
                
                // Still unverified legacy user - block
                resp.sendRedirect(req.getContextPath() + 
                    "/dashboard?warning=Please+verify+your+email+to+access+this+feature");
                return;
            }
        }

        // Allow request to proceed
        chain.doFilter(request, response);
    }
}