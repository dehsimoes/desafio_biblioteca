package com.dbc.biblioteca.validation;

import jakarta.validation.Constraint;
import jakarta.validation.Payload;

import java.lang.annotation.*;

@Documented
@Constraint(validatedBy = AnoPublicacaoValidator.class)
@Target({ElementType.FIELD, ElementType.PARAMETER, ElementType.RECORD_COMPONENT})
@Retention(RetentionPolicy.RUNTIME)
public @interface AnoPublicacaoValido {

    String message() default "O ano de publicação deve estar entre 1001 e o ano atual";

    Class<?>[] groups() default {};

    Class<? extends Payload>[] payload() default {};
}
