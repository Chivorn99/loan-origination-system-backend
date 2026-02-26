package com.example.loan_origination_system.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.example.loan_origination_system.model.loan.CfgLoan;

@Repository
public interface CfgLoanRepository extends JpaRepository<CfgLoan, Long> {
    
    @Query("SELECT c FROM CfgLoan c WHERE c.branch.id = :branchId AND c.currency.id = :currencyId AND c.status = 'ACTIVE'")
    List<CfgLoan> findActiveByBranchAndCurrency(@Param("branchId") Long branchId, @Param("currencyId") Long currencyId);
    
    @Query("SELECT c FROM CfgLoan c WHERE c.branch.id = :branchId AND c.currency.id = :currencyId " +
           "AND c.interestRate = :interestRate AND c.status = 'ACTIVE'")
    Optional<CfgLoan> findByBranchAndCurrencyAndInterestRate(
            @Param("branchId") Long branchId,
            @Param("currencyId") Long currencyId,
            @Param("interestRate") Double interestRate);
    
    boolean existsByBranchIdAndCurrencyIdAndInterestRateAndIdNot(
            Long branchId, Long currencyId, Double interestRate, Long id);
}