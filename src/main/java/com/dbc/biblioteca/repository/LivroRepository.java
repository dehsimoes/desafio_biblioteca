package com.dbc.biblioteca.repository;

import com.dbc.biblioteca.entity.GeneroPojo;
import com.dbc.biblioteca.entity.Livro;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.mongodb.repository.MongoRepository;

public interface LivroRepository extends MongoRepository<Livro, String> {

    boolean existsByIsbn(String isbn);

    Page<Livro> findByGenero(GeneroPojo genero, Pageable pageable);
}