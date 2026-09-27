package com.dbc.biblioteca.service;

import com.dbc.biblioteca.dto.request.LivroRequest;
import com.dbc.biblioteca.dto.response.LivroResponse;
import com.dbc.biblioteca.dto.response.LivroResponsePaginado;
import com.dbc.biblioteca.entity.GeneroPojo;
import com.dbc.biblioteca.entity.Livro;
import com.dbc.biblioteca.exception.NegocioException;
import com.dbc.biblioteca.repository.LivroRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.modelmapper.ModelMapper;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class LivroServiceTest {

    private static final String ID_CLEAN_CODE = "65a8b2c3d4e5f6789012345a";
    private static final String ID_REVOLUCAO_DOS_BICHOS = "65a8b2c3d4e5f6789012345b";

    @Mock
    private LivroRepository livroRepository;

    @Mock
    private ModelMapper modelMapper;

    @InjectMocks
    private LivroService livroService;

    private LivroRequest cleanCodeRequest() {
        return new LivroRequest(
                "Clean Code", "Robert C. Martin", "9780132350884",
                2008, GeneroPojo.TECNOLOGIA, true);
    }

    private Livro cleanCodeSalvo() {
        return Livro.builder()
                .id(ID_CLEAN_CODE)
                .titulo("Clean Code")
                .autor("Robert C. Martin")
                .isbn("9780132350884")
                .anoPublicacao(2008)
                .genero(GeneroPojo.TECNOLOGIA)
                .disponivel(true)
                .dataInclusao(LocalDateTime.now())
                .dataAtualizacao(LocalDateTime.now())
                .build();
    }

    @Test
    void criar_salvaLivroNovo() {
        var request = cleanCodeRequest();
        var livroSalvo = cleanCodeSalvo();

        when(livroRepository.existsByIsbn(request.isbn())).thenReturn(false);
        when(modelMapper.map(request, Livro.class)).thenReturn(livroSalvo);
        when(livroRepository.save(any(Livro.class))).thenReturn(livroSalvo);

        LivroResponse response = livroService.criar(request);

        assertThat(response.id()).isEqualTo(ID_CLEAN_CODE);
        assertThat(response.titulo()).isEqualTo("Clean Code");
        assertThat(response.isbn()).isEqualTo("9780132350884");
        verify(livroRepository).save(any(Livro.class));
    }

    @Test
    void criar_rejeitaIsbnDuplicado() {
        var request = cleanCodeRequest();
        when(livroRepository.existsByIsbn(request.isbn())).thenReturn(true);

        assertThatThrownBy(() -> livroService.criar(request))
                .isInstanceOf(NegocioException.class)
                .hasMessageContaining("9780132350884")
                .extracting("codigo").isEqualTo("LIVRO_ISBN_DUPLICADO");

        verify(livroRepository, never()).save(any());
    }

    @Test
    void buscarPorId_encontraLivro() {
        when(livroRepository.findById(ID_CLEAN_CODE)).thenReturn(Optional.of(cleanCodeSalvo()));

        LivroResponse response = livroService.buscarPorId(ID_CLEAN_CODE);

        assertThat(response.titulo()).isEqualTo("Clean Code");
        assertThat(response.autor()).isEqualTo("Robert C. Martin");
    }

    @Test
    void buscarPorId_livroInexistente() {
        when(livroRepository.findById(ID_CLEAN_CODE)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> livroService.buscarPorId(ID_CLEAN_CODE))
                .isInstanceOf(NegocioException.class)
                .hasMessageContaining(ID_CLEAN_CODE)
                .extracting("codigo").isEqualTo("LIVRO_NAO_ENCONTRADO");
    }

    @Test
    void listar_semFiltro() {
        Pageable pageable = PageRequest.of(0, 10);
        Page<Livro> page = new PageImpl<>(List.of(cleanCodeSalvo()), pageable, 1);

        when(livroRepository.findAll(pageable)).thenReturn(page);

        LivroResponsePaginado response = livroService.listar(0, 10, null);

        assertThat(response.livros()).hasSize(1);
        assertThat(response.totalElementos()).isEqualTo(1);
        verify(livroRepository).findAll(pageable);
        verify(livroRepository, never()).findByGenero(any(), any());
    }

    @Test
    void listar_comFiltroDeGenero() {
        Pageable pageable = PageRequest.of(0, 10);
        Page<Livro> page = new PageImpl<>(List.of(cleanCodeSalvo()), pageable, 1);

        when(livroRepository.findByGenero(GeneroPojo.TECNOLOGIA, pageable)).thenReturn(page);

        LivroResponsePaginado response = livroService.listar(0, 10, GeneroPojo.TECNOLOGIA);

        assertThat(response.livros()).hasSize(1);
        verify(livroRepository).findByGenero(GeneroPojo.TECNOLOGIA, pageable);
        verify(livroRepository, never()).findAll(pageable);
    }

    @Test
    void atualizar_persisteAlteracoes() {
        var request = new LivroRequest(
                "Clean Code", "Robert C. Martin", "9780132350884",
                2008, GeneroPojo.TECNOLOGIA, false);

        var livroAtualizado = cleanCodeSalvo();
        livroAtualizado.setDisponivel(false);

        when(livroRepository.findById(ID_CLEAN_CODE)).thenReturn(Optional.of(cleanCodeSalvo()));
        when(livroRepository.save(any(Livro.class))).thenReturn(livroAtualizado);

        LivroResponse response = livroService.atualizar(ID_CLEAN_CODE, request);

        assertThat(response.disponivel()).isFalse();
        verify(livroRepository).save(any(Livro.class));
    }

    @Test
    void atualizar_livroInexistente() {
        when(livroRepository.findById(ID_CLEAN_CODE)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> livroService.atualizar(ID_CLEAN_CODE, cleanCodeRequest()))
                .isInstanceOf(NegocioException.class)
                .extracting("codigo").isEqualTo("LIVRO_NAO_ENCONTRADO");
    }

    @Test
    void atualizar_isbnDeOutroLivro() {
        var outroLivro = Livro.builder()
                .id(ID_CLEAN_CODE)
                .titulo("A Revolução dos Bichos")
                .autor("George Orwell")
                .isbn("9788535909661")
                .build();

        when(livroRepository.findById(ID_CLEAN_CODE)).thenReturn(Optional.of(outroLivro));
        when(livroRepository.existsByIsbn("9780132350884")).thenReturn(true);

        assertThatThrownBy(() -> livroService.atualizar(ID_CLEAN_CODE, cleanCodeRequest()))
                .isInstanceOf(NegocioException.class)
                .extracting("codigo").isEqualTo("LIVRO_ISBN_DUPLICADO");
    }

    @Test
    void deletar_removeLivro() {
        when(livroRepository.existsById(ID_CLEAN_CODE)).thenReturn(true);

        livroService.deletar(ID_CLEAN_CODE);

        verify(livroRepository).deleteById(ID_CLEAN_CODE);
    }

    @Test
    void deletar_livroInexistente() {
        when(livroRepository.existsById(ID_REVOLUCAO_DOS_BICHOS)).thenReturn(false);

        assertThatThrownBy(() -> livroService.deletar(ID_REVOLUCAO_DOS_BICHOS))
                .isInstanceOf(NegocioException.class)
                .extracting("codigo").isEqualTo("LIVRO_NAO_ENCONTRADO");

        verify(livroRepository, never()).deleteById(any());
    }
}