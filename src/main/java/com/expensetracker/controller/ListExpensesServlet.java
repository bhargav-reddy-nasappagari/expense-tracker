package com.expensetracker.controller;

import com.expensetracker.model.Expense;
import com.expensetracker.service.CategoryService;
import com.expensetracker.service.ExpenseService;
import com.expensetracker.util.PagedResult;

import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

// Logger Imports
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.time.LocalDate;

public class ListExpensesServlet extends BaseServlet {

    private static final Logger log = LoggerFactory.getLogger(ListExpensesServlet.class);
    private final ExpenseService expenseService = new ExpenseService();
    private final CategoryService categoryService = new CategoryService();

    @Override
    protected void handleAuthenticatedGet(HttpServletRequest req, HttpServletResponse resp, Long userId)
            throws ServletException, IOException {

        log.debug("User {} requested expense list", userId);

        // 1. Read optional filters (including Sorting filters) from query params
        String fromStr = req.getParameter("from");
        String toStr = req.getParameter("to");
        String catIdStr = req.getParameter("categoryId");
        String keyword = req.getParameter("keyword");
        String minAmountStr = req.getParameter("minAmount");
        String maxAmountStr = req.getParameter("maxAmount");
        String pageStr = req.getParameter("page");
        String pageSizeStr = req.getParameter("pageSize");
        String sortBy = req.getParameter("sortBy");
        String sortOrder = req.getParameter("sortOrder");

        LocalDate from = (fromStr != null && !fromStr.isBlank()) ? LocalDate.parse(fromStr) : null;
        LocalDate to = (toStr != null && !toStr.isBlank()) ? LocalDate.parse(toStr) : null;
        Integer categoryId = (catIdStr != null && !catIdStr.isBlank()) ? Integer.parseInt(catIdStr) : null;
        
        // Parse Amount Range Filters
        java.math.BigDecimal minAmount = null;
        if (minAmountStr != null && !minAmountStr.isBlank()) {
            try {
                minAmount = new java.math.BigDecimal(minAmountStr);
            } catch (NumberFormatException e) {
                // Ignore invalid amount input
                log.warn("Invalid minAmount format for user {}: {}", userId, minAmountStr);
            }
        }

        java.math.BigDecimal maxAmount = null;
        if (maxAmountStr != null && !maxAmountStr.isBlank()) {
            try {
                maxAmount = new java.math.BigDecimal(maxAmountStr);
            } catch (NumberFormatException e) {
                // Ignore invalid amount input
                log.warn("Invalid maxAmount format for user {}: {}", userId, maxAmountStr);
            }
        }

        // Parse pagination parameters
        int page = 1;
        int pageSize = 20; // Default 20 items per page
        
        try {
            if (pageStr != null && !pageStr.isBlank()) {
                page = Integer.parseInt(pageStr);
                if (page < 1) page = 1;
            }
        } catch (NumberFormatException e) {
            page = 1; // Fallback to page 1 if invalid
        }
        
        try {
            if (pageSizeStr != null && !pageSizeStr.isBlank()) {
                pageSize = Integer.parseInt(pageSizeStr);
                if (pageSize < 5) pageSize = 5;
                if (pageSize > 100) pageSize = 100; // Max 100 per page
            }
        } catch (NumberFormatException e) {
            pageSize = 20; // Fallback to default
        }

        log.debug("Fetching expenses for user {}: page={}, pageSize={}, filters=[from={}, to={}, cat={}]", 
                  userId, page, pageSize, from, to, categoryId);

        // 2. Load paginated expenses
        PagedResult<Expense> pagedResult = expenseService.listExpensesPaginated(
            userId, from, to, categoryId, minAmount, maxAmount, keyword, page, pageSize, sortBy, sortOrder
        );

        // 3. Set attributes for JSP
        req.setAttribute("expenses", pagedResult.getItems());
        req.setAttribute("currentPage", pagedResult.getCurrentPage());
        req.setAttribute("totalPages", pagedResult.getTotalPages());
        req.setAttribute("totalItems", pagedResult.getTotalItems());
        req.setAttribute("pageSize", pagedResult.getPageSize());
        req.setAttribute("hasNext", pagedResult.hasNext());
        req.setAttribute("hasPrevious", pagedResult.hasPrevious());
        req.setAttribute("startIndex", pagedResult.getStartIndex());
        req.setAttribute("endIndex", pagedResult.getEndIndex());
        
        // Load categories for filter dropdown
        req.setAttribute("categories", categoryService.listCategories(userId));

        // Pass filter values back to JSP (for sticky filters)
        req.setAttribute("filterFrom", fromStr);
        req.setAttribute("filterTo", toStr);
        req.setAttribute("filterCategoryId", catIdStr);

        // -- PASS Amount Range & Keyword BACK --
        req.setAttribute("filterKeyword", keyword);
        req.setAttribute("filterMinAmount", minAmountStr);
        req.setAttribute("filterMaxAmount", maxAmountStr);

        // -- Pass Sort Values Back (For Sticky Sorting) --
        req.setAttribute("sortBy", sortBy);
        req.setAttribute("sortOrder", sortOrder);

        // 4. Forward to JSP
        req.getRequestDispatcher("/WEB-INF/views/expenses.jsp").forward(req, resp);
    }
}