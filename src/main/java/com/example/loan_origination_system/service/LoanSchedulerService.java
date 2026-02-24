package com.example.loan_origination_system.service;

import java.time.LocalDate;
import java.util.List;

import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.example.loan_origination_system.model.enums.LoanEvent;
import com.example.loan_origination_system.model.enums.LoanStatus;
import com.example.loan_origination_system.model.loan.PawnLoan;
import com.example.loan_origination_system.repository.PawnLoanRepository;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

/**
 * Scheduled jobs for loan lifecycle management.
 * Handles automated state transitions based on time-based events.
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class LoanSchedulerService {
    
    private final PawnLoanRepository pawnLoanRepository;
    private final LoanStateMachine loanStateMachine;
    
    /**
     * Daily job to detect overdue loans.
     * Business Rules:
     * 1. Loans with due date passed become OVERDUE
     * 2. Only ACTIVE and PARTIALLY_PAID loans can become overdue
     * 3. Sets grace period end date (30 days from overdue)
     * 
     * Runs daily at 1:00 AM
     */
    @Scheduled(cron = "0 0 1 * * ?") // Run daily at 1:00 AM
    @Transactional
    public void detectOverdueLoans() {
        LocalDate today = LocalDate.now();
        
        // Find loans that are due today or earlier and are still active/partially paid
        List<PawnLoan> dueLoans = pawnLoanRepository.findLoansDueByDate(today);
        
        int processedCount = 0;
        for (PawnLoan loan : dueLoans) {
            // Only process loans that are ACTIVE or PARTIALLY_PAID
            if (loan.getStatus() == LoanStatus.ACTIVE || loan.getStatus() == LoanStatus.PARTIALLY_PAID) {
                try {
                    loanStateMachine.transition(loan, LoanEvent.DUE_DATE_PASSED);
                    processedCount++;
                    log.debug("Marked loan {} as overdue", loan.getLoanCode());
                } catch (Exception e) {
                    log.error("Failed to mark loan {} as overdue: {}", loan.getLoanCode(), e.getMessage());
                }
            }
        }
        
        if (processedCount > 0) {
            log.info("Detected {} overdue loans", processedCount);
        }
    }
    
    /**
     * Daily job to process grace period expiration.
     * Business Rules:
     * 1. OVERDUE loans with grace period expired become DEFAULTED
     * 2. Grace period is typically 30 days from overdue date
     * 
     * Runs daily at 2:00 AM
     */
    @Scheduled(cron = "0 0 2 * * ?") // Run daily at 2:00 AM
    @Transactional
    public void processGracePeriodExpiration() {
        LocalDate today = LocalDate.now();
        
        // Find overdue loans where grace period has ended
        List<PawnLoan> expiredGraceLoans = pawnLoanRepository.findOverdueLoansWithExpiredGracePeriod(today);
        
        int processedCount = 0;
        for (PawnLoan loan : expiredGraceLoans) {
            if (loan.getStatus() == LoanStatus.OVERDUE) {
                try {
                    loanStateMachine.transition(loan, LoanEvent.GRACE_PERIOD_EXPIRED);
                    processedCount++;
                    log.debug("Marked loan {} as defaulted after grace period expired", loan.getLoanCode());
                } catch (Exception e) {
                    log.error("Failed to mark loan {} as defaulted: {}", loan.getLoanCode(), e.getMessage());
                }
            }
        }
        
        if (processedCount > 0) {
            log.info("Processed {} loans with expired grace period", processedCount);
        }
    }
    
    /**
     * Weekly job to generate overdue reports and notifications.
     * Runs every Monday at 3:00 AM
     */
    @Scheduled(cron = "0 0 3 * * MON") // Run every Monday at 3:00 AM
    @Transactional
    public void generateOverdueReports() {
        LocalDate today = LocalDate.now();
        
        // Get all overdue loans
        List<PawnLoan> overdueLoans = pawnLoanRepository.findByStatus(LoanStatus.OVERDUE);
        
        if (!overdueLoans.isEmpty()) {
            log.info("Generating overdue report for {} loans", overdueLoans.size());
            
            // In a real implementation, this would:
            // 1. Generate PDF reports
            // 2. Send email notifications to customers
            // 3. Notify collection department
            // 4. Update dashboard metrics
            
            for (PawnLoan loan : overdueLoans) {
                log.debug("Overdue loan: {} (Customer: {}, Due: {}, Days overdue: {})",
                    loan.getLoanCode(),
                    loan.getCustomer().getFullName(),
                    loan.getDueDate(),
                    loan.getDueDate() != null ?
                        today.toEpochDay() - loan.getDueDate().toEpochDay() : "N/A");
            }
        }
    }
    
    /**
     * Monthly job to generate defaulted loan reports.
     * Runs on the 1st of every month at 4:00 AM
     */
    @Scheduled(cron = "0 0 4 1 * ?") // Run on 1st of every month at 4:00 AM
    @Transactional
    public void generateDefaultedReports() {
        LocalDate firstOfMonth = LocalDate.now().withDayOfMonth(1);
        LocalDate lastOfMonth = firstOfMonth.plusMonths(1).minusDays(1);
        
        // Get loans defaulted in the previous month
        List<PawnLoan> defaultedLoans = pawnLoanRepository.findDefaultedLoansInPeriod(
            firstOfMonth.minusMonths(1), lastOfMonth.minusMonths(1));
        
        if (!defaultedLoans.isEmpty()) {
            log.info("Generating defaulted loan report for {} loans", defaultedLoans.size());
            
            // In a real implementation, this would:
            // 1. Generate regulatory reports
            // 2. Update financial statements
            // 3. Notify risk management department
            // 4. Trigger collateral liquidation processes
        }
    }
}