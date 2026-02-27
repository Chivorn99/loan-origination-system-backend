package com.example.loan_origination_system.mapper;

import org.mapstruct.BeanMapping;
import org.mapstruct.Mapper;
import org.mapstruct.MappingTarget;
import org.mapstruct.NullValuePropertyMappingStrategy;
import org.mapstruct.ReportingPolicy;

import com.example.loan_origination_system.dto.CustomerPatchRequest;
import com.example.loan_origination_system.dto.CustomerRequest;
import com.example.loan_origination_system.dto.CustomerResponse;
import com.example.loan_origination_system.model.people.Customer;

@Mapper(componentModel = "spring", unmappedTargetPolicy = ReportingPolicy.IGNORE)
public interface CustomerMapper {
    
    CustomerResponse toCustomerResponse(Customer customer);
    
    Customer toCustomer(CustomerRequest customerRequest);
    
    @BeanMapping(nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
    void updateCustomerFromRequest(CustomerRequest customerRequest, @MappingTarget Customer customer);
    
    @BeanMapping(nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
    void updateCustomerFromPatchRequest(CustomerPatchRequest customerPatchRequest, @MappingTarget Customer customer);
}