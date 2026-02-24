package com.example.loan_origination_system.dto;

import java.math.BigDecimal;
import java.time.LocalDate;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.Data;

@Data
public class PawnRepaymentRequest {
    @NotNull(message = "Pawn loan ID is required")
    private Long pawnLoanId;
    
    @NotNull(message = "Currency ID is required")
    private Long currencyId;
    
    @NotNull(message = "Payment method ID is required")
    private Long paymentMethodId;
    
    @NotNull(message = "Payment type ID is required")
    private Long paymentTypeId;
    
    @NotNull(message = "Paid amount is required")
    @Positive(message = "Paid amount must be positive")
    private BigDecimal paidAmount;
    
    @NotNull(message = "Principal paid is required")
    @Positive(message = "Principal paid must be positive")
    private BigDecimal principalPaid;
    
    @NotNull(message = "Interest paid is required")
    @Positive(message = "Interest paid must be positive")
    private BigDecimal interestPaid;
    
    @NotNull(message = "Penalty paid is required")
    @Positive(message = "Penalty paid must be positive")
    private BigDecimal penaltyPaid;
    
    @NotNull(message = "Remaining principal is required")
    @Positive(message = "Remaining principal must be positive")
    private BigDecimal remainingPrincipal;
    
    @NotNull(message = "Received by user ID is required")
    private Long receivedBy;
    
    private LocalDate paymentDate = LocalDate.now();
    
    // Optional: For partial payments, you might want to allow zero values
    public void validatePayment() {
        BigDecimal total = principalPaid.add(interestPaid).add(penaltyPaid);
        if (paidAmount.compareTo(total) != 0) {
            throw new IllegalArgumentException("Paid amount must equal the sum of principal, interest, and penalty");
        }
    }
}