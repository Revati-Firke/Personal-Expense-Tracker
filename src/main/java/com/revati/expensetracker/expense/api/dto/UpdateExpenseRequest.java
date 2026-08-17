package com.revati.expensetracker.expense.api.dto;

import com.revati.expensetracker.expense.domain.CategoryType;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.math.BigDecimal;
import java.time.LocalDate;

public record UpdateExpenseRequest(
        @NotBlank @Size(max = 160) String description,
        @NotNull @DecimalMin(value = "0.01") BigDecimal amount,
        @NotNull CategoryType category,
        @NotNull LocalDate expenseDate,
        boolean recurring,
        @Size(max = 180) String note
) {
}
