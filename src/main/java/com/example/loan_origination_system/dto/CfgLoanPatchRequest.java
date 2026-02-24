package com.example.loan_origination_system.dto;

import java.math.BigDecimal;
import java.time.LocalDate;

import lombok.Data;

@Data
public class CfgLoanPatchRequest {
    private Long branchId;
    private Long currencyId;
    private BigDecimal minLoanAmount;
    private BigDecimal maxLoanAmount;
    private BigDecimal interestRate;
    private String interestType;
    private String interestPeriod;
    private BigDecimal penaltyRate;
    private Integer penaltyGraceDays;
    private Integer maxLoanDuration;
    private Integer autoForfeitDays;
    private String status;
    private LocalDate effectiveFrom;
    private LocalDate effectiveTo;
}