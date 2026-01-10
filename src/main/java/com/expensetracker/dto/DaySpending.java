package com.expensetracker.dto;

import java.math.BigDecimal;
import java.time.LocalDate;

// 7. DaySpending.java
public class DaySpending {
    private LocalDate date;
    private BigDecimal amount;
    private int transactionCount;
    private String colorLevel; // none, low, medium, high, very-high

    public LocalDate getDate() { return date; }
    public void setDate(LocalDate date) { this.date = date; }
    public BigDecimal getAmount() { return amount; }
    public void setAmount(BigDecimal amount) { this.amount = amount; }
    public int getTransactionCount() { return transactionCount; }
    public void setTransactionCount(int transactionCount) { this.transactionCount = transactionCount; }
    public String getColorLevel() { return colorLevel; }
    public void setColorLevel(String colorLevel) { this.colorLevel = colorLevel; }
}