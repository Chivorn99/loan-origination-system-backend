package com.example.loan_origination_system.controller;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.example.loan_origination_system.dto.ApiResponse;
import com.example.loan_origination_system.dto.CfgLoanPatchRequest;
import com.example.loan_origination_system.dto.CfgLoanRequest;
import com.example.loan_origination_system.dto.CfgLoanResponse;
import com.example.loan_origination_system.mapper.LoanMapper;
import com.example.loan_origination_system.model.loan.CfgLoan;
import com.example.loan_origination_system.service.CfgLoanService;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/cfg-loans")
@RequiredArgsConstructor
public class CfgLoanController {

    private final CfgLoanService cfgLoanService;
    private final LoanMapper loanMapper;

    /**
     * Create loan configuration
     */
    @PostMapping
    public ResponseEntity<ApiResponse<CfgLoanResponse>> createCfgLoan(
            @Valid @RequestBody CfgLoanRequest request) {

        CfgLoan cfgLoan = cfgLoanService.createCfgLoan(request);
        CfgLoanResponse response = loanMapper.toCfgLoanResponse(cfgLoan);

        return ResponseEntity
                .status(201)
                .body(ApiResponse.success("Loan configuration created successfully", response));
    }

    /**
     * Get loan configuration by ID
     */
    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<CfgLoanResponse>> getCfgLoan(@PathVariable Long id) {
        CfgLoan cfgLoan = cfgLoanService.getCfgLoanById(id);
        CfgLoanResponse response = loanMapper.toCfgLoanResponse(cfgLoan);
        return ResponseEntity.ok(
                ApiResponse.success(response)
        );
    }

    /**
     * Update loan configuration (FULL UPDATE)
     */
    @PutMapping("/{id}")
    public ResponseEntity<ApiResponse<CfgLoanResponse>> updateCfgLoan(
            @PathVariable Long id,
            @Valid @RequestBody CfgLoanRequest request) {

        CfgLoan updatedCfgLoan = cfgLoanService.updateCfgLoan(id, request);
        CfgLoanResponse response = loanMapper.toCfgLoanResponse(updatedCfgLoan);

        return ResponseEntity.ok(
                ApiResponse.success("Loan configuration updated successfully", response)
        );
    }

    /**
     * Partial update (PATCH)
     */
    @PatchMapping("/{id}")
    public ResponseEntity<ApiResponse<CfgLoanResponse>> patchCfgLoan(
            @PathVariable Long id,
            @RequestBody CfgLoanPatchRequest request) {

        CfgLoan updatedCfgLoan = cfgLoanService.patchCfgLoan(id, request);
        CfgLoanResponse response = loanMapper.toCfgLoanResponse(updatedCfgLoan);

        return ResponseEntity.ok(
                ApiResponse.success("Loan configuration updated successfully", response)
        );
    }

    /**
     * Get loan configurations with pagination
     */
    @GetMapping
    public ResponseEntity<ApiResponse<Page<CfgLoanResponse>>> getCfgLoans(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "id") String sortBy,
            @RequestParam(defaultValue = "asc") String direction) {

        Sort.Direction sortDirection = direction.equalsIgnoreCase("desc") 
                ? Sort.Direction.DESC : Sort.Direction.ASC;
        Pageable pageable = PageRequest.of(page, size, Sort.by(sortDirection, sortBy));

        Page<CfgLoan> cfgLoans = cfgLoanService.getCfgLoans(pageable);
        Page<CfgLoanResponse> response = cfgLoans.map(loanMapper::toCfgLoanResponse);

        return ResponseEntity.ok(
                ApiResponse.success(response)
        );
    }

    /**
     * Delete loan configuration
     */
    @DeleteMapping("/{id}")
    public ResponseEntity<ApiResponse<Void>> deleteCfgLoan(@PathVariable Long id) {
        cfgLoanService.deleteCfgLoan(id);
        return ResponseEntity.ok(
                ApiResponse.success("Loan configuration deleted successfully", null)
        );
    }
}