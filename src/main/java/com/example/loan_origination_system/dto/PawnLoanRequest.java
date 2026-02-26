package com.example.loan_origination_system.dto;

import java.math.BigDecimal;
import java.time.LocalDate;

import com.example.loan_origination_system.model.enums.PaymentFrequency;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.Data;

@Data
public class PawnLoanRequest {
    @NotNull(message = "Customer ID is required")
    private Long customerId;
    
    @NotNull(message = "Pawn item ID is required")
    private Long pawnItemId;
    
    @NotNull(message = "Currency ID is required")
    private Long currencyId;
    
    @NotNull(message = "Branch ID is required")
    private Long branchId;
    
    @NotNull(message = "Loan amount is required")
    @Positive(message = "Loan amount must be positive")
    private BigDecimal loanAmount;
    
    @NotNull(message = "Interest rate is required")
    @Positive(message = "Interest rate must be positive")
    private BigDecimal interestRate;
    
    @NotNull(message = "Due date is required")
    private LocalDate dueDate;
    
    private LocalDate redemptionDeadline;
    
    @Min(value = 1, message = "Loan duration must be at least 1 day")
    private Integer loanDurationDays = 30; // Default 30 days
    
    @Min(value = 0, message = "Grace period cannot be negative")
    private Integer gracePeriodDays = 7; // Default 7 days grace period
    
    @Positive(message = "Storage fee must be positive if provided")
    private BigDecimal storageFee = BigDecimal.ZERO;
    
    @Positive(message = "Penalty rate must be positive if provided")
    private BigDecimal penaltyRate = BigDecimal.ZERO;
    
    private PaymentFrequency paymentFrequency = PaymentFrequency.ONE_TIME;
    
    @Min(value = 1, message = "Number of installments must be at least 1")
    private Integer numberOfInstallments = 1;
    
    @Positive(message = "Installment amount must be positive if provided")
    private BigDecimal installmentAmount;
}