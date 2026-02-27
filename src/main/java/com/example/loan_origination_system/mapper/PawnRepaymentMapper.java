package com.example.loan_origination_system.mapper;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.ReportingPolicy;

import com.example.loan_origination_system.dto.PawnRepaymentResponse;
import com.example.loan_origination_system.model.loan.PawnRepayment;

@Mapper(componentModel = "spring", unmappedTargetPolicy = ReportingPolicy.IGNORE, uses = {CurrencyMapper.class, PaymentMethodMapper.class, PaymentTypeMapper.class, UserMapper.class})
public interface PawnRepaymentMapper {
    
    @Mapping(target = "pawnLoanId", source = "pawnLoan.id")
    @Mapping(target = "loanCode", source = "pawnLoan.loanCode")
    PawnRepaymentResponse toPawnRepaymentResponse(PawnRepayment pawnRepayment);
}