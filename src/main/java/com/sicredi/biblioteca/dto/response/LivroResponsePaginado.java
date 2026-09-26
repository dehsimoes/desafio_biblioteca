package com.sicredi.biblioteca.dto.response;

import java.util.List;

public record LivroResponsePaginado(
        List<LivroResponse> livros,
        int pagina,
        int tamanho,
        long totalElementos,
        int totalPaginas,
        boolean ultimaPagina
) {}