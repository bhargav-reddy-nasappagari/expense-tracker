package com.expensetracker.dto;

import java.math.BigDecimal;
import java.time.LocalDate;

// 5. PeriodComparison.java
public class PeriodComparison {
    private LocalDate previousStartDate;
    private LocalDate previousEndDate;
    private BigDecimal previousTotal;
    private BigDecimal changeAmount;
    private Double percentageChange; // Renamed from changePercent to match existing code usage
    private BigDecimal absoluteChange; // Added based on context usage
    private String trend; // "increased", "decreased", "stable"

    public LocalDate getPreviousStartDate() { return previousStartDate; }
    public void setPreviousStartDate(LocalDate previousStartDate) { this.previousStartDate = previousStartDate; }
    public LocalDate getPreviousEndDate() { return previousEndDate; }
    public void setPreviousEndDate(LocalDate previousEndDate) { this.previousEndDate = previousEndDate; }
    public BigDecimal getPreviousTotal() { return previousTotal; }
    public void setPreviousTotal(BigDecimal previousTotal) { this.previousTotal = previousTotal; }
    public BigDecimal getChangeAmount() { return changeAmount; }
    public void setChangeAmount(BigDecimal changeAmount) { this.changeAmount = changeAmount; }
    public Double getPercentageChange() { return percentageChange; }
    public void setPercentageChange(Double percentageChange) { this.percentageChange = percentageChange; }
    public BigDecimal getAbsoluteChange() { return absoluteChange; }
    public void setAbsoluteChange(BigDecimal absoluteChange) { this.absoluteChange = absoluteChange; }
    public String getTrend() { return trend; }
    public void setTrend(String trend) { this.trend = trend; }
}