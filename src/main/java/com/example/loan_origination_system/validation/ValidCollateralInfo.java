package com.example.loan_origination_system.validation;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import jakarta.validation.Constraint;
import jakarta.validation.Payload;

@Documented
@Constraint(validatedBy = CollateralInfoValidator.class)
@Target({ ElementType.TYPE })
@Retention(RetentionPolicy.RUNTIME)
public @interface ValidCollateralInfo {
    String message() default "Collateral info must have either pawnItemId (existing collateral) or itemType with estimatedValue (new collateral)";
    
    Class<?>[] groups() default {};
    
    Class<? extends Payload>[] payload() default {};
}