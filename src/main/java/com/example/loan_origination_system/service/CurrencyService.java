package com.example.loan_origination_system.service;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import com.example.loan_origination_system.dto.CurrencyPatchRequest;
import com.example.loan_origination_system.dto.CurrencyRequest;
import com.example.loan_origination_system.exception.BusinessException;
import com.example.loan_origination_system.mapper.LoanMapper;
import com.example.loan_origination_system.model.master.Currency;
import com.example.loan_origination_system.repository.CurrencyRepository;

import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class CurrencyService {

    private final CurrencyRepository currencyRepository;
    private final LoanMapper loanMapper;

    /**
     * CREATE
     */
    @Transactional
    public Currency createCurrency(CurrencyRequest request) {

        if (currencyRepository.existsByCode(request.getCode())) {
            throw new BusinessException(
                    "CURRENCY_CODE_DUPLICATE",
                    "Currency with this code already exists"
            );
        }

        Currency currency = loanMapper.toCurrency(request);
        // Set default values if not provided in request
        if (currency.getDecimalPlace() == null) {
            currency.setDecimalPlace(2);
        }
        if (currency.getStatus() == null) {
            currency.setStatus("ACTIVE");
        }

        return currencyRepository.save(currency);
    }

    /**
     * FULL UPDATE (PUT)
     */
    @Transactional
    public Currency updateCurrency(Long id, CurrencyRequest request) {

        Currency currency = getCurrencyById(id);

        // Validate currency code if provided and changed
        if (request.getCode() != null && !request.getCode().equals(currency.getCode())) {
            if (currencyRepository.existsByCodeAndIdNot(request.getCode(), id)) {
                throw new BusinessException(
                        "CURRENCY_CODE_DUPLICATE",
                        "Currency with this code already exists"
                );
            }
        }

        // Use mapper to update entity
        loanMapper.updateCurrencyFromRequest(request, currency);

        return currencyRepository.save(currency);
    }

    /**
     * PARTIAL UPDATE (PATCH)
     */
    @Transactional
    public Currency patchCurrency(Long id, CurrencyPatchRequest patch) {

        Currency currency = getCurrencyById(id);

        if (patch.getCode() != null && !patch.getCode().equals(currency.getCode())) {
            if (currencyRepository.existsByCodeAndIdNot(patch.getCode(), id)) {
                throw new BusinessException(
                        "CURRENCY_CODE_DUPLICATE",
                        "Currency with this code already exists"
                );
            }
            currency.setCode(patch.getCode());
        }

        if (patch.getName() != null) {
            currency.setName(patch.getName());
        }

        if (patch.getSymbol() != null) {
            currency.setSymbol(patch.getSymbol());
        }

        if (patch.getDecimalPlace() != null) {
            currency.setDecimalPlace(patch.getDecimalPlace());
        }

        if (patch.getStatus() != null) {
            currency.setStatus(patch.getStatus());
        }

        return currencyRepository.save(currency);
    }

    /**
     * READ METHODS
     */
    public Currency getCurrencyById(Long id) {
        return currencyRepository.findById(id)
                .orElseThrow(() -> new BusinessException(
                        "CURRENCY_NOT_FOUND",
                        "Currency with ID " + id + " not found"
                ));
    }

    public Page<Currency> getAllCurrencies(Pageable pageable) {
        return currencyRepository.findAll(pageable);
    }

    public Page<Currency> getCurrenciesByStatus(String status, Pageable pageable) {
        return currencyRepository.findByStatus(status, pageable);
    }

    /**
     * SOFT DELETE (update status to INACTIVE)
     */
    @Transactional
    public void deleteCurrency(Long id) {
        Currency currency = getCurrencyById(id);
        currency.setStatus("INACTIVE");
        currencyRepository.save(currency);
    }

    /**
     * Check if currency exists by code
     */
    public boolean existsByCode(String code) {
        return currencyRepository.existsByCode(code);
    }
}