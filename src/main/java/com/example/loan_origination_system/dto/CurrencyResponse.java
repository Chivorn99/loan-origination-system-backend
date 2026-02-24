package com.example.loan_origination_system.dto;

import lombok.Data;

@Data
public class CurrencyResponse {
    private Long id;
    private String code;
    private String name;
    private String symbol;
    private Integer decimalPlace;
    private String status;
}