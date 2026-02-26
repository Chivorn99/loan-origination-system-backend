package com.example.loan_origination_system.repository;

import com.example.loan_origination_system.model.enums.LoanStatus;
import com.example.loan_origination_system.model.loan.PawnLoan;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@Repository
public interface PawnLoanRepository extends JpaRepository<PawnLoan, Long> {
    
    Optional<PawnLoan> findByLoanCode(String loanCode);
    
    List<PawnLoan> findByCustomerId(Long customerId);
    
    Page<PawnLoan> findByCustomerId(Long customerId, Pageable pageable);
    
    Page<PawnLoan> findByStatus(LoanStatus status, Pageable pageable);
    
    @Query("SELECT l FROM PawnLoan l WHERE l.status = 'ACTIVE' AND l.dueDate < :currentDate")
    List<PawnLoan> findOverdueLoans(@Param("currentDate") LocalDate currentDate);
    
    @Query("SELECT l FROM PawnLoan l WHERE (l.status = 'ACTIVE' OR l.status = 'PARTIALLY_PAID') " +
           "AND l.dueDate <= :currentDate AND l.dueDate IS NOT NULL")
    List<PawnLoan> findLoansDueByDate(@Param("currentDate") LocalDate currentDate);
    
    @Query("SELECT l FROM PawnLoan l WHERE l.status = 'OVERDUE' " +
           "AND l.gracePeriodEndDate <= :currentDate AND l.gracePeriodEndDate IS NOT NULL")
    List<PawnLoan> findOverdueLoansWithExpiredGracePeriod(@Param("currentDate") LocalDate currentDate);
    
    @Query("SELECT l FROM PawnLoan l WHERE l.status = :status")
    List<PawnLoan> findByStatus(@Param("status") LoanStatus status);
    
    @Query("SELECT l FROM PawnLoan l WHERE l.status = 'DEFAULTED' " +
           "AND l.defaultedAt >= :startDate AND l.defaultedAt <= :endDate")
    List<PawnLoan> findDefaultedLoansInPeriod(@Param("startDate") LocalDate startDate,
                                              @Param("endDate") LocalDate endDate);
    
    @Query("SELECT l FROM PawnLoan l WHERE l.status != 'CANCELLED'")
    Page<PawnLoan> findAllActive(Pageable pageable);
    
    @Query("SELECT COUNT(l) > 0 FROM PawnLoan l WHERE l.customer.id = :customerId AND l.status = 'ACTIVE'")
    boolean existsActiveLoanByCustomerId(@Param("customerId") Long customerId);
    
    @Query("SELECT COUNT(l) > 0 FROM PawnLoan l WHERE l.pawnItem.id = :pawnItemId AND l.status = 'ACTIVE'")
    boolean existsActiveLoanByPawnItemId(@Param("pawnItemId") Long pawnItemId);
    
    @Query("SELECT l FROM PawnLoan l WHERE l.pawnItem.id = :pawnItemId AND l.status = 'ACTIVE'")
    Optional<PawnLoan> findActiveLoanByPawnItemId(@Param("pawnItemId") Long pawnItemId);
}