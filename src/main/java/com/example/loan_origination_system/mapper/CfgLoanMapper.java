package com.example.loan_origination_system.mapper;

import org.mapstruct.BeanMapping;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;
import org.mapstruct.NullValuePropertyMappingStrategy;
import org.mapstruct.ReportingPolicy;

import com.example.loan_origination_system.dto.CfgLoanRequest;
import com.example.loan_origination_system.dto.CfgLoanResponse;
import com.example.loan_origination_system.model.loan.CfgLoan;

@Mapper(componentModel = "spring", unmappedTargetPolicy = ReportingPolicy.IGNORE, uses = {BranchMapper.class, CurrencyMapper.class})
public interface CfgLoanMapper {
    
    @Mapping(target = "branch", ignore = true)
    @Mapping(target = "currency", ignore = true)
    CfgLoan toCfgLoan(CfgLoanRequest cfgLoanRequest);
    
    CfgLoanResponse toCfgLoanResponse(CfgLoan cfgLoan);
    
    @BeanMapping(nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
    void updateCfgLoanFromRequest(CfgLoanRequest cfgLoanRequest, @MappingTarget CfgLoan cfgLoan);
}