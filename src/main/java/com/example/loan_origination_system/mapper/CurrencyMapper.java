package com.example.loan_origination_system.mapper;

import org.mapstruct.BeanMapping;
import org.mapstruct.Mapper;
import org.mapstruct.MappingTarget;
import org.mapstruct.NullValuePropertyMappingStrategy;
import org.mapstruct.ReportingPolicy;

import com.example.loan_origination_system.dto.CurrencyPatchRequest;
import com.example.loan_origination_system.dto.CurrencyRequest;
import com.example.loan_origination_system.dto.CurrencyResponse;
import com.example.loan_origination_system.model.master.Currency;

@Mapper(componentModel = "spring", unmappedTargetPolicy = ReportingPolicy.IGNORE)
public interface CurrencyMapper {
    
    CurrencyResponse toCurrencyResponse(Currency currency);
    
    Currency toCurrency(CurrencyRequest currencyRequest);
    
    @BeanMapping(nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
    void updateCurrencyFromRequest(CurrencyRequest currencyRequest, @MappingTarget Currency currency);
    
    @BeanMapping(nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
    void updateCurrencyFromPatchRequest(CurrencyPatchRequest currencyPatchRequest, @MappingTarget Currency currency);
}