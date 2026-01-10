package com.expensetracker.controller;

import com.expensetracker.dto.CategoryPerformance;
import com.expensetracker.repository.BudgetRepository;
import com.expensetracker.repository.CategoryRepository;
import com.expensetracker.repository.ExpenseRepository;
import com.expensetracker.service.ReportService;

// Logger Imports
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import java.io.IOException;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

// Mapped via web.xml, no @WebServlet annotation
public class CategoryReportServlet extends BaseServlet {

    private static final Logger log = LoggerFactory.getLogger(CategoryReportServlet.class);
    private ReportService reportService;

    @Override
    public void init() throws ServletException {
        log.info("Initializing CategoryReportServlet");
        // Instantiate concrete repositories using HikariCP
        ExpenseRepository expenseRepo = new ExpenseRepository();
        CategoryRepository categoryRepo = new CategoryRepository(); 
        BudgetRepository budgetRepo = new BudgetRepository();
        
        this.reportService = new ReportService(expenseRepo, categoryRepo, budgetRepo);
    }

    @Override
    protected void handleAuthenticatedGet(HttpServletRequest request, HttpServletResponse response, Long userId) 
            throws ServletException, IOException {

        log.debug("Generating category report for user {}", userId);

        // 1. Parse Dates and Filters
        String startDateStr = request.getParameter("startDate");
        String endDateStr = request.getParameter("endDate");
        String sortBy = request.getParameter("sortBy"); // amount, name, budget, change
        
        LocalDate startDate;
        LocalDate endDate;

        try {
            startDate = (startDateStr != null && !startDateStr.isEmpty()) 
                    ? LocalDate.parse(startDateStr) : LocalDate.now().withDayOfMonth(1);
            endDate = (endDateStr != null && !endDateStr.isEmpty()) 
                    ? LocalDate.parse(endDateStr) : LocalDate.now();
        } catch (Exception e) {
            log.warn("Invalid date format provided for user {}: start={}, end={}", userId, startDateStr, endDateStr);
            // Fallback to defaults on parse error
            startDate = LocalDate.now().withDayOfMonth(1);
            endDate = LocalDate.now();
        }

        // 2. Get Data from Service
        List<CategoryPerformance> reportData = reportService.generateCategoryPerformanceReport(userId, startDate, endDate);

        // 3. Sort Data
        if (sortBy != null) {
            switch (sortBy) {
                case "amount":
                    reportData.sort(Comparator.comparing(CategoryPerformance::getTotalSpent).reversed());
                    break;
                case "name":
                    reportData.sort(Comparator.comparing(CategoryPerformance::getCategoryName));
                    break;
                case "budget":
                    reportData.sort(Comparator.comparing(cp -> 
                        cp.getBudgetUsedPercent() != null ? cp.getBudgetUsedPercent() : -1.0, 
                        Comparator.reverseOrder()));
                    break;
                case "change":
                    reportData.sort(Comparator.comparing(cp -> 
                        Math.abs(cp.getChangePercent() != null ? cp.getChangePercent() : 0.0), 
                        Comparator.reverseOrder()));
                    break;
                default:
                    reportData.sort(Comparator.comparing(CategoryPerformance::getTotalSpent).reversed());
            }
        } else {
             reportData.sort(Comparator.comparing(CategoryPerformance::getTotalSpent).reversed());
        }

        // 4. Calculate Summary Statistics
        int totalCategories = reportData.size();
        long overBudgetCount = reportData.stream()
                .filter(c -> "over".equals(c.getBudgetStatus()))
                .count();
        
        BigDecimal totalAllSpent = reportData.stream()
                .map(CategoryPerformance::getTotalSpent)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
                
        BigDecimal avgPerCategory = totalCategories > 0 
                ? totalAllSpent.divide(BigDecimal.valueOf(totalCategories), 2, RoundingMode.HALF_UP)
                : BigDecimal.ZERO;
        
        CategoryPerformance mostImproved = reportData.stream()
                .filter(c -> c.getChangeAmount() != null && c.getChangeAmount().compareTo(BigDecimal.ZERO) < 0) 
                .min(Comparator.comparing(CategoryPerformance::getChangeAmount))
                .orElse(null);

        CategoryPerformance mostIncreased = reportData.stream()
                .filter(c -> c.getChangeAmount() != null && c.getChangeAmount().compareTo(BigDecimal.ZERO) > 0)
                .max(Comparator.comparing(CategoryPerformance::getChangeAmount))
                .orElse(null);

        Map<String, Object> stats = new HashMap<>();
        stats.put("totalCategories", totalCategories);
        stats.put("overBudgetCount", overBudgetCount);
        stats.put("avgSpending", avgPerCategory);
        stats.put("mostImproved", mostImproved);
        stats.put("mostIncreased", mostIncreased);

        // 5. Set Attributes
        request.setAttribute("categoryReport", reportData);
        request.setAttribute("statistics", stats);
        request.setAttribute("startDate", startDate);
        request.setAttribute("endDate", endDate);
        request.setAttribute("sortBy", sortBy);

        log.debug("Report generated for user {}: {} categories found", userId, totalCategories);

        // 6. Forward
        request.getRequestDispatcher("/WEB-INF/views/category-report.jsp").forward(request, response);
    }
}