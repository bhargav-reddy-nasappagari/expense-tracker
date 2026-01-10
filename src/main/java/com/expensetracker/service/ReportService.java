package com.expensetracker.service;

import com.expensetracker.dto.*;
import com.expensetracker.model.Budget;
import com.expensetracker.model.Category;
import com.expensetracker.model.Expense;
import com.expensetracker.repository.BudgetRepository;
import com.expensetracker.repository.CategoryRepository;
import com.expensetracker.repository.ExpenseRepository;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.format.TextStyle;
import java.time.temporal.ChronoUnit;
import java.time.temporal.TemporalAdjusters;
import java.util.*;
import java.util.stream.Collectors;

public class ReportService {

    private final ExpenseRepository expenseRepository;
    private final CategoryRepository categoryRepository;
    private final BudgetRepository budgetRepository;

    public ReportService(ExpenseRepository expenseRepository, 
                         CategoryRepository categoryRepository,
                         BudgetRepository budgetRepository) {
        this.expenseRepository = expenseRepository;
        this.categoryRepository = categoryRepository;
        this.budgetRepository = budgetRepository;
    }

    // ==================================================================================
    // 1. CUSTOM DATE RANGE REPORTS
    // ==================================================================================

    public ReportData generateCustomReport(Long userId, LocalDate startDate, LocalDate endDate, Integer categoryId) {
        // 1. Validate and Set Date Defaults
        if (endDate == null) {
            endDate = LocalDate.now();
        }

        if (startDate == null) {
            // FIX: Try to fetch the first-ever expense date
            LocalDate earliest = expenseRepository.findEarliestExpenseDateByUserId(userId);
            
            if (earliest != null) {
                startDate = earliest;
            } else {
                // Fallback: If user has 0 expenses, default to start of current month
                startDate = endDate.withDayOfMonth(1);
            }
        }

        if (startDate.isAfter(endDate)) {
            throw new IllegalArgumentException("Start date cannot be after end date");
        }
        if (ChronoUnit.DAYS.between(startDate, endDate) > 730) {
            throw new IllegalArgumentException("Date range cannot exceed 2 years");
        }

        // 2. Fetch Expenses & Categories
        List<Expense> expenses = expenseRepository.findByUserIdAndFilters(userId, startDate, endDate, categoryId, null, null, null);
        
        // Fetch categories to map IDs to Names (Resolving "Expense does not have getCategory" issue)
        List<Category> userCategories = categoryRepository.findAllByUserId(userId);
        Map<Integer, String> categoryNameMap = userCategories.stream()
                .collect(Collectors.toMap(Category::getId, Category::getName));

        // 3. Initialize Report Data
        ReportData report = new ReportData();
        report.setStartDate(startDate);
        report.setEndDate(endDate);
        
        if (categoryId != null) {
            String catName = categoryNameMap.get(categoryId);
            if (catName != null) {
                report.setCategoryName(catName);
            }
        }

        // 4. Calculate Basic Metrics
        BigDecimal totalSpending = expenses.stream()
                .map(Expense::getAmount)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
        
        long daysInRange = ChronoUnit.DAYS.between(startDate, endDate) + 1;
        BigDecimal avgDaily = daysInRange > 0 
                ? totalSpending.divide(BigDecimal.valueOf(daysInRange), 2, RoundingMode.HALF_UP) 
                : BigDecimal.ZERO;

        report.setTotalSpending(totalSpending);
        report.setTransactionCount(expenses.size());
        report.setAveragePerDay(avgDaily);
        report.setAveragePerWeek(avgDaily.multiply(BigDecimal.valueOf(7))); 
        report.setAveragePerMonth(avgDaily.multiply(BigDecimal.valueOf(30)));

        // 5. Category Breakdown
        Map<String, List<Expense>> expensesByCategory = expenses.stream()
                .collect(Collectors.groupingBy(e -> 
                    categoryNameMap.getOrDefault(e.getCategoryId(), "Uncategorized")
                ));

        List<CategorySummary> categoryBreakdown = expensesByCategory.entrySet().stream()
                .map(entry -> {
                    BigDecimal catTotal = entry.getValue().stream()
                            .map(Expense::getAmount)
                            .reduce(BigDecimal.ZERO, BigDecimal::add);
                    double percentage = totalSpending.compareTo(BigDecimal.ZERO) > 0
                            ? catTotal.divide(totalSpending, 4, RoundingMode.HALF_UP).doubleValue() * 100
                            : 0.0;
                    return new CategorySummary(entry.getKey(), catTotal, percentage, entry.getValue().size());
                })
                .sorted((a, b) -> b.getTotal().compareTo(a.getTotal()))
                .collect(Collectors.toList());
        report.setCategoryBreakdown(categoryBreakdown);

        // 6. Top Expenses
        List<ExpenseDetail> topExpenses = expenses.stream()
                .sorted(Comparator.comparing(Expense::getAmount).reversed())
                .limit(10)
                .map(e -> {
                    double pct = totalSpending.compareTo(BigDecimal.ZERO) > 0
                            ? e.getAmount().divide(totalSpending, 4, RoundingMode.HALF_UP).doubleValue() * 100
                            : 0.0;
                    // Fix: Pass name explicitly using the map
                    String catName = categoryNameMap.getOrDefault(e.getCategoryId(), "Uncategorized");
                    return new ExpenseDetail(e, catName, pct);
                })
                .collect(Collectors.toList());
        report.setTopExpenses(topExpenses);

        // 7. Spending Trend
        Map<LocalDate, BigDecimal> dailyTotals = expenses.stream()
                .collect(Collectors.groupingBy(
                        Expense::getExpenseDate,
                        Collectors.reducing(BigDecimal.ZERO, Expense::getAmount, BigDecimal::add)
                ));
        
        List<TrendPoint> trendData = new ArrayList<>();
        for (LocalDate date = startDate; !date.isAfter(endDate); date = date.plusDays(1)) {
            trendData.add(new TrendPoint(date, dailyTotals.getOrDefault(date, BigDecimal.ZERO)));
        }
        report.setTrendData(trendData);

        // 8. Day of Week Distribution
        Map<String, BigDecimal> dowMap = expenses.stream()
                .collect(Collectors.groupingBy(
                        e -> e.getExpenseDate().getDayOfWeek().getDisplayName(TextStyle.FULL, Locale.ENGLISH),
                        Collectors.reducing(BigDecimal.ZERO, Expense::getAmount, BigDecimal::add)
                ));
        report.setDayOfWeekDistribution(dowMap);

        // ==========================================================
        // 9. NEW: Period Comparison Logic (Required for PDF Trend)
        // ==========================================================
        
        // Calculate previous period dates (same duration shifted back)
        LocalDate prevStart = startDate.minusDays(daysInRange);
        LocalDate prevEnd = startDate.minusDays(1);

        // Fetch expenses for the previous period using the same filter
        List<Expense> prevExpenses = expenseRepository.findByUserIdAndFilters(userId, prevStart, prevEnd, categoryId, null, null, null);
        
        BigDecimal prevTotal = prevExpenses.stream()
                .map(Expense::getAmount)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        PeriodComparison comparison = new PeriodComparison();
        comparison.setPreviousStartDate(prevStart);
        comparison.setPreviousEndDate(prevEnd);
        comparison.setPreviousTotal(prevTotal);

        // Calculate Percentage Change
        if (prevTotal.compareTo(BigDecimal.ZERO) > 0) {
            BigDecimal diff = totalSpending.subtract(prevTotal);
            double pctChange = diff.divide(prevTotal, 4, RoundingMode.HALF_UP).doubleValue() * 100;
            comparison.setPercentageChange(pctChange);
        } else if (totalSpending.compareTo(BigDecimal.ZERO) > 0) {
            // If previous was 0 and current is > 0, it's a 100% increase (or treated as new spending)
            comparison.setPercentageChange(100.0);
        } else {
            // Both zero
            comparison.setPercentageChange(0.0);
        }

        report.setComparison(comparison);

        return report;
    }

    // ==================================================================================
    // 2. PRE-DEFINED PERIOD REPORTS
    // ==================================================================================

    public ReportData generatePredefinedReport(Long userId, String reportType) {
        LocalDate start, end;
        LocalDate prevStart, prevEnd;
        LocalDate now = LocalDate.now();

        switch (reportType) {
            case "THIS_WEEK":
                start = now.with(DayOfWeek.MONDAY); 
                end = now;
                prevStart = start.minusWeeks(1);
                prevEnd = end.minusWeeks(1);
                break;
            case "THIS_MONTH":
                start = now.withDayOfMonth(1);
                end = now;
                prevStart = start.minusMonths(1);
                prevEnd = end.minusMonths(1);
                break;
            case "LAST_MONTH":
                start = now.minusMonths(1).withDayOfMonth(1);
                end = now.minusMonths(1).with(TemporalAdjusters.lastDayOfMonth());
                prevStart = start.minusMonths(1);
                prevEnd = end.minusMonths(1);
                break;
            case "LAST_3_MONTHS":
                start = now.minusMonths(3).withDayOfMonth(1);
                end = now.minusMonths(1).with(TemporalAdjusters.lastDayOfMonth());
                prevStart = start.minusMonths(3);
                prevEnd = end.minusMonths(3);
                break;
            case "LAST_6_MONTHS":
                start = now.minusMonths(6).withDayOfMonth(1);
                end = now.minusMonths(1).with(TemporalAdjusters.lastDayOfMonth());
                prevStart = start.minusMonths(6);
                prevEnd = end.minusMonths(6);
                break;
            case "THIS_YEAR":
                start = LocalDate.of(now.getYear(), 1, 1);
                end = now;
                prevStart = start.minusYears(1);
                prevEnd = end.minusYears(1);
                break;
            case "LAST_YEAR":
                start = LocalDate.of(now.getYear() - 1, 1, 1);
                end = LocalDate.of(now.getYear() - 1, 12, 31);
                prevStart = start.minusYears(1);
                prevEnd = end.minusYears(1);
                break;
            default:
                return generatePredefinedReport(userId, "THIS_MONTH");
        }

        ReportData reportData = generateCustomReport(userId, start, end, null);

        // Comparison Logic
        List<Expense> prevExpenses = expenseRepository.findByUserIdAndFilters(userId, prevStart, prevEnd, null, null, null, null);
        BigDecimal prevTotal = prevExpenses.stream()
                .map(Expense::getAmount)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        PeriodComparison comparison = new PeriodComparison();
        comparison.setPreviousTotal(prevTotal);
        
        BigDecimal currentTotal = reportData.getTotalSpending();
        BigDecimal diff = currentTotal.subtract(prevTotal);
        comparison.setAbsoluteChange(diff);

        if (prevTotal.compareTo(BigDecimal.ZERO) != 0) {
            double pctChange = diff.divide(prevTotal, 4, RoundingMode.HALF_UP).doubleValue() * 100;
            comparison.setPercentageChange(pctChange);
        } else if (currentTotal.compareTo(BigDecimal.ZERO) > 0) {
            comparison.setPercentageChange(100.0);
        } else {
            comparison.setPercentageChange(0.0);
        }

        reportData.setComparison(comparison);
        return reportData;
    }

    // ==================================================================================
    // 3. PRE-DEFINED PERIOD REPORTS WITH CATEGORY
    // ==================================================================================

    public ReportData generatePredefinedReportWithCategory(Long userId, String reportType, Integer categoryId) {
        String normalizedType = reportType != null ? reportType.toUpperCase() : "THIS_MONTH";
        
        LocalDate start, end;
        LocalDate prevStart, prevEnd;
        LocalDate now = LocalDate.now();

        switch (reportType) {
            case "THIS_WEEK":
                start = now.with(DayOfWeek.MONDAY); 
                end = now;
                prevStart = start.minusWeeks(1);
                prevEnd = end.minusWeeks(1);
                break;
            case "THIS_MONTH":
                start = now.withDayOfMonth(1);
                end = now;
                prevStart = start.minusMonths(1);
                prevEnd = end.minusMonths(1);
                break;
            case "LAST_MONTH":
                start = now.minusMonths(1).withDayOfMonth(1);
                end = now.minusMonths(1).with(TemporalAdjusters.lastDayOfMonth());
                prevStart = start.minusMonths(1);
                prevEnd = end.minusMonths(1);
                break;
            case "LAST_3_MONTHS":
                start = now.minusMonths(3).withDayOfMonth(1);
                end = now.minusMonths(1).with(TemporalAdjusters.lastDayOfMonth());
                prevStart = start.minusMonths(3);
                prevEnd = end.minusMonths(3);
                break;
            case "LAST_6_MONTHS":
                start = now.minusMonths(6).withDayOfMonth(1);
                end = now.minusMonths(1).with(TemporalAdjusters.lastDayOfMonth());
                prevStart = start.minusMonths(6);
                prevEnd = end.minusMonths(6);
                break;
            case "THIS_YEAR":
                start = LocalDate.of(now.getYear(), 1, 1);
                end = now;
                prevStart = start.minusYears(1);
                prevEnd = end.minusYears(1);
                break;
            case "LAST_YEAR":
                start = LocalDate.of(now.getYear() - 1, 1, 1);
                end = LocalDate.of(now.getYear() - 1, 12, 31);
                prevStart = start.minusYears(1);
                prevEnd = end.minusYears(1);
                break;
            default:
                return generatePredefinedReport(userId, "THIS_MONTH");
        }
        
        // ✅ KEY CHANGE: Pass categoryId to generateCustomReport
        ReportData reportData = generateCustomReport(userId, start, end, categoryId);
        
        // Comparison Logic
        List<Expense> prevExpenses = expenseRepository.findByUserIdAndFilters(userId, prevStart, prevEnd, null, null, null, null);
        BigDecimal prevTotal = prevExpenses.stream()
                .map(Expense::getAmount)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        PeriodComparison comparison = new PeriodComparison();
        comparison.setPreviousTotal(prevTotal);
        
        BigDecimal currentTotal = reportData.getTotalSpending();
        BigDecimal diff = currentTotal.subtract(prevTotal);
        comparison.setAbsoluteChange(diff);

        if (prevTotal.compareTo(BigDecimal.ZERO) != 0) {
            double pctChange = diff.divide(prevTotal, 4, RoundingMode.HALF_UP).doubleValue() * 100;
            comparison.setPercentageChange(pctChange);
        } else if (currentTotal.compareTo(BigDecimal.ZERO) > 0) {
            comparison.setPercentageChange(100.0);
        } else {
            comparison.setPercentageChange(0.0);
        }

        reportData.setComparison(comparison);   
        return reportData;
    }

    // ==================================================================================
    // 4. SPENDING INSIGHTS (Now with Budget Logic!)
    // ==================================================================================

    public List<SpendingInsight> generateSpendingInsights(Long userId, ReportData reportData) {
        List<SpendingInsight> insights = new ArrayList<>();

        // 1. Spending Trends
        if (reportData.getComparison() != null) {
            double change = reportData.getComparison().getPercentageChange();
            if (change >= 15.0) {
                insights.add(new SpendingInsight("warning", 
                    String.format("Your spending increased by %.0f%% compared to last period", change), 
                    "trending-up", reportData.getComparison().getAbsoluteChange()));
            } else if (change <= -15.0) {
                insights.add(new SpendingInsight("positive", 
                    String.format("You saved %.0f%% compared to last period", Math.abs(change)), 
                    "trending-down", reportData.getComparison().getAbsoluteChange()));
            }
        }

        // 2. Budget Alerts (NEW)
        List<CategoryPerformance> catPerf = generateCategoryPerformanceReport(userId, reportData.getStartDate(), reportData.getEndDate());
        for (CategoryPerformance cp : catPerf) {
            if ("over".equals(cp.getBudgetStatus())) {
                insights.add(new SpendingInsight("warning", 
                    String.format("You exceeded your %s budget by %s", cp.getCategoryName(), cp.getBudgetRemaining().abs()), 
                    "alert-circle", cp.getTotalSpent()));
            }
        }

        // 3. Highest Category
        if (!reportData.getCategoryBreakdown().isEmpty()) {
            CategorySummary topCat = reportData.getCategoryBreakdown().get(0);
            insights.add(new SpendingInsight("neutral",
                String.format("%s is your highest expense (%.0f%% of total)", topCat.getName(), topCat.getPercentage()),
                "pie-chart", topCat.getTotal()));
        }

        return insights.stream().limit(7).collect(Collectors.toList());
    }

    // ==================================================================================
    // 5. HEATMAP
    // ==================================================================================

    public Map<LocalDate, DaySpending> generateHeatmapData(Long userId, int year, int month) {
        LocalDate start = LocalDate.of(year, month, 1);
        LocalDate end = start.with(TemporalAdjusters.lastDayOfMonth());

        List<Expense> expenses = expenseRepository.findByUserIdAndFilters(userId, start, end, null, null, null, null);
        Map<LocalDate, List<Expense>> grouped = expenses.stream()
                .collect(Collectors.groupingBy(Expense::getExpenseDate));

        BigDecimal total = expenses.stream().map(Expense::getAmount).reduce(BigDecimal.ZERO, BigDecimal::add);
        BigDecimal avgDaily = total.divide(BigDecimal.valueOf(end.getDayOfMonth()), 2, RoundingMode.HALF_UP);

        BigDecimal lowThresh = avgDaily.multiply(new BigDecimal("0.5"));
        BigDecimal medThresh = avgDaily.multiply(new BigDecimal("1.5"));
        BigDecimal highThresh = avgDaily.multiply(new BigDecimal("3.0"));

        Map<LocalDate, DaySpending> heatmap = new LinkedHashMap<>();

        for (LocalDate date = start; !date.isAfter(end); date = date.plusDays(1)) {
            DaySpending ds = new DaySpending();
            ds.setDate(date);
            
            if (grouped.containsKey(date)) {
                List<Expense> dailyExpenses = grouped.get(date);
                BigDecimal dailyTotal = dailyExpenses.stream()
                        .map(Expense::getAmount)
                        .reduce(BigDecimal.ZERO, BigDecimal::add);
                
                ds.setAmount(dailyTotal);
                ds.setTransactionCount(dailyExpenses.size());

                if (dailyTotal.compareTo(BigDecimal.ZERO) == 0) ds.setColorLevel("none");
                else if (dailyTotal.compareTo(lowThresh) <= 0) ds.setColorLevel("low");
                else if (dailyTotal.compareTo(medThresh) <= 0) ds.setColorLevel("medium");
                else if (dailyTotal.compareTo(highThresh) <= 0) ds.setColorLevel("high");
                else ds.setColorLevel("very-high");
            } else {
                ds.setAmount(BigDecimal.ZERO);
                ds.setTransactionCount(0);
                ds.setColorLevel("none");
            }
            heatmap.put(date, ds);
        }

        return heatmap;
    }

    // ==================================================================================
    // 6. MONTHLY TRENDS
    // ==================================================================================

    public List<MonthlyTrend> generateMonthlyTrendData(Long userId, int numberOfMonths) {
        List<MonthlyTrend> trends = new ArrayList<>();
        LocalDate end = LocalDate.now().minusMonths(1).with(TemporalAdjusters.lastDayOfMonth());
        LocalDate start = end.minusMonths(numberOfMonths - 1).withDayOfMonth(1);

        List<Expense> allExpenses = expenseRepository.findByUserIdAndFilters(userId, start, end, null, null, null, null);
        List<Category> allCategories = categoryRepository.findAllByUserId(userId);
        
        Map<Integer, String> categoryNameMap = allCategories.stream()
                .collect(Collectors.toMap(Category::getId, Category::getName));
        
        Map<String, List<Expense>> groupedByMonth = allExpenses.stream()
                .collect(Collectors.groupingBy(e -> 
                    e.getExpenseDate().getMonth().getDisplayName(TextStyle.SHORT, Locale.ENGLISH) + " " + e.getExpenseDate().getYear()
                ));

        for (int i = 0; i < numberOfMonths; i++) {
            LocalDate currentMonth = start.plusMonths(i);
            String monthKey = currentMonth.getMonth().getDisplayName(TextStyle.SHORT, Locale.ENGLISH) + " " + currentMonth.getYear();
            
            MonthlyTrend trend = new MonthlyTrend();
            trend.setMonth(monthKey);
            
            List<Expense> monthExpenses = groupedByMonth.getOrDefault(monthKey, Collections.emptyList());
            
            BigDecimal total = monthExpenses.stream()
                    .map(Expense::getAmount)
                    .reduce(BigDecimal.ZERO, BigDecimal::add);
            trend.setTotal(total);
            
            Map<String, BigDecimal> catTotals = monthExpenses.stream()
                    .collect(Collectors.groupingBy(
                            e -> categoryNameMap.getOrDefault(e.getCategoryId(), "Uncategorized"),
                            Collectors.reducing(BigDecimal.ZERO, Expense::getAmount, BigDecimal::add)
                    ));
            trend.setCategoryTotals(catTotals);

            if (i > 0) {
                BigDecimal prevTotal = trends.get(i - 1).getTotal();
                BigDecimal diff = total.subtract(prevTotal);
                trend.setChangeAmount(diff);
                if (prevTotal.compareTo(BigDecimal.ZERO) != 0) {
                    trend.setChangePercent(diff.divide(prevTotal, 4, RoundingMode.HALF_UP).doubleValue() * 100);
                } else {
                    trend.setChangePercent(total.compareTo(BigDecimal.ZERO) > 0 ? 100.0 : 0.0);
                }
            }
            trends.add(trend);
        }
        
        return trends;
    }

    // ==================================================================================
    // 7. CATEGORY PERFORMANCE REPORT (Now with Budget Logic!)
    // ==================================================================================

    public List<CategoryPerformance> generateCategoryPerformanceReport(Long userId, LocalDate startDate, LocalDate endDate) {
        // 1. Fetch Expenses & Categories
        List<Expense> currentExpenses = expenseRepository.findByUserIdAndFilters(userId, startDate, endDate, null, null, null, null);
        List<Category> allCategories = categoryRepository.findAllByUserId(userId); 
        
        // 2. Fetch Budgets
        // Using findAllActiveByUserId as inferred from your functional Budget feature
        List<Budget> activeBudgets = budgetRepository.findActiveByUserId(userId);
        Map<Integer, Budget> budgetMap = activeBudgets.stream()
                .collect(Collectors.toMap(Budget::getCategoryId, b -> b, (b1, b2) -> b1)); // Merge duplicates if any

        // 3. Previous Period Data
        long days = ChronoUnit.DAYS.between(startDate, endDate) + 1;
        LocalDate prevStart = startDate.minusDays(days);
        LocalDate prevEnd = endDate.minusDays(days);
        List<Expense> prevExpenses = expenseRepository.findByUserIdAndFilters(userId, prevStart, prevEnd, null,null, null, null);

        BigDecimal totalSpent = currentExpenses.stream()
                .map(Expense::getAmount)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        Map<Integer, List<Expense>> currentMap = currentExpenses.stream()
                .collect(Collectors.groupingBy(Expense::getCategoryId));
        Map<Integer, List<Expense>> prevMap = prevExpenses.stream()
                .collect(Collectors.groupingBy(Expense::getCategoryId));

        List<CategoryPerformance> performanceList = new ArrayList<>();

        for (Category cat : allCategories) {
            CategoryPerformance cp = new CategoryPerformance();
            cp.setCategoryName(cat.getName());

            // --- Spending Metrics ---
            List<Expense> catCurrent = currentMap.getOrDefault(cat.getId(), Collections.emptyList());
            BigDecimal catTotal = catCurrent.stream().map(Expense::getAmount).reduce(BigDecimal.ZERO, BigDecimal::add);
            
            cp.setTotalSpent(catTotal);
            cp.setTransactionCount(catCurrent.size());
            cp.setAverageTransaction(catCurrent.isEmpty() ? BigDecimal.ZERO : 
                catTotal.divide(BigDecimal.valueOf(catCurrent.size()), 2, RoundingMode.HALF_UP));

            // --- Previous Period Comparison ---
            List<Expense> catPrev = prevMap.getOrDefault(cat.getId(), Collections.emptyList());
            BigDecimal prevTotal = catPrev.stream().map(Expense::getAmount).reduce(BigDecimal.ZERO, BigDecimal::add);
            cp.setPreviousPeriodSpent(prevTotal);

            BigDecimal diff = catTotal.subtract(prevTotal);
            cp.setChangeAmount(diff);
            if (prevTotal.compareTo(BigDecimal.ZERO) != 0) {
                cp.setChangePercent(diff.divide(prevTotal, 4, RoundingMode.HALF_UP).doubleValue() * 100);
            } else {
                cp.setChangePercent(catTotal.compareTo(BigDecimal.ZERO) > 0 ? 100.0 : 0.0);
            }

            if (totalSpent.compareTo(BigDecimal.ZERO) > 0) {
                cp.setPercentOfTotal(catTotal.divide(totalSpent, 4, RoundingMode.HALF_UP).doubleValue() * 100);
            } else {
                cp.setPercentOfTotal(0.0);
            }

            // --- Budget Integration ---
            Budget budget = budgetMap.get(cat.getId());
            if (budget != null) {
                cp.setBudgetAllocated(budget.getAmount());
                cp.setBudgetRemaining(budget.getAmount().subtract(catTotal));
                
                double pctUsed = catTotal.divide(budget.getAmount(), 4, RoundingMode.HALF_UP).doubleValue() * 100;
                cp.setBudgetUsedPercent(pctUsed);
                
                if (pctUsed >= 100) cp.setBudgetStatus("over");
                else if (pctUsed >= 85) cp.setBudgetStatus("near");
                else cp.setBudgetStatus("under");
            } else {
                cp.setBudgetStatus("no-budget");
                cp.setBudgetAllocated(BigDecimal.ZERO);
                cp.setBudgetRemaining(BigDecimal.ZERO);
            }
            
            performanceList.add(cp);
        }

        performanceList.sort(Comparator.comparing(CategoryPerformance::getTotalSpent).reversed());

        return performanceList;
    }
}