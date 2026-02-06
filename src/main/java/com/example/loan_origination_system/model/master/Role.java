package com.example.loan_origination_system.model.master;

import jakarta.persistence.*;
import lombok.Data;

@Entity
@Table(name = "m_role")
@Data
public class Role {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(unique = true, nullable = false)
    private String code;

    @Column(nullable = false)
    private String name;

    private String description;
}