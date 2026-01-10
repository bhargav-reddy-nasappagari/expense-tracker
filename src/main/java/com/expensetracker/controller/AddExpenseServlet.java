package com.expensetracker.controller;

import com.expensetracker.service.CategoryService;
import com.expensetracker.service.ExpenseService;
import com.expensetracker.model.Category;

import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

// Logger Imports
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.HashMap;
import java.util.Map;

public class AddExpenseServlet extends BaseServlet {

    private static final Logger log = LoggerFactory.getLogger(AddExpenseServlet.class);
    private final ExpenseService expenseService = new ExpenseService();

    // GET → show form
    @Override
    protected void handleAuthenticatedGet(HttpServletRequest req, HttpServletResponse resp, Long userId)
            throws IOException, ServletException {
        
        log.debug("User {} requested add expense form", userId);

        CategoryService categoryService = new CategoryService();
        List<Category> categories = categoryService.listCategories(userId);
        req.setAttribute("categories", categories);
        req.getRequestDispatcher("/WEB-INF/views/add-expense.jsp").forward(req, resp);
    }

    // POST → actually create the expense
    @Override
    protected void handleAuthenticatedPost(HttpServletRequest req, HttpServletResponse resp, Long userId)
            throws IOException, ServletException {
        
        log.debug("Processing add expense for user {}", userId);

        // Validate parameters before parsing
        String desc = req.getParameter("description");
        String amountStr = req.getParameter("amount");
        String categoryIdStr = req.getParameter("categoryId");
        String dateStr = req.getParameter("expenseDate");

        // Prepare error container
        Map<String, String> errors = new HashMap<>();

        if (desc == null || desc.trim().isEmpty()) {
            errors.put("description", "Description is required.");
        }
        
        // Validate Amount
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
        
        // Validate Category
        Integer categoryId = null;
        if (categoryIdStr == null || categoryIdStr.trim().isEmpty()) {
            errors.put("categoryId", "Please select a category.");
        } else {
            try {
                categoryId = Integer.valueOf(categoryIdStr);
            } catch (NumberFormatException e) {
                errors.put("categoryId", "Invalid category.");
            }
        }
        
        // Validate Date
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

        // Decision: Forward or Proceed?
        if (!errors.isEmpty()) {
            log.warn("Validation failed for add expense (User {}): {}", userId, errors);

            // FAILURE: Send errors + input back to JSP
            req.setAttribute("errors", errors);
            
            // Reload categories so the dropdown isn't empty
            CategoryService categoryService = new CategoryService();
            req.setAttribute("categories", categoryService.listCategories(userId));
            
            // Forward back to the form (preserves request parameters automatically)
            req.getRequestDispatcher("/WEB-INF/views/add-expense.jsp").forward(req, resp);
            return;
        }

        // Success: Create Expense
        try {
            expenseService.addExpense(userId, desc, amount, categoryId, date);
            log.info("Expense added successfully for user {}", userId);
            resp.sendRedirect(req.getContextPath() + "/expenses"); // success
        } catch (Exception e) {
            log.error("Error adding expense for user {}: {}", userId, e.getMessage(), e);
            throw new ServletException("Error adding expense", e);
        }
    }
}