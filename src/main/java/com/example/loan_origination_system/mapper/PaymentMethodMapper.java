package com.example.loan_origination_system.mapper;

import org.mapstruct.BeanMapping;
import org.mapstruct.Mapper;
import org.mapstruct.MappingTarget;
import org.mapstruct.NullValuePropertyMappingStrategy;
import org.mapstruct.ReportingPolicy;

import com.example.loan_origination_system.dto.PaymentMethodPatchRequest;
import com.example.loan_origination_system.dto.PaymentMethodRequest;
import com.example.loan_origination_system.dto.PaymentMethodResponse;
import com.example.loan_origination_system.model.master.PaymentMethod;

@Mapper(componentModel = "spring", unmappedTargetPolicy = ReportingPolicy.IGNORE)
public interface PaymentMethodMapper {
    
    PaymentMethodResponse toPaymentMethodResponse(PaymentMethod paymentMethod);
    
    PaymentMethod toPaymentMethod(PaymentMethodRequest paymentMethodRequest);
    
    @BeanMapping(nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
    void updatePaymentMethodFromRequest(PaymentMethodRequest paymentMethodRequest, @MappingTarget PaymentMethod paymentMethod);
    
    @BeanMapping(nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
    void updatePaymentMethodFromPatchRequest(PaymentMethodPatchRequest paymentMethodPatchRequest, @MappingTarget PaymentMethod paymentMethod);
}