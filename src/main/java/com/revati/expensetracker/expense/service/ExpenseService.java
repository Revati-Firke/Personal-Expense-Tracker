package com.revati.expensetracker.expense.service;

import com.revati.expensetracker.expense.api.dto.*;
import com.revati.expensetracker.expense.domain.CategoryType;
import com.revati.expensetracker.expense.domain.Expense;
import com.revati.expensetracker.expense.repository.ExpenseRepository;
import com.revati.expensetracker.shared.exception.BadRequestException;
import com.revati.expensetracker.shared.exception.NotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.YearMonth;
import java.time.temporal.ChronoUnit;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class ExpenseService {

    private final ExpenseRepository expenseRepository;

    @Transactional
    public ExpenseResponse create(CreateExpenseRequest request) {
        validateAmount(request.amount());

        Expense expense = Expense.builder()
                .description(request.description().trim())
                .amount(request.amount())
                .category(request.category())
                .expenseDate(request.expenseDate() != null ? request.expenseDate() : LocalDate.now())
                .recurring(request.recurring())
                .note(request.note())
                .build();

        return toResponse(expenseRepository.save(expense));
    }

    @Transactional
    public ExpenseResponse update(Long id, UpdateExpenseRequest request) {
        validateAmount(request.amount());

        Expense expense = expenseRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("Expense not found: " + id));

        expense.setDescription(request.description().trim());
        expense.setAmount(request.amount());
        expense.setCategory(request.category());
        expense.setExpenseDate(request.expenseDate());
        expense.setRecurring(request.recurring());
        expense.setNote(request.note());

        return toResponse(expenseRepository.save(expense));
    }

    @Transactional
    public void delete(Long id) {
        if (!expenseRepository.existsById(id)) {
            throw new NotFoundException("Expense not found: " + id);
        }
        expenseRepository.deleteById(id);
    }

    @Transactional(readOnly = true)
    public List<ExpenseResponse> list(Integer month, CategoryType category) {
        YearMonth yearMonth = month == null
                ? YearMonth.now()
                : YearMonth.of(LocalDate.now().getYear(), month);

        LocalDate start = yearMonth.atDay(1);
        LocalDate end = yearMonth.atEndOfMonth();

        List<Expense> expenses = category == null
                ? expenseRepository.findByExpenseDateBetweenOrderByExpenseDateDescIdDesc(start, end)
                : expenseRepository.findByCategoryAndExpenseDateBetweenOrderByExpenseDateDescIdDesc(category, start, end);

        return expenses.stream().map(this::toResponse).toList();
    }

    @Transactional(readOnly = true)
    public ExpenseSummaryResponse summary(Integer month) {
        YearMonth yearMonth = month == null
                ? YearMonth.now()
                : YearMonth.of(LocalDate.now().getYear(), month);

        LocalDate start = yearMonth.atDay(1);
        LocalDate end = yearMonth.atEndOfMonth();

        BigDecimal total = expenseRepository.sumBetween(start, end);

        List<Object[]> rows = expenseRepository.sumByCategoryBetween(start, end);
        Map<CategoryType, BigDecimal> categoryBreakdown = new LinkedHashMap<>();
        for (Object[] row : rows) {
            categoryBreakdown.put((CategoryType) row[0], (BigDecimal) row[1]);
        }

        CategoryType topCategory = categoryBreakdown.isEmpty() ? null : categoryBreakdown.keySet().iterator().next();
        BigDecimal topCategoryAmount = topCategory == null ? BigDecimal.ZERO : categoryBreakdown.get(topCategory);

        int noSpendStreak = calculateNoSpendStreak();

        return new ExpenseSummaryResponse(
                start,
                end,
                total,
                topCategory,
                topCategoryAmount,
                noSpendStreak,
                categoryBreakdown
        );
    }

    @Transactional(readOnly = true)
    public String exportCsv(Integer month) {
        YearMonth yearMonth = month == null
                ? YearMonth.now()
                : YearMonth.of(LocalDate.now().getYear(), month);

        LocalDate start = yearMonth.atDay(1);
        LocalDate end = yearMonth.atEndOfMonth();

        List<Expense> expenses = expenseRepository.findByExpenseDateBetweenOrderByExpenseDateDescIdDesc(start, end);

        StringBuilder csv = new StringBuilder("id,date,description,category,amount,recurring,note\n");
        for (Expense expense : expenses) {
            csv.append(expense.getId()).append(',')
                    .append(expense.getExpenseDate()).append(',')
                    .append(escapeCsv(expense.getDescription())).append(',')
                    .append(expense.getCategory()).append(',')
                    .append(expense.getAmount()).append(',')
                    .append(expense.isRecurring()).append(',')
                    .append(escapeCsv(expense.getNote()))
                    .append('\n');
        }
        return csv.toString();
    }

    private int calculateNoSpendStreak() {
        LocalDate today = LocalDate.now();
        LocalDate previousSpendDate = expenseRepository
                .findTopByExpenseDateLessThanOrderByExpenseDateDesc(today.plusDays(1))
                .map(Expense::getExpenseDate)
                .orElse(null);

        if (previousSpendDate == null) {
            return 0;
        }

        long days = ChronoUnit.DAYS.between(previousSpendDate, today);
        return (int) Math.max(days, 0);
    }

    private String escapeCsv(String raw) {
        if (raw == null || raw.isBlank()) {
            return "";
        }
        String escaped = raw.replace("\"", "\"\"");
        if (escaped.contains(",") || escaped.contains("\"") || escaped.contains("\n")) {
            return "\"" + escaped + "\"";
        }
        return escaped;
    }

    private void validateAmount(BigDecimal amount) {
        if (amount == null || amount.compareTo(BigDecimal.ZERO) <= 0) {
            throw new BadRequestException("Amount must be greater than zero");
        }
    }

    public ExpenseResponse toResponse(Expense expense) {
        return new ExpenseResponse(
                expense.getId(),
                expense.getDescription(),
                expense.getAmount(),
                expense.getCategory(),
                expense.getExpenseDate(),
                expense.isRecurring(),
                expense.getNote(),
                expense.getCreatedAt(),
                expense.getUpdatedAt()
        );
    }
}
