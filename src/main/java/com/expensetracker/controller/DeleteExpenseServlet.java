package com.expensetracker.controller;

import com.expensetracker.service.ExpenseService;

// Logger Imports
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import java.io.IOException;

public class DeleteExpenseServlet extends BaseServlet {

    private static final Logger log = LoggerFactory.getLogger(DeleteExpenseServlet.class);
    private final ExpenseService expenseService = new ExpenseService();

    @Override
    protected void handleAuthenticatedGet(HttpServletRequest req, HttpServletResponse resp, Long userId)
            throws IOException {
        // SECURITY: Block GET requests for deletion.
        log.warn("User {} attempted GET on delete expense URL", userId);
        resp.sendError(HttpServletResponse.SC_METHOD_NOT_ALLOWED, "GET not supported for deletion. Use POST.");
    }

    @Override
    protected void handleAuthenticatedPost(HttpServletRequest req, HttpServletResponse resp, Long userId)
            throws IOException, ServletException {
        
        log.debug("Processing delete expense for user {}", userId);

        // Get ID (Check both possible names to be robust)
        String idStr = req.getParameter("expenseId");
        if (idStr == null || idStr.trim().isEmpty()) {
            idStr = req.getParameter("id");
        }

        // Validation: If ID is missing, just go back to list
        if (idStr == null || idStr.trim().isEmpty()) {
            log.warn("Delete expense failed: Missing ID for user {}", userId);
            resp.sendRedirect(req.getContextPath() + "/expenses");
            return;
        }

        try {
            // Parse & Execute
            Long expenseId = Long.valueOf(idStr);
            
            expenseService.deleteExpense(userId, expenseId);

            log.info("Expense {} deleted successfully for user {}", expenseId, userId);

            // Success Redirect
            resp.sendRedirect(req.getContextPath() + "/expenses?message=Expense+deleted+successfully");

        } catch (NumberFormatException e) {
            log.warn("Invalid expense ID format '{}' for user {}", idStr, userId);
            resp.sendRedirect(req.getContextPath() + "/expenses?error=Invalid+Expense+ID");
        } catch (Exception e) {
            log.error("Error deleting expense {} for user {}: {}", idStr, userId, e.getMessage(), e);
            throw new ServletException("Error deleting expense", e);
        }
    }
}