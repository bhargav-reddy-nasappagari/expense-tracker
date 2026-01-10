package com.expensetracker.service;

import com.expensetracker.model.Expense;
import com.expensetracker.repository.CategoryRepository;
import com.expensetracker.repository.ExpenseRepository;
import com.expensetracker.util.PagedResult;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

public class ExpenseService {

    private final ExpenseRepository expenseRepo;
    private final CategoryRepository catRepo;

    // Add constructor for dependency injection
    public ExpenseService(ExpenseRepository expenseRepo, CategoryRepository catRepo) {
        this.expenseRepo = expenseRepo;
        this.catRepo = catRepo;
    }

    // Keep default constructor for backward compatibility
    public ExpenseService() {
        this(new ExpenseRepository(), new CategoryRepository());
    }

    // 1. List with optional filters
    public List<Expense> listExpenses(Long userId, LocalDate from, LocalDate to, Integer categoryId) {
        return expenseRepo.findByUserIdAndFilters(userId, from, to, categoryId, null, null, null);
    }

    // 2. Add new expense
    public Expense addExpense(Long userId,
                              String description,
                              BigDecimal amount,
                              Integer categoryId,
                              LocalDate expenseDate) {

        description = ValidationService.validateDescription(description);
        amount = ValidationService.validateAndRoundAmount(amount);
        expenseDate = ValidationService.validateExpenseDate(expenseDate);

        // Security: category must belong to user
        if (!catRepo.existsByIdAndUserId(categoryId, userId)) {
            throw new IllegalArgumentException("Invalid or unauthorized category");
        }

        Expense expense = new Expense();
        expense.setUserId(userId);
        expense.setDescription(description);
        expense.setAmount(amount);
        expense.setCategoryId(categoryId);
        expense.setExpenseDate(expenseDate);

        return expenseRepo.save(expense);
    }

    // 3. Edit existing expense
    public Expense editExpense(Long userId,
                               Long expenseId,
                               String description,
                               BigDecimal amount,
                               Integer categoryId,
                               LocalDate expenseDate) {

        description = ValidationService.validateDescription(description);
        amount = ValidationService.validateAndRoundAmount(amount);
        expenseDate = ValidationService.validateExpenseDate(expenseDate);

        // Ownership check
        Expense existing = expenseRepo.findByIdAndUserId(expenseId, userId)
                .orElseThrow(() -> new IllegalArgumentException("Expense not found or not yours"));

        // Category ownership check
        if (!catRepo.existsByIdAndUserId(categoryId, userId)) {
            throw new IllegalArgumentException("Invalid or unauthorized category");
        }

        existing.setDescription(description);
        existing.setAmount(amount);
        existing.setCategoryId(categoryId);
        existing.setExpenseDate(expenseDate);

        return expenseRepo.update(existing);
    }

    // 4. Delete expense
    public void deleteExpense(Long userId, Long expenseId) {
        Expense expense = expenseRepo.findByIdAndUserId(expenseId, userId)
                .orElseThrow(() -> new IllegalArgumentException("Expense not found or not yours"));

        expenseRepo.delete(expenseId, userId);
    }

    // 5. Get single expense for editing (used by EditExpenseServlet)
    public Expense getExpenseForEdit(Long userId, Long expenseId) {
        return expenseRepo.findByIdAndUserId(expenseId, userId)
                .orElseThrow(() -> new IllegalArgumentException("Expense not found or access denied"));
    }

    /**
     * Get paginated expenses with filters and sorting
     * @param userId - User ID
     * @param from - Start date filter (optional)
     * @param to - End date filter (optional)
     * @param categoryId - Category filter (optional)
     * @param minAmount - Min Amount filter (optional) 
     * @param maxAmount - Max Amount filter (optional) 
     * @param keyword - Search keyword (optional)      
     * @param page - Current page (1-indexed)
     * @param pageSize - Items per page
     * @param sortBy - Column to sort by (date, amount, category, description)
     * @param sortOrder - Sort direction (ASC, DESC)
     * @return PagedResult containing expenses and pagination metadata
     */
    public PagedResult<Expense> listExpensesPaginated(
            Long userId, 
            LocalDate from, 
            LocalDate to, 
            Integer categoryId,
            BigDecimal minAmount,
            BigDecimal maxAmount,
            String keyword,
            int page,
            int pageSize,
            String sortBy,
            String sortOrder) {
        
        return expenseRepo.findByUserIdAndFiltersPaginated(
            userId, from, to, categoryId, minAmount, maxAmount, keyword, page, pageSize, sortBy, sortOrder
        );
    }
}