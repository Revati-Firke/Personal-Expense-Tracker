package com.revati.expensetracker.expense.api;

import com.revati.expensetracker.expense.api.dto.*;
import com.revati.expensetracker.expense.domain.CategoryType;
import com.revati.expensetracker.expense.service.ExpenseService;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.nio.charset.StandardCharsets;
import java.util.List;

@RestController
@RequestMapping("/api/v1/expenses")
@RequiredArgsConstructor
@Validated
public class ExpenseController {

    private final ExpenseService expenseService;

    @PostMapping
    public ResponseEntity<ExpenseResponse> create(@Valid @RequestBody CreateExpenseRequest request) {
        return ResponseEntity.ok(expenseService.create(request));
    }

    @PutMapping("/{id}")
    public ResponseEntity<ExpenseResponse> update(@PathVariable Long id, @Valid @RequestBody UpdateExpenseRequest request) {
        return ResponseEntity.ok(expenseService.update(id, request));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        expenseService.delete(id);
        return ResponseEntity.noContent().build();
    }

    @GetMapping
    public ResponseEntity<List<ExpenseResponse>> list(
            @RequestParam(required = false) @Min(1) @Max(12) Integer month,
            @RequestParam(required = false) CategoryType category
    ) {
        return ResponseEntity.ok(expenseService.list(month, category));
    }

    @GetMapping("/summary")
    public ResponseEntity<ExpenseSummaryResponse> summary(
            @RequestParam(required = false) @Min(1) @Max(12) Integer month
    ) {
        return ResponseEntity.ok(expenseService.summary(month));
    }

    @GetMapping(value = "/export/csv", produces = "text/csv")
    public ResponseEntity<byte[]> exportCsv(
            @RequestParam(required = false) @Min(1) @Max(12) Integer month
    ) {
        byte[] content = expenseService.exportCsv(month).getBytes(StandardCharsets.UTF_8);
        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=expenses.csv")
                .contentType(new MediaType("text", "csv"))
                .body(content);
    }
}
