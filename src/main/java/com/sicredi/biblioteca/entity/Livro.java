package com.sicredi.biblioteca.entity;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Document(collection = "livros")
public class Livro {

    @Id
    private String id;

    private String titulo;
    private String autor;

    @Indexed(unique = true)
    private String isbn;

    private Integer anoPublicacao;
    private GeneroPojo genero;
    private Boolean disponivel;

    private LocalDateTime dataInclusao;
    private LocalDateTime dataAtualizacao;
}