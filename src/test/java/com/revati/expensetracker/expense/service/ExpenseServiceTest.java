package com.revati.expensetracker.expense.service;

import com.revati.expensetracker.expense.api.dto.CreateExpenseRequest;
import com.revati.expensetracker.expense.api.dto.ExpenseSummaryResponse;
import com.revati.expensetracker.expense.domain.CategoryType;
import com.revati.expensetracker.expense.domain.Expense;
import com.revati.expensetracker.expense.repository.ExpenseRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ExpenseServiceTest {

    @Mock
    private ExpenseRepository expenseRepository;

    private ExpenseService expenseService;

    @BeforeEach
    void setUp() {
        expenseService = new ExpenseService(expenseRepository);
    }

    @Test
    void shouldCreateExpense() {
        CreateExpenseRequest request = new CreateExpenseRequest(
                "Lunch",
                new BigDecimal("12.50"),
                CategoryType.FOOD,
                LocalDate.now(),
                false,
                "Office cafeteria"
        );

        Expense saved = Expense.builder()
                .id(1L)
                .description("Lunch")
                .amount(new BigDecimal("12.50"))
                .category(CategoryType.FOOD)
                .expenseDate(LocalDate.now())
                .recurring(false)
                .note("Office cafeteria")
                .build();

        when(expenseRepository.save(any(Expense.class))).thenReturn(saved);

        var response = expenseService.create(request);

        assertThat(response.id()).isEqualTo(1L);
        assertThat(response.description()).isEqualTo("Lunch");
        assertThat(response.category()).isEqualTo(CategoryType.FOOD);
    }

    @Test
    void shouldBuildSummaryWithTopCategory() {
        LocalDate now = LocalDate.now();
        LocalDate monthStart = now.withDayOfMonth(1);
        LocalDate monthEnd = now.withDayOfMonth(now.lengthOfMonth());

        when(expenseRepository.sumBetween(monthStart, monthEnd)).thenReturn(new BigDecimal("100.00"));
        when(expenseRepository.sumByCategoryBetween(monthStart, monthEnd)).thenReturn(List.of(
                new Object[]{CategoryType.FOOD, new BigDecimal("70.00")},
                new Object[]{CategoryType.TRAVEL, new BigDecimal("30.00")}
        ));
        when(expenseRepository.findTopByExpenseDateLessThanOrderByExpenseDateDesc(any(LocalDate.class)))
                .thenReturn(Optional.of(Expense.builder().expenseDate(now.minusDays(2)).build()));

        ExpenseSummaryResponse summary = expenseService.summary(null);

        assertThat(summary.total()).isEqualByComparingTo("100.00");
        assertThat(summary.topCategory()).isEqualTo(CategoryType.FOOD);
        assertThat(summary.noSpendStreakDays()).isEqualTo(2);
        assertThat(summary.categoryBreakdown()).hasSize(2);
    }
}
