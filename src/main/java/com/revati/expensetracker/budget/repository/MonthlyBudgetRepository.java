package com.revati.expensetracker.budget.repository;

import com.revati.expensetracker.budget.domain.MonthlyBudget;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface MonthlyBudgetRepository extends JpaRepository<MonthlyBudget, Long> {

    Optional<MonthlyBudget> findByYearAndMonth(Integer year, Integer month);
}
