package com.expensetracker.controller;

import com.expensetracker.model.Budget;
import com.expensetracker.model.Category;
import com.expensetracker.model.Expense;
import com.expensetracker.service.BudgetService;
import com.expensetracker.service.CategoryService;
import com.expensetracker.repository.BudgetRepository;
import com.expensetracker.repository.CategoryRepository;
import com.expensetracker.repository.ExpenseRepository;

//Logger Imports
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

// Jakarta EE imports instead of javax
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;

import java.io.IOException;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

// No @WebServlet annotation used here; mapping is handled in web.xml
public class BudgetServlet extends BaseServlet {

    private static final Logger log = LoggerFactory.getLogger(BudgetServlet.class);
    
    private BudgetService budgetService;
    private CategoryService categoryService;

    @Override
    public void init() throws ServletException {
        log.info("Initializing BudgetServlet");
        BudgetRepository budgetRepo = new BudgetRepository();
        CategoryRepository categoryRepo = new CategoryRepository();
        ExpenseRepository expenseRepo = new ExpenseRepository();
        
        this.budgetService = new BudgetService(budgetRepo, categoryRepo, expenseRepo);
        this.categoryService = new CategoryService();
        log.info("BudgetServlet Initialized Successfully");
    }

    @Override
    protected void handleAuthenticatedGet(HttpServletRequest request, HttpServletResponse response, Long userId) 
            throws ServletException, IOException {
        
        String action = request.getParameter("action");
        log.debug("GET request from user {}, action: {}", userId, action);

        if ("detail".equals(action)) {
            // NView Budget Details
            handleDetail(request, response, userId);
        } else {
            // Default: Show the main budget list
            handleList(request, response, userId);
        }

    }

    @Override
    protected void handleAuthenticatedPost(HttpServletRequest request, HttpServletResponse response, Long userId) 
            throws ServletException, IOException {
        
        HttpSession session = request.getSession(false);

        String action = request.getParameter("action");
        
        try {
            if ("create".equals(action)) {
                handleCreate(request, userId);
                session.setAttribute("successMessage", "Budget created successfully!");
                log.info("Budget created successfully by user {}", userId);
            } else if ("update".equals(action)) {
                handleUpdate(request, userId);
                session.setAttribute("successMessage", "Budget updated successfully!");
                log.info("Budget updated successfully by user {}", userId);
            } else if ("delete".equals(action)) {
                handleDelete(request, userId);
                session.setAttribute("successMessage", "Budget deleted.");
                log.info("Budget deleted by user {}", userId);
            }
        } catch (IllegalArgumentException e) {
            log.warn("Validation error for user {}: {}", userId, e.getMessage());
            session.setAttribute("errorMessage", e.getMessage());
        } catch (Exception e) {
            log.error("Error processing budget action for user {}: {}", userId, e.getMessage(), e); 
            session.setAttribute("errorMessage", "An unexpected error occurred processing your budget.");
        }

        // PRG Pattern (Post-Redirect-Get)
        response.sendRedirect(request.getContextPath() + "/budgets");
    }

    private void handleCreate(HttpServletRequest request, Long userId) {
        Integer categoryId = Integer.parseInt(request.getParameter("categoryId"));
        BigDecimal amount = new BigDecimal(request.getParameter("amount"));
        LocalDate periodStart = LocalDate.parse(request.getParameter("periodStart"));
        
        String periodEndStr = request.getParameter("periodEnd");
        LocalDate periodEnd = (periodEndStr != null && !periodEndStr.isEmpty()) 
                ? LocalDate.parse(periodEndStr) : null;
        
        boolean recurring = request.getParameter("recurring") != null;

        log.debug("Creating budget for user {}: categoryId={}, amount={}, periodStart={}", 
                  userId, categoryId, amount, periodStart);
        budgetService.createBudget(userId, categoryId, amount, periodStart, periodEnd, recurring);
    }

    private void handleUpdate(HttpServletRequest request, Long userId) {
        Long budgetId = Long.parseLong(request.getParameter("budgetId"));
        BigDecimal amount = new BigDecimal(request.getParameter("amount"));
        
        String periodEndStr = request.getParameter("periodEnd");
        LocalDate periodEnd = (periodEndStr != null && !periodEndStr.isEmpty()) 
                ? LocalDate.parse(periodEndStr) : null;
        
        boolean recurring = request.getParameter("recurring") != null;

        log.debug("Updating budget {} for user {}: amount={}", budgetId, userId, amount);
        budgetService.updateBudget(userId, budgetId, amount, periodEnd, recurring);
    }

    private void handleDelete(HttpServletRequest request, Long userId) {
        Long budgetId = Long.parseLong(request.getParameter("budgetId"));
        // Check for specific delete flags if your UI supports hard delete options
        boolean soft = !"false".equals(request.getParameter("soft")); 
        
        log.debug("Deleting budget {} for user {}", budgetId, userId);
        // Logic assumes service handles the difference or defaults to soft delete
        budgetService.deleteBudget(userId, budgetId); 
    }
    private void handleList(HttpServletRequest request, HttpServletResponse response, Long userId) 
        throws ServletException, IOException {
    // 1. Fetch active budgets
    List<Budget> budgets = budgetService.getActiveBudgets(userId);
    request.setAttribute("budgets", budgets);

    // 2. Fetch categories for the "Create" modal
    List<Category> categories = categoryService.listCategories(userId);
    request.setAttribute("categories", categories);
    
    log.debug("Loaded {} budgets and {} categories for user {}", 
                  budgets.size(), categories.size(), userId);
    request.getRequestDispatcher("/WEB-INF/views/budgets.jsp").forward(request, response);
}

private void handleDetail(HttpServletRequest request, HttpServletResponse response, Long userId) 
        throws ServletException, IOException {
        
    String idStr = request.getParameter("id");
    if (idStr == null || idStr.isEmpty()) {
        response.sendRedirect(request.getContextPath() + "/budgets");
        return;
    }

    Long budgetId = Long.parseLong(idStr);

    log.debug("Loading budget detail {} for user {}", budgetId, userId);
    
    // 1. Fetch the specific budget
    Budget budget = budgetService.getBudgetById(userId, budgetId);
    
    if (budget == null) {
        log.warn("Budget {} not found or access denied for user {}", budgetId, userId);
        // Budget not found or doesn't belong to user
        response.sendRedirect(request.getContextPath() + "/budgets");
        return;
    }

    LocalDate today = LocalDate.now();
    LocalDate monthStart = today.withDayOfMonth(1);
    LocalDate monthEnd   = today.withDayOfMonth(today.lengthOfMonth());

    request.setAttribute("currentMonthStartStr", monthStart.toString());
    request.setAttribute("currentMonthEndStr",   monthEnd.toString());
    

    // 2. Fetch the expenses that contribute to this budget
    List<Expense> expenses = budgetService.getExpensesForBudget(userId, budget);

    request.setAttribute("selectedBudget", budget);
    request.setAttribute("relatedExpenses", expenses);

    log.debug("Loaded budget {} with {} expenses for user {}", 
                  budgetId, expenses.size(), userId);   
    request.getRequestDispatcher("/WEB-INF/views/budget-details.jsp").forward(request, response);
}
}

