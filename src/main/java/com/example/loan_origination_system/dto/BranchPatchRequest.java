package com.example.loan_origination_system.dto;

import lombok.Data;

@Data
public class BranchPatchRequest {
    private String name;
    private String address;
    private String phone;
    private String status;
}