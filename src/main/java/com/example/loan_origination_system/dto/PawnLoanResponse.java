package com.example.loan_origination_system.dto;

import com.example.loan_origination_system.model.enums.LoanStatus;

import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Data
public class PawnLoanResponse {
    private Long id;
    private String loanCode;
    private CustomerResponse customer;
    private PawnItemResponse pawnItem;
    private CurrencyResponse currency;
    private BranchResponse branch;
    private BigDecimal loanAmount;
    private BigDecimal interestRate;
    private BigDecimal totalPayableAmount;
    private LocalDate loanDate;
    private LocalDate dueDate;
    private LoanStatus status;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    private LocalDateTime redeemedAt;
    private LocalDateTime defaultedAt;
}