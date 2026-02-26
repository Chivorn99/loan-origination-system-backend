package com.example.loan_origination_system.dto;

import lombok.Data;

@Data
public class UserPatchRequest {
    private String username;
    private String email;
    private String password;
    private Long roleId;
    private Long branchId;
    private String status;
}