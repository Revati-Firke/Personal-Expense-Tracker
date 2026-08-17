package com.revati.expensetracker.expense.domain;

import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Entity
@Table(name = "expenses")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Expense {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 160)
    private String description;

    @Column(nullable = false, precision = 12, scale = 2)
    private BigDecimal amount;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 30)
    private CategoryType category;

    @Column(nullable = false)
    private LocalDate expenseDate;

    @Column(nullable = false)
    private boolean recurring;

    @Column(length = 180)
    private String note;

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
        if (expenseDate == null) {
            this.expenseDate = LocalDate.now();
        }
    }

    @PreUpdate
    @SuppressWarnings("unused")
    void preUpdate() {
        this.updatedAt = LocalDateTime.now();
    }
}
