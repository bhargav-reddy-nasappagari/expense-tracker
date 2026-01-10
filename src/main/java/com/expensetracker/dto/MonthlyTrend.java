package com.expensetracker.dto;

import java.math.BigDecimal;
import java.util.Map;

// 8. MonthlyTrend.java
public class MonthlyTrend {
    private String month;
    private BigDecimal total;
    private Map<String, BigDecimal> categoryTotals;
    private BigDecimal changeAmount;
    private Double changePercent;

    public String getMonth() { return month; }
    public void setMonth(String month) { this.month = month; }
    public BigDecimal getTotal() { return total; }
    public void setTotal(BigDecimal total) { this.total = total; }
    public Map<String, BigDecimal> getCategoryTotals() { return categoryTotals; }
    public void setCategoryTotals(Map<String, BigDecimal> categoryTotals) { this.categoryTotals = categoryTotals; }
    public BigDecimal getChangeAmount() { return changeAmount; }
    public void setChangeAmount(BigDecimal changeAmount) { this.changeAmount = changeAmount; }
    public Double getChangePercent() { return changePercent; }
    public void setChangePercent(Double changePercent) { this.changePercent = changePercent; }
}