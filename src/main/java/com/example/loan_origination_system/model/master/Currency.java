package com.example.loan_origination_system.model.master;

import jakarta.persistence.*;
import lombok.Data;

@Entity
@Table(name = "m_currency")
@Data
public class Currency {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(unique = true, nullable = false)
    private String code;
    private String name;
    private String symbol;
    private Integer decimalPlace = 2;
    private String status = "ACTIVE";
}