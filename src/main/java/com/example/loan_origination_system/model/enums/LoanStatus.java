package com.example.loan_origination_system.model.enums;

public enum LoanStatus {
    CREATED,        // Loan created but not yet issued
    PENDING,        // Legacy state - kept for backward compatibility
    ACTIVE,         // Loan issued and active
    PARTIALLY_PAID, // Loan has partial payments
    OVERDUE,        // Loan past due date but within grace period
    REDEEMED,       // Loan fully paid
    DEFAULTED,      // Loan defaulted after grace period expired
    CANCELLED       // Loan cancelled before issuance
    
    // Note: FORFEITED is a collateral status, not a loan status
}