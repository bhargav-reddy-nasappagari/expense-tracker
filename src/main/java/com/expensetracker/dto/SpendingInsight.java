package com.expensetracker.dto;

import java.math.BigDecimal;

// 6. SpendingInsight.java
public class SpendingInsight {
    private String type; // warning, positive, neutral, suggestion
    private String message;
    private String icon;
    private BigDecimal amount;

    public SpendingInsight(String type, String message, String icon, BigDecimal amount) {
        this.type = type;
        this.message = message;
        this.icon = icon;
        this.amount = amount;
    }

    public String getType() { return type; }
    public String getMessage() { return message; }
    public String getIcon() { return icon; }
    public BigDecimal getAmount() { return amount; }
}