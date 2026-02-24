package com.example.loan_origination_system.dto;

import java.time.LocalDate;

import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class PawnForfeitRequest {
    @NotNull(message = "Pawn loan ID is required")
    private Long pawnLoanId;
    
    @NotNull(message = "Forfeit date is required")
    private LocalDate forfeitDate;
    
    private String note;
}