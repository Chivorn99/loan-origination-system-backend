package com.example.loan_origination_system.dto;

import com.example.loan_origination_system.model.enums.CustomerStatus;

import lombok.Data;

import java.time.LocalDateTime;

@Data
public class CustomerResponse {
    private Long id;
    private String fullName;
    private String phone;
    private String idNumber;
    private String address;
    private CustomerStatus status;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}