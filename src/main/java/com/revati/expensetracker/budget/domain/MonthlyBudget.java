package com.revati.expensetracker.budget.domain;

import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "monthly_budgets", uniqueConstraints = {
        @UniqueConstraint(name = "uk_budget_year_month", columnNames = {"budget_year", "budget_month"})
})
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class MonthlyBudget {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "budget_year", nullable = false)
    private Integer year;

    @Column(name = "budget_month", nullable = false)
    private Integer month;

    @Column(nullable = false, precision = 12, scale = 2)
    private BigDecimal limitAmount;

    @Column(nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @Column(nullable = false)
    private LocalDateTime updatedAt;

    @PrePersist
    @SuppressWarnings("unused")
    void prePersist() {
        LocalDateTime now = LocalDateTime.now();
        this.createdAt = now;
        this.updatedAt = now;
    }

    @PreUpdate
    @SuppressWarnings("unused")
    void preUpdate() {
        this.updatedAt = LocalDateTime.now();
    }
}
