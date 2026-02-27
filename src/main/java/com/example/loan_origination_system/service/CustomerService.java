package com.example.loan_origination_system.service;

import java.time.LocalDateTime;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import com.example.loan_origination_system.dto.CustomerPatchRequest;
import com.example.loan_origination_system.dto.CustomerRequest;
import com.example.loan_origination_system.exception.BusinessException;
import com.example.loan_origination_system.model.enums.CustomerStatus;
import com.example.loan_origination_system.model.people.Customer;
import com.example.loan_origination_system.repository.CustomerRepository;
import com.example.loan_origination_system.repository.PawnLoanRepository;

import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class CustomerService {

    private final CustomerRepository customerRepository;
    private final PawnLoanRepository pawnLoanRepository;

    /**
     * CREATE
     */
    @Transactional
    public Customer createCustomer(CustomerRequest request) {

        if (customerRepository.existsByIdNumber(request.getIdNumber())) {
            throw new BusinessException(
                    "CUSTOMER_ID_DUPLICATE",
                    "Customer with national ID already exists"
            );
        }

        Customer customer = new Customer();
        customer.setFullName(request.getFullName());
        customer.setPhone(request.getPhone());
        customer.setIdNumber(request.getIdNumber());
        customer.setAddress(request.getAddress());
        customer.setStatus(CustomerStatus.ACTIVE);

        return customerRepository.save(customer);
    }

    /**
     * FULL UPDATE (PUT) — NOW EDITABLE / NULL SAFE
     */
    @Transactional
    public Customer updateCustomer(Long id, CustomerRequest request) {

        Customer customer = getCustomerById(id);

        // ✅ Only validate ID number IF provided AND changed
        if (request.getIdNumber() != null &&
                !request.getIdNumber().equals(customer.getIdNumber())) {

            if (customerRepository.existsByIdNumberAndIdNot(
                    request.getIdNumber(), id)) {

                throw new BusinessException(
                        "CUSTOMER_ID_DUPLICATE",
                        "Customer with national ID already exists"
                );
            }

            customer.setIdNumber(request.getIdNumber());
        }

        // ✅ Editable fields (only update if provided)
        if (request.getFullName() != null) {
            customer.setFullName(request.getFullName());
        }

        if (request.getPhone() != null) {
            customer.setPhone(request.getPhone());
        }

        if (request.getAddress() != null) {
            customer.setAddress(request.getAddress());
        }

        return customerRepository.save(customer);
    }

    /**
     * PARTIAL UPDATE (PATCH)
     */
    @Transactional
    public Customer patchCustomer(Long id, CustomerPatchRequest patch) {

        Customer customer = getCustomerById(id);

        if (patch.getFullName() != null)
            customer.setFullName(patch.getFullName());

        if (patch.getPhone() != null)
            customer.setPhone(patch.getPhone());

        if (patch.getAddress() != null)
            customer.setAddress(patch.getAddress());

        if (patch.getIdNumber() != null &&
                !patch.getIdNumber().equals(customer.getIdNumber())) {

            if (customerRepository.existsByIdNumberAndIdNot(
                    patch.getIdNumber(), id)) {

                throw new BusinessException(
                        "CUSTOMER_ID_DUPLICATE",
                        "Customer with national ID already exists"
                );
            }

            customer.setIdNumber(patch.getIdNumber());
        }

        return customerRepository.save(customer);
    }

    /**
     * READ METHODS
     */
    public Customer getCustomerById(Long id) {
        return customerRepository.findById(id)
                .orElseThrow(() -> new BusinessException(
                        "CUSTOMER_NOT_FOUND",
                        "Customer not found"
                ));
    }

    public Customer getCustomerByIdNumber(String idNumber) {
        return customerRepository.findByIdNumber(idNumber)
                .orElseThrow(() -> new BusinessException(
                        "CUSTOMER_NOT_FOUND",
                        "Customer not found"
                ));
    }

    public Page<Customer> getCustomers(CustomerStatus status, Pageable pageable) {
        return (status == null)
                ? customerRepository.findAllActive(pageable)
                : customerRepository.findByStatus(status, pageable);
    }

    /**
     * SEARCH CUSTOMERS BY NAME OR ID NUMBER
     */
    public Page<Customer> searchCustomers(String searchTerm, Pageable pageable) {
        return customerRepository.searchCustomers(searchTerm, pageable);
    }

    /**
     * SOFT DELETE
     */
    @Transactional
    public void deleteCustomer(Long id) {

        Customer customer = getCustomerById(id);

        if (pawnLoanRepository.existsActiveLoanByCustomerId(id)) {
            throw new BusinessException(
                    "CUSTOMER_HAS_ACTIVE_LOANS",
                    "Cannot delete customer with active loans"
            );
        }

        customer.setStatus(CustomerStatus.DELETED);
        customer.setDeletedAt(LocalDateTime.now());

        customerRepository.save(customer);
    }

    public boolean hasActiveLoans(Long customerId) {
        return pawnLoanRepository.existsActiveLoanByCustomerId(customerId);
    }
}