package com.example.loan_origination_system.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class PaymentTypeRequest {
    @NotBlank(message = "Payment type code is required")
    private String code;
    
    @NotBlank(message = "Payment type name is required")
    private String name;
}