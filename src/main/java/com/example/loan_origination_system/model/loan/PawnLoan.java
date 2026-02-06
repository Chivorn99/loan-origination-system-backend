package com.example.loan_origination_system.model.loan;

import com.example.loan_origination_system.model.master.Branch;
import com.example.loan_origination_system.model.master.Currency;
import com.example.loan_origination_system.model.people.Customer;
import jakarta.persistence.*;
import lombok.Data;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Entity
@Table(name = "pawn_loan")
@Data
public class PawnLoan {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(unique = true, nullable = false)
    private String loanCode;

    @ManyToOne
    @JoinColumn(name = "customer_id")
    private Customer customer;

    @OneToOne
    @JoinColumn(name = "pawn_item_id")
    private PawnItem pawnItem;

    @ManyToOne
    @JoinColumn(name = "currency_id")
    private Currency currency;

    @ManyToOne
    @JoinColumn(name = "branch_id")
    private Branch branch;

    @Column(nullable = false)
    private BigDecimal loanAmount;
    private BigDecimal interestRate;

    private LocalDate loanDate = LocalDate.now();
    private LocalDate dueDate;

    private String status = "PENDING";
    private LocalDateTime createdAt = LocalDateTime.now();
}