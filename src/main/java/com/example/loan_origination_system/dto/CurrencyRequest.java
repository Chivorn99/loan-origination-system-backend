package com.example.loan_origination_system.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class CurrencyRequest {
    @NotBlank(message = "Currency code is required")
    private String code;
    
    @NotBlank(message = "Currency name is required")
    private String name;
    
    private String symbol;
    
    private Integer decimalPlace = 2;
    
    private String status = "ACTIVE";
}