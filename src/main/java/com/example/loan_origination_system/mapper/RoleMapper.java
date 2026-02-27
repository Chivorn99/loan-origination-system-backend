package com.example.loan_origination_system.mapper;

import org.mapstruct.BeanMapping;
import org.mapstruct.Mapper;
import org.mapstruct.MappingTarget;
import org.mapstruct.NullValuePropertyMappingStrategy;
import org.mapstruct.ReportingPolicy;

import com.example.loan_origination_system.dto.RolePatchRequest;
import com.example.loan_origination_system.dto.RoleRequest;
import com.example.loan_origination_system.dto.RoleResponse;
import com.example.loan_origination_system.model.master.Role;

@Mapper(componentModel = "spring", unmappedTargetPolicy = ReportingPolicy.IGNORE)
public interface RoleMapper {
    
    RoleResponse toRoleResponse(Role role);
    
    Role toRole(RoleRequest roleRequest);
    
    @BeanMapping(nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
    void updateRoleFromRequest(RoleRequest roleRequest, @MappingTarget Role role);
    
    @BeanMapping(nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
    void updateRoleFromPatchRequest(RolePatchRequest rolePatchRequest, @MappingTarget Role role);
}