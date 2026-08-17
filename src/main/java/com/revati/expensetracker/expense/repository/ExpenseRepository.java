package com.revati.expensetracker.expense.repository;

import com.revati.expensetracker.expense.domain.CategoryType;
import com.revati.expensetracker.expense.domain.Expense;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

public interface ExpenseRepository extends JpaRepository<Expense, Long> {

    List<Expense> findByExpenseDateBetweenOrderByExpenseDateDescIdDesc(LocalDate start, LocalDate end);

    List<Expense> findByCategoryAndExpenseDateBetweenOrderByExpenseDateDescIdDesc(CategoryType category, LocalDate start, LocalDate end);

    @Query("""
            SELECT COALESCE(SUM(e.amount), 0)
            FROM Expense e
            WHERE e.expenseDate BETWEEN :startDate AND :endDate
            """)
    BigDecimal sumBetween(@Param("startDate") LocalDate startDate, @Param("endDate") LocalDate endDate);

    @Query("""
            SELECT e.category, COALESCE(SUM(e.amount), 0)
            FROM Expense e
            WHERE e.expenseDate BETWEEN :startDate AND :endDate
            GROUP BY e.category
            ORDER BY COALESCE(SUM(e.amount), 0) DESC
            """)
    List<Object[]> sumByCategoryBetween(@Param("startDate") LocalDate startDate, @Param("endDate") LocalDate endDate);

    Optional<Expense> findTopByExpenseDateLessThanOrderByExpenseDateDesc(LocalDate date);
}
