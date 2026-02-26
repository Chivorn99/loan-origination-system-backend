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
import com.example.loan_origination_system.dto.CurrencyPatchRequest;
import com.example.loan_origination_system.dto.CurrencyRequest;
import com.example.loan_origination_system.dto.CurrencyResponse;
import com.example.loan_origination_system.mapper.LoanMapper;
import com.example.loan_origination_system.model.master.Currency;
import com.example.loan_origination_system.service.CurrencyService;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/currencies")
@RequiredArgsConstructor
public class CurrencyController {

    private final CurrencyService currencyService;
    private final LoanMapper loanMapper;

    /**
     * Create currency
     */
    @PostMapping
    public ResponseEntity<ApiResponse<CurrencyResponse>> createCurrency(
            @Valid @RequestBody CurrencyRequest request) {

        Currency currency = currencyService.createCurrency(request);
        CurrencyResponse response = loanMapper.toCurrencyResponse(currency);

        return ResponseEntity
                .status(201)
                .body(ApiResponse.success("Currency created successfully", response));
    }

    /**
     * Get currency by ID
     */
    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<CurrencyResponse>> getCurrency(@PathVariable Long id) {
        Currency currency = currencyService.getCurrencyById(id);
        CurrencyResponse response = loanMapper.toCurrencyResponse(currency);
        return ResponseEntity.ok(
                ApiResponse.success(response)
        );
    }

    /**
     * Update currency (FULL UPDATE)
     */
    @PutMapping("/{id}")
    public ResponseEntity<ApiResponse<CurrencyResponse>> updateCurrency(
            @PathVariable Long id,
            @Valid @RequestBody CurrencyRequest request) {

        Currency updatedCurrency = currencyService.updateCurrency(id, request);
        CurrencyResponse response = loanMapper.toCurrencyResponse(updatedCurrency);

        return ResponseEntity.ok(
                ApiResponse.success("Currency updated successfully", response)
        );
    }

    /**
     * Partial update (PATCH)
     */
    @PatchMapping("/{id}")
    public ResponseEntity<ApiResponse<CurrencyResponse>> patchCurrency(
            @PathVariable Long id,
            @RequestBody CurrencyPatchRequest request) {

        Currency updatedCurrency = currencyService.patchCurrency(id, request);
        CurrencyResponse response = loanMapper.toCurrencyResponse(updatedCurrency);

        return ResponseEntity.ok(
                ApiResponse.success("Currency updated successfully", response)
        );
    }

    /**
     * Get currencies (ALL or FILTER BY STATUS)
     *
     * Examples:
     * GET /api/currencies
     * GET /api/currencies?status=ACTIVE
     */
    @GetMapping
    public ResponseEntity<ApiResponse<Page<CurrencyResponse>>> getCurrencies(
            @RequestParam(required = false) String status,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "code") String sortBy,
            @RequestParam(defaultValue = "asc") String direction) {

        Sort.Direction sortDirection =
                direction.equalsIgnoreCase("asc")
                        ? Sort.Direction.ASC
                        : Sort.Direction.DESC;

        Pageable pageable =
                PageRequest.of(page, size, Sort.by(sortDirection, sortBy));

        Page<Currency> currencies = (status == null)
                ? currencyService.getAllCurrencies(pageable)
                : currencyService.getCurrenciesByStatus(status, pageable);

        Page<CurrencyResponse> response = currencies.map(loanMapper::toCurrencyResponse);

        return ResponseEntity.ok(ApiResponse.success(response));
    }

    /**
     * Soft delete currency (set status to INACTIVE)
     */
    @DeleteMapping("/{id}")
    public ResponseEntity<ApiResponse<Void>> deleteCurrency(@PathVariable Long id) {

        currencyService.deleteCurrency(id);

        return ResponseEntity.ok(
                ApiResponse.success("Currency deleted successfully", null)
        );
    }

    /**
     * Check if currency exists by code
     */
    @GetMapping("/exists/{code}")
    public ResponseEntity<ApiResponse<Boolean>> checkCurrencyExists(@PathVariable String code) {
        return ResponseEntity.ok(
                ApiResponse.success(currencyService.existsByCode(code))
        );
    }
}