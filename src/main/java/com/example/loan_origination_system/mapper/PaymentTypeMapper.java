package com.example.loan_origination_system.mapper;

import org.mapstruct.BeanMapping;
import org.mapstruct.Mapper;
import org.mapstruct.MappingTarget;
import org.mapstruct.NullValuePropertyMappingStrategy;
import org.mapstruct.ReportingPolicy;

import com.example.loan_origination_system.dto.PaymentTypePatchRequest;
import com.example.loan_origination_system.dto.PaymentTypeRequest;
import com.example.loan_origination_system.dto.PaymentTypeResponse;
import com.example.loan_origination_system.model.master.PaymentType;

@Mapper(componentModel = "spring", unmappedTargetPolicy = ReportingPolicy.IGNORE)
public interface PaymentTypeMapper {
    
    PaymentTypeResponse toPaymentTypeResponse(PaymentType paymentType);
    
    PaymentType toPaymentType(PaymentTypeRequest paymentTypeRequest);
    
    @BeanMapping(nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
    void updatePaymentTypeFromRequest(PaymentTypeRequest paymentTypeRequest, @MappingTarget PaymentType paymentType);
    
    @BeanMapping(nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
    void updatePaymentTypeFromPatchRequest(PaymentTypePatchRequest paymentTypePatchRequest, @MappingTarget PaymentType paymentType);
}