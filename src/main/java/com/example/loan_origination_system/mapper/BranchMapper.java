package com.example.loan_origination_system.mapper;

import org.mapstruct.BeanMapping;
import org.mapstruct.Mapper;
import org.mapstruct.MappingTarget;
import org.mapstruct.NullValuePropertyMappingStrategy;
import org.mapstruct.ReportingPolicy;

import com.example.loan_origination_system.dto.BranchPatchRequest;
import com.example.loan_origination_system.dto.BranchRequest;
import com.example.loan_origination_system.dto.BranchResponse;
import com.example.loan_origination_system.model.master.Branch;

@Mapper(componentModel = "spring", unmappedTargetPolicy = ReportingPolicy.IGNORE)
public interface BranchMapper {
    
    BranchResponse toBranchResponse(Branch branch);
    
    Branch toBranch(BranchRequest branchRequest);
    
    @BeanMapping(nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
    void updateBranchFromRequest(BranchRequest branchRequest, @MappingTarget Branch branch);
    
    @BeanMapping(nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
    void updateBranchFromPatchRequest(BranchPatchRequest branchPatchRequest, @MappingTarget Branch branch);
}