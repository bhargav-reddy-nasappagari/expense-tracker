package com.expensetracker.dto;

import com.expensetracker.model.Expense;
import java.math.BigDecimal;
import java.time.LocalDate;

public class ExpenseDetail {
    private Long id;
    private LocalDate date;
    private String description;
    private BigDecimal amount;
    private String categoryName;
    private Double percentageOfTotal;

    // Default Constructor
    public ExpenseDetail() {}

    /**
     * Updated Constructor
     * We pass 'categoryName' explicitly because Expense.java only stores categoryId,
     * not the full Category object.
     */
    public ExpenseDetail(Expense expense, String categoryName, Double percentageOfTotal) {
        this.id = expense.getId();
        this.date = expense.getExpenseDate();
        this.description = expense.getDescription();
        this.amount = expense.getAmount();
        this.categoryName = (categoryName != null) ? categoryName : "Uncategorized";
        this.percentageOfTotal = percentageOfTotal;
    }

    // Getters and Setters
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public LocalDate getDate() { return date; }
    public void setDate(LocalDate date) { this.date = date; }

    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }

    public BigDecimal getAmount() { return amount; }
    public void setAmount(BigDecimal amount) { this.amount = amount; }

    public String getCategoryName() { return categoryName; }
    public void setCategoryName(String categoryName) { this.categoryName = categoryName; }

    public Double getPercentageOfTotal() { return percentageOfTotal; }
    public void setPercentageOfTotal(Double percentageOfTotal) { this.percentageOfTotal = percentageOfTotal; }
}