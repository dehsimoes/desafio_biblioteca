package com.dbc.biblioteca.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.dbc.biblioteca.dto.request.LivroRequest;
import com.dbc.biblioteca.entity.GeneroPojo;
import com.dbc.biblioteca.repository.LivroRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.testcontainers.service.connection.ServiceConnection;
import org.springframework.http.MediaType;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.test.web.servlet.MockMvc;
import org.testcontainers.containers.GenericContainer;
import org.testcontainers.containers.MongoDBContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import org.testcontainers.utility.DockerImageName;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@Testcontainers
@SpringBootTest
@AutoConfigureMockMvc
class LivroControllerIT {

    @Container
    @ServiceConnection
    static MongoDBContainer mongo = new MongoDBContainer(DockerImageName.parse("mongo:7.0"));

    @Container
    static GenericContainer<?> redis = new GenericContainer<>(DockerImageName.parse("redis:7.4-alpine"))
            .withExposedPorts(6379);

    @DynamicPropertySource
    static void redisProps(DynamicPropertyRegistry registry) {
        registry.add("spring.data.redis.host", redis::getHost);
        registry.add("spring.data.redis.port", () -> redis.getMappedPort(6379));
    }

    @Autowired
    MockMvc mockMvc;

    @Autowired
    ObjectMapper objectMapper;

    @Autowired
    LivroRepository livroRepository;

    @BeforeEach
    void limparBase() {
        livroRepository.deleteAll();
    }

    private LivroRequest hobbit() {
        return new LivroRequest(
                "O Hobbit", "J.R.R. Tolkien", "9788595084742",
                1937, GeneroPojo.FANTASIA, true);
    }

    private LivroRequest domCasmurro() {
        return new LivroRequest(
                "Dom Casmurro", "Machado de Assis", "9788535910667",
                1899, GeneroPojo.ROMANCE, true);
    }

    private String criarViaApi(LivroRequest request) throws Exception {
        String body = mockMvc.perform(post("/livros")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andReturn().getResponse().getContentAsString();

        return objectMapper.readTree(body).get("id").asText();
    }

    @Test
    void post_criaLivro() throws Exception {
        mockMvc.perform(post("/livros")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(hobbit())))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").isNotEmpty())
                .andExpect(jsonPath("$.titulo").value("O Hobbit"))
                .andExpect(jsonPath("$.isbn").value("9788595084742"))
                .andExpect(jsonPath("$.dataInclusao").isNotEmpty());
    }

    @Test
    void post_dadosInvalidos() throws Exception {
        LivroRequest invalido = new LivroRequest(
                "", "", "abc", 3000, null, null);

        mockMvc.perform(post("/livros")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(invalido)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.codigo").value("DADOS_INVALIDOS"))
                .andExpect(jsonPath("$.mensagem").isNotEmpty());
    }

    @Test
    void post_isbnDuplicado() throws Exception {
        criarViaApi(hobbit());

        mockMvc.perform(post("/livros")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(hobbit())))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.codigo").value("LIVRO_ISBN_DUPLICADO"));
    }

    @Test
    void post_booleanComoString() throws Exception {
        String body = """
                {
                  "titulo": "A Revolução dos Bichos",
                  "autor": "George Orwell",
                  "isbn": "9788535909661",
                  "anoPublicacao": 1945,
                  "genero": "FANTASIA",
                  "disponivel": "true"
                }
                """;

        mockMvc.perform(post("/livros")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isBadRequest());
    }

    @Test
    void get_porId() throws Exception {
        String id = criarViaApi(domCasmurro());

        mockMvc.perform(get("/livros/{id}", id))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(id))
                .andExpect(jsonPath("$.titulo").value("Dom Casmurro"))
                .andExpect(jsonPath("$.autor").value("Machado de Assis"));
    }

    @Test
    void get_idInexistente() throws Exception {
        mockMvc.perform(get("/livros/{id}", "65a8b2c3d4e5f678901234ff"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.codigo").value("LIVRO_NAO_ENCONTRADO"));
    }

    @Test
    void get_paginado() throws Exception {
        criarViaApi(hobbit());
        criarViaApi(domCasmurro());

        mockMvc.perform(get("/livros")
                        .param("pagina", "0")
                        .param("tamanho", "10"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.livros").isArray())
                .andExpect(jsonPath("$.livros.length()").value(2))
                .andExpect(jsonPath("$.totalElementos").value(2))
                .andExpect(jsonPath("$.totalPaginas").value(1))
                .andExpect(jsonPath("$.ultimaPagina").value(true));
    }

    @Test
    void get_filtroPorGenero() throws Exception {
        criarViaApi(hobbit());
        criarViaApi(domCasmurro());

        mockMvc.perform(get("/livros")
                        .param("genero", "FANTASIA"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.livros.length()").value(1))
                .andExpect(jsonPath("$.livros[0].titulo").value("O Hobbit"));
    }

    @Test
    void put_atualizaLivro() throws Exception {
        String id = criarViaApi(hobbit());

        LivroRequest revisado = new LivroRequest(
                "O Hobbit (Edição Comemorativa)", "J.R.R. Tolkien", "9788595084742",
                2007, GeneroPojo.FANTASIA, false);

        mockMvc.perform(put("/livros/{id}", id)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(revisado)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.titulo").value("O Hobbit (Edição Comemorativa)"))
                .andExpect(jsonPath("$.anoPublicacao").value(2007))
                .andExpect(jsonPath("$.disponivel").value(false))
                .andExpect(jsonPath("$.dataAtualizacao").isNotEmpty());
    }

    @Test
    void put_idInexistente() throws Exception {
        mockMvc.perform(put("/livros/{id}", "65a8b2c3d4e5f678901234ff")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(hobbit())))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.codigo").value("LIVRO_NAO_ENCONTRADO"));
    }

    @Test
    void delete_removeLivro() throws Exception {
        String id = criarViaApi(domCasmurro());

        mockMvc.perform(delete("/livros/{id}", id))
                .andExpect(status().isNoContent());

        mockMvc.perform(get("/livros/{id}", id))
                .andExpect(status().isNotFound());
    }

    @Test
    void delete_idInexistente() throws Exception {
        mockMvc.perform(delete("/livros/{id}", "65a8b2c3d4e5f678901234ff"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.codigo").value("LIVRO_NAO_ENCONTRADO"));
    }
}