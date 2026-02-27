package com.example.loan_origination_system.dto;

import java.math.BigDecimal;
import java.time.LocalDate;

import com.example.loan_origination_system.model.enums.PaymentFrequency;
import com.example.loan_origination_system.validation.FutureDate;
import com.example.loan_origination_system.validation.ValidCollateralInfo;
import com.example.loan_origination_system.validation.ValidNationalId;

import jakarta.validation.Valid;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.Data;

@Data
public class PawnLoanCreateFullRequest {
    
    // Customer identification
    @NotBlank(message = "National ID is required")
    @ValidNationalId
    private String nationalId;
    
    // Customer info (optional - used only if creating new customer)
    @Valid
    private CustomerInfo customerInfo;
    
    // Collateral info - required, either existing pawnItemId or new collateral details
    @NotNull(message = "Collateral information is required")
    @Valid
    private CollateralInfo collateralInfo;
    
    // Loan info - required
    @NotNull(message = "Loan information is required")
    @Valid
    private LoanInfo loanInfo;
    
    @Data
    public static class CustomerInfo {
        @NotBlank(message = "Full name is required for new customer")
        private String fullName;
        
        @NotBlank(message = "Phone is required for new customer")
        private String phone;
        
        private String address;
    }
    
    @Data
    @ValidCollateralInfo
    public static class CollateralInfo {
        // Either pawnItemId (for existing collateral) OR new collateral details
        private Long pawnItemId;
        
        private String itemType;
        private String description;
        
        @Positive(message = "Estimated value must be positive if provided")
        private BigDecimal estimatedValue;
        
        private String photoUrl;
        
        // Custom validation method
        public boolean isValid() {
            // Either pawnItemId is provided (existing collateral)
            // OR new collateral details are provided (itemType and estimatedValue)
            return (pawnItemId != null) ||
                   (itemType != null && !itemType.isBlank() && estimatedValue != null);
        }
    }
    
    @Data
    public static class LoanInfo {
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
        @FutureDate(message = "Due date must be in the future", includeToday = true)
        private LocalDate dueDate;
        
        private LocalDate redemptionDeadline;
        
        @Min(value = 1, message = "Loan duration must be at least 1 day")
        private Integer loanDurationDays = 30;
        
        @Min(value = 0, message = "Grace period cannot be negative")
        private Integer gracePeriodDays = 7;
        
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
    
    // Helper methods for validation
    public boolean hasExistingPawnItem() {
        return collateralInfo != null && collateralInfo.getPawnItemId() != null;
    }
    
    public boolean hasNewCollateral() {
        return collateralInfo != null && 
               collateralInfo.getItemType() != null && 
               !collateralInfo.getItemType().isBlank() &&
               collateralInfo.getEstimatedValue() != null;
    }
    
    public boolean hasNewCustomerInfo() {
        return customerInfo != null && 
               customerInfo.getFullName() != null && 
               !customerInfo.getFullName().isBlank() &&
               customerInfo.getPhone() != null &&
               !customerInfo.getPhone().isBlank();
    }
}