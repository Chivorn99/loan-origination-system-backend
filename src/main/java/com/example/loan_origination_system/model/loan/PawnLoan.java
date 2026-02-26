package com.example.loan_origination_system.model.loan;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

import com.example.loan_origination_system.model.enums.LoanStatus;
import com.example.loan_origination_system.model.enums.PaymentFrequency;
import com.example.loan_origination_system.model.master.Branch;
import com.example.loan_origination_system.model.master.Currency;
import com.example.loan_origination_system.model.people.Customer;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToOne;
import jakarta.persistence.PreUpdate;
import jakarta.persistence.Table;
import jakarta.persistence.Transient;
import jakarta.persistence.Version;
import lombok.Data;

@Entity
@Table(name = "pawn_loan")
@Data
public class PawnLoan {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Version
    private Long version;

    @Column(unique = true, nullable = false)
    private String loanCode;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "customer_id", nullable = false)
    private Customer customer;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "pawn_item_id", nullable = false)
    private PawnItem pawnItem;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "currency_id", nullable = false)
    private Currency currency;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "branch_id", nullable = false)
    private Branch branch;

    @Column(nullable = false)
    private BigDecimal loanAmount;
    
    @Column(nullable = false)
    private BigDecimal interestRate;
    
    @Column(nullable = false)
    private BigDecimal totalPayableAmount;

    private LocalDate loanDate = LocalDate.now();
    private LocalDate dueDate;
    private LocalDate redemptionDeadline; // When user can collect their item
    private LocalDate gracePeriodEndDate; // End date of grace period after overdue
    
    private Integer loanDurationDays; // Duration of loan in days
    private Integer gracePeriodDays; // Grace period after due date for redemption
    
    private BigDecimal storageFee; // Fee for storing the pawned item
    private BigDecimal penaltyRate; // Penalty rate for late payments
    
    @Enumerated(EnumType.STRING)
    private PaymentFrequency paymentFrequency = PaymentFrequency.ONE_TIME;
    
    private Integer numberOfInstallments; // For installment payments
    private BigDecimal installmentAmount; // Amount per installment
    
    @Enumerated(EnumType.STRING)
    private LoanStatus status = LoanStatus.CREATED;
    
    @Transient
    private List<PaymentScheduleItem> paymentSchedule = new ArrayList<>();
    
    private LocalDateTime createdAt = LocalDateTime.now();
    private LocalDateTime updatedAt;
    private LocalDateTime redeemedAt;
    private LocalDateTime defaultedAt;
    private LocalDateTime overdueAt; // When loan was marked as overdue
    
    @PreUpdate
    protected void onUpdate() {
        updatedAt = LocalDateTime.now();
    }
}