package com.expensetracker.service;

import com.expensetracker.model.Expense;
import com.expensetracker.util.PagedResult;
import com.expensetracker.repository.ExpenseRepository;
import com.expensetracker.repository.CategoryRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Collections;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT) // Eliminates UnnecessaryStubbingException
class ExpenseServiceTest {

    @Mock
    private ExpenseRepository expenseRepository;

    @Mock
    private CategoryRepository categoryRepository;

    @InjectMocks
    private ExpenseService expenseService;

    private final Long USER_ID = 1L;
    private final Integer CAT_ID = 10;

    private Expense existingExpense;

    @BeforeEach
    void setUp() {
        existingExpense = new Expense();
        existingExpense.setId(50L);
        existingExpense.setUserId(USER_ID);
        existingExpense.setDescription("Old");
        existingExpense.setAmount(new BigDecimal("100.00"));
        existingExpense.setCategoryId(CAT_ID);
        existingExpense.setExpenseDate(LocalDate.now());
    }

    @Test
    void testAddExpense_Success() {
        // Arrange
        when(categoryRepository.existsByIdAndUserId(eq(CAT_ID), eq(USER_ID))).thenReturn(true);
        when(expenseRepository.save(any(Expense.class))).thenAnswer(i -> {
            Expense e = i.getArgument(0);
            e.setId(100L);
            return e;
        });

        // Act
        Expense result = expenseService.addExpense(USER_ID, "Groceries", new BigDecimal("50.00"), CAT_ID, LocalDate.now());

        // Assert
        assertNotNull(result.getId());
        assertEquals("Groceries", result.getDescription());
        assertEquals(new BigDecimal("50.00"), result.getAmount());
        verify(categoryRepository).existsByIdAndUserId(CAT_ID, USER_ID);
        verify(expenseRepository).save(any(Expense.class));
    }

    @Test
    void testAddExpense_UnauthorizedCategory() {
        // Arrange
        when(categoryRepository.existsByIdAndUserId(eq(CAT_ID), eq(USER_ID))).thenReturn(false);

        // Act & Assert
        assertThrows(IllegalArgumentException.class, () ->
            expenseService.addExpense(USER_ID, "Test", new BigDecimal("10.00"), CAT_ID, LocalDate.now()));
    }

    @Test
    void testEditExpense_OwnershipCheck() {
        // Arrange
        when(expenseRepository.findByIdAndUserId(50L, USER_ID)).thenReturn(Optional.of(existingExpense));
        when(categoryRepository.existsByIdAndUserId(CAT_ID, USER_ID)).thenReturn(true);
        when(expenseRepository.update(any(Expense.class))).thenReturn(existingExpense);

        // Act
        Expense result = expenseService.editExpense(USER_ID, 50L, "Updated", new BigDecimal("20.00"), CAT_ID, LocalDate.now());

        // Assert
        assertEquals("Updated", result.getDescription());
        assertEquals(new BigDecimal("20.00"), result.getAmount());
        verify(categoryRepository).existsByIdAndUserId(CAT_ID, USER_ID);
        verify(expenseRepository).update(any(Expense.class));
    }

    @Test
    void testEditExpense_UnauthorizedAccess() {
        // Arrange - expense belongs to another user
        Expense wrongUserExpense = new Expense();
        wrongUserExpense.setId(50L);
        wrongUserExpense.setUserId(999L);

        when(expenseRepository.findByIdAndUserId(eq(50L), eq(USER_ID))).thenReturn(Optional.of(wrongUserExpense));

        // Act & Assert
        assertThrows(IllegalArgumentException.class, () ->
            expenseService.editExpense(USER_ID, 50L, "Hack", new BigDecimal("1.00"), CAT_ID, LocalDate.now()));
    }

    @Test
    void testListExpensesPaginated_CurrentBehavior_NoClamping() {

        PagedResult<Expense> emptyResult = new PagedResult<>(Collections.emptyList(), 0, 1, 1);

        when(expenseRepository.findByUserIdAndFiltersPaginated(
                eq(USER_ID), isNull(), isNull(), isNull(), isNull(), isNull(), isNull(),
                eq(1), eq(1), eq("date"), eq("asc")
        )).thenReturn(emptyResult);

        PagedResult<Expense> result = expenseService.listExpensesPaginated(
            USER_ID, null, null, null, null, null, null, 1, 1, "date", "asc");

        assertNotNull(result);
        verify(expenseRepository).findByUserIdAndFiltersPaginated(
                eq(USER_ID), isNull(), isNull(), isNull(), isNull(), isNull(), isNull(),
                eq(1), eq(1), eq("date"), eq("asc"));
    }
}