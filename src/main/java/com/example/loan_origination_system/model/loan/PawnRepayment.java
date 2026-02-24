package com.example.loan_origination_system.model.loan;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

import com.example.loan_origination_system.model.master.Currency;
import com.example.loan_origination_system.model.master.PaymentMethod;
import com.example.loan_origination_system.model.master.PaymentType;
import com.example.loan_origination_system.model.people.User;

import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import lombok.Data;

@Entity
@Table(name = "pawn_repayment")
@Data
public class PawnRepayment {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "pawn_loan_id")
    private PawnLoan pawnLoan;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "currency_id")
    private Currency currency;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "payment_method_id")
    private PaymentMethod paymentMethod;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "payment_type_id")
    private PaymentType paymentType;

    private LocalDate paymentDate = LocalDate.now();

    private BigDecimal paidAmount;
    private BigDecimal principalPaid;
    private BigDecimal interestPaid;
    private BigDecimal penaltyPaid;
    private BigDecimal remainingPrincipal;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "received_by")
    private User receivedBy;

    private LocalDateTime createdAt = LocalDateTime.now();
}