package com.example.loan_origination_system.repository;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.example.loan_origination_system.model.loan.PawnRepayment;

@Repository
public interface PawnRepaymentRepository extends JpaRepository<PawnRepayment, Long> {
    
    List<PawnRepayment> findByPawnLoanId(Long pawnLoanId);
    
    Page<PawnRepayment> findByPawnLoanId(Long pawnLoanId, Pageable pageable);
    
    List<PawnRepayment> findByPawnLoanIdOrderByPaymentDateDesc(Long pawnLoanId);
    
    @Query("SELECT r FROM PawnRepayment r WHERE r.pawnLoan.id = :pawnLoanId AND r.paymentDate BETWEEN :startDate AND :endDate")
    List<PawnRepayment> findByPawnLoanIdAndPaymentDateBetween(
            @Param("pawnLoanId") Long pawnLoanId,
            @Param("startDate") LocalDate startDate,
            @Param("endDate") LocalDate endDate);
    
    @Query("SELECT SUM(r.paidAmount) FROM PawnRepayment r WHERE r.pawnLoan.id = :pawnLoanId")
    Optional<BigDecimal> getTotalPaidAmountByPawnLoanId(@Param("pawnLoanId") Long pawnLoanId);
    
    @Query("SELECT SUM(r.principalPaid) FROM PawnRepayment r WHERE r.pawnLoan.id = :pawnLoanId")
    Optional<BigDecimal> getTotalPrincipalPaidByPawnLoanId(@Param("pawnLoanId") Long pawnLoanId);
    
    @Query("SELECT SUM(r.interestPaid) FROM PawnRepayment r WHERE r.pawnLoan.id = :pawnLoanId")
    Optional<BigDecimal> getTotalInterestPaidByPawnLoanId(@Param("pawnLoanId") Long pawnLoanId);
    
    @Query("SELECT SUM(r.penaltyPaid) FROM PawnRepayment r WHERE r.pawnLoan.id = :pawnLoanId")
    Optional<BigDecimal> getTotalPenaltyPaidByPawnLoanId(@Param("pawnLoanId") Long pawnLoanId);
    
    @Query("SELECT r FROM PawnRepayment r WHERE r.pawnLoan.customer.id = :customerId")
    Page<PawnRepayment> findByCustomerId(@Param("customerId") Long customerId, Pageable pageable);
    
    @Query("SELECT r FROM PawnRepayment r WHERE r.pawnLoan.customer.id = :customerId AND r.paymentDate BETWEEN :startDate AND :endDate")
    Page<PawnRepayment> findByCustomerIdAndPaymentDateBetween(
            @Param("customerId") Long customerId,
            @Param("startDate") LocalDate startDate,
            @Param("endDate") LocalDate endDate,
            Pageable pageable);
    
    @Query("SELECT r FROM PawnRepayment r WHERE r.paymentDate = :date")
    List<PawnRepayment> findByPaymentDate(@Param("date") LocalDate date);
    
    @Query("SELECT r FROM PawnRepayment r WHERE r.paymentDate BETWEEN :startDate AND :endDate")
    Page<PawnRepayment> findByPaymentDateBetween(
            @Param("startDate") LocalDate startDate,
            @Param("endDate") LocalDate endDate,
            Pageable pageable);
    
    @Query("SELECT r FROM PawnRepayment r WHERE r.receivedBy.id = :userId AND r.paymentDate = :date")
    List<PawnRepayment> findByReceivedByAndPaymentDate(
            @Param("userId") Long userId,
            @Param("date") LocalDate date);
    
    @Query("SELECT r FROM PawnRepayment r WHERE r.pawnLoan.branch.id = :branchId AND r.paymentDate = :date")
    List<PawnRepayment> findByBranchAndPaymentDate(
            @Param("branchId") Long branchId,
            @Param("date") LocalDate date);
}