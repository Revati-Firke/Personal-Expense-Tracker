package com.revati.expensetracker.budget.api.dto;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;

import java.math.BigDecimal;

public record SetBudgetRequest(
        @NotNull @Min(2000) @Max(2100) Integer year,
        @NotNull @Min(1) @Max(12) Integer month,
        @NotNull @DecimalMin(value = "0.01") BigDecimal limitAmount
) {
}
