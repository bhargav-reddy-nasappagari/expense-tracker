package com.expensetracker.controller;

import com.expensetracker.dto.ReportData;
import com.expensetracker.model.Budget;
import com.expensetracker.model.Category;
import com.expensetracker.model.Expense;
import com.expensetracker.repository.BudgetRepository;
import com.expensetracker.repository.CategoryRepository;
import com.expensetracker.repository.ExpenseRepository;
import com.expensetracker.service.PdfReportGenerator;
import com.expensetracker.service.ReportService;

// Logger Imports
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import java.io.IOException;
import java.io.PrintWriter;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.YearMonth;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class ExportServlet extends BaseServlet {

    private static final Logger log = LoggerFactory.getLogger(ExportServlet.class);

    private final ExpenseRepository expenseRepo = new ExpenseRepository();
    private final CategoryRepository categoryRepo = new CategoryRepository();
    private final BudgetRepository budgetRepo = new BudgetRepository();

    // Services
    private final ReportService reportService = new ReportService(expenseRepo, categoryRepo, budgetRepo);
    private final PdfReportGenerator pdfReportGenerator = new PdfReportGenerator();

    @Override
    protected void handleAuthenticatedGet(HttpServletRequest req, HttpServletResponse resp, Long userId)
            throws ServletException, IOException {

        log.debug("User {} requested export", userId);

        String format = req.getParameter("format"); // "csv" or "pdf"
        
        if ("csv".equalsIgnoreCase(format)) {
            exportCsv(req, resp, userId);
        } else if ("pdf".equalsIgnoreCase(format)) {
            exportPdf(req, resp, userId);
        } else {
            log.warn("Invalid export format '{}' requested by user {}", format, userId);
            resp.sendError(HttpServletResponse.SC_BAD_REQUEST, "Invalid format. Use 'csv' or 'pdf'.");
        }
    }

    private void exportCsv(HttpServletRequest req, HttpServletResponse resp, Long userId) throws IOException {
        log.debug("Generating Cumulative CSV for user {}", userId);

        // 1. Parse Filters
        LocalDate from = parseDate(req.getParameter("from"));
        LocalDate to = parseDate(req.getParameter("to"));
        Integer categoryId = parseInteger(req.getParameter("categoryId"));
        String keyword = req.getParameter("keyword");
        BigDecimal minAmount = parseBigDecimal(req.getParameter("minAmount"));
        BigDecimal maxAmount = parseBigDecimal(req.getParameter("maxAmount"));

        // 2. Fetch Data
        // Using "findByUserIdFiltered" assuming it maps to your repo's filter method
        List<Expense> expenses = expenseRepo.findByUserIdAndFilters(userId, from, to, categoryId, minAmount, maxAmount, keyword);

        Map<Integer, String> categoryMap = categoryRepo.findAllByUserId(userId).stream()
                .collect(Collectors.toMap(Category::getId, Category::getName));

        List<Budget> activeBudgets = budgetRepo.findActiveByUserId(userId);
        Map<Integer, BigDecimal> budgetMap = activeBudgets.stream()
                .collect(Collectors.toMap(Budget::getCategoryId, Budget::getAmount));

        // 3. Write CSV
        String filename = "expense_report_" + LocalDate.now() + ".csv";
        resp.setContentType("text/csv; charset=UTF-8");
        resp.setHeader("Content-Disposition", "attachment; filename=\"" + filename + "\"");

        // IMPORTANT: Sort Chronologically (Oldest -> Newest) for correct running balance
        expenses.sort(Comparator.comparing(Expense::getExpenseDate));

        // State Tracker: Key = "CategoryID-Year-Month", Value = RunningTotal
        Map<String, BigDecimal> runningTotals = new HashMap<>();

        try (PrintWriter writer = resp.getWriter()) {
            writer.write('\ufeff'); // BOM for Excel compatibility
            writer.println("Date,Description,Category,Amount,Budget Limit,Cumulative Spend,Remaining Balance,Status");

            for (Expense e : expenses) {
                Integer catId = e.getCategoryId();
                BigDecimal budgetLimit = budgetMap.get(catId);
                
                // Construct Key for Monthly Tracking (e.g., "5-2023-10")
                YearMonth ym = YearMonth.from(e.getExpenseDate());
                String key = catId + "-" + ym.toString();

                // Logic Variables
                String budgetStr = "N/A";
                String remainingStr = "N/A";
                String statusStr = "N/A";
                String cumulativeStr = "N/A";

                if (budgetLimit != null) {
                    // 1. Calculate Cumulative Spend for this Month+Category
                    BigDecimal previousTotal = runningTotals.getOrDefault(key, BigDecimal.ZERO);
                    BigDecimal currentTotal = previousTotal.add(e.getAmount());
                    runningTotals.put(key, currentTotal); // Update state

                    // 2. Calculate Remaining (Budget - Cumulative)
                    BigDecimal remaining = budgetLimit.subtract(currentTotal);

                    // 3. Calculate Percentage
                    BigDecimal percentage = BigDecimal.ZERO;
                    if (budgetLimit.compareTo(BigDecimal.ZERO) > 0) {
                        percentage = currentTotal.multiply(new BigDecimal(100))
                                .divide(budgetLimit, 2, RoundingMode.HALF_UP);
                    }

                    // 4. Determine Status String
                    if (percentage.compareTo(new BigDecimal(100)) > 0) {
                        statusStr = "EXCEEDED (" + percentage + "%)";
                    } else {
                        statusStr = percentage + "%";
                    }

                    budgetStr = budgetLimit.toString();
                    cumulativeStr = currentTotal.toString();
                    remainingStr = remaining.toString(); 
                }

                writer.printf("%s,\"%s\",%s,%.2f,%s,%s,%s,%s%n",
                        e.getExpenseDate(),
                        escapeCsv(e.getDescription()),
                        categoryMap.getOrDefault(catId, "Uncategorized"),
                        e.getAmount(),
                        budgetStr,
                        cumulativeStr,
                        remainingStr,
                        statusStr
                );
            }
            log.info("CSV export complete for user {}", userId);
        }
    }

    private void exportPdf(HttpServletRequest req, HttpServletResponse resp, Long userId) throws IOException {
        log.debug("Generating PDF for user {}", userId);

        // 1. Parse Filters
        LocalDate from = parseDate(req.getParameter("from"));
        LocalDate to = parseDate(req.getParameter("to"));
        Integer categoryId = parseInteger(req.getParameter("categoryId"));
        String keyword = req.getParameter("keyword");
        BigDecimal minAmount = parseBigDecimal(req.getParameter("minAmount"));
        BigDecimal maxAmount = parseBigDecimal(req.getParameter("maxAmount"));

        // 2. Fetch Expenses
        List<Expense> expenses = expenseRepo.findByUserIdAndFilters(userId, from, to, categoryId, minAmount, maxAmount, keyword);

        Map<Integer, String> categoryMap = categoryRepo.findAllByUserId(userId).stream()
                .collect(Collectors.toMap(Category::getId, Category::getName));

        // 3. SMART ANALYTICS LOGIC
        LocalDate reportStart = from;
        LocalDate reportEnd = (to != null) ? to : LocalDate.now();
        ReportData analytics;

        boolean hasAdvancedFilters = (keyword != null && !keyword.trim().isEmpty()) 
                                  || minAmount != null 
                                  || maxAmount != null;

        if (hasAdvancedFilters) {
            // CASE A: Filtered View -> Calculate simple totals manually
            analytics = new ReportData();

            if (reportStart == null) {
                // Try to fetch the first-ever expense date
                LocalDate earliest = expenseRepo.findEarliestExpenseDateByUserId(userId);
                if (earliest != null) {
                    reportStart = earliest;
                } else {
                    // Fallback
                    reportStart = reportEnd.withDayOfMonth(1);
                }
            }

            BigDecimal totalFiltered = expenses.stream()
                .map(Expense::getAmount)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
            
            analytics.setTotalSpending(totalFiltered);
            
            // Calculate simplistic average
            long days = java.time.temporal.ChronoUnit.DAYS.between(reportStart, reportEnd) + 1;
            BigDecimal avg = (days > 0) 
                ? totalFiltered.divide(BigDecimal.valueOf(days), 2, RoundingMode.HALF_UP)
                : BigDecimal.ZERO;
            analytics.setAveragePerDay(avg);
            
            // Empty Comparison/Trends
            analytics.setCategoryBreakdown(List.of()); 

        } else {
            // CASE B: Standard View -> Use full powerful ReportService
            if (reportStart == null) {
                // Try to fetch the first-ever expense date
                LocalDate earliest = expenseRepo.findEarliestExpenseDateByUserId(userId);
                if (earliest != null) {
                    reportStart = earliest;
                }
            }
            analytics = reportService.generateCustomReport(userId, reportStart, reportEnd, categoryId);
        }

        // 4. Generate PDF
        String filename = "expense_report_" + LocalDate.now() + ".pdf";
        resp.setContentType("application/pdf");
        resp.setHeader("Content-Disposition", "attachment; filename=\"" + filename + "\"");

        try {
            pdfReportGenerator.generate(resp, expenses, categoryMap, analytics, reportStart, reportEnd);
            log.info("PDF export complete for user {}", userId);
        } catch (Exception e) {
            log.error("Error generating PDF for user {}: {}", userId, e.getMessage(), e);
            throw new IOException("PDF Generation failed", e);
        }
    }

    private String escapeCsv(String value) {
        return (value == null) ? "" : value.replace("\"", "\"\"");
    }

    private LocalDate parseDate(String s) {
        try { return (s != null && !s.isEmpty()) ? LocalDate.parse(s) : null; } 
        catch (Exception e) { return null; }
    }

    private BigDecimal parseBigDecimal(String value) {
        if (value == null || value.trim().isEmpty()) return null;
        try { return new BigDecimal(value.trim()); } catch (Exception e) { return null; }
    }

    private Integer parseInteger(String s) {
        try { return (s != null && !s.isEmpty()) ? Integer.parseInt(s) : null; } 
        catch (Exception e) { return null; }
    }
}