package com.example.loan_origination_system.dto;

import lombok.Data;

@Data
public class CurrencyPatchRequest {
    private String code;
    private String name;
    private String symbol;
    private Integer decimalPlace;
    private String status;
}