package com.example.loan_origination_system.model.loan;

import jakarta.persistence.*;
import lombok.Data;
import java.time.LocalDate;

@Entity
@Table(name = "pawn_forfeit")
@Data
public class PawnForfeit {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @OneToOne
    @JoinColumn(name = "pawn_loan_id")
    private PawnLoan pawnLoan;

    private LocalDate forfeitDate;
    private String note;
}