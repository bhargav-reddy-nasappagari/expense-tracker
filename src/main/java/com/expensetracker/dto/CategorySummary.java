package com.expensetracker.dto;

import java.math.BigDecimal;

// 2. CategorySummary.java
public class CategorySummary {
    private String categoryName;
    private BigDecimal total;
    private double percentage;
    private int transactionCount;

    public CategorySummary(String categoryName, BigDecimal total, double percentage, int transactionCount) {
        this.categoryName = categoryName;
        this.total = total;
        this.percentage = percentage;
        this.transactionCount = transactionCount;
    }

    public String getName() { return categoryName; } // Alias for getCategoryName
    public String getCategoryName() { return categoryName; }
    public void setCategoryName(String categoryName) { this.categoryName = categoryName; }
    public BigDecimal getTotal() { return total; }
    public void setTotal(BigDecimal total) { this.total = total; }
    public double getPercentage() { return percentage; }
    public void setPercentage(double percentage) { this.percentage = percentage; }
    public int getTransactionCount() { return transactionCount; }
    public void setTransactionCount(int transactionCount) { this.transactionCount = transactionCount; }
}