package com.example.loan_origination_system.dto;

import java.time.LocalDate;

import lombok.Data;

@Data
public class PawnForfeitPatchRequest {
    private Long pawnLoanId;
    private LocalDate forfeitDate;
    private String note;
}