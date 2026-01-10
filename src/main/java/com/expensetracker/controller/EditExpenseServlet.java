package com.expensetracker.controller;

import com.expensetracker.model.Category;
import com.expensetracker.model.Expense;
import com.expensetracker.service.CategoryService;
import com.expensetracker.service.ExpenseService;

// Logger Imports
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import java.io.IOException;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class EditExpenseServlet extends BaseServlet {

    private static final Logger log = LoggerFactory.getLogger(EditExpenseServlet.class);
    private final ExpenseService expenseService = new ExpenseService();
    private final CategoryService categoryService = new CategoryService();

    // GET → show edit form
    @Override
    protected void handleAuthenticatedGet(HttpServletRequest req, HttpServletResponse resp, Long userId)
            throws IOException, ServletException {

        String idStr = req.getParameter("id");
        log.debug("User {} requested edit form for expense ID: {}", userId, idStr);

        if (idStr == null || idStr.trim().isEmpty()) {
            log.warn("Missing expense ID for edit request by user {}", userId);
            resp.sendRedirect(req.getContextPath() + "/expenses");
            return;
        }

        try {
            Long expenseId = Long.valueOf(idStr);

            // 1. Fetch Expense
            Expense expense = expenseService.getExpenseForEdit(userId, expenseId);
            
            if (expense == null) {
                log.warn("Expense {} not found or access denied for user {}", expenseId, userId);
                resp.sendRedirect(req.getContextPath() + "/expenses?error=Expense+not+found");
                return;
            }

            // 2. Fetch Categories (for the dropdown)
            List<Category> categories = categoryService.listCategories(userId);

            req.setAttribute("expense", expense);
            req.setAttribute("categories", categories);

            req.getRequestDispatcher("/WEB-INF/views/edit-expense.jsp").forward(req, resp);

        } catch (NumberFormatException e) {
            log.warn("Invalid expense ID format '{}' provided by user {}", idStr, userId);
            resp.sendRedirect(req.getContextPath() + "/expenses");
        }
    }

    // POST → process update
    @Override
    protected void handleAuthenticatedPost(HttpServletRequest req, HttpServletResponse resp, Long userId)
            throws IOException, ServletException {
        
        String idStr = req.getParameter("id");
        log.debug("Processing expense update for user {}, ID: {}", userId, idStr);

        // 1. Parse Parameters
        String desc = req.getParameter("description");
        String amountStr = req.getParameter("amount");
        String categoryIdStr = req.getParameter("categoryId");
        String dateStr = req.getParameter("expenseDate");

        Map<String, String> errors = new HashMap<>();

        // 2. Validate Inputs
        Long expenseId = null;
        try {
            expenseId = Long.valueOf(idStr);
        } catch (NumberFormatException e) {
            log.error("Invalid expense ID in POST for user {}: {}", userId, idStr);
            resp.sendRedirect(req.getContextPath() + "/expenses");
            return;
        }

        if (desc == null || desc.trim().isEmpty()) {
            errors.put("description", "Description is required.");
        }

        BigDecimal amount = null;
        if (amountStr == null || amountStr.trim().isEmpty()) {
            errors.put("amount", "Amount is required.");
        } else {
            try {
                amount = new BigDecimal(amountStr);
                if (amount.compareTo(BigDecimal.ZERO) <= 0) {
                    errors.put("amount", "Amount must be positive.");
                }
            } catch (NumberFormatException e) {
                errors.put("amount", "Invalid number format.");
            }
        }

        Integer categoryId = null;
        try {
            categoryId = Integer.parseInt(categoryIdStr);
        } catch (NumberFormatException e) {
            errors.put("categoryId", "Invalid category.");
        }

        LocalDate date = null;
        if (dateStr == null || dateStr.trim().isEmpty()) {
            errors.put("expenseDate", "Date is required.");
        } else {
            try {
                date = LocalDate.parse(dateStr);
                if (date.isAfter(LocalDate.now())) {
                    errors.put("expenseDate", "Date cannot be in the future.");
                }
            } catch (Exception e) {
                errors.put("expenseDate", "Invalid date format.");
            }
        }

        // 3. Handle Errors
        if (!errors.isEmpty()) {
            log.warn("Validation failed for expense update (User {}, ID {}): {}", userId, expenseId, errors);
            
            req.setAttribute("errors", errors);
            
            // Reload categories for the dropdown
            req.setAttribute("categories", categoryService.listCategories(userId));
            
            // Re-construct a temporary expense object to fill the form back
            // (We don't query DB again to preserve user's invalid input for correction)
            Expense tempExpense = new Expense();
            tempExpense.setId(expenseId);
            tempExpense.setDescription(desc);
            // Handle nulls safely for display
            if(amount != null) tempExpense.setAmount(amount);
            if(date != null) tempExpense.setExpenseDate(date);
            if(categoryId != null) tempExpense.setCategoryId(categoryId);
            
            req.setAttribute("expense", tempExpense);
            
            req.getRequestDispatcher("/WEB-INF/views/edit-expense.jsp").forward(req, resp);
            return;
        }

        // 4. Success - Update Data
        try {
            expenseService.editExpense(userId, expenseId, desc, amount, categoryId, date);
            log.info("Expense {} updated successfully for user {}", expenseId, userId);
            resp.sendRedirect(req.getContextPath() + "/expenses");
        } catch (Exception e) {
            log.error("Error updating expense {} for user {}: {}", expenseId, userId, e.getMessage(), e);
            throw new ServletException("System error processing expense update", e);
        }
    }
}