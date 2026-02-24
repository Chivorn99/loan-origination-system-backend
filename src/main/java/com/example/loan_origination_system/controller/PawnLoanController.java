package com.example.loan_origination_system.controller;

import java.util.List;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.example.loan_origination_system.dto.ApiResponse;
import com.example.loan_origination_system.dto.PawnLoanRequest;
import com.example.loan_origination_system.model.enums.LoanStatus;
import com.example.loan_origination_system.model.loan.PawnLoan;
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
     * Check if loan is overdue
     * GET /api/pawn-loans/{id}/overdue
     */
    @GetMapping("/{id}/overdue")
    public ResponseEntity<ApiResponse<Boolean>> isLoanOverdue(@PathVariable Long id) {
        PawnLoan loan = pawnLoanService.getLoanById(id);
        boolean isOverdue = pawnLoanService.isLoanOverdue(loan);
        return ResponseEntity.ok(ApiResponse.success(isOverdue));
    }
}