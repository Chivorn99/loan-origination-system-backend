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
}