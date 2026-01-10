package com.expensetracker.controller;

import com.expensetracker.service.CategoryService;
import com.expensetracker.service.ExpenseService;
import com.expensetracker.service.BudgetService;
import com.expensetracker.service.ReportService;
import com.expensetracker.repository.BudgetRepository;
import com.expensetracker.repository.CategoryRepository;
import com.expensetracker.repository.ExpenseRepository;
import com.expensetracker.model.Budget;
import com.expensetracker.dto.ReportData;

// Logger Imports
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;

import java.io.IOException;
import java.util.List;
import java.math.BigDecimal;
import java.util.stream.Collectors;

public class DashboardServlet extends BaseServlet {

    private static final Logger log = LoggerFactory.getLogger(DashboardServlet.class);

    private final CategoryService categoryService = new CategoryService();
    private final ExpenseService expenseService = new ExpenseService();
    private final BudgetService budgetService = new BudgetService(new BudgetRepository(), new CategoryRepository(), new ExpenseRepository());
    private final ReportService reportService = new ReportService(new ExpenseRepository(), new CategoryRepository(), new BudgetRepository());

    @Override
    protected void handleAuthenticatedGet(HttpServletRequest req, HttpServletResponse resp, Long userId)
            throws ServletException, IOException {

        log.debug("Loading dashboard for user {}", userId);

        // 1. Get user object for display (optional but nice for "Welcome back, [username]")
        HttpSession session = req.getSession(false);
        if (session != null && session.getAttribute("user") != null) {
            req.setAttribute("user", session.getAttribute("user"));
        }

        // 2. Load data for dashboard
        // Get all categories for the user
        req.setAttribute("categories", categoryService.listCategories(userId));

        // Get ALL expenses for analytics (charts need all data, not just recent)
        // The JSP will filter what's needed for charts vs table display
        List<?> allExpenses = expenseService.listExpenses(userId, null, null, null);

        // Set expenses for charts and analytics (all expenses)
        req.setAttribute("expenses", allExpenses);

        // 3. Budgets widget
        // Fetch all active budgets
        List<Budget> allBudgets = budgetService.getActiveBudgets(userId);

        // Sort by % used (Descending) -> High risk first
        List<Budget> topBudgets = allBudgets.stream()
                .sorted((b1, b2) -> {
                    double pct1 = b1.getAmount().doubleValue() > 0 ? (b1.getSpentAmount().doubleValue() / b1.getAmount().doubleValue()) : 0;
                    double pct2 = b2.getAmount().doubleValue() > 0 ? (b2.getSpentAmount().doubleValue() / b2.getAmount().doubleValue()) : 0;
                    return Double.compare(pct2, pct1); // Descending
                })
                .limit(5) // Take only top 5
                .collect(Collectors.toList());

        req.setAttribute("budgetOverview", topBudgets);

        // 4. Report Summary
        ReportData dashboardReport = reportService.generatePredefinedReport(userId, "thisMonth");

        if (dashboardReport != null && dashboardReport.getComparison() != null) {
            req.setAttribute("totalExpenses", dashboardReport.getTotalSpending());
            req.setAttribute("trendPercentage", dashboardReport.getComparison().getPercentageChange());
        } else {
            req.setAttribute("totalExpenses", BigDecimal.ZERO);
            req.setAttribute("trendPercentage", 0.0);
        }

        log.debug("Dashboard loaded successfully for user {}", userId);

        // 5. Forward to JSP
        req.getRequestDispatcher("/WEB-INF/views/dashboard.jsp").forward(req, resp);
    }
}