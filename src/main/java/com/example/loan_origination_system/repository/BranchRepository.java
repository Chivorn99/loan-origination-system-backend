package com.example.loan_origination_system.repository;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.example.loan_origination_system.model.master.Branch;

@Repository
public interface BranchRepository extends JpaRepository<Branch, Long> {
    
    boolean existsByName(String name);
    
    @Query("SELECT COUNT(b) > 0 FROM Branch b WHERE b.name = :name AND b.id != :excludeId")
    boolean existsByNameAndIdNot(@Param("name") String name, @Param("excludeId") Long excludeId);
    
    @Query("SELECT b FROM Branch b WHERE b.status = :status")
    Page<Branch> findByStatus(@Param("status") String status, Pageable pageable);
}