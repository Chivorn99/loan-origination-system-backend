package com.example.loan_origination_system.dto;

import java.math.BigDecimal;
import java.time.LocalDate;

import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class CfgLoanRequest {
    @NotNull(message = "Branch ID is required")
    private Long branchId;
    
    @NotNull(message = "Currency ID is required")
    private Long currencyId;
    
    @NotNull(message = "Minimum loan amount is required")
    private BigDecimal minLoanAmount;
    
    @NotNull(message = "Maximum loan amount is required")
    private BigDecimal maxLoanAmount;
    
    @NotNull(message = "Interest rate is required")
    private BigDecimal interestRate;
    
    private String interestType;
    private String interestPeriod;
    private BigDecimal penaltyRate;
    private Integer penaltyGraceDays;
    private Integer maxLoanDuration;
    private Integer autoForfeitDays;
    private String status = "ACTIVE";
    private LocalDate effectiveFrom;
    private LocalDate effectiveTo;
}