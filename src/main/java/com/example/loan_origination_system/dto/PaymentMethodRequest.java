package com.example.loan_origination_system.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class PaymentMethodRequest {
    @NotBlank(message = "Payment method code is required")
    private String code;
    
    @NotBlank(message = "Payment method name is required")
    private String name;
    
    private String status = "ACTIVE";
}