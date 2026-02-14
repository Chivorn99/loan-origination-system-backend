package com.example.loan_origination_system.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.example.loan_origination_system.model.master.Branch;

@Repository
public interface BranchRepository extends JpaRepository<Branch, Long> {
}