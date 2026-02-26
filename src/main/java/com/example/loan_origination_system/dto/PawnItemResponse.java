package com.example.loan_origination_system.dto;

import com.example.loan_origination_system.model.enums.CollateralStatus;

import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
public class PawnItemResponse {
    private Long id;
    private CustomerResponse customer;
    private String itemType;
    private String description;
    private BigDecimal estimatedValue;
    private String photoUrl;
    private CollateralStatus status;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}