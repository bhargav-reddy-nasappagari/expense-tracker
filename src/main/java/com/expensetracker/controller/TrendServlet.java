package com.expensetracker.controller;

import com.expensetracker.dto.MonthlyTrend;
import com.expensetracker.model.User;
import com.expensetracker.repository.BudgetRepository;
import com.expensetracker.repository.CategoryRepository;
import com.expensetracker.repository.ExpenseRepository;
import com.expensetracker.service.ReportService;

import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;

import java.io.IOException;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

// Mapping: /reports/trends -> Configure in web.xml
public class TrendServlet extends HttpServlet {

    private ReportService reportService;

    @Override
    public void init() throws ServletException {
        ExpenseRepository expenseRepo = new ExpenseRepository();
        CategoryRepository categoryRepo = new CategoryRepository();
        BudgetRepository budgetRepo = new BudgetRepository();

        this.reportService = new ReportService(expenseRepo, categoryRepo, budgetRepo);
    }

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response) 
            throws ServletException, IOException {
        
        HttpSession session = request.getSession(false);
        User user = (session != null) ? (User) session.getAttribute("user") : null;

        if (user == null) {
            response.sendRedirect(request.getContextPath() + "/login");
            return;
        }

        String monthsParam = request.getParameter("months");
        int months;
        try {
            months = (monthsParam != null && !monthsParam.isEmpty()) ? Integer.parseInt(monthsParam) : 12;
            if (months < 3) months = 3;
            if (months > 24) months = 24;
        } catch (NumberFormatException e) {
            months = 12;
        }

        List<MonthlyTrend> trendData = reportService.generateMonthlyTrendData(user.getId(), months);

        MonthlyTrend highestMonth = trendData.stream()
                .max(Comparator.comparing(MonthlyTrend::getTotal))
                .orElse(null);

        MonthlyTrend lowestMonth = trendData.stream()
                .filter(t -> t.getTotal().compareTo(BigDecimal.ZERO) > 0)
                .min(Comparator.comparing(MonthlyTrend::getTotal))
                .orElse(trendData.stream().min(Comparator.comparing(MonthlyTrend::getTotal)).orElse(null));

        BigDecimal averageSpending = BigDecimal.ZERO;
        if (!trendData.isEmpty()) {
            BigDecimal totalSum = trendData.stream()
                    .map(MonthlyTrend::getTotal)
                    .reduce(BigDecimal.ZERO, BigDecimal::add);
            averageSpending = totalSum.divide(BigDecimal.valueOf(trendData.size()), 2, RoundingMode.HALF_UP);
        }

        String trendDirection = "stable";
        if (trendData.size() >= 6) {
            BigDecimal first3Avg = calculateAverage(trendData.subList(0, 3));
            BigDecimal last3Avg = calculateAverage(trendData.subList(trendData.size() - 3, trendData.size()));

            if (first3Avg.compareTo(BigDecimal.ZERO) > 0) {
                BigDecimal change = last3Avg.subtract(first3Avg);
                double percentChange = change.divide(first3Avg, 4, RoundingMode.HALF_UP).doubleValue() * 100;

                if (percentChange > 10.0) trendDirection = "increasing";
                else if (percentChange < -10.0) trendDirection = "decreasing";
            }
        }

        String chartLabels = trendData.stream()
                .map(t -> "'" + t.getMonth() + "'")
                .collect(Collectors.joining(", ", "[", "]"));

        String chartData = trendData.stream()
                .map(t -> t.getTotal().toString())
                .collect(Collectors.joining(", ", "[", "]"));

        request.setAttribute("trendData", trendData);
        request.setAttribute("months", months);
        request.setAttribute("highestMonth", highestMonth);
        request.setAttribute("lowestMonth", lowestMonth);
        request.setAttribute("averageSpending", averageSpending);
        request.setAttribute("trendDirection", trendDirection);
        request.setAttribute("chartLabels", chartLabels);
        request.setAttribute("chartData", chartData);

        request.getRequestDispatcher("/WEB-INF/views/trends.jsp").forward(request, response);
    }

    private BigDecimal calculateAverage(List<MonthlyTrend> list) {
        if (list == null || list.isEmpty()) return BigDecimal.ZERO;
        BigDecimal sum = list.stream()
                .map(MonthlyTrend::getTotal)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
        return sum.divide(BigDecimal.valueOf(list.size()), 2, RoundingMode.HALF_UP);
    }
}