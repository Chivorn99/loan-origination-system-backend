package com.example.loan_origination_system.controller;

import java.util.List;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.example.loan_origination_system.dto.ApiResponse;
import com.example.loan_origination_system.dto.PawnItemRequest;
import com.example.loan_origination_system.model.enums.CollateralStatus;
import com.example.loan_origination_system.model.loan.PawnItem;
import com.example.loan_origination_system.service.PawnItemService;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/pawn-items")
@RequiredArgsConstructor
public class PawnItemController {
    
    private final PawnItemService pawnItemService;
    
    /**
     * Create a new collateral item
     * POST /api/pawn-items
     */
    @PostMapping
    public ResponseEntity<ApiResponse<PawnItem>> createPawnItem(@Valid @RequestBody PawnItemRequest request) {
        PawnItem createdPawnItem = pawnItemService.createPawnItem(request);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success("Collateral item created successfully", createdPawnItem));
    }
    
    /**
     * Get collateral by ID
     * GET /api/pawn-items/{id}
     */
    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<PawnItem>> getPawnItem(@PathVariable Long id) {
        PawnItem pawnItem = pawnItemService.getPawnItemById(id);
        return ResponseEntity.ok(ApiResponse.success(pawnItem));
    }
    
    /**
     * Get active collateral by ID (excluding deleted)
     * GET /api/pawn-items/{id}/active
     */
    @GetMapping("/{id}/active")
    public ResponseEntity<ApiResponse<PawnItem>> getActivePawnItem(@PathVariable Long id) {
        PawnItem pawnItem = pawnItemService.getActivePawnItemById(id);
        return ResponseEntity.ok(ApiResponse.success(pawnItem));
    }
    
    /**
     * Update collateral item
     * PUT /api/pawn-items/{id}
     */
    @PutMapping("/{id}")
    public ResponseEntity<ApiResponse<PawnItem>> updatePawnItem(
            @PathVariable Long id,
            @Valid @RequestBody PawnItemRequest request) {
        PawnItem updatedPawnItem = pawnItemService.updatePawnItem(id, request);
        return ResponseEntity.ok(ApiResponse.success("Collateral item updated successfully", updatedPawnItem));
    }
    
    /**
     * Get all collateral items with pagination
     * GET /api/pawn-items
     */
    @GetMapping
    public ResponseEntity<ApiResponse<Page<PawnItem>>> getAllPawnItems(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "createdAt") String sortBy,
            @RequestParam(defaultValue = "desc") String direction) {
        
        Sort.Direction sortDirection = direction.equalsIgnoreCase("asc") ? Sort.Direction.ASC : Sort.Direction.DESC;
        Pageable pageable = PageRequest.of(page, size, Sort.by(sortDirection, sortBy));
        
        Page<PawnItem> pawnItems = pawnItemService.getAllPawnItems(pageable);
        return ResponseEntity.ok(ApiResponse.success(pawnItems));
    }
    
    /**
     * Get collateral items by customer ID
     * GET /api/pawn-items/customer/{customerId}
     */
    @GetMapping("/customer/{customerId}")
    public ResponseEntity<ApiResponse<List<PawnItem>>> getPawnItemsByCustomerId(@PathVariable Long customerId) {
        List<PawnItem> pawnItems = pawnItemService.getPawnItemsByCustomerId(customerId);
        return ResponseEntity.ok(ApiResponse.success(pawnItems));
    }
    
    /**
     * Get collateral items by customer ID with pagination
     * GET /api/pawn-items/customer/{customerId}/page
     */
    @GetMapping("/customer/{customerId}/page")
    public ResponseEntity<ApiResponse<Page<PawnItem>>> getPawnItemsByCustomerIdPage(
            @PathVariable Long customerId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        
        Pageable pageable = PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "createdAt"));
        Page<PawnItem> pawnItems = pawnItemService.getPawnItemsByCustomerId(customerId, pageable);
        return ResponseEntity.ok(ApiResponse.success(pawnItems));
    }
    
    /**
     * Get collateral items by status with pagination
     * GET /api/pawn-items/status/{status}
     */
    @GetMapping("/status/{status}")
    public ResponseEntity<ApiResponse<Page<PawnItem>>> getPawnItemsByStatus(
            @PathVariable CollateralStatus status,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        
        Pageable pageable = PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "createdAt"));
        Page<PawnItem> pawnItems = pawnItemService.getPawnItemsByStatus(status, pageable);
        return ResponseEntity.ok(ApiResponse.success(pawnItems));
    }
    
    /**
     * Soft delete collateral item
     * DELETE /api/pawn-items/{id}
     */
    @DeleteMapping("/{id}")
    public ResponseEntity<ApiResponse<Void>> deletePawnItem(@PathVariable Long id) {
        pawnItemService.deletePawnItem(id);
        return ResponseEntity.ok(ApiResponse.success("Collateral item deleted successfully", null));
    }
    
    /**
     * Check if collateral is available for pawn
     * GET /api/pawn-items/{id}/available
     */
    @GetMapping("/{id}/available")
    public ResponseEntity<ApiResponse<Boolean>> isPawnItemAvailable(@PathVariable Long id) {
        boolean isAvailable = pawnItemService.isPawnItemAvailable(id);
        return ResponseEntity.ok(ApiResponse.success(isAvailable));
    }
}