package com.example.loan_origination_system.model.people;

import com.example.loan_origination_system.model.master.Branch;
import com.example.loan_origination_system.model.master.Role;
import jakarta.persistence.*;
import lombok.Data;
import java.time.LocalDateTime;

@Entity
@Table(name = "m_user")
@Data
public class User {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(unique = true, nullable = false)
    private String username;

    @Column(nullable = false)
    private String password;

    @ManyToOne
    @JoinColumn(name = "role_id")
    private Role role;

    @ManyToOne
    @JoinColumn(name = "branch_id")
    private Branch branch;

    private String status = "ACTIVE";
    private LocalDateTime createdAt = LocalDateTime.now();
}