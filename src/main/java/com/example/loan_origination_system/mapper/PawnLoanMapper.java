package com.example.loan_origination_system.mapper;

import org.mapstruct.BeanMapping;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;
import org.mapstruct.NullValuePropertyMappingStrategy;
import org.mapstruct.ReportingPolicy;

import com.example.loan_origination_system.dto.PawnLoanRequest;
import com.example.loan_origination_system.dto.PawnLoanResponse;
import com.example.loan_origination_system.model.loan.PawnLoan;

@Mapper(componentModel = "spring", unmappedTargetPolicy = ReportingPolicy.IGNORE, uses = {CustomerMapper.class, PawnItemMapper.class, CurrencyMapper.class, BranchMapper.class})
public interface PawnLoanMapper {
    
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
    
    @BeanMapping(nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
    void updatePawnLoanFromRequest(PawnLoanRequest pawnLoanRequest, @MappingTarget PawnLoan pawnLoan);
}