package com.sicredi.biblioteca.validation;

import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;

import java.time.Year;

public class AnoPublicacaoValidator
        implements ConstraintValidator<AnoPublicacaoValido, Integer> {

    @Override
    public boolean isValid(
            Integer anoPublicacao,
            ConstraintValidatorContext context) {

        if (anoPublicacao == null) {
            return true;
        }

        int anoAtual = Year.now().getValue();

        return anoPublicacao >= 1001
                && anoPublicacao <= anoAtual;
    }
}