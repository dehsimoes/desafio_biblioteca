package com.dbc.biblioteca.validation;

import org.junit.jupiter.api.Test;

import java.time.Year;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class AnoPublicacaoValidatorTest {
    private final AnoPublicacaoValidator validator = new AnoPublicacaoValidator();

    @Test
    void aceitaAnoValido() {
        assertTrue(validator.isValid(2008, null));
    }

    @Test
    void aceitaNull() {
        assertTrue(validator.isValid(null, null));
    }

    @Test
    void rejeitaAnoFuturo() {
        assertFalse(validator.isValid(Year.now().getValue() + 1, null));
    }

    @Test
    void rejeitaAnoAntesDe1001() {
        assertFalse(validator.isValid(1000, null));
    }
}
