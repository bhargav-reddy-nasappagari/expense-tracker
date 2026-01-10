package com.expensetracker.controller;

import com.expensetracker.dto.ReportData;
import com.expensetracker.dto.SpendingInsight;
import com.expensetracker.model.Category;
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
import java.time.LocalDate;
import java.time.format.DateTimeParseException;
import java.util.List;

// Mapping: /reports -> Configure in web.xml
public class ReportServlet extends BaseServlet {

    private static final Logger log = LoggerFactory.getLogger(ReportServlet.class);
    private ReportService reportService;
    private CategoryRepository categoryRepository;

    @Override
    public void init() throws ServletException {
        log.info("Initializing ReportServlet");
        // Instantiate concrete repositories (using HikariCP internally)
        ExpenseRepository expenseRepo = new ExpenseRepository();
        CategoryRepository categoryRepo = new CategoryRepository();
        BudgetRepository budgetRepo = new BudgetRepository();

        this.categoryRepository = categoryRepo;
        this.reportService = new ReportService(expenseRepo, categoryRepo, budgetRepo);
    }

    @Override
    protected void handleAuthenticatedGet(HttpServletRequest request, HttpServletResponse response, Long userId) 
            throws ServletException, IOException {
        
        log.debug("User {} requested reports page", userId);

        String reportType = request.getParameter("reportType");
        String startDateStr = request.getParameter("startDate");
        String endDateStr = request.getParameter("endDate");
        String categoryIdStr = request.getParameter("categoryId");

        if (reportType == null || reportType.isEmpty()) {
            reportType = "THIS_MONTH";
        }

        ReportData reportData = null;
        String errorMessage = null;

        try {
            Integer categoryId = (categoryIdStr != null && !categoryIdStr.isEmpty()) ? Integer.parseInt(categoryIdStr) : null;
    
            if ("CUSTOM".equalsIgnoreCase(reportType)) {
                LocalDate startDate = (startDateStr != null && !startDateStr.isEmpty()) 
                        ? LocalDate.parse(startDateStr) : null;
                LocalDate endDate = (endDateStr != null && !endDateStr.isEmpty()) 
                        ? LocalDate.parse(endDateStr) : null;
                
                log.debug("Generating custom report for user {}: {} to {}", userId, startDate, endDate);
                reportData = reportService.generateCustomReport(userId, startDate, endDate, categoryId);
            } else {
                log.debug("Generating predefined report '{}' for user {}", reportType, userId);
                reportData = reportService.generatePredefinedReportWithCategory(userId, reportType, categoryId);
            }

            List<SpendingInsight> insights = reportService.generateSpendingInsights(userId, reportData);
            request.setAttribute("insights", insights);

        } catch (DateTimeParseException e) {
            log.warn("Invalid date format provided by user {}: {}", userId, e.getMessage());
            errorMessage = "Invalid date format. Please use YYYY-MM-DD.";
        } catch (IllegalArgumentException e) {
            log.warn("Illegal argument in report generation for user {}: {}", userId, e.getMessage());
            errorMessage = e.getMessage();
        } catch (Exception e) {
            log.error("Unexpected error generating report for user {}: {}", userId, e.getMessage(), e);
            errorMessage = "An unexpected error occurred while generating the report.";
        }

        // Load categories for filter dropdown
        try {
            List<Category> categories = categoryRepository.findAllByUserId(userId);
            request.setAttribute("categories", categories);
        } catch (Exception e) {
            log.error("Error loading categories for report filter (User {}): {}", userId, e.getMessage());
        }

        request.setAttribute("reportData", reportData);
        request.setAttribute("selectedReportType", reportType);
        
        request.setAttribute("startDate", startDateStr);
        request.setAttribute("endDate", endDateStr);
        request.setAttribute("selectedCategoryId", categoryIdStr);

        if (errorMessage != null) {
            request.setAttribute("errorMessage", errorMessage);
        }

        request.getRequestDispatcher("/WEB-INF/views/reports.jsp").forward(request, response);
    }
}