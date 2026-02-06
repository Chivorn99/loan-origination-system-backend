package com.example.loan_origination_system.model.people;

import jakarta.persistence.*;
import lombok.Data;
import java.time.LocalDateTime;

@Entity
@Table(name = "m_customer")
@Data
public class Customer {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String fullName;
    private String phone;

    @Column(unique = true)
    private String idNumber;

    private String address;
    private LocalDateTime createdAt = LocalDateTime.now();
}