package com.example.loan_origination_system.service;

import com.example.loan_origination_system.exception.BusinessException;
import com.example.loan_origination_system.model.enums.CollateralStatus;
import com.example.loan_origination_system.model.enums.LoanStatus;
import com.example.loan_origination_system.model.loan.PawnItem;
import com.example.loan_origination_system.model.loan.PawnLoan;
import com.example.loan_origination_system.model.master.Branch;
import com.example.loan_origination_system.model.master.Currency;
import com.example.loan_origination_system.model.people.Customer;
import com.example.loan_origination_system.repository.*;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class PawnLoanService {
    
    private final PawnLoanRepository pawnLoanRepository;
    private final CustomerRepository customerRepository;
    private final PawnItemRepository pawnItemRepository;
    private final BranchRepository branchRepository;
    private final CurrencyRepository currencyRepository;
    private final PawnItemService pawnItemService;
    
    /**
     * Create a new loan with comprehensive business logic
     * Business Rules:
     * 1. principalAmount <= 70% of collateral estimatedValue
     * 2. Calculate totalPayableAmount
     * 3. Change collateral status when loan created
     */
    @Transactional
    public PawnLoan createLoan(PawnLoan loan) {
        // Validate customer exists
        Customer customer = customerRepository.findById(loan.getCustomer().getId())
            .orElseThrow(() -> new BusinessException("CUSTOMER_NOT_FOUND",
                "Customer with ID " + loan.getCustomer().getId() + " not found"));
        
        // Validate collateral exists and is available
        PawnItem pawnItem = pawnItemRepository.findById(loan.getPawnItem().getId())
            .orElseThrow(() -> new BusinessException("COLLATERAL_NOT_FOUND",
                "Collateral with ID " + loan.getPawnItem().getId() + " not found"));
        
        if (pawnItem.getStatus() != CollateralStatus.AVAILABLE) {
            throw new BusinessException("COLLATERAL_NOT_AVAILABLE",
                "Collateral with ID " + pawnItem.getId() + " is not available for pawn. Current status: " + pawnItem.getStatus());
        }
        
        // Validate branch exists
        Branch branch = branchRepository.findById(loan.getBranch().getId())
            .orElseThrow(() -> new BusinessException("BRANCH_NOT_FOUND",
                "Branch with ID " + loan.getBranch().getId() + " not found"));
        
        // Validate currency exists
        Currency currency = currencyRepository.findById(loan.getCurrency().getId())
            .orElseThrow(() -> new BusinessException("CURRENCY_NOT_FOUND",
                "Currency with ID " + loan.getCurrency().getId() + " not found"));
        
        // Business Rule: principalAmount <= 70% of collateral estimatedValue
        BigDecimal maxLoanAmount = pawnItem.getEstimatedValue()
            .multiply(new BigDecimal("0.70"))
            .setScale(2, RoundingMode.HALF_UP);
        
        if (loan.getLoanAmount().compareTo(maxLoanAmount) > 0) {
            throw new BusinessException("LOAN_AMOUNT_EXCEEDS_LIMIT",
                String.format("Loan amount %.2f exceeds maximum allowed %.2f (70%% of collateral value %.2f)",
                    loan.getLoanAmount(), maxLoanAmount, pawnItem.getEstimatedValue()));
        }
        
        // Calculate total payable amount (principal + interest)
        BigDecimal interestAmount = loan.getLoanAmount()
            .multiply(loan.getInterestRate().divide(new BigDecimal("100"), 4, RoundingMode.HALF_UP));
        BigDecimal totalPayableAmount = loan.getLoanAmount().add(interestAmount)
            .setScale(2, RoundingMode.HALF_UP);
        
        // Generate unique loan code
        String loanCode = generateLoanCode();
        
        // Set loan properties
        loan.setCustomer(customer);
        loan.setPawnItem(pawnItem);
        loan.setBranch(branch);
        loan.setCurrency(currency);
        loan.setLoanCode(loanCode);
        loan.setTotalPayableAmount(totalPayableAmount);
        loan.setStatus(LoanStatus.ACTIVE);
        
        // Update collateral status to PAWNED
        pawnItem.setStatus(CollateralStatus.PAWNED);
        pawnItemRepository.save(pawnItem);
        
        return pawnLoanRepository.save(loan);
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
        
        // Update loan status
        loan.setStatus(LoanStatus.REDEEMED);
        loan.setRedeemedAt(LocalDateTime.now());
        
        // Update collateral status to AVAILABLE
        PawnItem pawnItem = loan.getPawnItem();
        pawnItem.setStatus(CollateralStatus.AVAILABLE);
        pawnItemRepository.save(pawnItem);
        
        return pawnLoanRepository.save(loan);
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
        
        loan.setStatus(LoanStatus.DEFAULTED);
        loan.setDefaultedAt(LocalDateTime.now());
        
        // Update collateral status to FORFEITED
        PawnItem pawnItem = loan.getPawnItem();
        pawnItem.setStatus(CollateralStatus.FORFEITED);
        pawnItemRepository.save(pawnItem);
        
        return pawnLoanRepository.save(loan);
    }
    
    /**
     * Scheduled job to automatically mark overdue loans as defaulted
     * Business Rule: If past maturityDate → status DEFAULTED
     */
    @Scheduled(cron = "0 0 0 * * ?") // Run daily at midnight
    @Transactional
    public void processOverdueLoans() {
        LocalDate today = LocalDate.now();
        List<PawnLoan> overdueLoans = pawnLoanRepository.findOverdueLoans(today);
        
        for (PawnLoan loan : overdueLoans) {
            if (loan.getStatus() == LoanStatus.ACTIVE) {
                loan.setStatus(LoanStatus.DEFAULTED);
                loan.setDefaultedAt(LocalDateTime.now());
                
                // Update collateral status to FORFEITED
                PawnItem pawnItem = loan.getPawnItem();
                pawnItem.setStatus(CollateralStatus.FORFEITED);
                pawnItemRepository.save(pawnItem);
                
                pawnLoanRepository.save(loan);
            }
        }
    }
    
    /**
     * Calculate total payable amount for a loan
     */
    public BigDecimal calculateTotalPayableAmount(BigDecimal principalAmount, BigDecimal interestRate) {
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
     * Generate unique loan code
     */
    private String generateLoanCode() {
        String timestamp = String.valueOf(System.currentTimeMillis());
        String uuid = UUID.randomUUID().toString().substring(0, 8).toUpperCase();
        return "LOAN-" + timestamp.substring(timestamp.length() - 6) + "-" + uuid;
    }
}