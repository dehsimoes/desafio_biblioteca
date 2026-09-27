package com.dbc.biblioteca.dto.response;

import com.dbc.biblioteca.entity.GeneroPojo;

import java.time.LocalDateTime;

public record LivroResponse(
        String id,
        String titulo,
        String autor,
        String isbn,
        Integer anoPublicacao,
        GeneroPojo genero,
        Boolean disponivel,
        LocalDateTime dataInclusao,
        LocalDateTime dataAtualizacao
) {}