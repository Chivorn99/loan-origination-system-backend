package com.example.loan_origination_system.service;

import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import com.example.loan_origination_system.dto.RegisterRequest;
import com.example.loan_origination_system.dto.UserPatchRequest;
import com.example.loan_origination_system.dto.UserRequest;
import com.example.loan_origination_system.exception.BusinessException;
import com.example.loan_origination_system.mapper.LoanMapper;
import com.example.loan_origination_system.model.master.Branch;
import com.example.loan_origination_system.model.master.Role;
import com.example.loan_origination_system.model.people.User;
import com.example.loan_origination_system.repository.BranchRepository;
import com.example.loan_origination_system.repository.RoleRepository;
import com.example.loan_origination_system.repository.UserRepository;

import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class UserService {

    private final UserRepository userRepository;
    private final RoleRepository roleRepository;
    private final BranchRepository branchRepository;
    private final LoanMapper loanMapper;
    private final PasswordEncoder passwordEncoder;

    /**
     * CREATE
     */
    @Transactional
    public User createUser(UserRequest request) {
        // Check if username already exists
        if (userRepository.existsByUsername(request.getUsername())) {
            throw new BusinessException(
                    "USERNAME_DUPLICATE",
                    "Username already exists"
            );
        }

        // Check if email already exists
        if (userRepository.existsByEmail(request.getEmail())) {
            throw new BusinessException(
                    "EMAIL_DUPLICATE",
                    "Email already exists"
            );
        }

        // Validate role exists
        Role role = roleRepository.findById(request.getRoleId())
                .orElseThrow(() -> new BusinessException(
                        "ROLE_NOT_FOUND",
                        "Role not found with ID: " + request.getRoleId()
                ));

        // Validate branch if provided
        Branch branch = null;
        if (request.getBranchId() != null) {
            branch = branchRepository.findById(request.getBranchId())
                    .orElseThrow(() -> new BusinessException(
                            "BRANCH_NOT_FOUND",
                            "Branch not found with ID: " + request.getBranchId()
                    ));
        }

        User user = loanMapper.toUser(request);
        user.setRole(role);
        user.setBranch(branch);

        // Set default values
        if (user.getStatus() == null) {
            user.setStatus("ACTIVE");
        }

        return userRepository.save(user);
    }

    /**
     * REGISTER new user (only superadmin can create users with roles)
     */
    @Transactional
    public User registerUser(RegisterRequest request, String currentUsername) {
        // Check if current user is superadmin
        User currentUser = userRepository.findByUsername(currentUsername)
                .orElseThrow(() -> new BusinessException(
                        "USER_NOT_FOUND",
                        "Current user not found"
                ));
        
        if (currentUser.getRole() == null || !"SUPERADMIN".equalsIgnoreCase(currentUser.getRole().getCode())) {
            throw new BusinessException(
                    "UNAUTHORIZED",
                    "Only SUPERADMIN can register new users"
            );
        }

        // Check if username already exists
        if (userRepository.existsByUsername(request.getUsername())) {
            throw new BusinessException(
                    "USERNAME_DUPLICATE",
                    "Username already exists"
            );
        }

        // Check if email already exists (if email is provided)
        if (request.getEmail() != null && !request.getEmail().trim().isEmpty()) {
            if (userRepository.existsByEmail(request.getEmail())) {
                throw new BusinessException(
                        "EMAIL_DUPLICATE",
                        "Email already exists"
                );
            }
        }

        // Check if phone number already exists (if phone number is provided)
        if (request.getPhoneNumber() != null && !request.getPhoneNumber().trim().isEmpty()) {
            if (userRepository.existsByPhoneNumber(request.getPhoneNumber())) {
                throw new BusinessException(
                        "PHONE_NUMBER_DUPLICATE",
                        "Phone number already exists"
                );
            }
        }

        // Validate role exists
        Role role = roleRepository.findById(request.getRoleId())
                .orElseThrow(() -> new BusinessException(
                        "ROLE_NOT_FOUND",
                        "Role not found with ID: " + request.getRoleId()
                ));

        // Validate branch if provided
        Branch branch = null;
        if (request.getBranchId() != null) {
            branch = branchRepository.findById(request.getBranchId())
                    .orElseThrow(() -> new BusinessException(
                            "BRANCH_NOT_FOUND",
                            "Branch not found with ID: " + request.getBranchId()
                    ));
        }

        // Create user from request
        User user = new User();
        user.setUsername(request.getUsername());
        user.setEmail(request.getEmail());
        user.setPassword(passwordEncoder.encode(request.getPassword()));
        user.setPhoneNumber(request.getPhoneNumber());
        user.setRole(role);
        user.setBranch(branch);
        user.setStatus("ACTIVE");

        return userRepository.save(user);
    }

    /**
     * GET by ID
     */
    public User getUserById(Long id) {
        return userRepository.findById(id)
                .orElseThrow(() -> new BusinessException(
                        "USER_NOT_FOUND",
                        "User not found with ID: " + id
                ));
    }

    /**
     * FULL UPDATE (PUT)
     */
    @Transactional
    public User updateUser(Long id, UserRequest request) {
        User user = getUserById(id);

        // Validate username if provided and changed
        if (request.getUsername() != null && !request.getUsername().equals(user.getUsername())) {
            if (userRepository.existsByUsername(request.getUsername())) {
                throw new BusinessException(
                        "USERNAME_DUPLICATE",
                        "Username already exists"
                );
            }
            user.setUsername(request.getUsername());
        }

        // Validate email if provided and changed
        if (request.getEmail() != null && !request.getEmail().equals(user.getEmail())) {
            if (userRepository.existsByEmail(request.getEmail())) {
                throw new BusinessException(
                        "EMAIL_DUPLICATE",
                        "Email already exists"
                );
            }
            user.setEmail(request.getEmail());
        }

        // Validate role if provided
        if (request.getRoleId() != null) {
            Role role = roleRepository.findById(request.getRoleId())
                    .orElseThrow(() -> new BusinessException(
                            "ROLE_NOT_FOUND",
                            "Role not found with ID: " + request.getRoleId()
                    ));
            user.setRole(role);
        }

        // Validate branch if provided
        if (request.getBranchId() != null) {
            Branch branch = branchRepository.findById(request.getBranchId())
                    .orElseThrow(() -> new BusinessException(
                            "BRANCH_NOT_FOUND",
                            "Branch not found with ID: " + request.getBranchId()
                    ));
            user.setBranch(branch);
        }

        // Update other fields
        if (request.getPassword() != null) {
            user.setPassword(request.getPassword());
        }

        if (request.getStatus() != null) {
            user.setStatus(request.getStatus());
        }

        return userRepository.save(user);
    }

    /**
     * PARTIAL UPDATE (PATCH)
     */
    @Transactional
    public User patchUser(Long id, UserPatchRequest patch) {
        User user = getUserById(id);

        if (patch.getUsername() != null && !patch.getUsername().equals(user.getUsername())) {
            if (userRepository.existsByUsername(patch.getUsername())) {
                throw new BusinessException(
                        "USERNAME_DUPLICATE",
                        "Username already exists"
                );
            }
            user.setUsername(patch.getUsername());
        }

        if (patch.getEmail() != null && !patch.getEmail().equals(user.getEmail())) {
            if (userRepository.existsByEmail(patch.getEmail())) {
                throw new BusinessException(
                        "EMAIL_DUPLICATE",
                        "Email already exists"
                );
            }
            user.setEmail(patch.getEmail());
        }

        if (patch.getPassword() != null) {
            user.setPassword(patch.getPassword());
        }

        if (patch.getRoleId() != null) {
            Role role = roleRepository.findById(patch.getRoleId())
                    .orElseThrow(() -> new BusinessException(
                            "ROLE_NOT_FOUND",
                            "Role not found with ID: " + patch.getRoleId()
                    ));
            user.setRole(role);
        }

        if (patch.getBranchId() != null) {
            Branch branch = branchRepository.findById(patch.getBranchId())
                    .orElseThrow(() -> new BusinessException(
                            "BRANCH_NOT_FOUND",
                            "Branch not found with ID: " + patch.getBranchId()
                    ));
            user.setBranch(branch);
        }

        if (patch.getStatus() != null) {
            user.setStatus(patch.getStatus());
        }

        return userRepository.save(user);
    }

    /**
     * DELETE
     */
    @Transactional
    public void deleteUser(Long id) {
        User user = getUserById(id);
        userRepository.delete(user);
    }
}