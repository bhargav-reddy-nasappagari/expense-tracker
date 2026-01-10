package com.expensetracker.service;

import com.expensetracker.model.Budget;
import com.expensetracker.model.Expense;
import com.expensetracker.repository.BudgetRepository;
import com.expensetracker.repository.CategoryRepository;
import com.expensetracker.repository.ExpenseRepository;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.util.List;

public class BudgetService {

    private final BudgetRepository budgetRepository;
    private final CategoryRepository categoryRepository;
    private final ExpenseRepository expenseRepository;

    public BudgetService(BudgetRepository budgetRepo, CategoryRepository categoryRepo, ExpenseRepository expenseRepo) {
        this.budgetRepository = budgetRepo;
        this.categoryRepository = categoryRepo;
        this.expenseRepository = expenseRepo;
    }

    public Budget createBudget(Long userId, Integer categoryId, BigDecimal amount, 
                               LocalDate periodStart, LocalDate periodEnd, boolean recurring) {
        // 1. Validation
        if (amount == null || amount.compareTo(BigDecimal.ZERO) <= 0) {
            throw new IllegalArgumentException("Budget amount must be positive.");
        }
        if (periodStart == null) {
            throw new IllegalArgumentException("Start date is required.");
        }
        if (periodEnd != null && periodEnd.isBefore(periodStart)) {
            throw new IllegalArgumentException("End date cannot be before start date.");
        }
        
        // Verify category ownership
        // Note: If your CategoryRepository only returns lists/void, 
        // you might need to adjust this check or trust the FK constraint in DB will catch it.
        // Assuming existsByIdAndUserId exists or we skip this pre-check and let DB fail.
        if (!categoryRepository.existsByIdAndUserId(categoryId, userId)) {
             throw new IllegalArgumentException("Invalid category selected.");
        }

        // Check for duplicates
        if (budgetRepository.existsByUserCategoryAndPeriod(userId, categoryId, periodStart)) {
            throw new IllegalArgumentException("A budget already exists for this category starting on this date.");
        }

        // 2. Create Budget
        Budget budget = new Budget(userId, categoryId, amount, periodStart);
        budget.setPeriodEnd(periodEnd);
        budget.setRecurring(recurring);
        budget.setActive(true);

        // 3. Save
        return budgetRepository.save(budget);
    }

    public List<Budget> getActiveBudgets(Long userId) {
        List<Budget> budgets = budgetRepository.findActiveByUserId(userId);
        
        // Enrich with spent amounts
        for (Budget budget : budgets) {
            BigDecimal spent = calculateSpentAmount(userId, budget);
            budget.setSpentAmount(spent != null ? spent : BigDecimal.ZERO);

                    if (budget.getAmount().compareTo(BigDecimal.ZERO) > 0) {
                        double percentage = spent.divide(budget.getAmount(), 4, RoundingMode.HALF_UP)
                                                .multiply(new BigDecimal(100))
                                                .doubleValue();
                        budget.setPercentageUsed(Math.min(percentage, 100.0)); // Cap at 100 for display
                    } else {
                        budget.setPercentageUsed(0.0);
                    }
        }
        
        return budgets;
    }

    public Budget getBudgetById(Long userId, Long budgetId) {
        Budget budget = budgetRepository.findById(budgetId)
                .orElseThrow(() -> new IllegalArgumentException("Budget not found."));

        if (!budget.getUserId().equals(userId)) {
            throw new IllegalArgumentException("Access denied.");
        }

        // Enrich with spent amount
        BigDecimal spent = calculateSpentAmount(userId, budget);
        budget.setSpentAmount(spent);
        
        // Removed call to categoryRepository.findById()
        // budget.getCategoryName() is already populated by the Repository JOIN
        
        return budget;
    }

    public List<Expense> getExpensesForBudget(Long userId, Budget budget) {
        LocalDate end;
        LocalDate start;

        if (budget.isRecurring()) {
            LocalDate today = LocalDate.now();
            start = today.withDayOfMonth(1);
            end = today.withDayOfMonth(today.lengthOfMonth());
        } else{
            start = budget.getPeriodStart();
            end = LocalDate.now();
        }

        return expenseRepository.findByUserIdAndFilters(
            userId, start, end, budget.getCategoryId(), null, null, null
        );
    }    

    public void updateBudget(Long userId, Long budgetId, BigDecimal amount, 
                             LocalDate periodEnd, boolean recurring) {
        Budget budget = getBudgetById(userId, budgetId); // Checks ownership
        
        if (amount != null) budget.setAmount(amount);
        budget.setPeriodEnd(periodEnd); 
        budget.setRecurring(recurring);
        
        budgetRepository.update(budget);
    }

    public void deleteBudget(Long userId, Long budgetId) {
        Budget budget = getBudgetById(userId, budgetId); // Checks ownership
        budgetRepository.softDelete(budgetId, userId);
    }

    private BigDecimal calculateSpentAmount(Long userId, Budget budget) {
        LocalDate effectiveStart;
        if (budget.isRecurring()) {
            // Assuming monthly recurring
            LocalDate today = LocalDate.now();
            effectiveStart = today.withDayOfMonth(1);  // First day of current month
        } else {
            effectiveStart = budget.getPeriodStart();
        }
        LocalDate end = budget.getPeriodEnd();
        if (end == null) {
            end = LocalDate.now(); 
        }
        
        return expenseRepository.sumAmountByCategoryAndDateRange(
                userId, 
                budget.getCategoryId(), 
                effectiveStart, 
                end
        );
    }
}