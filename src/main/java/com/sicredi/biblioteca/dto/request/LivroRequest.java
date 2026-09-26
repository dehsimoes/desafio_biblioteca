package com.sicredi.biblioteca.dto.request;

import com.sicredi.biblioteca.entity.GeneroPojo;
import com.sicredi.biblioteca.validation.AnoPublicacaoValido;
import jakarta.validation.constraints.*;

public record LivroRequest(

        @NotBlank(message = "Título é obrigatório")
        @Size(max = 200, message = "Título deve ter no máximo 200 caracteres")
        String titulo,

        @NotBlank(message = "Autor é obrigatório")
        @Size(max = 150, message = "Autor deve ter no máximo 150 caracteres")
        String autor,

        //Por falta de conhecimento de todas as regras existentes do ISBN, optei por utilizar regex documentados na web
        @NotBlank(message = "O ISBN é obrigatório")
        @Pattern(regexp = "^(?:\\d{9}[\\dX]|\\d{13})$",
                message = "IISBN inválido. Use 10 caracteres (com X opcional no final) ou 13 dígitos")
        String isbn,

        @NotNull(message = "Ano de publicação é obrigatório")
        @AnoPublicacaoValido
        Integer anoPublicacao,

        @NotNull(message = "Gênero é obrigatório")
        GeneroPojo genero,

        @NotNull(message = "Disponibilidade é obrigatória")
        Boolean disponivel
) {}