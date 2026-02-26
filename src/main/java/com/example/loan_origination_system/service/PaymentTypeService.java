package com.example.loan_origination_system.service;

import org.springframework.stereotype.Service;

import com.example.loan_origination_system.dto.PaymentTypePatchRequest;
import com.example.loan_origination_system.dto.PaymentTypeRequest;
import com.example.loan_origination_system.exception.BusinessException;
import com.example.loan_origination_system.mapper.LoanMapper;
import com.example.loan_origination_system.model.master.PaymentType;
import com.example.loan_origination_system.repository.PaymentTypeRepository;

import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class PaymentTypeService {

    private final PaymentTypeRepository paymentTypeRepository;
    private final LoanMapper loanMapper;

    /**
     * CREATE
     */
    @Transactional
    public PaymentType createPaymentType(PaymentTypeRequest request) {
        PaymentType paymentType = loanMapper.toPaymentType(request);
        return paymentTypeRepository.save(paymentType);
    }

    /**
     * GET by ID
     */
    public PaymentType getPaymentTypeById(Long id) {
        return paymentTypeRepository.findById(id)
                .orElseThrow(() -> new BusinessException(
                        "PAYMENT_TYPE_NOT_FOUND",
                        "Payment type not found with ID: " + id
                ));
    }

    /**
     * FULL UPDATE (PUT)
     */
    @Transactional
    public PaymentType updatePaymentType(Long id, PaymentTypeRequest request) {
        PaymentType paymentType = getPaymentTypeById(id);
        
        // Use mapper to update entity
        loanMapper.updatePaymentTypeFromRequest(request, paymentType);

        return paymentTypeRepository.save(paymentType);
    }

    /**
     * PARTIAL UPDATE (PATCH)
     */
    @Transactional
    public PaymentType patchPaymentType(Long id, PaymentTypePatchRequest patch) {
        PaymentType paymentType = getPaymentTypeById(id);

        if (patch.getCode() != null) {
            paymentType.setCode(patch.getCode());
        }

        if (patch.getName() != null) {
            paymentType.setName(patch.getName());
        }

        return paymentTypeRepository.save(paymentType);
    }

    /**
     * DELETE
     */
    @Transactional
    public void deletePaymentType(Long id) {
        PaymentType paymentType = getPaymentTypeById(id);
        paymentTypeRepository.delete(paymentType);
    }
}