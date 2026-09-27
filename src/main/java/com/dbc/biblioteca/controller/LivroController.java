package com.dbc.biblioteca.controller;

import com.dbc.biblioteca.dto.request.LivroRequest;
import com.dbc.biblioteca.dto.response.LivroResponse;
import com.dbc.biblioteca.dto.response.LivroResponsePaginado;
import com.dbc.biblioteca.entity.GeneroPojo;
import com.dbc.biblioteca.service.LivroService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/livros")
@RequiredArgsConstructor
@Tag(name = "Livros")
public class LivroController {

    private final LivroService livroService;

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    @Operation(summary = "Cria novo livro")
    @ApiResponses({
            @ApiResponse(responseCode = "400", description = "Dados inválidos"),
            @ApiResponse(responseCode = "409", description = "ISBN já cadastrado")
    })
    public LivroResponse criar(@Valid @RequestBody LivroRequest request) {
        return livroService.criar(request);
    }

    @GetMapping("/{id}")
    @Operation(summary = "Busca livro por ID")
    @ApiResponses({
            @ApiResponse(responseCode = "404", description = "Livro não encontrado")
    })
    public LivroResponse buscarPorId(@PathVariable String id) {
        return livroService.buscarPorId(id);
    }

    @GetMapping
    @Operation(summary = "Lista livros com paginação")
    public LivroResponsePaginado listar(
            @Parameter(description = "Número da página (inicia em 0)")
            @RequestParam(defaultValue = "0") int pagina,

            @Parameter(description = "Tamanho da página")
            @RequestParam(defaultValue = "10") int tamanho,

            @Parameter(description = "Filtro por gênero")
            @RequestParam(required = false) GeneroPojo genero) {
        return livroService.listar(pagina, tamanho, genero);
    }

    @PutMapping("/{id}")
    @Operation(summary = "Atualiza livro")
    @ApiResponses({
            @ApiResponse(responseCode = "400", description = "Dados inválidos"),
            @ApiResponse(responseCode = "404", description = "Livro não encontrado"),
            @ApiResponse(responseCode = "409", description = "ISBN já cadastrado")
    })
    public LivroResponse atualizar(
            @PathVariable String id,
            @Valid @RequestBody LivroRequest request) {
        return livroService.atualizar(id, request);
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    @Operation(summary = "Remove livro por ID")
    @ApiResponses({
            @ApiResponse(responseCode = "404", description = "Livro não encontrado")
    })
    public void deletar(@PathVariable String id) {
        livroService.deletar(id);
    }
}