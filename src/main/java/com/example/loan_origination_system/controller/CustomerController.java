package com.example.loan_origination_system.controller;

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
import com.example.loan_origination_system.dto.CustomerRequest;
import com.example.loan_origination_system.model.enums.CustomerStatus;
import com.example.loan_origination_system.model.people.Customer;
import com.example.loan_origination_system.service.CustomerService;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/customers")
@RequiredArgsConstructor
public class CustomerController {
    
    private final CustomerService customerService;
    
    /**
     * Create a new customer
     * POST /api/customers
     */
    @PostMapping
    public ResponseEntity<ApiResponse<Customer>> createCustomer(@Valid @RequestBody CustomerRequest request) {
        Customer customer = new Customer();
        customer.setFullName(request.getFullName());
        customer.setPhone(request.getPhone());
        customer.setIdNumber(request.getIdNumber());
        customer.setAddress(request.getAddress());
        
        Customer createdCustomer = customerService.createCustomer(customer);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success("Customer created successfully", createdCustomer));
    }
    
    /**
     * Get customer by ID
     * GET /api/customers/{id}
     */
    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<Customer>> getCustomer(@PathVariable Long id) {
        Customer customer = customerService.getCustomerById(id);
        return ResponseEntity.ok(ApiResponse.success(customer));
    }
    
    /**
     * Get customer by national ID
     * GET /api/customers/by-id-number/{idNumber}
     */
    @GetMapping("/by-id-number/{idNumber}")
    public ResponseEntity<ApiResponse<Customer>> getCustomerByIdNumber(@PathVariable String idNumber) {
        Customer customer = customerService.getCustomerByIdNumber(idNumber);
        return ResponseEntity.ok(ApiResponse.success(customer));
    }
    
    /**
     * Update customer
     * PUT /api/customers/{id}
     */
    @PutMapping("/{id}")
    public ResponseEntity<ApiResponse<Customer>> updateCustomer(
            @PathVariable Long id,
            @Valid @RequestBody CustomerRequest request) {
        Customer customerDetails = new Customer();
        customerDetails.setFullName(request.getFullName());
        customerDetails.setPhone(request.getPhone());
        customerDetails.setIdNumber(request.getIdNumber());
        customerDetails.setAddress(request.getAddress());
        
        Customer updatedCustomer = customerService.updateCustomer(id, customerDetails);
        return ResponseEntity.ok(ApiResponse.success("Customer updated successfully", updatedCustomer));
    }
    
    /**
     * Get all customers with pagination
     * GET /api/customers
     */
    @GetMapping
    public ResponseEntity<ApiResponse<Page<Customer>>> getAllCustomers(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "createdAt") String sortBy,
            @RequestParam(defaultValue = "desc") String direction) {
        
        Sort.Direction sortDirection = direction.equalsIgnoreCase("asc") ? Sort.Direction.ASC : Sort.Direction.DESC;
        Pageable pageable = PageRequest.of(page, size, Sort.by(sortDirection, sortBy));
        
        Page<Customer> customers = customerService.getAllCustomers(pageable);
        return ResponseEntity.ok(ApiResponse.success(customers));
    }
    
    /**
     * Get customers by status with pagination
     * GET /api/customers/status/{status}
     */
    @GetMapping("/status/{status}")
    public ResponseEntity<ApiResponse<Page<Customer>>> getCustomersByStatus(
            @PathVariable CustomerStatus status,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        
        Pageable pageable = PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "createdAt"));
        Page<Customer> customers = customerService.getCustomersByStatus(status, pageable);
        return ResponseEntity.ok(ApiResponse.success(customers));
    }
    
    /**
     * Soft delete customer
     * DELETE /api/customers/{id}
     */
    @DeleteMapping("/{id}")
    public ResponseEntity<ApiResponse<Void>> deleteCustomer(@PathVariable Long id) {
        customerService.deleteCustomer(id);
        return ResponseEntity.ok(ApiResponse.success("Customer deleted successfully", null));
    }
    
    /**
     * Check if customer has active loans
     * GET /api/customers/{id}/has-active-loans
     */
    @GetMapping("/{id}/has-active-loans")
    public ResponseEntity<ApiResponse<Boolean>> hasActiveLoans(@PathVariable Long id) {
        boolean hasActiveLoans = customerService.hasActiveLoans(id);
        return ResponseEntity.ok(ApiResponse.success(hasActiveLoans));
    }
}