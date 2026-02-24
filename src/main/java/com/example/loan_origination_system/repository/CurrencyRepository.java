package com.example.loan_origination_system.repository;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.example.loan_origination_system.model.master.Currency;

@Repository
public interface CurrencyRepository extends JpaRepository<Currency, Long> {
    
    boolean existsByCode(String code);
    
    @Query("SELECT COUNT(c) > 0 FROM Currency c WHERE c.code = :code AND c.id != :excludeId")
    boolean existsByCodeAndIdNot(@Param("code") String code, @Param("excludeId") Long excludeId);
    
    @Query("SELECT c FROM Currency c WHERE c.status = :status")
    Page<Currency> findByStatus(@Param("status") String status, Pageable pageable);
}