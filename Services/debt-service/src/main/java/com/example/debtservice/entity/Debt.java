package com.example.debtservice.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Entity
@Table(name = "debts")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Debt {

    @Id
    @Column(nullable = false, updatable = false)
    private String id;

    @Column(name = "debtor_id", nullable = false)
    private String debtorId;

    @Column(nullable = false, length = 300)
    private String description;

    @Column(name = "original_amount", nullable = false, precision = 15, scale = 2)
    private BigDecimal originalAmount;

    @Column(name = "current_balance", nullable = false, precision = 15, scale = 2)
    private BigDecimal currentBalance;

    @Column(nullable = false, length = 3)
    private String currency;

    @Column(nullable = false, length = 10)
    private String status;

    @Column(name = "due_date")
    private LocalDate dueDate;

    @Column(name = "created_at")
    private LocalDateTime createdAt;

    @Column(name = "updated_at")
    private LocalDateTime updatedAt;
}
