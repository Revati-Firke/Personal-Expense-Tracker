package com.revati.expensetracker.budget.service;

import com.revati.expensetracker.budget.api.dto.BudgetStatusResponse;
import com.revati.expensetracker.budget.api.dto.SetBudgetRequest;
import com.revati.expensetracker.budget.domain.MonthlyBudget;
import com.revati.expensetracker.budget.repository.MonthlyBudgetRepository;
import com.revati.expensetracker.expense.repository.ExpenseRepository;
import com.revati.expensetracker.shared.exception.NotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.YearMonth;

@Service
@RequiredArgsConstructor
public class BudgetService {

    private final MonthlyBudgetRepository monthlyBudgetRepository;
    private final ExpenseRepository expenseRepository;

    @Transactional
    public BudgetStatusResponse setBudget(SetBudgetRequest request) {
        MonthlyBudget budget = monthlyBudgetRepository
                .findByYearAndMonth(request.year(), request.month())
                .orElseGet(MonthlyBudget::new);

        budget.setYear(request.year());
        budget.setMonth(request.month());
        budget.setLimitAmount(request.limitAmount());

        monthlyBudgetRepository.save(budget);

        return buildStatus(request.year(), request.month(), request.limitAmount());
    }

    @Transactional(readOnly = true)
    public BudgetStatusResponse getStatus(Integer year, Integer month) {
        MonthlyBudget budget = monthlyBudgetRepository.findByYearAndMonth(year, month)
                .orElseThrow(() -> new NotFoundException("Budget not found for " + year + "-" + month));

        return buildStatus(year, month, budget.getLimitAmount());
    }

    private BudgetStatusResponse buildStatus(Integer year, Integer month, BigDecimal limit) {
        YearMonth yearMonth = YearMonth.of(year, month);
        LocalDate start = yearMonth.atDay(1);
        LocalDate end = yearMonth.atEndOfMonth();

        BigDecimal spent = expenseRepository.sumBetween(start, end);
        BigDecimal remaining = limit.subtract(spent);
        boolean exceeded = remaining.compareTo(BigDecimal.ZERO) < 0;

        String message = exceeded
                ? "Budget exceeded by " + remaining.abs()
                : "Within budget. Remaining " + remaining;

        return new BudgetStatusResponse(
                year,
                month,
                limit,
                spent,
                remaining,
                exceeded,
                message
        );
    }
}
