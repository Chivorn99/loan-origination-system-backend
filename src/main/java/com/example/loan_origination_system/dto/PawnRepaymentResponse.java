package com.example.loan_origination_system.dto;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

import lombok.Data;

@Data
public class PawnRepaymentResponse {
    private Long id;
    private Long pawnLoanId;
    private String loanCode; // For easier reference
    private CurrencyResponse currency;
    private PaymentMethodResponse paymentMethod;
    private PaymentTypeResponse paymentType;
    private UserResponse receivedBy;
    private LocalDate paymentDate;
    private BigDecimal paidAmount;
    private BigDecimal principalPaid;
    private BigDecimal interestPaid;
    private BigDecimal penaltyPaid;
    private BigDecimal remainingPrincipal;
    private LocalDateTime createdAt;
}