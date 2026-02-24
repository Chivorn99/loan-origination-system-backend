package com.example.loan_origination_system.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class RoleRequest {
    @NotBlank(message = "Role code is required")
    private String code;
    
    @NotBlank(message = "Role name is required")
    private String name;
    
    private String description;
}