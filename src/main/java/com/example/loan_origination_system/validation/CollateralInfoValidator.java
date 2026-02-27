package com.example.loan_origination_system.validation;

import com.example.loan_origination_system.dto.PawnLoanCreateFullRequest.CollateralInfo;

import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;

public class CollateralInfoValidator implements ConstraintValidator<ValidCollateralInfo, CollateralInfo> {

    @Override
    public void initialize(ValidCollateralInfo constraintAnnotation) {
    }

    @Override
    public boolean isValid(CollateralInfo collateralInfo, ConstraintValidatorContext context) {
        if (collateralInfo == null) {
            return true; // @NotNull should handle null case
        }
        
        // Either pawnItemId is provided (existing collateral)
        // OR new collateral details are provided (itemType and estimatedValue)
        boolean hasExistingPawnItem = collateralInfo.getPawnItemId() != null;
        boolean hasNewCollateral = collateralInfo.getItemType() != null && 
                                  !collateralInfo.getItemType().isBlank() && 
                                  collateralInfo.getEstimatedValue() != null;
        
        return hasExistingPawnItem || hasNewCollateral;
    }
}