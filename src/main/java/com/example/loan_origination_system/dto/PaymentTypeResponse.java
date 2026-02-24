package com.example.loan_origination_system.dto;

import lombok.Data;

@Data
public class PaymentTypeResponse {
    private Long id;
    private String code;
    private String name;
}