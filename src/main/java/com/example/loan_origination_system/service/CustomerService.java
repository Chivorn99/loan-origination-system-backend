package com.example.loan_origination_system.service;

import com.example.loan_origination_system.exception.BusinessException;
import com.example.loan_origination_system.model.enums.CustomerStatus;
import com.example.loan_origination_system.model.people.Customer;
import com.example.loan_origination_system.repository.CustomerRepository;
import com.example.loan_origination_system.repository.PawnLoanRepository;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class CustomerService {
    
    private final CustomerRepository customerRepository;
    private final PawnLoanRepository pawnLoanRepository;
    
    /**
     * Create a new customer with business validation
     * Business Rule: nationalId must be unique
     */
    @Transactional
    public Customer createCustomer(Customer customer) {
        // Validate nationalId uniqueness
        if (customerRepository.existsByIdNumber(customer.getIdNumber())) {
            throw new BusinessException("CUSTOMER_ID_DUPLICATE", 
                "Customer with national ID " + customer.getIdNumber() + " already exists");
        }
        
        // Set default status if not provided
        if (customer.getStatus() == null) {
            customer.setStatus(CustomerStatus.ACTIVE);
        }
        
        return customerRepository.save(customer);
    }
    
    /**
     * Update customer information
     */
    @Transactional
    public Customer updateCustomer(Long id, Customer customerDetails) {
        Customer customer = getCustomerById(id);
        
        // Check if nationalId is being changed and validate uniqueness
        if (!customer.getIdNumber().equals(customerDetails.getIdNumber())) {
            if (customerRepository.existsByIdNumber(customerDetails.getIdNumber())) {
                throw new BusinessException("CUSTOMER_ID_DUPLICATE",
                    "Customer with national ID " + customerDetails.getIdNumber() + " already exists");
            }
            customer.setIdNumber(customerDetails.getIdNumber());
        }
        
        // Update other fields
        customer.setFullName(customerDetails.getFullName());
        customer.setPhone(customerDetails.getPhone());
        customer.setAddress(customerDetails.getAddress());
        
        return customerRepository.save(customer);
    }
    
    /**
     * Get customer by ID
     */
    public Customer getCustomerById(Long id) {
        return customerRepository.findById(id)
            .orElseThrow(() -> new BusinessException("CUSTOMER_NOT_FOUND", 
                "Customer with ID " + id + " not found"));
    }
    
    /**
     * Get customer by national ID
     */
    public Customer getCustomerByIdNumber(String idNumber) {
        return customerRepository.findByIdNumber(idNumber)
            .orElseThrow(() -> new BusinessException("CUSTOMER_NOT_FOUND", 
                "Customer with national ID " + idNumber + " not found"));
    }
    
    /**
     * Get all customers with pagination (excluding deleted)
     */
    public Page<Customer> getAllCustomers(Pageable pageable) {
        return customerRepository.findAllActive(pageable);
    }
    
    /**
     * Soft delete customer
     * Business Rule: Cannot delete customer if they have ACTIVE loans
     */
    @Transactional
    public void deleteCustomer(Long id) {
        Customer customer = getCustomerById(id);
        
        // Check if customer has active loans
        if (pawnLoanRepository.existsActiveLoanByCustomerId(id)) {
            throw new BusinessException("CUSTOMER_HAS_ACTIVE_LOANS",
                "Cannot delete customer with ID " + id + " because they have active loans");
        }
        
        // Soft delete
        customer.setStatus(CustomerStatus.DELETED);
        customer.setDeletedAt(LocalDateTime.now());
        customerRepository.save(customer);
    }
    
    /**
     * Get customers by status with pagination
     */
    public Page<Customer> getCustomersByStatus(CustomerStatus status, Pageable pageable) {
        return customerRepository.findByStatus(status, pageable);
    }
    
    /**
     * Check if customer has active loans
     */
    public boolean hasActiveLoans(Long customerId) {
        return pawnLoanRepository.existsActiveLoanByCustomerId(customerId);
    }
}