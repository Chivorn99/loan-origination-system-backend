package com.example.loan_origination_system.model.loan;

import com.example.loan_origination_system.model.master.Branch;
import com.example.loan_origination_system.model.master.Currency;
import jakarta.persistence.*;
import lombok.Data;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.LocalDate;

@Entity
@Table(name = "cfg_loan")
@Data
public class CfgLoan {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "branch_id")
    private Branch branch;

    @ManyToOne
    @JoinColumn(name = "currency_id")
    private Currency currency;

    private BigDecimal minLoanAmount;
    private BigDecimal maxLoanAmount;
    private BigDecimal interestRate;
    private String interestType;
    private String interestPeriod;
    private BigDecimal penaltyRate;
    private Integer penaltyGraceDays;
    private Integer maxLoanDuration;
    private Integer autoForfeitDays;

    private String status;
    private LocalDate effectiveFrom;
    private LocalDate effectiveTo;
    private LocalDateTime createdAt;

    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
    }
}