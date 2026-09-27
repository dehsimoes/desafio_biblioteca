package com.dbc.biblioteca.service;

import com.dbc.biblioteca.dto.request.LivroRequest;
import com.dbc.biblioteca.dto.response.LivroResponse;
import com.dbc.biblioteca.dto.response.LivroResponsePaginado;
import com.dbc.biblioteca.entity.GeneroPojo;
import com.dbc.biblioteca.entity.Livro;
import com.dbc.biblioteca.exception.NegocioException;
import com.dbc.biblioteca.repository.LivroRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.modelmapper.ModelMapper;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;

@Slf4j
@Service
@RequiredArgsConstructor
public class LivroService {

    private static final String CACHE_NAME = "biblioteca:livro";

    private final LivroRepository livroRepository;
    private final ModelMapper modelMapper;

    public LivroResponse criar(LivroRequest request) {
        if (livroRepository.existsByIsbn(request.isbn())) {
            throw new NegocioException(
                    "LIVRO_ISBN_DUPLICADO",
                    "Já existe um livro cadastrado com o ISBN '" + request.isbn() + "'.",
                    HttpStatus.CONFLICT);
        }

        Livro livro = modelMapper.map(request, Livro.class);
        livro.setDataInclusao(LocalDateTime.now());
        livro.setDataAtualizacao(LocalDateTime.now());

        Livro salvo = livroRepository.save(livro);

        log.info("Livro criado. id={}, isbn={}", salvo.getId(), salvo.getIsbn());

        return toResponse(salvo);
    }

    @Cacheable(value = CACHE_NAME, key = "#id")
    public LivroResponse buscarPorId(String id) {
        log.info("Buscando livro por ID: {} (consulta do banco)", id);

        Livro livro = livroRepository.findById(id)
                .orElseThrow(() -> new NegocioException(
                        "LIVRO_NAO_ENCONTRADO",
                        "Livro com id '" + id + "' não encontrado.",
                        HttpStatus.NOT_FOUND));

        return toResponse(livro);
    }

    public LivroResponsePaginado listar(int pagina, int tamanho, GeneroPojo genero) {
        Pageable pageable = PageRequest.of(pagina, tamanho);

        Page<Livro> page = (genero != null)
                ? livroRepository.findByGenero(genero, pageable)
                : livroRepository.findAll(pageable);

        return new LivroResponsePaginado(
                page.getContent().stream()
                        .map(this::toResponse)
                        .toList(),
                page.getNumber(),
                page.getSize(),
                page.getTotalElements(),
                page.getTotalPages(),
                page.isLast()
        );
    }

    @CacheEvict(value = CACHE_NAME, key = "#id")
    public LivroResponse atualizar(String id, LivroRequest request) {
        log.info("Atualizando livro ID: {}", id);

        Livro livro = livroRepository.findById(id)
                .orElseThrow(() -> new NegocioException(
                        "LIVRO_NAO_ENCONTRADO",
                        "Livro com id '" + id + "' não encontrado.",
                        HttpStatus.NOT_FOUND));

        if (!livro.getIsbn().equals(request.isbn())
                && livroRepository.existsByIsbn(request.isbn())) {
            throw new NegocioException(
                    "LIVRO_ISBN_DUPLICADO",
                    "Já existe um livro cadastrado com o ISBN '" + request.isbn() + "'.",
                    HttpStatus.CONFLICT);
        }

        modelMapper.map(request, livro);
        livro.setDataAtualizacao(LocalDateTime.now());

        Livro atualizado = livroRepository.save(livro);

        log.info("Livro atualizado. id={}, isbn={}", atualizado.getId(), atualizado.getIsbn());

        return toResponse(atualizado);
    }

    @CacheEvict(value = CACHE_NAME, key = "#id")
    public void deletar(String id) {
        log.info("Deletando livro ID: {}", id);

        if (!livroRepository.existsById(id)) {
            throw new NegocioException(
                    "LIVRO_NAO_ENCONTRADO",
                    "Livro com id '" + id + "' não encontrado.",
                    HttpStatus.NOT_FOUND);
        }

        livroRepository.deleteById(id);
        log.info("Livro deletado: {}", id);
    }

    private LivroResponse toResponse(Livro livro) {
        return new LivroResponse(
                livro.getId(),
                livro.getTitulo(),
                livro.getAutor(),
                livro.getIsbn(),
                livro.getAnoPublicacao(),
                livro.getGenero(),
                livro.getDisponivel(),
                livro.getDataInclusao(),
                livro.getDataAtualizacao()
        );
    }
}