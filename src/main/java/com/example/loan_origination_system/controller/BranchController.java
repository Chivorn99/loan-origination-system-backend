package com.example.loan_origination_system.controller;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.example.loan_origination_system.dto.ApiResponse;
import com.example.loan_origination_system.dto.BranchPatchRequest;
import com.example.loan_origination_system.dto.BranchRequest;
import com.example.loan_origination_system.model.master.Branch;
import com.example.loan_origination_system.service.BranchService;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/branches")
@RequiredArgsConstructor
public class BranchController {

    private final BranchService branchService;

    /**
     * Create branch
     */
    @PostMapping
    public ResponseEntity<ApiResponse<Branch>> createBranch(
            @Valid @RequestBody BranchRequest request) {

        Branch branch = branchService.createBranch(request);

        return ResponseEntity
                .status(201)
                .body(ApiResponse.success("Branch created successfully", branch));
    }

    /**
     * Get branch by ID
     */
    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<Branch>> getBranch(@PathVariable Long id) {
        return ResponseEntity.ok(
                ApiResponse.success(branchService.getBranchById(id))
        );
    }

    /**
     * Update branch (FULL UPDATE)
     */
    @PutMapping("/{id}")
    public ResponseEntity<ApiResponse<Branch>> updateBranch(
            @PathVariable Long id,
            @Valid @RequestBody BranchRequest request) {

        Branch updatedBranch = branchService.updateBranch(id, request);

        return ResponseEntity.ok(
                ApiResponse.success("Branch updated successfully", updatedBranch)
        );
    }

    /**
     * Partial update (PATCH)
     */
    @PatchMapping("/{id}")
    public ResponseEntity<ApiResponse<Branch>> patchBranch(
            @PathVariable Long id,
            @RequestBody BranchPatchRequest request) {

        Branch updatedBranch = branchService.patchBranch(id, request);

        return ResponseEntity.ok(
                ApiResponse.success("Branch updated successfully", updatedBranch)
        );
    }

    /**
     * Get branches (ALL or FILTER BY STATUS)
     *
     * Examples:
     * GET /api/branches
     * GET /api/branches?status=ACTIVE
     */
    @GetMapping
    public ResponseEntity<ApiResponse<Page<Branch>>> getBranches(
            @RequestParam(required = false) String status,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "name") String sortBy,
            @RequestParam(defaultValue = "asc") String direction) {

        Sort.Direction sortDirection =
                direction.equalsIgnoreCase("asc")
                        ? Sort.Direction.ASC
                        : Sort.Direction.DESC;

        Pageable pageable =
                PageRequest.of(page, size, Sort.by(sortDirection, sortBy));

        Page<Branch> branches = (status == null)
                ? branchService.getAllBranches(pageable)
                : branchService.getBranchesByStatus(status, pageable);

        return ResponseEntity.ok(ApiResponse.success(branches));
    }

    /**
     * Soft delete branch (set status to INACTIVE)
     */
    @DeleteMapping("/{id}")
    public ResponseEntity<ApiResponse<Void>> deleteBranch(@PathVariable Long id) {

        branchService.deleteBranch(id);

        return ResponseEntity.ok(
                ApiResponse.success("Branch deleted successfully", null)
        );
    }

    /**
     * Check if branch exists by name
     */
    @GetMapping("/exists/{name}")
    public ResponseEntity<ApiResponse<Boolean>> checkBranchExists(@PathVariable String name) {
        return ResponseEntity.ok(
                ApiResponse.success(branchService.existsByName(name))
        );
    }
}