package com.example.loan_origination_system.service;

import java.time.LocalDateTime;
import java.util.List;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import com.example.loan_origination_system.dto.PawnItemRequest;
import com.example.loan_origination_system.exception.BusinessException;
import com.example.loan_origination_system.mapper.LoanMapper;
import com.example.loan_origination_system.model.enums.CollateralStatus;
import com.example.loan_origination_system.model.loan.PawnItem;
import com.example.loan_origination_system.model.people.Customer;
import com.example.loan_origination_system.repository.CustomerRepository;
import com.example.loan_origination_system.repository.PawnItemRepository;
import com.example.loan_origination_system.repository.PawnLoanRepository;

import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class PawnItemService {
    
    private final PawnItemRepository pawnItemRepository;
    private final CustomerRepository customerRepository;
    private final PawnLoanRepository pawnLoanRepository;
    private final LoanMapper loanMapper;
    
    /**
     * Create a new collateral item
     */
    @Transactional
    public PawnItem createPawnItem(PawnItemRequest request) {
        // Validate customer exists
        Customer customer = customerRepository.findById(request.getCustomerId())
            .orElseThrow(() -> new BusinessException("CUSTOMER_NOT_FOUND",
                "Customer with ID " + request.getCustomerId() + " not found"));
        
        // Convert DTO to entity
        PawnItem pawnItem = loanMapper.toPawnItem(request);
        pawnItem.setCustomer(customer);
        
        // Set default status if not provided
        if (pawnItem.getStatus() == null) {
            pawnItem.setStatus(CollateralStatus.AVAILABLE);
        }
        
        return pawnItemRepository.save(pawnItem);
    }
    
    /**
     * Update collateral item
     * Business Rule: Cannot modify collateral if it is already PAWNED
     */
    @Transactional
    public PawnItem updatePawnItem(Long id, PawnItemRequest request) {
        PawnItem pawnItem = getPawnItemById(id);
        
        // Check if collateral is already pawned
        if (pawnItem.getStatus() == CollateralStatus.PAWNED) {
            throw new BusinessException("COLLATERAL_ALREADY_PAWNED",
                "Cannot modify collateral with ID " + id + " because it is already pawned");
        }
        
        // Update fields from request
        if (request.getItemType() != null) {
            pawnItem.setItemType(request.getItemType());
        }
        if (request.getDescription() != null) {
            pawnItem.setDescription(request.getDescription());
        }
        if (request.getEstimatedValue() != null) {
            pawnItem.setEstimatedValue(request.getEstimatedValue());
        }
        if (request.getPhotoUrl() != null) {
            pawnItem.setPhotoUrl(request.getPhotoUrl());
        }
        
        return pawnItemRepository.save(pawnItem);
    }
    
    /**
     * Get collateral by ID
     */
    public PawnItem getPawnItemById(Long id) {
        return pawnItemRepository.findById(id)
            .orElseThrow(() -> new BusinessException("COLLATERAL_NOT_FOUND",
                "Collateral with ID " + id + " not found"));
    }
    
    /**
     * Get active collateral by ID (excluding deleted)
     */
    public PawnItem getActivePawnItemById(Long id) {
        return pawnItemRepository.findByIdAndStatusNot(id, CollateralStatus.DELETED)
            .orElseThrow(() -> new BusinessException("COLLATERAL_NOT_FOUND",
                "Active collateral with ID " + id + " not found"));
    }
    
    /**
     * List collateral by customer ID
     */
    public List<PawnItem> getPawnItemsByCustomerId(Long customerId) {
        // Validate customer exists
        if (!customerRepository.existsById(customerId)) {
            throw new BusinessException("CUSTOMER_NOT_FOUND",
                "Customer with ID " + customerId + " not found");
        }
        
        return pawnItemRepository.findActiveByCustomerId(customerId);
    }
    
    /**
     * List collateral by customer ID with pagination
     */
    public Page<PawnItem> getPawnItemsByCustomerId(Long customerId, Pageable pageable) {
        // Validate customer exists
        if (!customerRepository.existsById(customerId)) {
            throw new BusinessException("CUSTOMER_NOT_FOUND",
                "Customer with ID " + customerId + " not found");
        }
        
        return pawnItemRepository.findByCustomerId(customerId, pageable);
    }
    
    /**
     * Get all collateral with pagination (excluding deleted)
     */
    public Page<PawnItem> getAllPawnItems(Pageable pageable) {
        return pawnItemRepository.findAllActive(pageable);
    }
    
    /**
     * Soft delete collateral
     * Business Rule: Cannot delete if linked to ACTIVE loan
     */
    @Transactional
    public void deletePawnItem(Long id) {
        PawnItem pawnItem = getPawnItemById(id);
        
        // Check if collateral is linked to active loan
        if (pawnLoanRepository.existsActiveLoanByPawnItemId(id)) {
            throw new BusinessException("COLLATERAL_LINKED_TO_ACTIVE_LOAN",
                "Cannot delete collateral with ID " + id + " because it is linked to an active loan");
        }
        
        // Soft delete
        pawnItem.setStatus(CollateralStatus.DELETED);
        pawnItem.setDeletedAt(LocalDateTime.now());
        pawnItemRepository.save(pawnItem);
    }
    
    /**
     * Get collateral by status with pagination
     */
    public Page<PawnItem> getPawnItemsByStatus(CollateralStatus status, Pageable pageable) {
        return pawnItemRepository.findByStatus(status, pageable);
    }
    
    /**
     * Update collateral status (internal use for loan operations)
     */
    @Transactional
    public void updatePawnItemStatus(Long id, CollateralStatus status) {
        PawnItem pawnItem = getPawnItemById(id);
        pawnItem.setStatus(status);
        pawnItemRepository.save(pawnItem);
    }
    
    /**
     * Check if collateral is available for pawn
     */
    public boolean isPawnItemAvailable(Long id) {
        PawnItem pawnItem = getPawnItemById(id);
        return pawnItem.getStatus() == CollateralStatus.AVAILABLE;
    }
}