package com.example.loan_origination_system.model.master;

import jakarta.persistence.*;
import lombok.Data;

@Entity
@Table(name = "m_branch")
@Data
public class Branch {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String name;
    private String address;
    private String phone;
    private String status = "ACTIVE";
}