package com.revati.expensetracker.budget.service;

import com.revati.expensetracker.budget.api.dto.BudgetStatusResponse;
import com.revati.expensetracker.budget.api.dto.SetBudgetRequest;
import com.revati.expensetracker.budget.domain.MonthlyBudget;
import com.revati.expensetracker.budget.repository.MonthlyBudgetRepository;
import com.revati.expensetracker.expense.repository.ExpenseRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class BudgetServiceTest {

    @Mock
    private MonthlyBudgetRepository monthlyBudgetRepository;

    @Mock
    private ExpenseRepository expenseRepository;

    private BudgetService budgetService;

    @BeforeEach
    void setUp() {
        budgetService = new BudgetService(monthlyBudgetRepository, expenseRepository);
    }

    @Test
    void shouldReturnExceededBudgetStatus() {
        SetBudgetRequest request = new SetBudgetRequest(2026, 7, new BigDecimal("500.00"));

        when(monthlyBudgetRepository.findByYearAndMonth(2026, 7)).thenReturn(Optional.of(new MonthlyBudget()));
        when(monthlyBudgetRepository.save(any(MonthlyBudget.class))).thenAnswer(invocation -> invocation.getArgument(0));

        LocalDate start = LocalDate.of(2026, 7, 1);
        LocalDate end = LocalDate.of(2026, 7, 31);
        when(expenseRepository.sumBetween(start, end)).thenReturn(new BigDecimal("620.00"));

        BudgetStatusResponse response = budgetService.setBudget(request);

        assertThat(response.exceeded()).isTrue();
        assertThat(response.remaining()).isEqualByComparingTo("-120.00");
    }
}
