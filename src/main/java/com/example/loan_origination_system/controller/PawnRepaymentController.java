package com.example.loan_origination_system.controller;

import java.time.LocalDate;
import java.util.List;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.format.annotation.DateTimeFormat;
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
import com.example.loan_origination_system.dto.PawnRepaymentRequest;
import com.example.loan_origination_system.model.loan.PawnRepayment;
import com.example.loan_origination_system.service.PawnRepaymentService;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/pawn-repayments")
@RequiredArgsConstructor
public class PawnRepaymentController {
    
    private final PawnRepaymentService pawnRepaymentService;
    
    /**
     * Create a new repayment
     * POST /api/pawn-repayments
     */
    @PostMapping
    public ResponseEntity<ApiResponse<PawnRepayment>> createRepayment(@Valid @RequestBody PawnRepaymentRequest request) {
        try {
            PawnRepayment repayment = pawnRepaymentService.createRepayment(request);
            return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success("Repayment created successfully", repayment));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest()
                .body(ApiResponse.error("Invalid payment amounts: " + e.getMessage()));
        }
    }
    
    /**
     * Get repayment by ID
     * GET /api/pawn-repayments/{id}
     */
    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<PawnRepayment>> getRepayment(@PathVariable Long id) {
        PawnRepayment repayment = pawnRepaymentService.getRepaymentById(id);
        return ResponseEntity.ok(ApiResponse.success("Repayment retrieved successfully", repayment));
    }
    
    /**
     * Get repayment history for a loan
     * GET /api/pawn-repayments/loan/{loanId}
     */
    @GetMapping("/loan/{loanId}")
    public ResponseEntity<ApiResponse<List<PawnRepayment>>> getRepaymentHistory(@PathVariable Long loanId) {
        List<PawnRepayment> repayments = pawnRepaymentService.getRepaymentHistory(loanId);
        return ResponseEntity.ok(ApiResponse.success("Repayment history retrieved successfully", repayments));
    }
    
    /**
     * Get repayment history for a loan with pagination
     * GET /api/pawn-repayments/loan/{loanId}/page
     */
    @GetMapping("/loan/{loanId}/page")
    public ResponseEntity<ApiResponse<Page<PawnRepayment>>> getRepaymentHistoryPaginated(
            @PathVariable Long loanId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "paymentDate") String sortBy,
            @RequestParam(defaultValue = "DESC") String direction) {
        
        Sort.Direction sortDirection = direction.equalsIgnoreCase("ASC") ? Sort.Direction.ASC : Sort.Direction.DESC;
        Pageable pageable = PageRequest.of(page, size, Sort.by(sortDirection, sortBy));
        
        Page<PawnRepayment> repayments = pawnRepaymentService.getRepaymentHistory(loanId, pageable);
        return ResponseEntity.ok(ApiResponse.success("Repayment history retrieved successfully", repayments));
    }
    
    /**
     * Calculate repayment schedule for a loan
     * GET /api/pawn-repayments/loan/{loanId}/schedule
     */
    @GetMapping("/loan/{loanId}/schedule")
    public ResponseEntity<ApiResponse<PawnRepaymentService.RepaymentSchedule>> getRepaymentSchedule(@PathVariable Long loanId) {
        PawnRepaymentService.RepaymentSchedule schedule = pawnRepaymentService.calculateRepaymentSchedule(loanId);
        return ResponseEntity.ok(ApiResponse.success("Repayment schedule calculated successfully", schedule));
    }
    
    /**
     * Get total paid amount for a loan
     * GET /api/pawn-repayments/loan/{loanId}/total-paid
     */
    @GetMapping("/loan/{loanId}/total-paid")
    public ResponseEntity<ApiResponse<Double>> getTotalPaidAmount(@PathVariable Long loanId) {
        Double totalPaid = pawnRepaymentService.getTotalPaidAmountByLoanId(loanId).doubleValue();
        return ResponseEntity.ok(ApiResponse.success("Total paid amount retrieved successfully", totalPaid));
    }
    
    /**
     * Get repayments by date range
     * GET /api/pawn-repayments/by-date
     */
    @GetMapping("/by-date")
    public ResponseEntity<ApiResponse<Page<PawnRepayment>>> getRepaymentsByDateRange(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        
        Pageable pageable = PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "paymentDate"));
        Page<PawnRepayment> repayments = pawnRepaymentService.getRepaymentsByDateRange(startDate, endDate, pageable);
        
        return ResponseEntity.ok(ApiResponse.success("Repayments retrieved successfully", repayments));
    }
    
    /**
     * Get daily collection report for a branch
     * GET /api/pawn-repayments/daily-collection/{branchId}
     */
    @GetMapping("/daily-collection/{branchId}")
    public ResponseEntity<ApiResponse<PawnRepaymentService.DailyCollectionReport>> getDailyCollectionReport(
            @PathVariable Long branchId,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date) {
        
        if (date == null) {
            date = LocalDate.now();
        }
        
        PawnRepaymentService.DailyCollectionReport report = pawnRepaymentService.getDailyCollectionReport(branchId, date);
        return ResponseEntity.ok(ApiResponse.success("Daily collection report retrieved successfully", report));
    }
    
    /**
     * Get today's repayments for a branch
     * GET /api/pawn-repayments/today/{branchId}
     */
    @GetMapping("/today/{branchId}")
    public ResponseEntity<ApiResponse<List<PawnRepayment>>> getTodayRepayments(@PathVariable Long branchId) {
        LocalDate today = LocalDate.now();
        // This would need a repository method to get today's repayments by branch
        // For now, we'll use the date range method
        Pageable pageable = PageRequest.of(0, 100, Sort.by(Sort.Direction.DESC, "paymentDate"));
        Page<PawnRepayment> repaymentsPage = pawnRepaymentService.getRepaymentsByDateRange(today, today, pageable);
        
        return ResponseEntity.ok(ApiResponse.success("Today's repayments retrieved successfully", repaymentsPage.getContent()));
    }
    
    /**
     * Get customer repayment logs for N months
     * GET /api/pawn-repayments/customer/{customerId}/months/{months}
     */
    @GetMapping("/customer/{customerId}/months/{months}")
    public ResponseEntity<ApiResponse<Page<PawnRepayment>>> getCustomerRepaymentLogs(
            @PathVariable Long customerId,
            @PathVariable int months,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size,
            @RequestParam(defaultValue = "paymentDate") String sortBy,
            @RequestParam(defaultValue = "DESC") String direction) {
        
        Sort.Direction sortDirection = direction.equalsIgnoreCase("ASC") ? Sort.Direction.ASC : Sort.Direction.DESC;
        Pageable pageable = PageRequest.of(page, size, Sort.by(sortDirection, sortBy));
        
        Page<PawnRepayment> repayments = pawnRepaymentService.getCustomerRepaymentsByMonthRange(customerId, months, pageable);
        return ResponseEntity.ok(ApiResponse.success("Customer repayment logs retrieved successfully", repayments));
    }
    
    /**
     * Get customer repayment summary for N months
     * GET /api/pawn-repayments/customer/{customerId}/summary/{months}
     */
    @GetMapping("/customer/{customerId}/summary/{months}")
    public ResponseEntity<ApiResponse<PawnRepaymentService.CustomerRepaymentSummary>> getCustomerRepaymentSummary(
            @PathVariable Long customerId,
            @PathVariable int months) {
        
        PawnRepaymentService.CustomerRepaymentSummary summary =
            pawnRepaymentService.getCustomerRepaymentSummary(customerId, months);
        return ResponseEntity.ok(ApiResponse.success("Customer repayment summary retrieved successfully", summary));
    }
    
    /**
     * Get overdue loans with repayment status
     * GET /api/pawn-repayments/overdue
     */
    @GetMapping("/overdue")
    public ResponseEntity<ApiResponse<List<PawnRepaymentService.RepaymentSchedule>>> getOverdueLoans() {
        // This would need additional service method to get all overdue loans
        // For now, return empty list as placeholder
        return ResponseEntity.ok(ApiResponse.success("Overdue loans retrieved successfully", List.of()));
    }
}