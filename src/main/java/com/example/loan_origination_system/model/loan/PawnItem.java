package com.example.loan_origination_system.model.loan;

import com.example.loan_origination_system.model.people.Customer;
import jakarta.persistence.*;
import lombok.Data;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "pawn_item")
@Data
public class PawnItem {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "customer_id")
    private Customer customer;

    private String itemType;
    private String description;
    private BigDecimal estimatedValue;
    private String photoUrl;

    private LocalDateTime createdAt = LocalDateTime.now();
}