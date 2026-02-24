package com.example.loan_origination_system.dto;

import lombok.Data;

@Data
public class PaymentTypePatchRequest {
    private String code;
    private String name;
}