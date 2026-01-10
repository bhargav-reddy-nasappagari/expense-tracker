package com.expensetracker.filter;

import com.expensetracker.model.User;
import com.expensetracker.repository.UserRepository;
import com.expensetracker.util.TokenUtil; 
import jakarta.servlet.*;
import jakarta.servlet.annotation.WebFilter;
import jakarta.servlet.http.*;
import java.io.IOException;
import java.util.Optional;

// Ensure this filter maps to protected URLs (e.g., /dashboard, /profile)
// usually NOT /login or /register
public class AuthenticationFilter implements Filter {

    private static final String CSRF_TOKEN_ATTR = "csrfToken";
    
    // We need the repository to verify tokens
    private final UserRepository userRepository = new UserRepository();

    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain) 
            throws IOException, ServletException {
        
        HttpServletRequest httpRequest = (HttpServletRequest) request;
        HttpServletResponse httpResponse = (HttpServletResponse) response;
        
        // 1. Get session without creating one
        HttpSession session = httpRequest.getSession(false);
        
        boolean isLoggedIn = (session != null && session.getAttribute("user") != null);

        // 2. If not logged in via session → try Remember Me
        if (!isLoggedIn) {
            Cookie[] cookies = httpRequest.getCookies();
            String rawToken = null;

            if (cookies != null) {
                for (Cookie c : cookies) {
                    if ("remember_token".equals(c.getName())) {
                        rawToken = c.getValue();
                        break;
                    }
                }
            }

            if (rawToken != null && !rawToken.isEmpty()) {
                String tokenHash = TokenUtil.hashToken(rawToken);
                
                Optional<User> userOpt = userRepository.findByRememberToken(tokenHash);
                
                if (userOpt.isPresent()) {
                    User user = userOpt.get();

                    // SUCCESS: Create a fresh session and log the user in
                    HttpSession newSession = httpRequest.getSession(true); // creates new if needed
                    newSession.setAttribute("user", user);

                    // Regenerate CSRF token for the new authenticated session
                    com.expensetracker.util.CSRFUtil.generateToken(newSession);

                    isLoggedIn = true; // Now user is authenticated

                    // Optional: log for audit
                    System.out.println("Auto-login successful via Remember Me for: " + user.getUsername());
                } else {
                    // Invalid/expired token → clean up cookie to avoid repeated attempts
                    Cookie killCookie = new Cookie("remember_token", "");
                    killCookie.setMaxAge(0);
                    killCookie.setPath("/");
                    httpResponse.addCookie(killCookie);
                }
            }
        }

        // 3. FINAL CHECK: If still not logged in → redirect to login
        if (!isLoggedIn) {
            // Preserve query params if needed (e.g., ?registered=true)
            String redirectUrl = httpRequest.getContextPath() + "/login";
            if (httpRequest.getQueryString() != null) {
                redirectUrl += "?" + httpRequest.getQueryString();
            }
            httpResponse.sendRedirect(redirectUrl);
            return;
        }

        // 4. Ensure CSRF token exists for authenticated session
        HttpSession currentSession = httpRequest.getSession(); // now guaranteed to exist
        if (currentSession.getAttribute(CSRF_TOKEN_ATTR) == null) {
            com.expensetracker.util.CSRFUtil.generateToken(currentSession);
        }

        // 5. Proceed to protected resource
        chain.doFilter(request, response);
    }
}