package com.sicredi.biblioteca.repository;

import com.sicredi.biblioteca.entity.GeneroPojo;
import com.sicredi.biblioteca.entity.Livro;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.mongodb.repository.MongoRepository;

import java.util.Optional;

public interface LivroRepository extends MongoRepository<Livro, String> {

    Optional<Livro> findByIsbn(String isbn);

    boolean existsByIsbn(String isbn);

    Page<Livro> findByGenero(GeneroPojo genero, Pageable pageable);
}