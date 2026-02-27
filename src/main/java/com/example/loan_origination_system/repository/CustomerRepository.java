package com.example.loan_origination_system.repository;

import java.util.Optional;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.example.loan_origination_system.model.enums.CustomerStatus;
import com.example.loan_origination_system.model.people.Customer;

@Repository
public interface CustomerRepository extends JpaRepository<Customer, Long> {
    
    Optional<Customer> findByIdNumber(String idNumber);
    
    boolean existsByIdNumber(String idNumber);
    
    @Query("SELECT c FROM Customer c WHERE c.status != 'DELETED'")
    Page<Customer> findAllActive(Pageable pageable);
    
    @Query("SELECT c FROM Customer c WHERE c.status = :status")
    Page<Customer> findByStatus(@Param("status") CustomerStatus status, Pageable pageable);
    
    @Query("SELECT COUNT(l) > 0 FROM PawnLoan l WHERE l.customer.id = :customerId AND l.status = 'ACTIVE'")
    boolean hasActiveLoans(@Param("customerId") Long customerId);
    
    @Query("SELECT COUNT(c) > 0 FROM Customer c WHERE c.idNumber = :idNumber AND c.id != :excludeId")
    boolean existsByIdNumberAndIdNot(@Param("idNumber") String idNumber, @Param("excludeId") Long excludeId);
    
    @Query("SELECT c FROM Customer c WHERE c.status != 'DELETED' AND " +
           "(LOWER(c.fullName) LIKE LOWER(CONCAT('%', :searchTerm, '%')) OR " +
           "c.idNumber LIKE CONCAT('%', :searchTerm, '%'))")
    Page<Customer> searchCustomers(@Param("searchTerm") String searchTerm, Pageable pageable);
}