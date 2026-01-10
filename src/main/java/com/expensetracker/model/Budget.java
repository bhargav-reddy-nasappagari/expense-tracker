package com.expensetracker.model;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.LocalDateTime;

public class Budget {
    private Long id;
    private Long userId;
    private Integer categoryId;
    private BigDecimal amount;
    private LocalDate periodStart;
    private LocalDate periodEnd;
    private boolean recurring;
    private boolean active;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    // Transient fields for UI display (not in DB)
    private String categoryName;
    private BigDecimal spentAmount = BigDecimal.ZERO; // ✅ Default to 0
    private double percentageUsed = 0.0; // ✅ Pre-calculated
    public Budget() {
        this.recurring = true;
        this.active = true;
        this.spentAmount = BigDecimal.ZERO;
    }

    public Budget(Long userId, Integer categoryId, BigDecimal amount, LocalDate periodStart) {
        this();
        this.userId = userId;
        this.categoryId = categoryId;
        this.amount = amount;
        this.periodStart = periodStart;
    }
    

    // Getters and Setters

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public Long getUserId() { return userId; }
    public void setUserId(Long userId) { this.userId = userId; }

    public Integer getCategoryId() { return categoryId; }
    public void setCategoryId(Integer categoryId) { this.categoryId = categoryId; }

    public BigDecimal getAmount() { return amount; }
    public void setAmount(BigDecimal amount) {
        if (amount != null && amount.compareTo(BigDecimal.ZERO) <= 0) {
            throw new IllegalArgumentException("Budget amount must be positive");
        }
        this.amount = amount;
    }

    public LocalDate getPeriodStart() { return periodStart; }
    public void setPeriodStart(LocalDate periodStart) { this.periodStart = periodStart; }

    public LocalDate getPeriodEnd() { return periodEnd; }
    public void setPeriodEnd(LocalDate periodEnd) {
        if (periodEnd != null && periodStart != null && periodEnd.isBefore(periodStart)) {
            throw new IllegalArgumentException("End date cannot be before start date");
        }
        this.periodEnd = periodEnd;
    }

    public boolean isRecurring() { return recurring; }
    public void setRecurring(boolean recurring) { this.recurring = recurring; }

    public boolean isActive() { return active; }
    public void setActive(boolean active) { this.active = active; }

    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }

    public LocalDateTime getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(LocalDateTime updatedAt) { this.updatedAt = updatedAt; }

    // Transient getters/setters
    public String getCategoryName() { return categoryName; }
    public void setCategoryName(String categoryName) { this.categoryName = categoryName; }

    public BigDecimal getSpentAmount() { return spentAmount != null ? spentAmount : BigDecimal.ZERO; }
    public void setSpentAmount(BigDecimal spentAmount) { this.spentAmount = spentAmount; }

    // Domain Logic / Helper Methods

    /**
     * Checks if the budget is currently active based on the current date.
     */
    public boolean isCurrentlyActive() {
        LocalDate today = LocalDate.now();
        return !today.isBefore(periodStart) && 
               (periodEnd == null || !today.isAfter(periodEnd));
    }

    /**
     * Calculates the remaining amount in the budget.
     */
    public BigDecimal getRemainingAmount() {
        if (amount == null) return BigDecimal.ZERO;
        BigDecimal spent = getSpentAmount();
        return amount.subtract(spent);
    }

    /**
     * Calculates the percentage of the budget used.
     * Returns a value between 0 and 100+
     */
    public double getPercentageUsed() { return percentageUsed; }
    public void setPercentageUsed(double percentageUsed) { this.percentageUsed = percentageUsed; }
    /**
     * Helper to determine CSS class for progress bars based on percentage
     */
    public String getStatusColorClass() {
        double pct = getPercentageUsed();
        if (pct >= 100) return "bg-danger"; // Red
        if (pct >= 75) return "bg-warning"; // Yellow
        return "bg-success"; // Green
    }
}