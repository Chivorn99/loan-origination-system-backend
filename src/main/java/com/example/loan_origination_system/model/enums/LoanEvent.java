package com.example.loan_origination_system.model.enums;

/**
 * Events that trigger loan state transitions.
 * All state changes must be triggered by these events.
 */
public enum LoanEvent {
    /**
     * Loan is issued (created → active)
     */
    ISSUE_LOAN,
    
    /**
     * Partial payment received
     */
    PARTIAL_PAYMENT,
    
    /**
     * Full payment received (loan fully paid)
     */
    FULL_PAYMENT,
    
    /**
     * Due date has passed (loan becomes overdue)
     */
    DUE_DATE_PASSED,
    
    /**
     * Grace period expired (overdue → defaulted)
     */
    GRACE_PERIOD_EXPIRED,
    
    /**
     * Loan is cancelled before issuance
     */
    CANCEL,
    
    /**
     * Manual override to mark as defaulted (for admin use)
     */
    MANUAL_DEFAULT,
    
    /**
     * Manual override to mark as redeemed (for admin use)
     */
    MANUAL_REDEEM,
    
    /**
     * Manual override to mark as cancelled (for admin use)
     */
    MANUAL_CANCEL
}