package com.example.loan_origination_system.validation;

import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;

public class NationalIdValidator implements ConstraintValidator<ValidNationalId, String> {

    @Override
    public void initialize(ValidNationalId constraintAnnotation) {
    }

    @Override
    public boolean isValid(String nationalId, ConstraintValidatorContext context) {
        if (nationalId == null || nationalId.isBlank()) {
            return true; // Let @NotBlank handle empty/null cases
        }
        
        // Must be exactly 9 digits and contain only numbers
        return nationalId.matches("^\\d{9}$");
    }
}