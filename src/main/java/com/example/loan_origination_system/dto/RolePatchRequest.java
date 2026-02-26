package com.example.loan_origination_system.dto;

import lombok.Data;

@Data
public class RolePatchRequest {
    private String code;
    private String name;
    private String description;
}