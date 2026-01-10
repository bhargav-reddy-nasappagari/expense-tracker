package com.expensetracker.service;

import com.expensetracker.model.User;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import java.io.IOException;

public final class AuthService {

    private AuthService() {}

    public static User getCurrentUser(HttpSession session) {
        return (User) session.getAttribute("user");
    }

    public static Long getCurrentUserId(HttpSession session) {
        User user = getCurrentUser(session);
        return user != null ? user.getId() : null;
    }

    public static boolean isLoggedIn(HttpSession session) {
        return getCurrentUser(session) != null;
    }

    public static void requireLogin(HttpServletRequest request, HttpServletResponse response) throws IOException {
        if (!isLoggedIn(request.getSession(false))) {
            response.sendRedirect(request.getContextPath() + "/login");
        }
    }
}