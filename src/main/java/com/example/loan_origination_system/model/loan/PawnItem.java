package com.example.loan_origination_system.model.loan;

import java.math.BigDecimal;
import java.time.LocalDateTime;

import com.example.loan_origination_system.model.enums.CollateralStatus;
import com.example.loan_origination_system.model.people.Customer;

import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.PreUpdate;
import jakarta.persistence.Table;
import lombok.Data;

@Entity
@Table(name = "pawn_item")
@Data
public class PawnItem {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "customer_id", nullable = false)
    private Customer customer;

    private String itemType;
    private String description;
    private BigDecimal estimatedValue;
    private String photoUrl;
    
    @Enumerated(EnumType.STRING)
    private CollateralStatus status = CollateralStatus.AVAILABLE;
    
    private LocalDateTime createdAt = LocalDateTime.now();
    private LocalDateTime updatedAt;
    private LocalDateTime deletedAt;
    
    @PreUpdate
    protected void onUpdate() {
        updatedAt = LocalDateTime.now();
    }
}