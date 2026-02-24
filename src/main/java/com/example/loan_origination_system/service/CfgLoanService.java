package com.example.loan_origination_system.service;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import com.example.loan_origination_system.dto.CfgLoanPatchRequest;
import com.example.loan_origination_system.dto.CfgLoanRequest;
import com.example.loan_origination_system.exception.BusinessException;
import com.example.loan_origination_system.mapper.LoanMapper;
import com.example.loan_origination_system.model.loan.CfgLoan;
import com.example.loan_origination_system.model.master.Branch;
import com.example.loan_origination_system.model.master.Currency;
import com.example.loan_origination_system.repository.BranchRepository;
import com.example.loan_origination_system.repository.CfgLoanRepository;
import com.example.loan_origination_system.repository.CurrencyRepository;

import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class CfgLoanService {

    private final CfgLoanRepository cfgLoanRepository;
    private final BranchRepository branchRepository;
    private final CurrencyRepository currencyRepository;
    private final LoanMapper loanMapper;

    /**
     * CREATE
     */
    @Transactional
    public CfgLoan createCfgLoan(CfgLoanRequest request) {
        // Validate branch exists
        Branch branch = branchRepository.findById(request.getBranchId())
                .orElseThrow(() -> new BusinessException(
                        "BRANCH_NOT_FOUND",
                        "Branch not found with ID: " + request.getBranchId()
                ));

        // Validate currency exists
        Currency currency = currencyRepository.findById(request.getCurrencyId())
                .orElseThrow(() -> new BusinessException(
                        "CURRENCY_NOT_FOUND",
                        "Currency not found with ID: " + request.getCurrencyId()
                ));

        // Check for duplicate active configuration
        if (request.getInterestRate() != null) {
            cfgLoanRepository.findByBranchAndCurrencyAndInterestRate(
                    branch.getId(), currency.getId(), request.getInterestRate().doubleValue()
            ).ifPresent(existing -> {
                throw new BusinessException(
                        "CFG_LOAN_DUPLICATE",
                        "Active loan configuration already exists for branch " + branch.getName() +
                        ", currency " + currency.getCode() + " with interest rate " + request.getInterestRate()
                );
            });
        }

        CfgLoan cfgLoan = loanMapper.toCfgLoan(request);
        cfgLoan.setBranch(branch);
        cfgLoan.setCurrency(currency);

        // Set default values
        if (cfgLoan.getStatus() == null) {
            cfgLoan.setStatus("ACTIVE");
        }

        return cfgLoanRepository.save(cfgLoan);
    }

    /**
     * GET by ID
     */
    public CfgLoan getCfgLoanById(Long id) {
        return cfgLoanRepository.findById(id)
                .orElseThrow(() -> new BusinessException(
                        "CFG_LOAN_NOT_FOUND",
                        "Loan configuration not found with ID: " + id
                ));
    }

    /**
     * FULL UPDATE (PUT)
     */
    @Transactional
    public CfgLoan updateCfgLoan(Long id, CfgLoanRequest request) {
        CfgLoan cfgLoan = getCfgLoanById(id);

        Long branchId = request.getBranchId() != null ? request.getBranchId() : cfgLoan.getBranch().getId();
        Long currencyId = request.getCurrencyId() != null ? request.getCurrencyId() : cfgLoan.getCurrency().getId();
        
        // Validate branch if provided
        Branch branch = null;
        if (request.getBranchId() != null) {
            branch = branchRepository.findById(request.getBranchId())
                    .orElseThrow(() -> new BusinessException(
                            "BRANCH_NOT_FOUND",
                            "Branch not found with ID: " + request.getBranchId()
                    ));
            cfgLoan.setBranch(branch);
        } else {
            branch = cfgLoan.getBranch();
        }

        // Validate currency if provided
        Currency currency = null;
        if (request.getCurrencyId() != null) {
            currency = currencyRepository.findById(request.getCurrencyId())
                    .orElseThrow(() -> new BusinessException(
                            "CURRENCY_NOT_FOUND",
                            "Currency not found with ID: " + request.getCurrencyId()
                    ));
            cfgLoan.setCurrency(currency);
        } else {
            currency = cfgLoan.getCurrency();
        }

        // Check for duplicate if interest rate is being changed
        if (request.getInterestRate() != null &&
            !request.getInterestRate().equals(cfgLoan.getInterestRate())) {
            
            // Check if another active configuration exists with same branch, currency, and new interest rate
            if (cfgLoanRepository.existsByBranchIdAndCurrencyIdAndInterestRateAndIdNot(
                    branchId, currencyId, request.getInterestRate().doubleValue(), id)) {
                throw new BusinessException(
                        "CFG_LOAN_DUPLICATE",
                        "Active loan configuration already exists for branch " + branch.getName() +
                        ", currency " + currency.getCode() + " with interest rate " + request.getInterestRate()
                );
            }
        }

        // Use mapper to update entity
        loanMapper.updateCfgLoanFromRequest(request, cfgLoan);

        return cfgLoanRepository.save(cfgLoan);
    }

    /**
     * PARTIAL UPDATE (PATCH)
     */
    @Transactional
    public CfgLoan patchCfgLoan(Long id, CfgLoanPatchRequest patch) {
        CfgLoan cfgLoan = getCfgLoanById(id);

        if (patch.getBranchId() != null) {
            Branch branch = branchRepository.findById(patch.getBranchId())
                    .orElseThrow(() -> new BusinessException(
                            "BRANCH_NOT_FOUND",
                            "Branch not found with ID: " + patch.getBranchId()
                    ));
            cfgLoan.setBranch(branch);
        }

        if (patch.getCurrencyId() != null) {
            Currency currency = currencyRepository.findById(patch.getCurrencyId())
                    .orElseThrow(() -> new BusinessException(
                            "CURRENCY_NOT_FOUND",
                            "Currency not found with ID: " + patch.getCurrencyId()
                    ));
            cfgLoan.setCurrency(currency);
        }

        if (patch.getMinLoanAmount() != null) {
            cfgLoan.setMinLoanAmount(patch.getMinLoanAmount());
        }

        if (patch.getMaxLoanAmount() != null) {
            cfgLoan.setMaxLoanAmount(patch.getMaxLoanAmount());
        }

        if (patch.getInterestRate() != null) {
            cfgLoan.setInterestRate(patch.getInterestRate());
        }

        if (patch.getInterestType() != null) {
            cfgLoan.setInterestType(patch.getInterestType());
        }

        if (patch.getInterestPeriod() != null) {
            cfgLoan.setInterestPeriod(patch.getInterestPeriod());
        }

        if (patch.getPenaltyRate() != null) {
            cfgLoan.setPenaltyRate(patch.getPenaltyRate());
        }

        if (patch.getPenaltyGraceDays() != null) {
            cfgLoan.setPenaltyGraceDays(patch.getPenaltyGraceDays());
        }

        if (patch.getMaxLoanDuration() != null) {
            cfgLoan.setMaxLoanDuration(patch.getMaxLoanDuration());
        }

        if (patch.getAutoForfeitDays() != null) {
            cfgLoan.setAutoForfeitDays(patch.getAutoForfeitDays());
        }

        if (patch.getStatus() != null) {
            cfgLoan.setStatus(patch.getStatus());
        }

        if (patch.getEffectiveFrom() != null) {
            cfgLoan.setEffectiveFrom(patch.getEffectiveFrom());
        }

        if (patch.getEffectiveTo() != null) {
            cfgLoan.setEffectiveTo(patch.getEffectiveTo());
        }

        return cfgLoanRepository.save(cfgLoan);
    }

    /**
     * GET all with pagination
     */
    public Page<CfgLoan> getCfgLoans(Pageable pageable) {
        return cfgLoanRepository.findAll(pageable);
    }

    /**
     * DELETE
     */
    @Transactional
    public void deleteCfgLoan(Long id) {
        CfgLoan cfgLoan = getCfgLoanById(id);
        cfgLoanRepository.delete(cfgLoan);
    }
}