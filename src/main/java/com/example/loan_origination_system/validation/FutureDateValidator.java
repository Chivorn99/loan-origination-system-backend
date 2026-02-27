package com.example.loan_origination_system.validation;

import java.time.LocalDate;

import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;

public class FutureDateValidator implements ConstraintValidator<FutureDate, LocalDate> {

    private boolean includeToday;

    @Override
    public void initialize(FutureDate constraintAnnotation) {
        this.includeToday = constraintAnnotation.includeToday();
    }

    @Override
    public boolean isValid(LocalDate date, ConstraintValidatorContext context) {
        if (date == null) {
            return true; // Let @NotNull handle null cases
        }
        
        LocalDate today = LocalDate.now();
        
        if (includeToday) {
            return !date.isBefore(today); // date >= today
        } else {
            return date.isAfter(today); // date > today
        }
    }
}