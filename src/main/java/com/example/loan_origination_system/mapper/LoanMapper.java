package com.example.loan_origination_system.mapper;

import org.mapstruct.BeanMapping;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;
import org.mapstruct.NullValuePropertyMappingStrategy;
import org.mapstruct.ReportingPolicy;

import com.example.loan_origination_system.dto.BranchRequest;
import com.example.loan_origination_system.dto.BranchResponse;
import com.example.loan_origination_system.dto.CfgLoanRequest;
import com.example.loan_origination_system.dto.CfgLoanResponse;
import com.example.loan_origination_system.dto.CurrencyRequest;
import com.example.loan_origination_system.dto.CurrencyResponse;
import com.example.loan_origination_system.dto.CustomerRequest;
import com.example.loan_origination_system.dto.CustomerResponse;
import com.example.loan_origination_system.dto.PawnForfeitRequest;
import com.example.loan_origination_system.dto.PawnForfeitResponse;
import com.example.loan_origination_system.dto.PawnItemRequest;
import com.example.loan_origination_system.dto.PawnItemResponse;
import com.example.loan_origination_system.dto.PawnLoanRequest;
import com.example.loan_origination_system.dto.PawnLoanResponse;
import com.example.loan_origination_system.dto.PaymentMethodRequest;
import com.example.loan_origination_system.dto.PaymentMethodResponse;
import com.example.loan_origination_system.dto.PaymentTypeRequest;
import com.example.loan_origination_system.dto.PaymentTypeResponse;
import com.example.loan_origination_system.dto.RoleRequest;
import com.example.loan_origination_system.dto.RoleResponse;
import com.example.loan_origination_system.dto.UserRequest;
import com.example.loan_origination_system.dto.UserResponse;
import com.example.loan_origination_system.model.loan.CfgLoan;
import com.example.loan_origination_system.model.loan.PawnForfeit;
import com.example.loan_origination_system.model.loan.PawnItem;
import com.example.loan_origination_system.model.loan.PawnLoan;
import com.example.loan_origination_system.model.master.Branch;
import com.example.loan_origination_system.model.master.Currency;
import com.example.loan_origination_system.model.master.PaymentMethod;
import com.example.loan_origination_system.model.master.PaymentType;
import com.example.loan_origination_system.model.master.Role;
import com.example.loan_origination_system.model.people.Customer;
import com.example.loan_origination_system.model.people.User;

@Mapper(componentModel = "spring", unmappedTargetPolicy = ReportingPolicy.IGNORE)
public interface LoanMapper {

    // Currency mappings
    CurrencyResponse toCurrencyResponse(Currency currency);
    Currency toCurrency(CurrencyRequest currencyRequest);
    
    // Branch mappings
    BranchResponse toBranchResponse(Branch branch);
    Branch toBranch(BranchRequest branchRequest);
    
    // Customer mappings
    CustomerResponse toCustomerResponse(Customer customer);
    Customer toCustomer(CustomerRequest customerRequest);
    
    // PawnItem mappings
    @Mapping(target = "customer", source = "customer")
    PawnItemResponse toPawnItemResponse(PawnItem pawnItem);
    
    @Mapping(target = "customer", ignore = true)
    PawnItem toPawnItem(PawnItemRequest pawnItemRequest);
    
    // PawnLoan mappings
    @Mapping(target = "customer", source = "customer")
    @Mapping(target = "pawnItem", source = "pawnItem")
    @Mapping(target = "currency", source = "currency")
    @Mapping(target = "branch", source = "branch")
    PawnLoanResponse toPawnLoanResponse(PawnLoan pawnLoan);
    
    @Mapping(target = "customer", ignore = true)
    @Mapping(target = "pawnItem", ignore = true)
    @Mapping(target = "currency", ignore = true)
    @Mapping(target = "branch", ignore = true)
    @Mapping(target = "loanCode", ignore = true)
    @Mapping(target = "totalPayableAmount", ignore = true)
    @Mapping(target = "status", ignore = true)
    PawnLoan toPawnLoan(PawnLoanRequest pawnLoanRequest);
    
    // Update mappings
    @BeanMapping(nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
    void updateCurrencyFromRequest(CurrencyRequest currencyRequest, @MappingTarget Currency currency);
    
    @BeanMapping(nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
    void updateBranchFromRequest(BranchRequest branchRequest, @MappingTarget Branch branch);
    
    @BeanMapping(nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
    void updateCustomerFromRequest(CustomerRequest customerRequest, @MappingTarget Customer customer);
    
    // CfgLoan mappings
    @Mapping(target = "branch", ignore = true)
    @Mapping(target = "currency", ignore = true)
    CfgLoan toCfgLoan(CfgLoanRequest cfgLoanRequest);
    
    CfgLoanResponse toCfgLoanResponse(CfgLoan cfgLoan);
    
    @BeanMapping(nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
    void updateCfgLoanFromRequest(CfgLoanRequest cfgLoanRequest, @MappingTarget CfgLoan cfgLoan);
    
    // PaymentMethod mappings
    PaymentMethodResponse toPaymentMethodResponse(PaymentMethod paymentMethod);
    
    PaymentMethod toPaymentMethod(PaymentMethodRequest paymentMethodRequest);
    
    @BeanMapping(nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
    void updatePaymentMethodFromRequest(PaymentMethodRequest paymentMethodRequest, @MappingTarget PaymentMethod paymentMethod);
    
    // PaymentType mappings
    PaymentTypeResponse toPaymentTypeResponse(PaymentType paymentType);
    
    PaymentType toPaymentType(PaymentTypeRequest paymentTypeRequest);
    
    @BeanMapping(nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
    void updatePaymentTypeFromRequest(PaymentTypeRequest paymentTypeRequest, @MappingTarget PaymentType paymentType);
    
    // Role mappings
    RoleResponse toRoleResponse(Role role);
    
    Role toRole(RoleRequest roleRequest);
    
    @BeanMapping(nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
    void updateRoleFromRequest(RoleRequest roleRequest, @MappingTarget Role role);
    
    // User mappings
    @Mapping(target = "role", ignore = true)
    @Mapping(target = "branch", ignore = true)
    User toUser(UserRequest userRequest);
    
    UserResponse toUserResponse(User user);
    
    @BeanMapping(nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
    void updateUserFromRequest(UserRequest userRequest, @MappingTarget User user);
    
    // PawnForfeit mappings
    @Mapping(target = "pawnLoan", ignore = true)
    PawnForfeit toPawnForfeit(PawnForfeitRequest pawnForfeitRequest);
    
    PawnForfeitResponse toPawnForfeitResponse(PawnForfeit pawnForfeit);
    
    @BeanMapping(nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
    void updatePawnForfeitFromRequest(PawnForfeitRequest pawnForfeitRequest, @MappingTarget PawnForfeit pawnForfeit);
}