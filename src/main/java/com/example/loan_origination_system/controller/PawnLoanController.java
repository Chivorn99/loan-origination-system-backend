package com.example.loan_origination_system.controller;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.example.loan_origination_system.dto.ApiResponse;
import com.example.loan_origination_system.dto.PawnLoanCreateFullRequest;
import com.example.loan_origination_system.dto.PawnLoanRequest;
import com.example.loan_origination_system.model.enums.LoanStatus;
import com.example.loan_origination_system.model.loan.PawnLoan;
import com.example.loan_origination_system.model.loan.PaymentScheduleItem;
import com.example.loan_origination_system.service.PawnLoanService;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/pawn-loans")
@RequiredArgsConstructor
public class PawnLoanController {
    
    private final PawnLoanService pawnLoanService;
    
    /**
     * Create a new loan
     * POST /api/pawn-loans
     */
    @PostMapping
    public ResponseEntity<ApiResponse<PawnLoan>> createLoan(@Valid @RequestBody PawnLoanRequest request) {
        PawnLoan createdLoan = pawnLoanService.createLoan(request);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success("Loan created successfully", createdLoan));
    }
    
    /**
     * Create a full loan with customer, collateral, and loan in one request
     * POST /api/pawn-loans/create-full
     */
    @PostMapping("/create-full")
    public ResponseEntity<ApiResponse<PawnLoan>> createFullLoan(@Valid @RequestBody PawnLoanCreateFullRequest request) {
        PawnLoan createdLoan = pawnLoanService.createFullLoan(request);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success("Loan created successfully with customer and collateral", createdLoan));
    }
    
    /**
     * Get loan by ID
     * GET /api/pawn-loans/{id}
     */
    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<PawnLoan>> getLoan(@PathVariable Long id) {
        PawnLoan loan = pawnLoanService.getLoanById(id);
        return ResponseEntity.ok(ApiResponse.success(loan));
    }
    
    /**
     * Get loan by loan code
     * GET /api/pawn-loans/code/{loanCode}
     */
    @GetMapping("/code/{loanCode}")
    public ResponseEntity<ApiResponse<PawnLoan>> getLoanByCode(@PathVariable String loanCode) {
        PawnLoan loan = pawnLoanService.getLoanByCode(loanCode);
        return ResponseEntity.ok(ApiResponse.success(loan));
    }
    
    /**
     * Get all loans with pagination
     * GET /api/pawn-loans
     */
    @GetMapping
    public ResponseEntity<ApiResponse<Page<PawnLoan>>> getAllLoans(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "createdAt") String sortBy,
            @RequestParam(defaultValue = "desc") String direction) {
        
        Sort.Direction sortDirection = direction.equalsIgnoreCase("asc") ? Sort.Direction.ASC : Sort.Direction.DESC;
        Pageable pageable = PageRequest.of(page, size, Sort.by(sortDirection, sortBy));
        
        Page<PawnLoan> loans = pawnLoanService.getAllLoans(pageable);
        return ResponseEntity.ok(ApiResponse.success(loans));
    }
    
    /**
     * Get loans by customer ID
     * GET /api/pawn-loans/customer/{customerId}
     */
    @GetMapping("/customer/{customerId}")
    public ResponseEntity<ApiResponse<List<PawnLoan>>> getLoansByCustomerId(@PathVariable Long customerId) {
        List<PawnLoan> loans = pawnLoanService.getLoansByCustomerId(customerId);
        return ResponseEntity.ok(ApiResponse.success(loans));
    }
    
    /**
     * Get loans by customer ID with pagination
     * GET /api/pawn-loans/customer/{customerId}/page
     */
    @GetMapping("/customer/{customerId}/page")
    public ResponseEntity<ApiResponse<Page<PawnLoan>>> getLoansByCustomerIdPage(
            @PathVariable Long customerId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        
        Pageable pageable = PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "createdAt"));
        Page<PawnLoan> loans = pawnLoanService.getLoansByCustomerId(customerId, pageable);
        return ResponseEntity.ok(ApiResponse.success(loans));
    }
    
    /**
     * Get loans by status with pagination
     * GET /api/pawn-loans/status/{status}
     */
    @GetMapping("/status/{status}")
    public ResponseEntity<ApiResponse<Page<PawnLoan>>> getLoansByStatus(
            @PathVariable LoanStatus status,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        
        Pageable pageable = PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "createdAt"));
        Page<PawnLoan> loans = pawnLoanService.getLoansByStatus(status, pageable);
        return ResponseEntity.ok(ApiResponse.success(loans));
    }
    
    /**
     * Redeem a loan
     * POST /api/pawn-loans/{id}/redeem
     */
    @PostMapping("/{id}/redeem")
    public ResponseEntity<ApiResponse<PawnLoan>> redeemLoan(@PathVariable Long id) {
        PawnLoan redeemedLoan = pawnLoanService.redeemLoan(id);
        return ResponseEntity.ok(ApiResponse.success("Loan redeemed successfully", redeemedLoan));
    }
    
    /**
     * Mark loan as defaulted
     * POST /api/pawn-loans/{id}/default
     */
    @PostMapping("/{id}/default")
    public ResponseEntity<ApiResponse<PawnLoan>> markLoanAsDefaulted(@PathVariable Long id) {
        PawnLoan defaultedLoan = pawnLoanService.markLoanAsDefaulted(id);
        return ResponseEntity.ok(ApiResponse.success("Loan marked as defaulted", defaultedLoan));
    }
    
    /**
     * Calculate total payable amount
     * POST /api/pawn-loans/calculate-total
     */
    @PostMapping("/calculate-total")
    public ResponseEntity<ApiResponse<Double>> calculateTotalPayableAmount(
            @RequestParam Double principalAmount,
            @RequestParam Double interestRate) {
        
        // Note: In a real implementation, you would use BigDecimal
        Double totalPayable = principalAmount * (1 + interestRate / 100);
        return ResponseEntity.ok(ApiResponse.success(totalPayable));
    }
    
    /**
     * Get payment schedule for a loan
     * Lists 1st payment, 2nd payment, until last payment
     * GET /api/pawn-loans/{id}/payment-schedule
     */
    @GetMapping("/{id}/payment-schedule")
    public ResponseEntity<ApiResponse<List<PaymentScheduleItem>>> getPaymentSchedule(@PathVariable Long id) {
        PawnLoan loan = pawnLoanService.getLoanById(id);
        List<PaymentScheduleItem> schedule = pawnLoanService.generatePaymentSchedule(loan);
        return ResponseEntity.ok(ApiResponse.success("Payment schedule retrieved successfully", schedule));
    }
    
    /**
     * Check if loan is overdue
     * GET /api/pawn-loans/{id}/overdue
     */
    @GetMapping("/{id}/overdue")
    public ResponseEntity<ApiResponse<Boolean>> isLoanOverdue(@PathVariable Long id) {
        PawnLoan loan = pawnLoanService.getLoanById(id);
        boolean isOverdue = pawnLoanService.isLoanOverdue(loan);
        return ResponseEntity.ok(ApiResponse.success(isOverdue));
    }
    
    /**
     * Delete a pawn loan
     * DELETE /api/pawn-loans/{id}
     */
    @DeleteMapping("/{id}")
    public ResponseEntity<ApiResponse<Void>> deleteLoan(@PathVariable Long id) {
        pawnLoanService.deleteLoan(id);
        return ResponseEntity.ok(ApiResponse.success("Loan deleted successfully", null));
    }
    
    /**
     * Get loans with upcoming repayments for follow-up
     * GET /api/pawn-loans/upcoming-repayments
     */
    @GetMapping("/upcoming-repayments")
    public ResponseEntity<ApiResponse<List<PawnLoan>>> getLoansWithUpcomingRepayments(
            @RequestParam(defaultValue = "7") int daysAhead) {
        List<PawnLoan> loans = pawnLoanService.getLoansWithUpcomingRepayments(daysAhead);
        return ResponseEntity.ok(ApiResponse.success("Upcoming repayment loans retrieved successfully", loans));
    }
    
    /**
     * Get loans with upcoming repayments with pagination
     * GET /api/pawn-loans/upcoming-repayments/page
     */
    @GetMapping("/upcoming-repayments/page")
    public ResponseEntity<ApiResponse<Page<PawnLoan>>> getLoansWithUpcomingRepaymentsPage(
            @RequestParam(defaultValue = "7") int daysAhead,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "dueDate") String sortBy,
            @RequestParam(defaultValue = "asc") String direction) {
        
        Sort.Direction sortDirection = direction.equalsIgnoreCase("asc") ? Sort.Direction.ASC : Sort.Direction.DESC;
        Pageable pageable = PageRequest.of(page, size, Sort.by(sortDirection, sortBy));
        
        Page<PawnLoan> loans = pawnLoanService.getLoansWithUpcomingRepayments(daysAhead, pageable);
        return ResponseEntity.ok(ApiResponse.success("Upcoming repayment loans retrieved successfully", loans));
    }
    
    /**
     * Get loans needing follow-up (overdue or approaching due date)
     * GET /api/pawn-loans/needing-follow-up
     */
    @GetMapping("/needing-follow-up")
    public ResponseEntity<ApiResponse<List<PawnLoan>>> getLoansNeedingFollowUp() {
        List<PawnLoan> loans = pawnLoanService.getLoansNeedingFollowUp();
        return ResponseEntity.ok(ApiResponse.success("Loans needing follow-up retrieved successfully", loans));
    }
    
    /**
     * Get customer loans needing follow-up
     * GET /api/pawn-loans/customer/{customerId}/needing-follow-up
     */
    @GetMapping("/customer/{customerId}/needing-follow-up")
    public ResponseEntity<ApiResponse<List<PawnLoan>>> getCustomerLoansNeedingFollowUp(@PathVariable Long customerId) {
        List<PawnLoan> loans = pawnLoanService.getCustomerLoansNeedingFollowUp(customerId);
        return ResponseEntity.ok(ApiResponse.success("Customer loans needing follow-up retrieved successfully", loans));
    }
    
    /**
     * Get detailed upcoming repayment information with customer contact details
     * GET /api/pawn-loans/upcoming-repayments/detailed
     */
    @GetMapping("/upcoming-repayments/detailed")
    public ResponseEntity<ApiResponse<List<Map<String, Object>>>> getDetailedUpcomingRepayments(
            @RequestParam(defaultValue = "7") int daysAhead) {
        
        List<PawnLoan> loans = pawnLoanService.getLoansWithUpcomingRepayments(daysAhead);
        List<Map<String, Object>> detailedLoans = new ArrayList<>();
        
        for (PawnLoan loan : loans) {
            Map<String, Object> loanDetails = new HashMap<>();
            loanDetails.put("id", loan.getId());
            loanDetails.put("loanCode", loan.getLoanCode());
            loanDetails.put("customerName", loan.getCustomer().getFullName());
            loanDetails.put("customerPhone", loan.getCustomer().getPhone());
            loanDetails.put("loanAmount", loan.getLoanAmount());
            loanDetails.put("totalPayableAmount", loan.getTotalPayableAmount());
            loanDetails.put("dueDate", loan.getDueDate());
            loanDetails.put("daysUntilDue", pawnLoanService.calculateDaysUntilDue(loan.getDueDate()));
            loanDetails.put("overdueDays", pawnLoanService.calculateOverdueDays(loan.getDueDate()));
            loanDetails.put("followUpPriority", pawnLoanService.determineFollowUpPriority(loan.getDueDate()));
            loanDetails.put("status", loan.getStatus());
            
            // Add payment schedule for next payment
            List<PaymentScheduleItem> schedule = pawnLoanService.generatePaymentSchedule(loan);
            if (!schedule.isEmpty()) {
                PaymentScheduleItem nextPayment = schedule.stream()
                    .filter(item -> "PENDING".equals(item.getStatus()))
                    .findFirst()
                    .orElse(schedule.get(0));
                loanDetails.put("nextPaymentAmount", nextPayment.getAmountDue());
                loanDetails.put("nextPaymentDueDate", nextPayment.getDueDate());
            }
            
            detailedLoans.add(loanDetails);
        }
        
        return ResponseEntity.ok(ApiResponse.success("Detailed upcoming repayment information retrieved", detailedLoans));
    }
}