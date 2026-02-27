package com.example.loan_origination_system.service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import com.example.loan_origination_system.dto.BranchResponse;
import com.example.loan_origination_system.dto.CurrencyResponse;
import com.example.loan_origination_system.dto.CustomerResponse;
import com.example.loan_origination_system.dto.PawnRepaymentRequest;
import com.example.loan_origination_system.dto.UpcomingRepaymentLoanResponse;
import com.example.loan_origination_system.exception.BusinessException;
import com.example.loan_origination_system.model.enums.LoanEvent;
import com.example.loan_origination_system.model.enums.LoanStatus;
import com.example.loan_origination_system.model.loan.PawnLoan;
import com.example.loan_origination_system.model.loan.PawnRepayment;
import com.example.loan_origination_system.model.master.Currency;
import com.example.loan_origination_system.model.master.PaymentMethod;
import com.example.loan_origination_system.model.master.PaymentType;
import com.example.loan_origination_system.model.people.User;
import com.example.loan_origination_system.repository.CurrencyRepository;
import com.example.loan_origination_system.repository.PawnLoanRepository;
import com.example.loan_origination_system.repository.PawnRepaymentRepository;
import com.example.loan_origination_system.repository.PaymentMethodRepository;
import com.example.loan_origination_system.repository.PaymentTypeRepository;
import com.example.loan_origination_system.repository.UserRepository;

import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class PawnRepaymentService {
    
    private final PawnRepaymentRepository pawnRepaymentRepository;
    private final PawnLoanRepository pawnLoanRepository;
    private final CurrencyRepository currencyRepository;
    private final PaymentMethodRepository paymentMethodRepository;
    private final PaymentTypeRepository paymentTypeRepository;
    private final UserRepository userRepository;
    private final LoanStateMachine loanStateMachine;
    
    /**
     * Create a new repayment record
     * Business Rules:
     * 1. Loan must be ACTIVE
     * 2. Payment amount validation
     * 3. Update loan status if fully paid
     */
    @Transactional
    public PawnRepayment createRepayment(PawnRepaymentRequest request) {
        // Validate the payment amounts
        request.validatePayment();
        
        // Fetch and validate the loan
        PawnLoan loan = pawnLoanRepository.findById(request.getPawnLoanId())
            .orElseThrow(() -> new BusinessException("LOAN_NOT_FOUND",
                "Loan with ID " + request.getPawnLoanId() + " not found"));
        
        // Check if loan is active
        if (loan.getStatus() != LoanStatus.ACTIVE) {
            throw new BusinessException("LOAN_NOT_ACTIVE",
                "Loan is not active. Current status: " + loan.getStatus());
        }
        
        // Validate currency
        Currency currency = currencyRepository.findById(request.getCurrencyId())
            .orElseThrow(() -> new BusinessException("CURRENCY_NOT_FOUND",
                "Currency with ID " + request.getCurrencyId() + " not found"));
        
        // Validate payment method
        PaymentMethod paymentMethod = paymentMethodRepository.findById(request.getPaymentMethodId())
            .orElseThrow(() -> new BusinessException("PAYMENT_METHOD_NOT_FOUND",
                "Payment method with ID " + request.getPaymentMethodId() + " not found"));
        
        // Validate payment type
        PaymentType paymentType = paymentTypeRepository.findById(request.getPaymentTypeId())
            .orElseThrow(() -> new BusinessException("PAYMENT_TYPE_NOT_FOUND",
                "Payment type with ID " + request.getPaymentTypeId() + " not found"));
        
        // Validate receiving user
        User receivedBy = userRepository.findById(request.getReceivedBy())
            .orElseThrow(() -> new BusinessException("USER_NOT_FOUND",
                "User with ID " + request.getReceivedBy() + " not found"));
        
        // Calculate remaining balance after this payment
        BigDecimal totalPaidSoFar = getTotalPaidAmountByLoanId(loan.getId());
        BigDecimal totalPayable = loan.getTotalPayableAmount();
        BigDecimal newTotalPaid = totalPaidSoFar.add(request.getPaidAmount());
        
        // Check if payment exceeds total payable
        if (newTotalPaid.compareTo(totalPayable) > 0) {
            throw new BusinessException("PAYMENT_EXCEEDS_TOTAL",
                "Payment exceeds total payable amount. Total payable: " + totalPayable + 
                ", Already paid: " + totalPaidSoFar + ", New payment: " + request.getPaidAmount());
        }
        
        // Create repayment record
        PawnRepayment repayment = new PawnRepayment();
        repayment.setPawnLoan(loan);
        repayment.setCurrency(currency);
        repayment.setPaymentMethod(paymentMethod);
        repayment.setPaymentType(paymentType);
        repayment.setPaidAmount(request.getPaidAmount());
        repayment.setPrincipalPaid(request.getPrincipalPaid());
        repayment.setInterestPaid(request.getInterestPaid());
        repayment.setPenaltyPaid(request.getPenaltyPaid());
        repayment.setRemainingPrincipal(request.getRemainingPrincipal());
        repayment.setReceivedBy(receivedBy);
        repayment.setPaymentDate(request.getPaymentDate() != null ? request.getPaymentDate() : LocalDate.now());
        
        PawnRepayment savedRepayment = pawnRepaymentRepository.save(repayment);
        
        // Update loan status based on payment using state machine
        if (newTotalPaid.compareTo(totalPayable) >= 0) {
            // Loan is fully paid - trigger FULL_PAYMENT event
            loanStateMachine.transition(loan, LoanEvent.FULL_PAYMENT);
        } else if (newTotalPaid.compareTo(BigDecimal.ZERO) > 0) {
            // Loan has partial payments - trigger PARTIAL_PAYMENT event
            loanStateMachine.transition(loan, LoanEvent.PARTIAL_PAYMENT);
        }
        // Note: If payment is zero (shouldn't happen due to validation), no state change
        
        return savedRepayment;
    }
    
    /**
     * Get total paid amount for a loan
     */
    public BigDecimal getTotalPaidAmountByLoanId(Long loanId) {
        return pawnRepaymentRepository.getTotalPaidAmountByPawnLoanId(loanId)
            .orElse(BigDecimal.ZERO);
    }
    
    /**
     * Get repayment history for a loan
     */
    public List<PawnRepayment> getRepaymentHistory(Long loanId) {
        return pawnRepaymentRepository.findByPawnLoanIdOrderByPaymentDateDesc(loanId);
    }
    
    /**
     * Get repayment history with pagination
     */
    public Page<PawnRepayment> getRepaymentHistory(Long loanId, Pageable pageable) {
        return pawnRepaymentRepository.findByPawnLoanId(loanId, pageable);
    }
    
    /**
     * Get repayment by ID
     */
    public PawnRepayment getRepaymentById(Long id) {
        return pawnRepaymentRepository.findById(id)
            .orElseThrow(() -> new BusinessException("REPAYMENT_NOT_FOUND",
                "Repayment with ID " + id + " not found"));
    }
    
    /**
     * Calculate repayment schedule (simplified)
     */
    public RepaymentSchedule calculateRepaymentSchedule(Long loanId) {
        PawnLoan loan = pawnLoanRepository.findById(loanId)
            .orElseThrow(() -> new BusinessException("LOAN_NOT_FOUND",
                "Loan with ID " + loanId + " not found"));
        
        BigDecimal totalPaid = getTotalPaidAmountByLoanId(loanId);
        BigDecimal remainingBalance = loan.getTotalPayableAmount().subtract(totalPaid);
        
        RepaymentSchedule schedule = new RepaymentSchedule();
        schedule.setLoanId(loanId);
        schedule.setLoanCode(loan.getLoanCode());
        schedule.setTotalPayable(loan.getTotalPayableAmount());
        schedule.setTotalPaid(totalPaid);
        schedule.setRemainingBalance(remainingBalance);
        schedule.setDueDate(loan.getDueDate());
        schedule.setIsOverdue(LocalDate.now().isAfter(loan.getDueDate()));
        
        // Calculate days overdue if applicable
        if (schedule.getIsOverdue()) {
            long daysOverdue = LocalDate.now().toEpochDay() - loan.getDueDate().toEpochDay();
            schedule.setDaysOverdue(daysOverdue);
            
            // Calculate penalty (simplified: 1% per month overdue)
            BigDecimal monthlyPenaltyRate = new BigDecimal("0.01");
            BigDecimal monthsOverdue = new BigDecimal(daysOverdue).divide(new BigDecimal("30"), 2, RoundingMode.HALF_UP);
            BigDecimal penaltyAmount = remainingBalance.multiply(monthlyPenaltyRate).multiply(monthsOverdue);
            schedule.setEstimatedPenalty(penaltyAmount.max(BigDecimal.ZERO));
        }
        
        return schedule;
    }
    
    /**
     * Get repayments by date range
     */
    public Page<PawnRepayment> getRepaymentsByDateRange(LocalDate startDate, LocalDate endDate, Pageable pageable) {
        return pawnRepaymentRepository.findByPaymentDateBetween(startDate, endDate, pageable);
    }
    
    /**
     * Get repayments by customer ID and month range
     * @param customerId Customer ID
     * @param months Number of months to look back (e.g., 3 for last 3 months)
     * @param pageable Pagination information
     * @return Page of repayments within the specified month range
     */
    public Page<PawnRepayment> getCustomerRepaymentsByMonthRange(Long customerId, int months, Pageable pageable) {
        LocalDate endDate = LocalDate.now();
        LocalDate startDate = endDate.minusMonths(months);
        
        return pawnRepaymentRepository.findByCustomerIdAndPaymentDateBetween(
            customerId, startDate, endDate, pageable);
    }
    
    /**
     * Get customer repayment summary for N months
     * @param customerId Customer ID
     * @param months Number of months to look back
     * @return Summary of repayments including totals
     */
    public CustomerRepaymentSummary getCustomerRepaymentSummary(Long customerId, int months) {
        LocalDate endDate = LocalDate.now();
        LocalDate startDate = endDate.minusMonths(months);
        
        Pageable pageable = Pageable.unpaged();
        Page<PawnRepayment> repaymentsPage = pawnRepaymentRepository.findByCustomerIdAndPaymentDateBetween(
            customerId, startDate, endDate, pageable);
        
        List<PawnRepayment> repayments = repaymentsPage.getContent();
        
        BigDecimal totalPaid = BigDecimal.ZERO;
        BigDecimal totalPrincipal = BigDecimal.ZERO;
        BigDecimal totalInterest = BigDecimal.ZERO;
        BigDecimal totalPenalty = BigDecimal.ZERO;
        Map<String, BigDecimal> monthlyTotals = new HashMap<>();
        
        for (PawnRepayment repayment : repayments) {
            totalPaid = totalPaid.add(repayment.getPaidAmount());
            totalPrincipal = totalPrincipal.add(repayment.getPrincipalPaid());
            totalInterest = totalInterest.add(repayment.getInterestPaid());
            totalPenalty = totalPenalty.add(repayment.getPenaltyPaid());
            
            // Group by month-year
            String monthKey = repayment.getPaymentDate().getMonth().toString() + " " +
                             repayment.getPaymentDate().getYear();
            monthlyTotals.merge(monthKey, repayment.getPaidAmount(), BigDecimal::add);
        }
        
        CustomerRepaymentSummary summary = new CustomerRepaymentSummary();
        summary.setCustomerId(customerId);
        summary.setStartDate(startDate);
        summary.setEndDate(endDate);
        summary.setTotalRepayments(repayments.size());
        summary.setTotalPaidAmount(totalPaid);
        summary.setTotalPrincipal(totalPrincipal);
        summary.setTotalInterest(totalInterest);
        summary.setTotalPenalty(totalPenalty);
        summary.setMonthlyTotals(monthlyTotals);
        
        return summary;
    }
    
    /**
     * Get daily collection report for a branch
     */
    public DailyCollectionReport getDailyCollectionReport(Long branchId, LocalDate date) {
        List<PawnRepayment> repayments = pawnRepaymentRepository.findByBranchAndPaymentDate(branchId, date);
        
        BigDecimal totalCollection = BigDecimal.ZERO;
        BigDecimal totalPrincipal = BigDecimal.ZERO;
        BigDecimal totalInterest = BigDecimal.ZERO;
        BigDecimal totalPenalty = BigDecimal.ZERO;
        
        for (PawnRepayment repayment : repayments) {
            totalCollection = totalCollection.add(repayment.getPaidAmount());
            totalPrincipal = totalPrincipal.add(repayment.getPrincipalPaid());
            totalInterest = totalInterest.add(repayment.getInterestPaid());
            totalPenalty = totalPenalty.add(repayment.getPenaltyPaid());
        }
        
        DailyCollectionReport report = new DailyCollectionReport();
        report.setDate(date);
        report.setBranchId(branchId);
        report.setTotalCollection(totalCollection);
        report.setTotalPrincipal(totalPrincipal);
        report.setTotalInterest(totalInterest);
        report.setTotalPenalty(totalPenalty);
        report.setNumberOfTransactions(repayments.size());
        
        return report;
    }
    
    /**
     * Get loans with upcoming repayments within the next X days
     * @param daysAhead Number of days to look ahead (default 7)
     * @return List of loans with upcoming repayments mapped to UpcomingRepaymentLoanResponse
     */
    public List<UpcomingRepaymentLoanResponse> getUpcomingRepaymentLoans(int daysAhead) {
        LocalDate startDate = LocalDate.now();
        LocalDate endDate = startDate.plusDays(daysAhead);
        
        List<PawnLoan> loans = pawnLoanRepository.findLoansWithUpcomingRepayments(startDate, endDate);
        return mapLoansToUpcomingRepaymentResponse(loans);
    }
    
    /**
     * Get loans with upcoming repayments within the next X days with pagination
     */
    public Page<UpcomingRepaymentLoanResponse> getUpcomingRepaymentLoans(int daysAhead, Pageable pageable) {
        LocalDate startDate = LocalDate.now();
        LocalDate endDate = startDate.plusDays(daysAhead);
        
        Page<PawnLoan> loansPage = pawnLoanRepository.findLoansWithUpcomingRepayments(startDate, endDate, pageable);
        List<UpcomingRepaymentLoanResponse> responses = mapLoansToUpcomingRepaymentResponse(loansPage.getContent());
        
        return new PageImpl<>(responses, pageable, loansPage.getTotalElements());
    }
    
    /**
     * Get loans with upcoming repayments within a custom date range
     */
    public List<UpcomingRepaymentLoanResponse> getUpcomingRepaymentLoans(LocalDate startDate, LocalDate endDate) {
        if (startDate == null) {
            startDate = LocalDate.now();
        }
        if (endDate == null) {
            endDate = startDate.plusDays(7);
        }
        
        List<PawnLoan> loans = pawnLoanRepository.findLoansWithUpcomingRepayments(startDate, endDate);
        return mapLoansToUpcomingRepaymentResponse(loans);
    }
    
    /**
     * Helper method to map PawnLoan entities to UpcomingRepaymentLoanResponse DTOs
     */
    private List<UpcomingRepaymentLoanResponse> mapLoansToUpcomingRepaymentResponse(List<PawnLoan> loans) {
        List<UpcomingRepaymentLoanResponse> responses = new ArrayList<>();
        
        for (PawnLoan loan : loans) {
            UpcomingRepaymentLoanResponse response = new UpcomingRepaymentLoanResponse();
            
            // Basic loan information
            response.setId(loan.getId());
            response.setLoanCode(loan.getLoanCode());
            
            // Customer information
            if (loan.getCustomer() != null) {
                CustomerResponse customerResponse = new CustomerResponse();
                customerResponse.setId(loan.getCustomer().getId());
                customerResponse.setFullName(loan.getCustomer().getFullName());
                customerResponse.setPhone(loan.getCustomer().getPhone());
                customerResponse.setIdNumber(loan.getCustomer().getIdNumber());
                customerResponse.setAddress(loan.getCustomer().getAddress());
                customerResponse.setStatus(loan.getCustomer().getStatus());
                response.setCustomer(customerResponse);
                response.setCustomerPhone(loan.getCustomer().getPhone());
                // Customer email not available in Customer model
                response.setCustomerEmail(null);
            }
            
            // Currency information
            if (loan.getCurrency() != null) {
                CurrencyResponse currencyResponse = new CurrencyResponse();
                currencyResponse.setId(loan.getCurrency().getId());
                currencyResponse.setCode(loan.getCurrency().getCode());
                currencyResponse.setName(loan.getCurrency().getName());
                currencyResponse.setSymbol(loan.getCurrency().getSymbol());
                response.setCurrency(currencyResponse);
            }
            
            // Branch information
            if (loan.getBranch() != null) {
                BranchResponse branchResponse = new BranchResponse();
                branchResponse.setId(loan.getBranch().getId());
                branchResponse.setName(loan.getBranch().getName());
                branchResponse.setAddress(loan.getBranch().getAddress());
                branchResponse.setPhone(loan.getBranch().getPhone());
                branchResponse.setStatus(loan.getBranch().getStatus());
                response.setBranch(branchResponse);
            }
            
            // Loan amounts
            response.setLoanAmount(loan.getLoanAmount());
            response.setTotalPayableAmount(loan.getTotalPayableAmount());
            
            // Calculate remaining balance
            BigDecimal totalPaid = getTotalPaidAmountByLoanId(loan.getId());
            BigDecimal remainingBalance = loan.getTotalPayableAmount().subtract(totalPaid);
            response.setRemainingBalance(remainingBalance);
            
            // For simplicity, set next payment amount as the remaining balance
            // In a real system, this would be calculated based on payment schedule
            response.setNextPaymentAmount(remainingBalance);
            
            // Dates
            response.setLoanDate(loan.getLoanDate());
            response.setDueDate(loan.getDueDate());
            response.setNextPaymentDueDate(loan.getDueDate()); // Assuming due date is next payment
            
            // Calculate days until due
            if (loan.getDueDate() != null) {
                long daysUntilDue = loan.getDueDate().toEpochDay() - LocalDate.now().toEpochDay();
                response.setDaysUntilDue((int) daysUntilDue);
                
                // Calculate overdue days if applicable
                if (daysUntilDue < 0) {
                    response.setOverdueDays((int) Math.abs(daysUntilDue));
                }
            }
            
            // Status
            response.setStatus(loan.getStatus());
            
            // Determine follow-up priority
            if (loan.getDueDate() != null) {
                long daysUntilDue = loan.getDueDate().toEpochDay() - LocalDate.now().toEpochDay();
                if (daysUntilDue < 0) {
                    response.setFollowUpPriority("HIGH"); // Overdue
                } else if (daysUntilDue <= 3) {
                    response.setFollowUpPriority("HIGH"); // Due within 3 days
                } else if (daysUntilDue <= 7) {
                    response.setFollowUpPriority("MEDIUM"); // Due within a week
                } else {
                    response.setFollowUpPriority("LOW"); // More than a week away
                }
            }
            
            // Timestamps
            response.setCreatedAt(loan.getCreatedAt());
            response.setUpdatedAt(loan.getUpdatedAt());
            
            responses.add(response);
        }
        
        return responses;
    }
    
    // Inner classes for response objects
    public static class RepaymentSchedule {
        private Long loanId;
        private String loanCode;
        private BigDecimal totalPayable;
        private BigDecimal totalPaid;
        private BigDecimal remainingBalance;
        private LocalDate dueDate;
        private boolean isOverdue;
        private Long daysOverdue;
        private BigDecimal estimatedPenalty;
        
        // Getters and setters
        public Long getLoanId() { return loanId; }
        public void setLoanId(Long loanId) { this.loanId = loanId; }
        
        public String getLoanCode() { return loanCode; }
        public void setLoanCode(String loanCode) { this.loanCode = loanCode; }
        
        public BigDecimal getTotalPayable() { return totalPayable; }
        public void setTotalPayable(BigDecimal totalPayable) { this.totalPayable = totalPayable; }
        
        public BigDecimal getTotalPaid() { return totalPaid; }
        public void setTotalPaid(BigDecimal totalPaid) { this.totalPaid = totalPaid; }
        
        public BigDecimal getRemainingBalance() { return remainingBalance; }
        public void setRemainingBalance(BigDecimal remainingBalance) { this.remainingBalance = remainingBalance; }
        
        public LocalDate getDueDate() { return dueDate; }
        public void setDueDate(LocalDate dueDate) { this.dueDate = dueDate; }
        
        public boolean getIsOverdue() { return isOverdue; }
        public void setIsOverdue(boolean overdue) { isOverdue = overdue; }
        
        public Long getDaysOverdue() { return daysOverdue; }
        public void setDaysOverdue(Long daysOverdue) { this.daysOverdue = daysOverdue; }
        
        public BigDecimal getEstimatedPenalty() { return estimatedPenalty; }
        public void setEstimatedPenalty(BigDecimal estimatedPenalty) { this.estimatedPenalty = estimatedPenalty; }
    }
    
    public static class DailyCollectionReport {
        private LocalDate date;
        private Long branchId;
        private BigDecimal totalCollection;
        private BigDecimal totalPrincipal;
        private BigDecimal totalInterest;
        private BigDecimal totalPenalty;
        private Integer numberOfTransactions;
        
        // Getters and setters
        public LocalDate getDate() { return date; }
        public void setDate(LocalDate date) { this.date = date; }
        
        public Long getBranchId() { return branchId; }
        public void setBranchId(Long branchId) { this.branchId = branchId; }
        
        public BigDecimal getTotalCollection() { return totalCollection; }
        public void setTotalCollection(BigDecimal totalCollection) { this.totalCollection = totalCollection; }
        
        public BigDecimal getTotalPrincipal() { return totalPrincipal; }
        public void setTotalPrincipal(BigDecimal totalPrincipal) { this.totalPrincipal = totalPrincipal; }
        
        public BigDecimal getTotalInterest() { return totalInterest; }
        public void setTotalInterest(BigDecimal totalInterest) { this.totalInterest = totalInterest; }
        
        public BigDecimal getTotalPenalty() { return totalPenalty; }
        public void setTotalPenalty(BigDecimal totalPenalty) { this.totalPenalty = totalPenalty; }
        
        public Integer getNumberOfTransactions() { return numberOfTransactions; }
        public void setNumberOfTransactions(Integer numberOfTransactions) { this.numberOfTransactions = numberOfTransactions; }
    }
    
    public static class CustomerRepaymentSummary {
        private Long customerId;
        private LocalDate startDate;
        private LocalDate endDate;
        private Integer totalRepayments;
        private BigDecimal totalPaidAmount;
        private BigDecimal totalPrincipal;
        private BigDecimal totalInterest;
        private BigDecimal totalPenalty;
        private Map<String, BigDecimal> monthlyTotals;
        
        // Getters and setters
        public Long getCustomerId() { return customerId; }
        public void setCustomerId(Long customerId) { this.customerId = customerId; }
        
        public LocalDate getStartDate() { return startDate; }
        public void setStartDate(LocalDate startDate) { this.startDate = startDate; }
        
        public LocalDate getEndDate() { return endDate; }
        public void setEndDate(LocalDate endDate) { this.endDate = endDate; }
        
        public Integer getTotalRepayments() { return totalRepayments; }
        public void setTotalRepayments(Integer totalRepayments) { this.totalRepayments = totalRepayments; }
        
        public BigDecimal getTotalPaidAmount() { return totalPaidAmount; }
        public void setTotalPaidAmount(BigDecimal totalPaidAmount) { this.totalPaidAmount = totalPaidAmount; }
        
        public BigDecimal getTotalPrincipal() { return totalPrincipal; }
        public void setTotalPrincipal(BigDecimal totalPrincipal) { this.totalPrincipal = totalPrincipal; }
        
        public BigDecimal getTotalInterest() { return totalInterest; }
        public void setTotalInterest(BigDecimal totalInterest) { this.totalInterest = totalInterest; }
        
        public BigDecimal getTotalPenalty() { return totalPenalty; }
        public void setTotalPenalty(BigDecimal totalPenalty) { this.totalPenalty = totalPenalty; }
        
        public Map<String, BigDecimal> getMonthlyTotals() { return monthlyTotals; }
        public void setMonthlyTotals(Map<String, BigDecimal> monthlyTotals) { this.monthlyTotals = monthlyTotals; }
    }
}