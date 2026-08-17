package com.revati.expensetracker.expense.api.dto;

import com.revati.expensetracker.expense.domain.CategoryType;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

public record ExpenseResponse(
        Long id,
        String description,
        BigDecimal amount,
        CategoryType category,
        LocalDate expenseDate,
        boolean recurring,
        String note,
        LocalDateTime createdAt,
        LocalDateTime updatedAt
) {
}
