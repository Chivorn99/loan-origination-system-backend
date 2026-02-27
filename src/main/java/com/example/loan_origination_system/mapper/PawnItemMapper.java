package com.example.loan_origination_system.mapper;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.ReportingPolicy;

import com.example.loan_origination_system.dto.PawnItemRequest;
import com.example.loan_origination_system.dto.PawnItemResponse;
import com.example.loan_origination_system.model.loan.PawnItem;

@Mapper(componentModel = "spring", unmappedTargetPolicy = ReportingPolicy.IGNORE, uses = {CustomerMapper.class})
public interface PawnItemMapper {
    
    @Mapping(target = "customer", source = "customer")
    PawnItemResponse toPawnItemResponse(PawnItem pawnItem);
    
    @Mapping(target = "customer", ignore = true)
    PawnItem toPawnItem(PawnItemRequest pawnItemRequest);
}