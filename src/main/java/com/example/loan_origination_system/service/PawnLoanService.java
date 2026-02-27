package com.example.loan_origination_system.service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import com.example.loan_origination_system.dto.PawnLoanCreateFullRequest;
import com.example.loan_origination_system.dto.PawnLoanRequest;
import com.example.loan_origination_system.exception.BusinessException;
import com.example.loan_origination_system.mapper.LoanMapper;
import com.example.loan_origination_system.model.enums.CollateralStatus;
import com.example.loan_origination_system.model.enums.LoanEvent;
import com.example.loan_origination_system.model.enums.LoanStatus;
import com.example.loan_origination_system.model.enums.PaymentFrequency;
import com.example.loan_origination_system.model.loan.PawnItem;
import com.example.loan_origination_system.model.loan.PawnLoan;
import com.example.loan_origination_system.model.loan.PaymentScheduleItem;
import com.example.loan_origination_system.model.master.Branch;
import com.example.loan_origination_system.model.master.Currency;
import com.example.loan_origination_system.model.people.Customer;
import com.example.loan_origination_system.repository.BranchRepository;
import com.example.loan_origination_system.repository.CurrencyRepository;
import com.example.loan_origination_system.repository.CustomerRepository;
import com.example.loan_origination_system.repository.PawnItemRepository;
import com.example.loan_origination_system.repository.PawnLoanRepository;

import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class PawnLoanService {
    
    private final PawnLoanRepository pawnLoanRepository;
    private final CustomerRepository customerRepository;
    private final PawnItemRepository pawnItemRepository;
    private final BranchRepository branchRepository;
    private final CurrencyRepository currencyRepository;
    private final PawnItemService pawnItemService;
    private final LoanStateMachine loanStateMachine;
    private final LoanMapper loanMapper;
    
    /**
     * Find or create customer by national ID
     * If customer exists, return existing customer
     * If not exists and customerInfo provided, create new customer
     * If not exists and no customerInfo, throw exception
     */
    @Transactional
    public Customer findOrCreateCustomer(String nationalId, PawnLoanCreateFullRequest.CustomerInfo customerInfo) {
        // Try to find existing customer
        return customerRepository.findByIdNumber(nationalId)
            .orElseGet(() -> {
                // Customer not found, check if we have info to create new customer
                if (customerInfo == null) {
                    throw new BusinessException("CUSTOMER_NOT_FOUND",
                        "Customer with national ID " + nationalId + " not found and no customer info provided");
                }
                
                // Validate required fields for new customer
                if (customerInfo.getFullName() == null || customerInfo.getFullName().isBlank()) {
                    throw new BusinessException("CUSTOMER_NAME_REQUIRED",
                        "Full name is required for new customer");
                }
                
                if (customerInfo.getPhone() == null || customerInfo.getPhone().isBlank()) {
                    throw new BusinessException("CUSTOMER_PHONE_REQUIRED",
                        "Phone is required for new customer");
                }
                
                // Create new customer
                Customer newCustomer = new Customer();
                newCustomer.setFullName(customerInfo.getFullName());
                newCustomer.setPhone(customerInfo.getPhone());
                newCustomer.setIdNumber(nationalId);
                newCustomer.setAddress(customerInfo.getAddress());
                
                return customerRepository.save(newCustomer);
            });
    }
    
    /**
     * Find or create pawn item for a customer
     * If pawnItemId provided, fetch and validate it belongs to customer
     * If new collateral details provided, create new pawn item
     * If neither provided, throw exception
     */
    @Transactional
    public PawnItem findOrCreatePawnItem(Customer customer, PawnLoanCreateFullRequest.CollateralInfo collateralInfo) {
        // If pawnItemId provided, fetch existing item
        if (collateralInfo.getPawnItemId() != null) {
            PawnItem pawnItem = pawnItemRepository.findById(collateralInfo.getPawnItemId())
                .orElseThrow(() -> new BusinessException("COLLATERAL_NOT_FOUND",
                    "Collateral with ID " + collateralInfo.getPawnItemId() + " not found"));
            
            // Validate ownership
            if (!pawnItem.getCustomer().getId().equals(customer.getId())) {
                throw new BusinessException("COLLATERAL_OWNERSHIP_MISMATCH",
                    "Collateral with ID " + collateralInfo.getPawnItemId() + " does not belong to customer " + customer.getId());
            }
            
            // Validate collateral is available (not already pawned)
            if (pawnItem.getStatus() != CollateralStatus.AVAILABLE) {
                throw new BusinessException("COLLATERAL_NOT_AVAILABLE",
                    "Collateral with ID " + pawnItem.getId() + " is not available for pawn. Current status: " + pawnItem.getStatus());
            }
            
            return pawnItem;
        }
        
        // Otherwise, create new pawn item from collateral info
        if (collateralInfo.getItemType() == null || collateralInfo.getItemType().isBlank()) {
            throw new BusinessException("COLLATERAL_TYPE_REQUIRED",
                "Item type is required for new collateral");
        }
        
        if (collateralInfo.getEstimatedValue() == null) {
            throw new BusinessException("COLLATERAL_VALUE_REQUIRED",
                "Estimated value is required for new collateral");
        }
        
        PawnItem newPawnItem = new PawnItem();
        newPawnItem.setCustomer(customer);
        newPawnItem.setItemType(collateralInfo.getItemType());
        newPawnItem.setDescription(collateralInfo.getDescription());
        newPawnItem.setEstimatedValue(collateralInfo.getEstimatedValue());
        newPawnItem.setPhotoUrl(collateralInfo.getPhotoUrl());
        newPawnItem.setStatus(CollateralStatus.AVAILABLE);
        
        return pawnItemRepository.save(newPawnItem);
    }
    
    /**
     * Create a new loan with comprehensive business logic
     * Business Rules:
     * 1. principalAmount <= 70% of collateral estimatedValue
     * 2. Calculate totalPayableAmount
     * 3. Change collateral status when loan created
     */
    @Transactional
    public PawnLoan createLoan(PawnLoanRequest request) {
        // Convert DTO to entity (ignoring relationships)
        PawnLoan loan = loanMapper.toPawnLoan(request);
        
        // Validate customer exists
        Customer customer = customerRepository.findById(request.getCustomerId())
            .orElseThrow(() -> new BusinessException("CUSTOMER_NOT_FOUND",
                "Customer with ID " + request.getCustomerId() + " not found"));
        
        // Validate collateral exists and is available
        PawnItem pawnItem = pawnItemRepository.findById(request.getPawnItemId())
            .orElseThrow(() -> new BusinessException("COLLATERAL_NOT_FOUND",
                "Collateral with ID " + request.getPawnItemId() + " not found"));
        
        if (pawnItem.getStatus() != CollateralStatus.AVAILABLE) {
            throw new BusinessException("COLLATERAL_NOT_AVAILABLE",
                "Collateral with ID " + pawnItem.getId() + " is not available for pawn. Current status: " + pawnItem.getStatus());
        }
        
        // Validate branch exists
        Branch branch = branchRepository.findById(request.getBranchId())
            .orElseThrow(() -> new BusinessException("BRANCH_NOT_FOUND",
                "Branch with ID " + request.getBranchId() + " not found"));
        
        // Validate currency exists
        Currency currency = currencyRepository.findById(request.getCurrencyId())
            .orElseThrow(() -> new BusinessException("CURRENCY_NOT_FOUND",
                "Currency with ID " + request.getCurrencyId() + " not found"));
        
        // Business Rule: principalAmount <= 70% of collateral estimatedValue
        BigDecimal maxLoanAmount = pawnItem.getEstimatedValue()
            .multiply(new BigDecimal("0.70"))
            .setScale(2, RoundingMode.HALF_UP);
        
        if (loan.getLoanAmount().compareTo(maxLoanAmount) > 0) {
            throw new BusinessException("LOAN_AMOUNT_EXCEEDS_LIMIT",
                String.format("Loan amount %.2f exceeds maximum allowed %.2f (70%% of collateral value %.2f)",
                    loan.getLoanAmount(), maxLoanAmount, pawnItem.getEstimatedValue()));
        }
        
        // Calculate due date from loan duration if not provided
        if (loan.getDueDate() == null && loan.getLoanDurationDays() != null) {
            LocalDate dueDate = loan.getLoanDate() != null ?
                loan.getLoanDate().plusDays(loan.getLoanDurationDays()) :
                LocalDate.now().plusDays(loan.getLoanDurationDays());
            loan.setDueDate(dueDate);
        }
        
        // Validate interest rate is not null before calculation
        if (loan.getInterestRate() == null) {
            throw new BusinessException("INTEREST_RATE_REQUIRED", "Interest rate is required for loan calculation");
        }
        
        // Calculate total payable amount (principal + interest + storage fee)
        BigDecimal interestAmount = loan.getLoanAmount()
            .multiply(loan.getInterestRate().divide(new BigDecimal("100"), 4, RoundingMode.HALF_UP));
        
        BigDecimal storageFee = loan.getStorageFee() != null ? loan.getStorageFee() : BigDecimal.ZERO;
        BigDecimal totalPayableAmount = loan.getLoanAmount()
            .add(interestAmount)
            .add(storageFee)
            .setScale(2, RoundingMode.HALF_UP);
        
        // Set redemption deadline if not provided (due date + grace period)
        if (loan.getRedemptionDeadline() == null && loan.getDueDate() != null && loan.getGracePeriodDays() != null) {
            LocalDate redemptionDeadline = loan.getDueDate().plusDays(loan.getGracePeriodDays());
            loan.setRedemptionDeadline(redemptionDeadline);
        }
        
        // Calculate installment amount if not provided for installment payments
        if (loan.getPaymentFrequency() != null &&
            loan.getPaymentFrequency() != PaymentFrequency.ONE_TIME &&
            loan.getNumberOfInstallments() != null &&
            loan.getNumberOfInstallments() > 1 &&
            loan.getInstallmentAmount() == null) {
            
            // Calculate equal installments including interest
            BigDecimal installmentAmount = totalPayableAmount
                .divide(new BigDecimal(loan.getNumberOfInstallments()), 2, RoundingMode.HALF_UP);
            loan.setInstallmentAmount(installmentAmount);
        }
        
        // Set default values if not provided
        if (loan.getLoanDurationDays() == null) {
            loan.setLoanDurationDays(30); // Default 30 days
        }
        
        if (loan.getGracePeriodDays() == null) {
            loan.setGracePeriodDays(7); // Default 7 days grace period
        }
        
        if (loan.getPenaltyRate() == null) {
            loan.setPenaltyRate(BigDecimal.ZERO);
        }
        
        if (loan.getStorageFee() == null) {
            loan.setStorageFee(BigDecimal.ZERO);
        }
        
        if (loan.getPaymentFrequency() == null) {
            loan.setPaymentFrequency(PaymentFrequency.ONE_TIME);
        }
        
        if (loan.getNumberOfInstallments() == null) {
            loan.setNumberOfInstallments(1);
        }
        
        // Generate unique loan code
        String loanCode = generateLoanCode();
        
        // Set loan properties (loan starts as CREATED)
        loan.setCustomer(customer);
        loan.setPawnItem(pawnItem);
        loan.setBranch(branch);
        loan.setCurrency(currency);
        loan.setLoanCode(loanCode);
        loan.setTotalPayableAmount(totalPayableAmount);
        // Status defaults to CREATED (set in entity)
        
        // Save the loan first
        PawnLoan savedLoan = pawnLoanRepository.save(loan);
        
        // Issue the loan using state machine (CREATED → ACTIVE)
        // This will also update collateral status to PAWNED
        return loanStateMachine.issueLoan(savedLoan.getId());
    }
    
    /**
     * Create a full loan with customer, collateral, and loan in one request
     * Business Rules:
     * 1. Find or create customer by national ID
     * 2. Find or create collateral
     * 3. Validate collateral availability
     * 4. Create loan with business rules
     * 5. Update collateral status to PAWNED
     */
    @Transactional
    public PawnLoan createFullLoan(PawnLoanCreateFullRequest request) {
        // 1. Find or create customer
        Customer customer = findOrCreateCustomer(
            request.getNationalId(),
            request.getCustomerInfo()
        );
        
        // 2. Find or create collateral
        PawnItem pawnItem = findOrCreatePawnItem(
            customer,
            request.getCollateralInfo()
        );
        
        // 3. Validate collateral is available (already done in findOrCreatePawnItem for existing items)
        // For new items, status is already set to AVAILABLE
        
        // 4. Validate branch exists
        Branch branch = branchRepository.findById(request.getLoanInfo().getBranchId())
            .orElseThrow(() -> new BusinessException("BRANCH_NOT_FOUND",
                "Branch with ID " + request.getLoanInfo().getBranchId() + " not found"));
        
        // 5. Validate currency exists
        Currency currency = currencyRepository.findById(request.getLoanInfo().getCurrencyId())
            .orElseThrow(() -> new BusinessException("CURRENCY_NOT_FOUND",
                "Currency with ID " + request.getLoanInfo().getCurrencyId() + " not found"));
        
        // 6. Business Rule: loanAmount <= 70% of collateral estimatedValue
        BigDecimal maxLoanAmount = pawnItem.getEstimatedValue()
            .multiply(new BigDecimal("0.70"))
            .setScale(2, RoundingMode.HALF_UP);
        
        if (request.getLoanInfo().getLoanAmount().compareTo(maxLoanAmount) > 0) {
            throw new BusinessException("LOAN_AMOUNT_EXCEEDS_LIMIT",
                String.format("Loan amount %.2f exceeds maximum allowed %.2f (70%% of collateral value %.2f)",
                    request.getLoanInfo().getLoanAmount(), maxLoanAmount, pawnItem.getEstimatedValue()));
        }
        
        // 7. Create loan entity from request
        PawnLoan loan = new PawnLoan();
        loan.setCustomer(customer);
        loan.setPawnItem(pawnItem);
        loan.setBranch(branch);
        loan.setCurrency(currency);
        loan.setLoanAmount(request.getLoanInfo().getLoanAmount());
        loan.setInterestRate(request.getLoanInfo().getInterestRate());
        loan.setDueDate(request.getLoanInfo().getDueDate());
        loan.setRedemptionDeadline(request.getLoanInfo().getRedemptionDeadline());
        loan.setLoanDurationDays(request.getLoanInfo().getLoanDurationDays());
        loan.setGracePeriodDays(request.getLoanInfo().getGracePeriodDays());
        loan.setStorageFee(request.getLoanInfo().getStorageFee());
        loan.setPenaltyRate(request.getLoanInfo().getPenaltyRate());
        loan.setPaymentFrequency(request.getLoanInfo().getPaymentFrequency());
        loan.setNumberOfInstallments(request.getLoanInfo().getNumberOfInstallments());
        loan.setInstallmentAmount(request.getLoanInfo().getInstallmentAmount());
        
        // 8. Calculate total payable amount
        BigDecimal interestAmount = loan.getLoanAmount()
            .multiply(loan.getInterestRate().divide(new BigDecimal("100"), 4, RoundingMode.HALF_UP));
        
        BigDecimal storageFee = loan.getStorageFee() != null ? loan.getStorageFee() : BigDecimal.ZERO;
        BigDecimal totalPayableAmount = loan.getLoanAmount()
            .add(interestAmount)
            .add(storageFee)
            .setScale(2, RoundingMode.HALF_UP);
        
        loan.setTotalPayableAmount(totalPayableAmount);
        
        // 9. Generate unique loan code
        String loanCode = generateLoanCode();
        loan.setLoanCode(loanCode);
        
        // 10. Save the loan
        PawnLoan savedLoan = pawnLoanRepository.save(loan);
        
        // 11. Issue the loan using state machine (CREATED → ACTIVE)
        // This will also update collateral status to PAWNED
        return loanStateMachine.issueLoan(savedLoan.getId());
    }
    
    /**
     * Get loan by ID
     */
    public PawnLoan getLoanById(Long id) {
        return pawnLoanRepository.findById(id)
            .orElseThrow(() -> new BusinessException("LOAN_NOT_FOUND",
                "Loan with ID " + id + " not found"));
    }
    
    /**
     * Get loan by loan code
     */
    public PawnLoan getLoanByCode(String loanCode) {
        return pawnLoanRepository.findByLoanCode(loanCode)
            .orElseThrow(() -> new BusinessException("LOAN_NOT_FOUND",
                "Loan with code " + loanCode + " not found"));
    }
    
    /**
     * Get all loans with pagination (excluding cancelled)
     */
    public Page<PawnLoan> getAllLoans(Pageable pageable) {
        return pawnLoanRepository.findAllActive(pageable);
    }
    
    /**
     * Get loans by customer ID
     */
    public List<PawnLoan> getLoansByCustomerId(Long customerId) {
        // Validate customer exists
        if (!customerRepository.existsById(customerId)) {
            throw new BusinessException("CUSTOMER_NOT_FOUND",
                "Customer with ID " + customerId + " not found");
        }
        
        return pawnLoanRepository.findByCustomerId(customerId);
    }
    
    /**
     * Get loans by customer ID with pagination
     */
    public Page<PawnLoan> getLoansByCustomerId(Long customerId, Pageable pageable) {
        // Validate customer exists
        if (!customerRepository.existsById(customerId)) {
            throw new BusinessException("CUSTOMER_NOT_FOUND",
                "Customer with ID " + customerId + " not found");
        }
        
        return pawnLoanRepository.findByCustomerId(customerId, pageable);
    }
    
    /**
     * Get loans by status with pagination
     */
    public Page<PawnLoan> getLoansByStatus(LoanStatus status, Pageable pageable) {
        return pawnLoanRepository.findByStatus(status, pageable);
    }
    
    /**
     * Redeem a loan
     * Business Rules:
     * 1. Cannot redeem if status != ACTIVE
     * 2. When redeemed → collateral AVAILABLE
     */
    @Transactional
    public PawnLoan redeemLoan(Long id) {
        PawnLoan loan = getLoanById(id);
        
        // Business Rule: Cannot redeem if status != ACTIVE
        if (loan.getStatus() != LoanStatus.ACTIVE) {
            throw new BusinessException("LOAN_NOT_ACTIVE",
                "Cannot redeem loan with ID " + id + " because it is not active. Current status: " + loan.getStatus());
        }
        
        // Use state machine to redeem the loan (ACTIVE → REDEEMED)
        return loanStateMachine.processFullPayment(id);
    }
    
    /**
     * Mark loan as defaulted (manual override)
     */
    @Transactional
    public PawnLoan markLoanAsDefaulted(Long id) {
        PawnLoan loan = getLoanById(id);
        
        if (loan.getStatus() != LoanStatus.ACTIVE) {
            throw new BusinessException("LOAN_NOT_ACTIVE",
                "Cannot mark loan with ID " + id + " as defaulted because it is not active. Current status: " + loan.getStatus());
        }
        
        // Use state machine to mark as defaulted (ACTIVE → DEFAULTED)
        return loanStateMachine.transition(id, LoanEvent.MANUAL_DEFAULT);
    }
    
    /**
     * Delete a pawn loan
     * Business Rules:
     * 1. Can only delete loans in CREATED or CANCELLED status
     * 2. Cannot delete if there are existing repayments or forfeits
     */
    @Transactional
    public void deleteLoan(Long id) {
        PawnLoan loan = getLoanById(id);
        
        // Business Rule: Can only delete loans in CREATED or CANCELLED status
        if (loan.getStatus() != LoanStatus.CREATED && loan.getStatus() != LoanStatus.CANCELLED) {
            throw new BusinessException("LOAN_CANNOT_BE_DELETED",
                "Cannot delete loan with ID " + id + " because it is not in CREATED or CANCELLED status. Current status: " + loan.getStatus());
        }
        
        // Note: We could also check for existing repayments/forfeits here
        // but loans in CREATED/CANCELLED status should not have any.
        
        pawnLoanRepository.delete(loan);
    }
    
    /**
     * Scheduled job to automatically mark overdue loans as defaulted
     * Business Rule: If past maturityDate → status DEFAULTED
     *
     * DEPRECATED: Replaced by LoanSchedulerService with two-step process
     * (ACTIVE → OVERDUE → DEFAULTED)
     */
    // @Scheduled(cron = "0 0 0 * * ?") // Run daily at midnight
    // @Transactional
    // public void processOverdueLoans() {
    //     LocalDate today = LocalDate.now();
    //     List<PawnLoan> overdueLoans = pawnLoanRepository.findOverdueLoans(today);
    //
    //     for (PawnLoan loan : overdueLoans) {
    //         if (loan.getStatus() == LoanStatus.ACTIVE) {
    //             loan.setStatus(LoanStatus.DEFAULTED);
    //             loan.setDefaultedAt(LocalDateTime.now());
    //
    //             // Update collateral status to FORFEITED
    //             PawnItem pawnItem = loan.getPawnItem();
    //             pawnItem.setStatus(CollateralStatus.FORFEITED);
    //             pawnItemRepository.save(pawnItem);
    //
    //             pawnLoanRepository.save(loan);
    //         }
    //     }
    // }
    
    /**
     * Calculate total payable amount for a loan
     */
    public BigDecimal calculateTotalPayableAmount(BigDecimal principalAmount, BigDecimal interestRate) {
        if (principalAmount == null) {
            throw new IllegalArgumentException("Principal amount cannot be null");
        }
        if (interestRate == null) {
            throw new IllegalArgumentException("Interest rate cannot be null");
        }
        BigDecimal interestAmount = principalAmount
            .multiply(interestRate.divide(new BigDecimal("100"), 4, RoundingMode.HALF_UP));
        return principalAmount.add(interestAmount).setScale(2, RoundingMode.HALF_UP);
    }
    
    /**
     * Check if loan is overdue
     */
    public boolean isLoanOverdue(PawnLoan loan) {
        if (loan.getDueDate() == null || loan.getStatus() != LoanStatus.ACTIVE) {
            return false;
        }
        return loan.getDueDate().isBefore(LocalDate.now());
    }
    
    /**
     * Generate payment schedule for a loan
     * Creates a list of installments from 1st payment to last payment
     */
    public List<PaymentScheduleItem> generatePaymentSchedule(PawnLoan loan) {
        List<PaymentScheduleItem> schedule = new ArrayList<>();
        
        if (loan.getPaymentFrequency() == PaymentFrequency.ONE_TIME ||
            loan.getNumberOfInstallments() == null || loan.getNumberOfInstallments() <= 1) {
            // One-time payment
            PaymentScheduleItem item = new PaymentScheduleItem(
                1,
                loan.getDueDate(),
                loan.getTotalPayableAmount(),
                loan.getLoanAmount(),
                loan.getTotalPayableAmount().subtract(loan.getLoanAmount()),
                BigDecimal.ZERO
            );
            schedule.add(item);
            return schedule;
        }
        
        // Installment payments
        int numberOfInstallments = loan.getNumberOfInstallments();
        if (numberOfInstallments <= 0) {
            throw new IllegalArgumentException("Number of installments must be greater than 0");
        }
        
        BigDecimal installmentAmount = loan.getInstallmentAmount();
        if (installmentAmount == null) {
            // Calculate equal installments
            installmentAmount = loan.getTotalPayableAmount()
                .divide(new BigDecimal(numberOfInstallments), 2, RoundingMode.HALF_UP);
        }
        
        BigDecimal remainingBalance = loan.getTotalPayableAmount();
        LocalDate currentDueDate = loan.getLoanDate() != null ? loan.getLoanDate() : LocalDate.now();
        
        // Calculate interest per installment (simple interest distributed equally)
        BigDecimal totalInterest = loan.getTotalPayableAmount().subtract(loan.getLoanAmount());
        BigDecimal interestPerInstallment = totalInterest.divide(
            new BigDecimal(numberOfInstallments), 2, RoundingMode.HALF_UP);
        
        // Calculate principal per installment
        BigDecimal principalPerInstallment = loan.getLoanAmount().divide(
            new BigDecimal(numberOfInstallments), 2, RoundingMode.HALF_UP);
        
        for (int i = 1; i <= numberOfInstallments; i++) {
            // Calculate due date based on payment frequency
            LocalDate dueDate = calculateNextDueDate(currentDueDate, loan.getPaymentFrequency(), i);
            
            // Update remaining balance
            remainingBalance = remainingBalance.subtract(installmentAmount);
            if (remainingBalance.compareTo(BigDecimal.ZERO) < 0) {
                remainingBalance = BigDecimal.ZERO;
            }
            
            PaymentScheduleItem item = new PaymentScheduleItem(
                i,
                dueDate,
                installmentAmount,
                principalPerInstallment,
                interestPerInstallment,
                remainingBalance
            );
            schedule.add(item);
        }
        
        return schedule;
    }
    
    /**
     * Calculate next due date based on payment frequency
     */
    private LocalDate calculateNextDueDate(LocalDate startDate, PaymentFrequency frequency, int installmentNumber) {
        switch (frequency) {
            case WEEKLY:
                return startDate.plusWeeks(installmentNumber);
            case BI_WEEKLY:
                return startDate.plusWeeks(installmentNumber * 2);
            case MONTHLY:
                return startDate.plusMonths(installmentNumber);
            case QUARTERLY:
                return startDate.plusMonths(installmentNumber * 3);
            default:
                return startDate.plusDays(installmentNumber * 30); // Default monthly
        }
    }
    
    /**
     * Generate unique loan code
     */
    private String generateLoanCode() {
        String timestamp = String.valueOf(System.currentTimeMillis());
        String uuid = UUID.randomUUID().toString().substring(0, 8).toUpperCase();
        return "LOAN-" + timestamp.substring(timestamp.length() - 6) + "-" + uuid;
    }
    
    /**
     * Get loans with upcoming repayments within the next X days
     * @param daysAhead Number of days to look ahead (default 7)
     * @return List of loans with upcoming repayments
     */
    public List<PawnLoan> getLoansWithUpcomingRepayments(int daysAhead) {
        LocalDate startDate = LocalDate.now();
        LocalDate endDate = startDate.plusDays(daysAhead);
        
        return pawnLoanRepository.findLoansWithUpcomingRepayments(startDate, endDate);
    }
    
    /**
     * Get loans with upcoming repayments within the next X days with pagination
     */
    public Page<PawnLoan> getLoansWithUpcomingRepayments(int daysAhead, Pageable pageable) {
        LocalDate startDate = LocalDate.now();
        LocalDate endDate = startDate.plusDays(daysAhead);
        
        return pawnLoanRepository.findLoansWithUpcomingRepayments(startDate, endDate, pageable);
    }
    
    /**
     * Get loans with upcoming repayments within a custom date range
     */
    public List<PawnLoan> getLoansWithUpcomingRepayments(LocalDate startDate, LocalDate endDate) {
        if (startDate == null) {
            startDate = LocalDate.now();
        }
        if (endDate == null) {
            endDate = startDate.plusDays(7);
        }
        
        return pawnLoanRepository.findLoansWithUpcomingRepayments(startDate, endDate);
    }
    
    /**
     * Get loans that need follow-up (overdue or approaching due date)
     * @return List of loans needing follow-up
     */
    public List<PawnLoan> getLoansNeedingFollowUp() {
        LocalDate currentDate = LocalDate.now();
        return pawnLoanRepository.findLoansNeedingFollowUp(currentDate);
    }
    
    /**
     * Get customer loans that need follow-up
     * @param customerId Customer ID
     * @return List of customer loans needing follow-up
     */
    public List<PawnLoan> getCustomerLoansNeedingFollowUp(Long customerId) {
        return pawnLoanRepository.findCustomerLoansNeedingFollowUp(customerId);
    }
    
    /**
     * Calculate days until due date
     */
    public int calculateDaysUntilDue(LocalDate dueDate) {
        if (dueDate == null) {
            return 0;
        }
        LocalDate today = LocalDate.now();
        return (int) java.time.temporal.ChronoUnit.DAYS.between(today, dueDate);
    }
    
    /**
     * Calculate overdue days
     */
    public int calculateOverdueDays(LocalDate dueDate) {
        if (dueDate == null) {
            return 0;
        }
        LocalDate today = LocalDate.now();
        if (dueDate.isBefore(today)) {
            return (int) java.time.temporal.ChronoUnit.DAYS.between(dueDate, today);
        }
        return 0;
    }
    
    /**
     * Determine follow-up priority based on due date proximity
     */
    public String determineFollowUpPriority(LocalDate dueDate) {
        if (dueDate == null) {
            return "LOW";
        }
        
        int daysUntilDue = calculateDaysUntilDue(dueDate);
        int overdueDays = calculateOverdueDays(dueDate);
        
        if (overdueDays > 0) {
            if (overdueDays > 30) return "CRITICAL";
            if (overdueDays > 14) return "HIGH";
            if (overdueDays > 7) return "MEDIUM";
            return "LOW";
        } else {
            if (daysUntilDue <= 3) return "HIGH";
            if (daysUntilDue <= 7) return "MEDIUM";
            return "LOW";
        }
    }
}
