package com.pyokemon.user.api.validation;

import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;
import java.time.LocalDate;
import java.time.Period;

public class BirthDateValidator implements ConstraintValidator<ValidBirthDate, LocalDate> {
    private static final int MIN_AGE = 14;
    private static final int MAX_AGE = 120;

    @Override
    public void initialize(ValidBirthDate constraintAnnotation) {
    }

    @Override
    public boolean isValid(LocalDate birthDate, ConstraintValidatorContext context) {
        if (birthDate == null) {
            return false;
        }

        LocalDate now = LocalDate.now();
        
        // 미래 날짜 체크
        if (birthDate.isAfter(now)) {
            return false;
        }

        // 나이 계산
        int age = Period.between(birthDate, now).getYears();
        
        // 최소 나이(14세)와 최대 나이(120세) 체크
        return age >= MIN_AGE && age <= MAX_AGE;
    }
} 