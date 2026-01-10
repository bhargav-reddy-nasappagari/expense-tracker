package com.expensetracker.dto;

import java.math.BigDecimal;

// 9. CategoryPerformance.java
public class CategoryPerformance {
    private String categoryName;
    private BigDecimal totalSpent;
    private BigDecimal budgetAllocated;
    private BigDecimal budgetRemaining;
    private Double budgetUsedPercent;
    private String budgetStatus; // "under", "near", "over", "no-budget"
    private int transactionCount;
    private BigDecimal averageTransaction;
    private BigDecimal previousPeriodSpent;
    private BigDecimal changeAmount;
    private Double changePercent;
    private Double percentOfTotal;

    // Getters and Setters
    public String getCategoryName() { return categoryName; }
    public void setCategoryName(String categoryName) { this.categoryName = categoryName; }
    public BigDecimal getTotalSpent() { return totalSpent; }
    public void setTotalSpent(BigDecimal totalSpent) { this.totalSpent = totalSpent; }
    public BigDecimal getBudgetAllocated() { return budgetAllocated; }
    public void setBudgetAllocated(BigDecimal budgetAllocated) { this.budgetAllocated = budgetAllocated; }
    public BigDecimal getBudgetRemaining() { return budgetRemaining; }
    public void setBudgetRemaining(BigDecimal budgetRemaining) { this.budgetRemaining = budgetRemaining; }
    public Double getBudgetUsedPercent() { return budgetUsedPercent; }
    public void setBudgetUsedPercent(Double budgetUsedPercent) { this.budgetUsedPercent = budgetUsedPercent; }
    public String getBudgetStatus() { return budgetStatus; }
    public void setBudgetStatus(String budgetStatus) { this.budgetStatus = budgetStatus; }
    public int getTransactionCount() { return transactionCount; }
    public void setTransactionCount(int transactionCount) { this.transactionCount = transactionCount; }
    public BigDecimal getAverageTransaction() { return averageTransaction; }
    public void setAverageTransaction(BigDecimal averageTransaction) { this.averageTransaction = averageTransaction; }
    public BigDecimal getPreviousPeriodSpent() { return previousPeriodSpent; }
    public void setPreviousPeriodSpent(BigDecimal previousPeriodSpent) { this.previousPeriodSpent = previousPeriodSpent; }
    public BigDecimal getChangeAmount() { return changeAmount; }
    public void setChangeAmount(BigDecimal changeAmount) { this.changeAmount = changeAmount; }
    public Double getChangePercent() { return changePercent; }
    public void setChangePercent(Double changePercent) { this.changePercent = changePercent; }
    public Double getPercentOfTotal() { return percentOfTotal; }
    public void setPercentOfTotal(Double percentOfTotal) { this.percentOfTotal = percentOfTotal; }
}