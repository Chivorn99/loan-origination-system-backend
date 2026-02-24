package com.example.loan_origination_system.controller;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import com.example.loan_origination_system.dto.ApiResponse;
import com.example.loan_origination_system.dto.CustomerPatchRequest;
import com.example.loan_origination_system.dto.CustomerRequest;
import com.example.loan_origination_system.model.enums.CustomerStatus;
import com.example.loan_origination_system.model.people.Customer;
import com.example.loan_origination_system.service.CustomerService;

import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/customers")
@RequiredArgsConstructor
public class CustomerController {

    private final CustomerService customerService;

    /**
     * Create customer
     */
    @PostMapping
    public ResponseEntity<ApiResponse<Customer>> createCustomer(
            @RequestBody CustomerRequest request) {

        Customer customer = customerService.createCustomer(request);

        return ResponseEntity
                .status(201)
                .body(ApiResponse.success("Customer created successfully", customer));
    }

    /**
     * Get customer by ID
     */
    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<Customer>> getCustomer(@PathVariable Long id) {
        return ResponseEntity.ok(
                ApiResponse.success(customerService.getCustomerById(id))
        );
    }

    /**
     * Get customer by national ID
     */
    @GetMapping("/by-id-number/{idNumber}")
    public ResponseEntity<ApiResponse<Customer>> getCustomerByIdNumber(
            @PathVariable String idNumber) {

        return ResponseEntity.ok(
                ApiResponse.success(customerService.getCustomerByIdNumber(idNumber))
        );
    }

    /**
     * Update customer (FULL UPDATE)
     */
    @PutMapping("/{id}")
    public ResponseEntity<ApiResponse<Customer>> updateCustomer(
            @PathVariable Long id,
            @RequestBody CustomerRequest request) {

        Customer updatedCustomer =
                customerService.updateCustomer(id, request);

        return ResponseEntity.ok(
                ApiResponse.success("Customer updated successfully", updatedCustomer)
        );
    }

    /**
     * Partial update (PATCH)
     */
    @PatchMapping("/{id}")
    public ResponseEntity<ApiResponse<Customer>> patchCustomer(
            @PathVariable Long id,
            @RequestBody CustomerPatchRequest request) {

        Customer updatedCustomer =
                customerService.patchCustomer(id, request);

        return ResponseEntity.ok(
                ApiResponse.success("Customer updated successfully", updatedCustomer)
        );
    }

    /**
     * Get customers (ALL or FILTER BY STATUS)
     *
     * Examples:
     * GET /api/customers
     * GET /api/customers?status=ACTIVE
     */
    @GetMapping
    public ResponseEntity<ApiResponse<Page<Customer>>> getCustomers(
            @RequestParam(required = false) CustomerStatus status,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "createdAt") String sortBy,
            @RequestParam(defaultValue = "desc") String direction) {

        Sort.Direction sortDirection =
                direction.equalsIgnoreCase("asc")
                        ? Sort.Direction.ASC
                        : Sort.Direction.DESC;

        Pageable pageable =
                PageRequest.of(page, size, Sort.by(sortDirection, sortBy));

        Page<Customer> customers =
                customerService.getCustomers(status, pageable);

        return ResponseEntity.ok(ApiResponse.success(customers));
    }

    /**
     * Soft delete customer
     */
    @DeleteMapping("/{id}")
    public ResponseEntity<ApiResponse<Void>> deleteCustomer(@PathVariable Long id) {

        customerService.deleteCustomer(id);

        return ResponseEntity.ok(
                ApiResponse.success("Customer deleted successfully", null)
        );
    }

    /**
     * Check active loans
     */
    @GetMapping("/{id}/has-active-loans")
    public ResponseEntity<ApiResponse<Boolean>> hasActiveLoans(@PathVariable Long id) {

        return ResponseEntity.ok(
                ApiResponse.success(customerService.hasActiveLoans(id))
        );
    }
}