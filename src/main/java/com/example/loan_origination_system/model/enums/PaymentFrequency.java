package com.example.loan_origination_system.model.enums;

public enum PaymentFrequency {
    ONE_TIME,      // Single payment at the end
    WEEKLY,        // Weekly installments
    BI_WEEKLY,     // Every two weeks
    MONTHLY,       // Monthly installments
    QUARTERLY,     // Quarterly installments
    CUSTOM         // Custom payment schedule
}