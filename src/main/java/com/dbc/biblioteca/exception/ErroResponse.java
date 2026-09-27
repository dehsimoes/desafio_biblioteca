package com.dbc.biblioteca.exception;

import java.time.LocalDateTime;

public record ErroResponse(
        String codigo,
        String mensagem,
        LocalDateTime timestamp
) {
    public ErroResponse(String codigo, String mensagem) {
        this(codigo, mensagem, LocalDateTime.now());
    }
}