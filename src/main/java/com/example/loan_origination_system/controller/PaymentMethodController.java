package com.example.loan_origination_system.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.example.loan_origination_system.dto.ApiResponse;
import com.example.loan_origination_system.dto.PaymentMethodPatchRequest;
import com.example.loan_origination_system.dto.PaymentMethodRequest;
import com.example.loan_origination_system.dto.PaymentMethodResponse;
import com.example.loan_origination_system.mapper.LoanMapper;
import com.example.loan_origination_system.model.master.PaymentMethod;
import com.example.loan_origination_system.service.PaymentMethodService;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/payment-methods")
@RequiredArgsConstructor
public class PaymentMethodController {

    private final PaymentMethodService paymentMethodService;
    private final LoanMapper loanMapper;

    @PostMapping
    public ResponseEntity<ApiResponse<PaymentMethodResponse>> createPaymentMethod(
            @Valid @RequestBody PaymentMethodRequest request) {
        PaymentMethod paymentMethod = paymentMethodService.createPaymentMethod(request);
        PaymentMethodResponse response = loanMapper.toPaymentMethodResponse(paymentMethod);
        return ResponseEntity.status(201)
                .body(ApiResponse.success("Payment method created successfully", response));
    }

    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<PaymentMethodResponse>> getPaymentMethod(@PathVariable Long id) {
        PaymentMethod paymentMethod = paymentMethodService.getPaymentMethodById(id);
        PaymentMethodResponse response = loanMapper.toPaymentMethodResponse(paymentMethod);
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    @PutMapping("/{id}")
    public ResponseEntity<ApiResponse<PaymentMethodResponse>> updatePaymentMethod(
            @PathVariable Long id,
            @Valid @RequestBody PaymentMethodRequest request) {
        PaymentMethod updatedPaymentMethod = paymentMethodService.updatePaymentMethod(id, request);
        PaymentMethodResponse response = loanMapper.toPaymentMethodResponse(updatedPaymentMethod);
        return ResponseEntity.ok(ApiResponse.success("Payment method updated successfully", response));
    }

    @PatchMapping("/{id}")
    public ResponseEntity<ApiResponse<PaymentMethodResponse>> patchPaymentMethod(
            @PathVariable Long id,
            @RequestBody PaymentMethodPatchRequest request) {
        PaymentMethod updatedPaymentMethod = paymentMethodService.patchPaymentMethod(id, request);
        PaymentMethodResponse response = loanMapper.toPaymentMethodResponse(updatedPaymentMethod);
        return ResponseEntity.ok(ApiResponse.success("Payment method updated successfully", response));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<ApiResponse<Void>> deletePaymentMethod(@PathVariable Long id) {
        paymentMethodService.deletePaymentMethod(id);
        return ResponseEntity.ok(ApiResponse.success("Payment method deleted successfully", null));
    }
}