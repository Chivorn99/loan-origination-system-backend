package com.example.loan_origination_system.mapper;

import org.mapstruct.BeanMapping;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;
import org.mapstruct.NullValuePropertyMappingStrategy;
import org.mapstruct.ReportingPolicy;

import com.example.loan_origination_system.dto.UserPatchRequest;
import com.example.loan_origination_system.dto.UserRequest;
import com.example.loan_origination_system.dto.UserResponse;
import com.example.loan_origination_system.model.people.User;

@Mapper(componentModel = "spring", unmappedTargetPolicy = ReportingPolicy.IGNORE, uses = {BranchMapper.class, RoleMapper.class})
public interface UserMapper {
    
    @Mapping(target = "role", ignore = true)
    @Mapping(target = "branch", ignore = true)
    User toUser(UserRequest userRequest);
    
    UserResponse toUserResponse(User user);
    
    @BeanMapping(nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
    void updateUserFromRequest(UserRequest userRequest, @MappingTarget User user);
    
    @BeanMapping(nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
    void updateUserFromPatchRequest(UserPatchRequest userPatchRequest, @MappingTarget User user);
}