package com.example.loan_origination_system.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.example.loan_origination_system.model.enums.CollateralStatus;
import com.example.loan_origination_system.model.loan.PawnItem;

@Repository
public interface PawnItemRepository extends JpaRepository<PawnItem, Long> {
    
    List<PawnItem> findByCustomerId(Long customerId);
    
    Page<PawnItem> findByCustomerId(Long customerId, Pageable pageable);
    
    @Query("SELECT p FROM PawnItem p WHERE p.customer.id = :customerId AND p.status != 'DELETED'")
    List<PawnItem> findActiveByCustomerId(@Param("customerId") Long customerId);
    
    @Query("SELECT p FROM PawnItem p WHERE p.status = :status")
    Page<PawnItem> findByStatus(@Param("status") CollateralStatus status, Pageable pageable);
    
    @Query("SELECT COUNT(l) > 0 FROM PawnLoan l WHERE l.pawnItem.id = :pawnItemId AND l.status = 'ACTIVE'")
    boolean isLinkedToActiveLoan(@Param("pawnItemId") Long pawnItemId);
    
    @Query("SELECT p FROM PawnItem p WHERE p.status != 'DELETED'")
    Page<PawnItem> findAllActive(Pageable pageable);
    
    Optional<PawnItem> findByIdAndStatusNot(Long id, CollateralStatus status);
}