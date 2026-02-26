package com.example.loan_origination_system.service;

import org.springframework.stereotype.Service;

import com.example.loan_origination_system.dto.PaymentMethodPatchRequest;
import com.example.loan_origination_system.dto.PaymentMethodRequest;
import com.example.loan_origination_system.exception.BusinessException;
import com.example.loan_origination_system.mapper.LoanMapper;
import com.example.loan_origination_system.model.master.PaymentMethod;
import com.example.loan_origination_system.repository.PaymentMethodRepository;

import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class PaymentMethodService {

    private final PaymentMethodRepository paymentMethodRepository;
    private final LoanMapper loanMapper;

    /**
     * CREATE
     */
    @Transactional
    public PaymentMethod createPaymentMethod(PaymentMethodRequest request) {
        // Check if code already exists
        if (paymentMethodRepository.existsByCode(request.getCode())) {
            throw new BusinessException(
                    "PAYMENT_METHOD_CODE_DUPLICATE",
                    "Payment method with this code already exists"
            );
        }

        PaymentMethod paymentMethod = loanMapper.toPaymentMethod(request);
        
        // Set default values
        if (paymentMethod.getStatus() == null) {
            paymentMethod.setStatus("ACTIVE");
        }

        return paymentMethodRepository.save(paymentMethod);
    }

    /**
     * GET by ID
     */
    public PaymentMethod getPaymentMethodById(Long id) {
        return paymentMethodRepository.findById(id)
                .orElseThrow(() -> new BusinessException(
                        "PAYMENT_METHOD_NOT_FOUND",
                        "Payment method not found with ID: " + id
                ));
    }

    /**
     * FULL UPDATE (PUT)
     */
    @Transactional
    public PaymentMethod updatePaymentMethod(Long id, PaymentMethodRequest request) {
        PaymentMethod paymentMethod = getPaymentMethodById(id);

        // Validate code if provided and changed
        if (request.getCode() != null && !request.getCode().equals(paymentMethod.getCode())) {
            // Check if new code already exists (excluding current record)
            if (paymentMethodRepository.existsByCode(request.getCode())) {
                throw new BusinessException(
                        "PAYMENT_METHOD_CODE_DUPLICATE",
                        "Payment method with this code already exists"
                );
            }
        }

        // Use mapper to update entity
        loanMapper.updatePaymentMethodFromRequest(request, paymentMethod);

        return paymentMethodRepository.save(paymentMethod);
    }

    /**
     * PARTIAL UPDATE (PATCH)
     */
    @Transactional
    public PaymentMethod patchPaymentMethod(Long id, PaymentMethodPatchRequest patch) {
        PaymentMethod paymentMethod = getPaymentMethodById(id);

        if (patch.getCode() != null && !patch.getCode().equals(paymentMethod.getCode())) {
            // Check if new code already exists
            if (paymentMethodRepository.existsByCode(patch.getCode())) {
                throw new BusinessException(
                        "PAYMENT_METHOD_CODE_DUPLICATE",
                        "Payment method with this code already exists"
                );
            }
            paymentMethod.setCode(patch.getCode());
        }

        if (patch.getName() != null) {
            paymentMethod.setName(patch.getName());
        }

        if (patch.getStatus() != null) {
            paymentMethod.setStatus(patch.getStatus());
        }

        return paymentMethodRepository.save(paymentMethod);
    }

    /**
     * DELETE
     */
    @Transactional
    public void deletePaymentMethod(Long id) {
        PaymentMethod paymentMethod = getPaymentMethodById(id);
        paymentMethodRepository.delete(paymentMethod);
    }
}