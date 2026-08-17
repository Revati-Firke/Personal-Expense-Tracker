package com.revati.expensetracker.expense.api.dto;

import com.revati.expensetracker.expense.domain.CategoryType;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Map;

public record ExpenseSummaryResponse(
        LocalDate startDate,
        LocalDate endDate,
        BigDecimal total,
        CategoryType topCategory,
        BigDecimal topCategoryAmount,
        int noSpendStreakDays,
        Map<CategoryType, BigDecimal> categoryBreakdown
) {
}
