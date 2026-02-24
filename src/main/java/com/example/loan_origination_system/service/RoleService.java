package com.example.loan_origination_system.service;

import org.springframework.stereotype.Service;

import com.example.loan_origination_system.dto.RolePatchRequest;
import com.example.loan_origination_system.dto.RoleRequest;
import com.example.loan_origination_system.exception.BusinessException;
import com.example.loan_origination_system.mapper.LoanMapper;
import com.example.loan_origination_system.model.master.Role;
import com.example.loan_origination_system.repository.RoleRepository;

import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class RoleService {

    private final RoleRepository roleRepository;
    private final LoanMapper loanMapper;

    /**
     * CREATE
     */
    @Transactional
    public Role createRole(RoleRequest request) {
        // Check if code already exists
        if (roleRepository.existsByCode(request.getCode())) {
            throw new BusinessException(
                    "ROLE_CODE_DUPLICATE",
                    "Role with this code already exists"
            );
        }

        Role role = loanMapper.toRole(request);
        return roleRepository.save(role);
    }

    /**
     * GET by ID
     */
    public Role getRoleById(Long id) {
        return roleRepository.findById(id)
                .orElseThrow(() -> new BusinessException(
                        "ROLE_NOT_FOUND",
                        "Role not found with ID: " + id
                ));
    }

    /**
     * FULL UPDATE (PUT)
     */
    @Transactional
    public Role updateRole(Long id, RoleRequest request) {
        Role role = getRoleById(id);

        // Validate code if provided and changed
        if (request.getCode() != null && !request.getCode().equals(role.getCode())) {
            // Check if new code already exists
            if (roleRepository.existsByCode(request.getCode())) {
                throw new BusinessException(
                        "ROLE_CODE_DUPLICATE",
                        "Role with this code already exists"
                );
            }
        }

        // Use mapper to update entity
        loanMapper.updateRoleFromRequest(request, role);

        return roleRepository.save(role);
    }

    /**
     * PARTIAL UPDATE (PATCH)
     */
    @Transactional
    public Role patchRole(Long id, RolePatchRequest patch) {
        Role role = getRoleById(id);

        if (patch.getCode() != null && !patch.getCode().equals(role.getCode())) {
            // Check if new code already exists
            if (roleRepository.existsByCode(patch.getCode())) {
                throw new BusinessException(
                        "ROLE_CODE_DUPLICATE",
                        "Role with this code already exists"
                );
            }
            role.setCode(patch.getCode());
        }

        if (patch.getName() != null) {
            role.setName(patch.getName());
        }

        if (patch.getDescription() != null) {
            role.setDescription(patch.getDescription());
        }

        return roleRepository.save(role);
    }

    /**
     * DELETE
     */
    @Transactional
    public void deleteRole(Long id) {
        Role role = getRoleById(id);
        roleRepository.delete(role);
    }
}