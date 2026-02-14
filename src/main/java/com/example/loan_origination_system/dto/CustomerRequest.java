package com.example.loan_origination_system.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class CustomerRequest {
    @NotBlank(message = "Full name is required")
    private String fullName;
    
    private String phone;
    
    @NotBlank(message = "National ID is required")
    private String idNumber;
    
    private String address;
}