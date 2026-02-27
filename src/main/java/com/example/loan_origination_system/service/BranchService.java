package com.example.loan_origination_system.service;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import com.example.loan_origination_system.dto.BranchPatchRequest;
import com.example.loan_origination_system.dto.BranchRequest;
import com.example.loan_origination_system.dto.BranchResponse;
import com.example.loan_origination_system.exception.BusinessException;
import com.example.loan_origination_system.mapper.BranchMapper;
import com.example.loan_origination_system.model.master.Branch;
import com.example.loan_origination_system.repository.BranchRepository;

import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class BranchService {

    private final BranchRepository branchRepository;
    private final BranchMapper branchMapper;

    /**
     * CREATE
     */
    @Transactional
    public BranchResponse createBranch(BranchRequest request) {

        if (branchRepository.existsByName(request.getName())) {
            throw new BusinessException(
                    "BRANCH_NAME_DUPLICATE",
                    "Branch with this name already exists"
            );
        }

        Branch branch = branchMapper.toBranch(request);
        if (branch.getStatus() == null) {
            branch.setStatus("ACTIVE");
        }

        Branch savedBranch = branchRepository.save(branch);
        return branchMapper.toBranchResponse(savedBranch);
    }

    /**
     * FULL UPDATE (PUT)
     */
    @Transactional
    public BranchResponse updateBranch(Long id, BranchRequest request) {

        Branch branch = getBranchEntityById(id);

        // Validate branch name if provided and changed
        if (request.getName() != null && !request.getName().equals(branch.getName())) {
            if (branchRepository.existsByNameAndIdNot(request.getName(), id)) {
                throw new BusinessException(
                        "BRANCH_NAME_DUPLICATE",
                        "Branch with this name already exists"
                );
            }
        }

        branchMapper.updateBranchFromRequest(request, branch);
        Branch updatedBranch = branchRepository.save(branch);
        return branchMapper.toBranchResponse(updatedBranch);
    }

    /**
     * PARTIAL UPDATE (PATCH)
     */
    @Transactional
    public BranchResponse patchBranch(Long id, BranchPatchRequest patch) {

        Branch branch = getBranchEntityById(id);

        if (patch.getName() != null && !patch.getName().equals(branch.getName())) {
            if (branchRepository.existsByNameAndIdNot(patch.getName(), id)) {
                throw new BusinessException(
                        "BRANCH_NAME_DUPLICATE",
                        "Branch with this name already exists"
                );
            }
        }

        branchMapper.updateBranchFromPatchRequest(patch, branch);
        Branch updatedBranch = branchRepository.save(branch);
        return branchMapper.toBranchResponse(updatedBranch);
    }

    /**
     * READ METHODS - Return DTOs
     */
    public BranchResponse getBranchById(Long id) {
        Branch branch = getBranchEntityById(id);
        return branchMapper.toBranchResponse(branch);
    }

    public Page<BranchResponse> getAllBranches(Pageable pageable) {
        return branchRepository.findAll(pageable)
                .map(branchMapper::toBranchResponse);
    }

    public Page<BranchResponse> getBranchesByStatus(String status, Pageable pageable) {
        return branchRepository.findByStatus(status, pageable)
                .map(branchMapper::toBranchResponse);
    }

    /**
     * SOFT DELETE (update status to INACTIVE)
     */
    @Transactional
    public void deleteBranch(Long id) {
        Branch branch = getBranchEntityById(id);
        branch.setStatus("INACTIVE");
        branchRepository.save(branch);
    }

    /**
     * Check if branch exists by name
     */
    public boolean existsByName(String name) {
        return branchRepository.existsByName(name);
    }

    /**
     * Internal method to get entity (not exposed)
     */
    private Branch getBranchEntityById(Long id) {
        return branchRepository.findById(id)
                .orElseThrow(() -> new BusinessException(
                        "BRANCH_NOT_FOUND",
                        "Branch with ID " + id + " not found"
                ));
    }
}