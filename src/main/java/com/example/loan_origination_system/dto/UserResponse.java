package com.example.loan_origination_system.dto;

import java.time.LocalDateTime;

import lombok.Data;

@Data
public class UserResponse {
    private Long id;
    private String username;
    private String email;
    private Long roleId;
    private String roleName;
    private Long branchId;
    private String branchName;
    private String status;
    private LocalDateTime createdAt;
}