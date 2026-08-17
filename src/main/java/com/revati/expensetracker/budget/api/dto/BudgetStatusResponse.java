package com.revati.expensetracker.budget.api.dto;

import java.math.BigDecimal;

public record BudgetStatusResponse(
        Integer year,
        Integer month,
        BigDecimal budgetLimit,
        BigDecimal spent,
        BigDecimal remaining,
        boolean exceeded,
        String message
) {
}
