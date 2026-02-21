package com.example.loan_origination_system.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class BranchRequest {
    @NotBlank(message = "Branch name is required")
    private String name;
    
    private String address;
    
    private String phone;
    
    private String status = "ACTIVE";
}