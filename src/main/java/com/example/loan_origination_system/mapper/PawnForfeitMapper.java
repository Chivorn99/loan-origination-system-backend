package com.example.loan_origination_system.mapper;

import org.mapstruct.BeanMapping;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;
import org.mapstruct.NullValuePropertyMappingStrategy;
import org.mapstruct.ReportingPolicy;

import com.example.loan_origination_system.dto.PawnForfeitRequest;
import com.example.loan_origination_system.dto.PawnForfeitResponse;
import com.example.loan_origination_system.model.loan.PawnForfeit;

@Mapper(componentModel = "spring", unmappedTargetPolicy = ReportingPolicy.IGNORE, uses = {PawnLoanMapper.class})
public interface PawnForfeitMapper {
    
    @Mapping(target = "pawnLoan", ignore = true)
    PawnForfeit toPawnForfeit(PawnForfeitRequest pawnForfeitRequest);
    
    PawnForfeitResponse toPawnForfeitResponse(PawnForfeit pawnForfeit);
    
    @BeanMapping(nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
    void updatePawnForfeitFromRequest(PawnForfeitRequest pawnForfeitRequest, @MappingTarget PawnForfeit pawnForfeit);
}