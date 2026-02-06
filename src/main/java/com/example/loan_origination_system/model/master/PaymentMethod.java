package com.example.loan_origination_system.model.master;

import jakarta.persistence.*;
import lombok.Data;

@Entity
@Table(name = "m_payment_method")
@Data
public class PaymentMethod {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(unique = true, nullable = false)
    private String code;
    private String name;
    private String status = "ACTIVE";
}