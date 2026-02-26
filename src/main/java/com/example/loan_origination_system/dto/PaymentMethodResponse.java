package com.example.loan_origination_system.dto;

import lombok.Data;

@Data
public class PaymentMethodResponse {
    private Long id;
    private String code;
    private String name;
    private String status;
}