package com.expensetracker.dto;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Map;

// 1. ReportData.java
public class ReportData {
    private LocalDate startDate;
    private LocalDate endDate;
    private String categoryName;
    private BigDecimal totalSpending;
    private int transactionCount;
    private BigDecimal averagePerDay;
    private BigDecimal averagePerWeek;
    private BigDecimal averagePerMonth;
    private List<CategorySummary> categoryBreakdown;
    private List<ExpenseDetail> topExpenses;
    private List<TrendPoint> trendData;
    private Map<String, BigDecimal> dayOfWeekDistribution;
    private PeriodComparison comparison;

    // Getters and Setters
    public LocalDate getStartDate() { return startDate; }
    public void setStartDate(LocalDate startDate) { this.startDate = startDate; }
    public LocalDate getEndDate() { return endDate; }
    public void setEndDate(LocalDate endDate) { this.endDate = endDate; }
    public String getCategoryName() { return categoryName; }
    public void setCategoryName(String categoryName) { this.categoryName = categoryName; }
    public BigDecimal getTotalSpending() { return totalSpending; }
    public void setTotalSpending(BigDecimal totalSpending) { this.totalSpending = totalSpending; }
    public int getTransactionCount() { return transactionCount; }
    public void setTransactionCount(int transactionCount) { this.transactionCount = transactionCount; }
    public BigDecimal getAveragePerDay() { return averagePerDay; }
    public void setAveragePerDay(BigDecimal averagePerDay) { this.averagePerDay = averagePerDay; }
    public BigDecimal getAveragePerWeek() { return averagePerWeek; }
    public void setAveragePerWeek(BigDecimal averagePerWeek) { this.averagePerWeek = averagePerWeek; }
    public BigDecimal getAveragePerMonth() { return averagePerMonth; }
    public void setAveragePerMonth(BigDecimal averagePerMonth) { this.averagePerMonth = averagePerMonth; }
    public List<CategorySummary> getCategoryBreakdown() { return categoryBreakdown; }
    public void setCategoryBreakdown(List<CategorySummary> categoryBreakdown) { this.categoryBreakdown = categoryBreakdown; }
    public List<ExpenseDetail> getTopExpenses() { return topExpenses; }
    public void setTopExpenses(List<ExpenseDetail> topExpenses) { this.topExpenses = topExpenses; }
    public List<TrendPoint> getTrendData() { return trendData; }
    public void setTrendData(List<TrendPoint> trendData) { this.trendData = trendData; }
    public Map<String, BigDecimal> getDayOfWeekDistribution() { return dayOfWeekDistribution; }
    public void setDayOfWeekDistribution(Map<String, BigDecimal> dayOfWeekDistribution) { this.dayOfWeekDistribution = dayOfWeekDistribution; }
    public PeriodComparison getComparison() { return comparison; }
    public void setComparison(PeriodComparison comparison) { this.comparison = comparison; }
}