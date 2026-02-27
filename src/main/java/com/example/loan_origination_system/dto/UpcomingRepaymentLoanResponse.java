package com.example.loan_origination_system.dto;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

import com.example.loan_origination_system.model.enums.LoanStatus;
import com.example.loan_origination_system.model.loan.PaymentScheduleItem;

import lombok.Data;

/**
 * DTO for upcoming repayment loans with customer contact information for follow-up
 */
@Data
public class UpcomingRepaymentLoanResponse {
    private Long id;
    private String loanCode;
    
    // Customer information for follow-up
    private CustomerResponse customer;
    private String customerPhone;
    private String customerEmail;
    
    // Loan details
    private CurrencyResponse currency;
    private BranchResponse branch;
    private BigDecimal loanAmount;
    private BigDecimal totalPayableAmount;
    private BigDecimal remainingBalance;
    private BigDecimal nextPaymentAmount;
    
    // Payment schedule information
    private LocalDate loanDate;
    private LocalDate dueDate;
    private LocalDate nextPaymentDueDate;
    private Integer daysUntilDue;
    private Integer overdueDays; // If already overdue
    
    // Status information
    private LoanStatus status;
    private String followUpPriority; // HIGH, MEDIUM, LOW based on due date proximity
    
    // Payment schedule for upcoming payments
    private List<PaymentScheduleItem> upcomingPayments;
    
    // Contact history/tracking
    private LocalDateTime lastContactDate;
    private String lastContactMethod; // PHONE, EMAIL, SMS
    private String followUpNotes;
    
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}