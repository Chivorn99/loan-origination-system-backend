package com.example.loan_origination_system.model.master;

import jakarta.persistence.*;
import lombok.Data;

@Entity
@Table(name = "m_payment_type")
@Data
public class PaymentType {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String code;
    private String name;
}