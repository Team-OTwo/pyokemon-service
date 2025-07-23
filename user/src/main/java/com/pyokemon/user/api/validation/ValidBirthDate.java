package com.pyokemon.user.api.validation;

import jakarta.validation.Constraint;
import jakarta.validation.Payload;
import java.lang.annotation.*;

@Documented
@Constraint(validatedBy = BirthDateValidator.class)
@Target({ElementType.FIELD})
@Retention(RetentionPolicy.RUNTIME)
public @interface ValidBirthDate {
    String message() default "생년월일이 유효하지 않습니다. 만 14세 이상 120세 이하만 가입 가능합니다.";
    Class<?>[] groups() default {};
    Class<? extends Payload>[] payload() default {};
} 