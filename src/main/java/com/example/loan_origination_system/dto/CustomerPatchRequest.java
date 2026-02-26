package com.example.loan_origination_system.dto;

import lombok.Data;

@Data
public class CustomerPatchRequest {
    private String fullName;
    private String phone;
    private String idNumber;
    private String address;
}