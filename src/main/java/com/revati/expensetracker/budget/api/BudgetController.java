package com.revati.expensetracker.budget.api;

import com.revati.expensetracker.budget.api.dto.BudgetStatusResponse;
import com.revati.expensetracker.budget.api.dto.SetBudgetRequest;
import com.revati.expensetracker.budget.service.BudgetService;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/budgets")
@RequiredArgsConstructor
@Validated
public class BudgetController {

    private final BudgetService budgetService;

    @PostMapping
    public ResponseEntity<BudgetStatusResponse> setBudget(@Valid @RequestBody SetBudgetRequest request) {
        return ResponseEntity.ok(budgetService.setBudget(request));
    }

    @GetMapping("/{year}/{month}")
    public ResponseEntity<BudgetStatusResponse> getBudgetStatus(
            @PathVariable @Min(2000) @Max(2100) Integer year,
            @PathVariable @Min(1) @Max(12) Integer month
    ) {
        return ResponseEntity.ok(budgetService.getStatus(year, month));
    }
}
