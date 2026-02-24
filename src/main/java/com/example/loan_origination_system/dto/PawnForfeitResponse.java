package com.example.loan_origination_system.dto;

import java.time.LocalDate;

import lombok.Data;

@Data
public class PawnForfeitResponse {
    private Long id;
    private Long pawnLoanId;
    private String pawnLoanReference;
    private LocalDate forfeitDate;
    private String note;
}