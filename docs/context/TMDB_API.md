# Integração com TMDB

## Base e autenticação

- Base URL v3: `https://api.themoviedb.org/3/`
- Autenticação do aplicativo: header Bearer com o API Read Access Token.
- Header recomendado: `Accept: application/json`.
- Idioma padrão do app: `pt-BR`.
- Região padrão quando aplicável: `BR`.
- Conteúdo adulto: `include_adult=false`.

O token entra pelo ambiente de build local e é fornecido ao interceptor do OkHttp. Não inclua o token em query string, código, recursos, fixtures ou logs.

## Endpoints da primeira versão

### Filmes para descoberta

```http
GET /3/discover/movie
```

Uso: tela sem consulta de texto. Parâmetros previstos:

- `page`
- `language=pt-BR`
- `region=BR`
- `include_adult=false`
- `sort_by`
- `with_genres`
- `vote_average.gte`
- `primary_release_year` ou intervalo de data, se escolhido pela UI

### Pesquisa por título

```http
GET /3/search/movie
```

Uso: consulta não vazia. Parâmetros previstos:

- `query`
- `page`
- `language=pt-BR`
- `include_adult=false`
- `primary_release_year`, se a UI permitir ano durante a pesquisa

### Detalhes

```http
GET /3/movie/{movie_id}
```

Parâmetros:

- `language=pt-BR`

### Gêneros

```http
GET /3/genre/movie/list
```

Parâmetros:

- `language=pt-BR`

### Popular

```http
GET /3/movie/popular
```

É válido para uma implementação mínima, mas a arquitetura escolhida usa `discover/movie` como fonte padrão porque o mesmo endpoint suporta filtros. Não mantenha dois caminhos equivalentes sem necessidade.

## Política de busca e filtros

Search e discover possuem capacidades diferentes. Para não entregar resultados enganosos:

- consulta vazia → `discover/movie`, com gênero, ordenação, nota mínima e ano;
- consulta preenchida → `search/movie`, com texto e somente parâmetros oficialmente suportados, como ano;
- filtros incompatíveis ficam desabilitados ou ocultos durante a pesquisa;
- não filtre localmente apenas os itens já carregados de uma lista paginada e apresente isso como resultado global;
- ao trocar modo, filtro ou consulta, crie um novo `Pager`/`PagingSource` e retorne à primeira página.

## Paginação

A resposta paginada deve mapear:

```kotlin
@Serializable
data class PagedResponseDto<T>(
    val page: Int,
    val results: List<T>,
    @SerialName("total_pages") val totalPages: Int,
    @SerialName("total_results") val totalResults: Int,
)
```

Regras de `PagingSource.load`:

- página inicial = 1;
- `prevKey = null` quando página 1;
- `nextKey = null` quando `page >= totalPages` ou resultados vazios;
- erro de rede/parsing → `LoadResult.Error`;
- chave de refresh deriva da posição âncora quando possível;
- respeite limites documentados pela API caso ela restrinja páginas acessíveis.

## Modelos e nullabilidade

O JSON externo não é confiável o suficiente para assumir todos os campos presentes. DTOs devem aceitar ausência onde a API pode omitir valor.

Campos úteis de lista:

- `id`
- `title`
- `overview`
- `poster_path`
- `backdrop_path`
- `release_date`
- `vote_average`
- `genre_ids`

Campos úteis de detalhes:

- dados acima;
- `genres` com objetos;
- `runtime`, se exibido como melhoria opcional.

Mapeamento deve:

- manter `movieId` como `Long`;
- tratar data inválida ou vazia sem exceção;
- normalizar sinopse vazia para fallback na camada de UI;
- preservar o caminho relativo da imagem, não uma URL fixa dentro do banco.

## Imagens

A URL de imagem é formada por base + tamanho + caminho retornado pela API. Para este desafio, um helper central pode produzir URLs como:

```text
https://image.tmdb.org/t/p/w500/{poster_path}
```

Não concatene em cada Composable/Fragment. Use Coil com placeholder e fallback. O `contentDescription` deve ser nulo para imagem puramente decorativa ou descrever o pôster quando ele acrescentar informação.

Uma evolução mais robusta pode consultar `/configuration` e usar os tamanhos retornados pelo TMDB, mas isso não é obrigatório para a primeira versão.

## Erros HTTP

Tratamento mínimo:

- `401`: token ausente/inválido; mensagem de configuração em build de desenvolvimento.
- `404`: filme não encontrado/removido.
- `429`: limite de requisições; permitir tentar novamente posteriormente.
- `5xx`: indisponibilidade temporária.
- timeout/sem conexão: mensagem offline e retry.
- falha de serialização: registrar de forma segura e mostrar erro genérico.

Não registre Authorization header nem corpo que contenha segredo.

## Atribuição obrigatória

Inclua em uma tela/seção de créditos:

- logotipo oficial do TMDB conforme diretrizes de uso;
- aviso textual exato:

> This product uses the TMDB API but is not endorsed or certified by TMDB.

O TMDB não deve ser apresentado como patrocinador ou certificador do aplicativo.

## Referências oficiais

- TMDB — Getting started: https://developer.themoviedb.org/docs/getting-started
- TMDB — Application authentication: https://developer.themoviedb.org/docs/authentication-application
- TMDB — Discover movie: https://developer.themoviedb.org/reference/discover-movie
- TMDB — Search movie: https://developer.themoviedb.org/reference/search-movie
- TMDB — Movie details: https://developer.themoviedb.org/reference/movie-details
- TMDB — Movie genres: https://developer.themoviedb.org/reference/genre-movie-list
- TMDB — Popular movies: https://developer.themoviedb.org/reference/movie-popular-list
- TMDB — Images: https://developer.themoviedb.org/docs/image-basics
- TMDB — Attribution: https://developer.themoviedb.org/docs/attribution
