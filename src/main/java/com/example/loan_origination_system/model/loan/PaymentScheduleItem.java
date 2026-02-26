package com.example.loan_origination_system.model.loan;

import java.math.BigDecimal;
import java.time.LocalDate;

import lombok.Data;

@Data
public class PaymentScheduleItem {
    private Integer installmentNumber; // 1st, 2nd, 3rd, etc.
    private LocalDate dueDate;
    private BigDecimal amountDue;
    private BigDecimal principalAmount;
    private BigDecimal interestAmount;
    private BigDecimal remainingBalance;
    private String status; // PENDING, PAID, OVERDUE
    
    public PaymentScheduleItem() {}
    
    public PaymentScheduleItem(Integer installmentNumber, LocalDate dueDate, 
                              BigDecimal amountDue, BigDecimal principalAmount, 
                              BigDecimal interestAmount, BigDecimal remainingBalance) {
        this.installmentNumber = installmentNumber;
        this.dueDate = dueDate;
        this.amountDue = amountDue;
        this.principalAmount = principalAmount;
        this.interestAmount = interestAmount;
        this.remainingBalance = remainingBalance;
        this.status = "PENDING";
    }
}