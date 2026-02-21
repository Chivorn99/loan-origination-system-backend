package com.example.loan_origination_system.service;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import com.example.loan_origination_system.dto.BranchPatchRequest;
import com.example.loan_origination_system.dto.BranchRequest;
import com.example.loan_origination_system.exception.BusinessException;
import com.example.loan_origination_system.model.master.Branch;
import com.example.loan_origination_system.repository.BranchRepository;

import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class BranchService {

    private final BranchRepository branchRepository;

    /**
     * CREATE
     */
    @Transactional
    public Branch createBranch(BranchRequest request) {

        if (branchRepository.existsByName(request.getName())) {
            throw new BusinessException(
                    "BRANCH_NAME_DUPLICATE",
                    "Branch with this name already exists"
            );
        }

        Branch branch = new Branch();
        branch.setName(request.getName());
        branch.setAddress(request.getAddress());
        branch.setPhone(request.getPhone());
        branch.setStatus(request.getStatus() != null ? request.getStatus() : "ACTIVE");

        return branchRepository.save(branch);
    }

    /**
     * FULL UPDATE (PUT)
     */
    @Transactional
    public Branch updateBranch(Long id, BranchRequest request) {

        Branch branch = getBranchById(id);

        // Validate branch name if provided and changed
        if (request.getName() != null && !request.getName().equals(branch.getName())) {
            if (branchRepository.existsByNameAndIdNot(request.getName(), id)) {
                throw new BusinessException(
                        "BRANCH_NAME_DUPLICATE",
                        "Branch with this name already exists"
                );
            }
            branch.setName(request.getName());
        }

        // Update other fields if provided
        if (request.getAddress() != null) {
            branch.setAddress(request.getAddress());
        }

        if (request.getPhone() != null) {
            branch.setPhone(request.getPhone());
        }

        if (request.getStatus() != null) {
            branch.setStatus(request.getStatus());
        }

        return branchRepository.save(branch);
    }

    /**
     * PARTIAL UPDATE (PATCH)
     */
    @Transactional
    public Branch patchBranch(Long id, BranchPatchRequest patch) {

        Branch branch = getBranchById(id);

        if (patch.getName() != null && !patch.getName().equals(branch.getName())) {
            if (branchRepository.existsByNameAndIdNot(patch.getName(), id)) {
                throw new BusinessException(
                        "BRANCH_NAME_DUPLICATE",
                        "Branch with this name already exists"
                );
            }
            branch.setName(patch.getName());
        }

        if (patch.getAddress() != null) {
            branch.setAddress(patch.getAddress());
        }

        if (patch.getPhone() != null) {
            branch.setPhone(patch.getPhone());
        }

        if (patch.getStatus() != null) {
            branch.setStatus(patch.getStatus());
        }

        return branchRepository.save(branch);
    }

    /**
     * READ METHODS
     */
    public Branch getBranchById(Long id) {
        return branchRepository.findById(id)
                .orElseThrow(() -> new BusinessException(
                        "BRANCH_NOT_FOUND",
                        "Branch with ID " + id + " not found"
                ));
    }

    public Page<Branch> getAllBranches(Pageable pageable) {
        return branchRepository.findAll(pageable);
    }

    public Page<Branch> getBranchesByStatus(String status, Pageable pageable) {
        return branchRepository.findByStatus(status, pageable);
    }

    /**
     * SOFT DELETE (update status to INACTIVE)
     */
    @Transactional
    public void deleteBranch(Long id) {
        Branch branch = getBranchById(id);
        branch.setStatus("INACTIVE");
        branchRepository.save(branch);
    }

    /**
     * Check if branch exists by name
     */
    public boolean existsByName(String name) {
        return branchRepository.existsByName(name);
    }
}