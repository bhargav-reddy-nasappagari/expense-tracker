package com.expensetracker.dto;

import java.math.BigDecimal;

// 4. TrendPoint.java
public class TrendPoint {
    private String label; // e.g., "Jan 2024" or "2024-01-01"
    private BigDecimal amount;

    public TrendPoint(Object label, BigDecimal amount) {
        this.label = String.valueOf(label);
        this.amount = amount;
    }

    public String getLabel() { return label; }
    public void setLabel(String label) { this.label = label; }
    public BigDecimal getAmount() { return amount; }
    public void setAmount(BigDecimal amount) { this.amount = amount; }
}