# Biblioteca — API REST de Livros

API REST para gerenciamento de livros, feita como parte do desafio técnico da DBC Company.
Spring Boot 3 + MongoDB + Redis, com cache nas leituras por ID.

## Como rodar

Precisa de Docker e Java 21.

```bash
# Sobe MongoDB e Redis
docker compose up -d

# Roda a aplicação
./mvnw spring-boot:run
```

Swagger em http://localhost:8080/swagger-ui.html

## Stack

- Java 21
- Spring Boot 3.5
- Spring Data MongoDB e Redis
- Lombok, ModelMapper
- SpringDoc OpenAPI
- JUnit 5, Mockito, Testcontainers

## Endpoints

| Método | Rota | O que faz |
|---|---|---|
| POST | `/livros` | Cadastra livro (valida ISBN único) |
| GET | `/livros/{id}` | Busca por ID (com cache Redis) |
| GET | `/livros` | Lista paginada, filtro opcional por gênero |
| PUT | `/livros/{id}` | Atualiza livro |
| DELETE | `/livros/{id}` | Remove livro |

Exemplo:

```bash
curl -X POST localhost:8080/livros \
  -H "Content-Type: application/json" \
  -d '{
    "titulo": "Dom Casmurro",
    "autor": "Machado de Assis",
    "isbn": "9788535910667",
    "anoPublicacao": 1899,
    "genero": "ROMANCE",
    "disponivel": true
  }'
```

## Cache

Uso `@Cacheable` no `buscarPorId` e `@CacheEvict` no `atualizar` e `deletar`.
Chave no Redis: `biblioteca:livro:{id}`, TTL de 10 minutos.

Escolhi invalidar em vez de sobrescrever o cache na escrita.
Se duas requisições concorrentes (uma leitura e uma escrita) acontecem juntas, invalidar
evita deixar dado velho no cache por até 10 minutos.

Pra ver funcionando:

```bash
# Primeira chamada — cache miss, vai no Mongo
curl localhost:8080/livros/{id}

# Segunda chamada — cache hit, nem aparece log no service
curl localhost:8080/livros/{id}
```

## Estrutura

```
com.dbc.biblioteca
├── config        # beans de configuração (ModelMapper, Redis, OpenAPI, Jackson)
├── controller    # endpoints REST
├── dto           # request/response como records
|── entity        # Livro e o enum GeneroPojo
├── exception     # NegocioException + handler global
├── repository    # LivroRepository (Spring Data)
├── service       # regras de negócio e cache
└── validation    # validador customizado de ano de publicação
```

## Decisões que tomei

**`JacksonConfig` restringindo boolean.** Adicionei depois de testar no Postman e ver
que a API aceitava `"disponivel": "true"` (string) como `true` e também números como 19 geravam `true`.
Com a config, vira 400.

- **Validador de ISBN.** Por não conhecer o ISBN, fiz algumas consulta e utilizei uma regex pronta de validador de ISBN.

**Cache invalida, não atualiza.** Ver seção "Cache" acima.

## Testes

```bash
./mvnw clean verify
```

O relatório de cobertura sai em `target/site/jacoco/index.html`. Configurei o jacoco
pra excluir DTOs, entities, exceptions e configs da regra de 80%, acredito que testar os geters e seters não agregaria no desafio

## O que faria diferente com mais tempo

- **Índice composto** em `genero + anoPublicacao` se surgirem filtros combinados.
- **Validador de ISBN.** Hoje só valido formato (10 ou 13 dígitos). Pelo que entendi, um
  validador completo tem um dígito verificador
