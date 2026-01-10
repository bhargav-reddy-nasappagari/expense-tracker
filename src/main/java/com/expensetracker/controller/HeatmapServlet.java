package com.expensetracker.controller;

import com.expensetracker.dto.DaySpending;
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
import java.time.LocalDate;
import java.time.Month;
import java.time.format.TextStyle;
import java.util.Comparator;
import java.util.Locale;
import java.util.Map;

public class HeatmapServlet extends BaseServlet {

    private static final Logger log = LoggerFactory.getLogger(HeatmapServlet.class);
    private ReportService reportService;

    @Override
    public void init() throws ServletException {
        ExpenseRepository expenseRepo = new ExpenseRepository();
        CategoryRepository categoryRepo = new CategoryRepository();
        BudgetRepository budgetRepo = new BudgetRepository();

        this.reportService = new ReportService(expenseRepo, categoryRepo, budgetRepo);
    }

    @Override
    protected void handleAuthenticatedGet(HttpServletRequest request, HttpServletResponse response, Long userId) 
            throws ServletException, IOException {
        
        log.debug("User {} requested heatmap", userId);

        LocalDate now = LocalDate.now();
        int year = now.getYear();
        int month = now.getMonthValue();

        String yearParam = request.getParameter("year");
        String monthParam = request.getParameter("month");

        try {
            if (yearParam != null && !yearParam.isEmpty()) {
                year = Integer.parseInt(yearParam);
            }
            if (monthParam != null && !monthParam.isEmpty()) {
                month = Integer.parseInt(monthParam);
            }
            if (month < 1 || month > 12) {
                month = now.getMonthValue();
            }
            if (year < 2000 || year > now.getYear() + 1) {
                year = now.getYear();
            }
        } catch (NumberFormatException e) {
            log.warn("Invalid date parameters provided by user {}: year={}, month={}", userId, yearParam, monthParam);
            year = now.getYear();
            month = now.getMonthValue();
        }

        // Using your specific method name: generateHeatmapData
        Map<LocalDate, DaySpending> heatmapData = reportService.generateHeatmapData(userId, year, month);

        // Your specific navigation logic
        int prevMonth = (month == 1) ? 12 : month - 1;
        int prevYear = (month == 1) ? year - 1 : year;

        int nextMonth = (month == 12) ? 1 : month + 1;
        int nextYear = (month == 12) ? year + 1 : year;

        boolean isFuture = (year > now.getYear()) || (year == now.getYear() && month >= now.getMonthValue());
        
        // Your specific stats calculation logic
        BigDecimal totalSpent = heatmapData.values().stream()
                .map(DaySpending::getAmount)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        int totalTransactions = heatmapData.values().stream()
                .mapToInt(DaySpending::getTransactionCount)
                .sum();

        DaySpending highestDay = heatmapData.values().stream()
                .max(Comparator.comparing(DaySpending::getAmount))
                .orElse(new DaySpending());

        long activeDays = heatmapData.values().stream()
                .filter(d -> d.getTransactionCount() > 0)
                .count();

        // Setting attributes exactly as requested
        request.setAttribute("heatmapData", heatmapData);
        request.setAttribute("currentYear", year);
        request.setAttribute("currentMonth", month);
        request.setAttribute("monthName", Month.of(month).getDisplayName(TextStyle.FULL, Locale.ENGLISH));
        
        request.setAttribute("prevYear", prevYear);
        request.setAttribute("prevMonth", prevMonth);
        request.setAttribute("nextYear", nextYear);
        request.setAttribute("nextMonth", nextMonth);
        request.setAttribute("disableNext", isFuture);

        request.setAttribute("totalSpent", totalSpent);
        request.setAttribute("totalTransactions", totalTransactions);
        request.setAttribute("highestDayAmount", highestDay.getAmount());
        request.setAttribute("highestDayDate", highestDay.getDate() != null ? highestDay.getDate().toString() : "");
        request.setAttribute("activeDays", activeDays);

        log.debug("Heatmap generated for user {} ({}/{}) - Total: {}", userId, month, year, totalSpent);

        request.getRequestDispatcher("/WEB-INF/views/heatmap.jsp").forward(request, response);
    }
}