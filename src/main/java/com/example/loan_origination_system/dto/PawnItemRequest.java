package com.example.loan_origination_system.dto;

import java.math.BigDecimal;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.Data;

@Data
public class PawnItemRequest {
    @NotNull(message = "Customer ID is required")
    private Long customerId;
    
    @NotBlank(message = "Item type is required")
    private String itemType;
    
    private String description;
    
    @NotNull(message = "Estimated value is required")
    @Positive(message = "Estimated value must be positive")
    private BigDecimal estimatedValue;
    
    private String photoUrl;
}