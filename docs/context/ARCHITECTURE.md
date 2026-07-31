# Arquitetura

## Escolha

Arquitetura em camadas, orientada por features, em um único módulo Android. O desenho mantém separação suficiente para testes e manutenção sem introduzir custo de multi-módulo para um desafio pequeno.

```text
Fluxo em execução:
UI → contratos de domínio → implementações de dados → TMDB/Room

Dependências de código:
UI → domínio ← dados
```

A comunicação da UI segue fluxo unidirecional: eventos entram no ViewModel; o ViewModel transforma estado; a UI renderiza esse estado e emite novos eventos.

## Estrutura de pacotes

```text
<application_id>/
├── app/
│   ├── TmdbMoviesApplication.kt
│   └── MainActivity.kt
├── core/
│   ├── common/       # Resultados, erros, dispatchers e extensões realmente compartilhadas
│   ├── database/     # RoomDatabase, converters e migrations
│   ├── network/      # Retrofit, OkHttp, autenticação e utilitários de rede
│   └── ui/           # Componentes e recursos compartilhados por 2+ features
├── data/
│   ├── local/        # DAO, entidades e mapeadores Room
│   ├── remote/       # API, DTOs, PagingSources e mapeadores remotos
│   └── repository/   # Implementações dos contratos do domínio
├── domain/
│   ├── model/        # Movie, MovieDetails, Genre, Filter etc.
│   └── repository/   # Interfaces MovieRepository e FavoriteRepository
├── feature/
│   ├── movies/       # Lista, busca e filtros em Compose
│   ├── details/      # Fragment XML + LiveData
│   └── favorites/    # Lista local em Compose
└── di/               # Módulos Koin agrupados por responsabilidade
```

Não crie pacotes vazios antecipadamente. Crie cada estrutura quando a primeira classe real precisar dela.

## Componentes centrais

### Domínio

Modelos Android-free:

- `Movie`: resumo para listas.
- `MovieDetails`: conteúdo da tela de detalhes.
- `Genre`: identificador e nome.
- `MovieFilters`: parâmetros válidos para descoberta.
- `AppError`: categorias de falha independentes de texto ou recurso Android.
- `AppResult<T>`: sucesso ou falha tipada para operações não paginadas.

Contratos:

```kotlin
interface MovieRepository {
    fun pagedMovies(query: String, filters: MovieFilters): Flow<PagingData<Movie>>
    suspend fun movieDetails(movieId: Long): AppResult<MovieDetails>
    suspend fun genres(): AppResult<List<Genre>>
}

interface FavoriteRepository {
    fun observeFavorites(): Flow<List<Movie>>
    fun observeFavorite(movieId: Long): Flow<Boolean>
    suspend fun setFavorite(movie: Movie, favorite: Boolean): AppResult<Unit>
}
```

As assinaturas podem ser refinadas durante a implementação, mas a fronteira permanece livre do framework Android, Retrofit, Room, Coil e Koin. `PagingData` é uma exceção consciente: usá-lo no contrato evita criar uma abstração própria sobre Paging 3 em um projeto pequeno.

A tela de detalhes converte `MovieDetails` em um resumo `Movie` por um mapeador explícito antes de persistir o favorito. A camada de dados converte falhas técnicas em `AppError`; operações paginadas entregam ao `LoadState` uma falha já mapeada, nunca a exceção bruta de Retrofit, Room ou parsing.

### Dados remotos

- `TmdbApi`: endpoints tipados.
- DTOs refletem o JSON e usam nomes serializados explícitos.
- Mapeadores tratam nulls e produzem domínio.
- Um `PagingSource` recebe um snapshot imutável da consulta/filtros.
- Nova consulta ou filtro cria nova instância de PagingSource por `flatMapLatest`/novo Pager.
- Detalhes são uma chamada suspensa separada.

### Dados locais

`FavoriteMovieEntity` deve conter somente os dados necessários para renderizar a lista de favoritos e abrir detalhes:

- `movieId` como chave primária;
- título;
- sinopse resumida opcional;
- caminho da imagem opcional;
- data de lançamento opcional;
- momento em que foi favoritado, para ordenação estável.

DAO recomendado:

- `observeAll(): Flow<List<FavoriteMovieEntity>>`
- `observeExists(movieId): Flow<Boolean>`
- `upsert(entity)`
- `deleteById(movieId)`

Room é a fonte de verdade para favoritos. A UI não mantém uma segunda lista mutável concorrente.

## UI híbrida

### Navegação

- `MainActivity` usa layout XML com `FragmentContainerView`/`NavHostFragment`.
- Destinos: `MoviesFragment`, `MovieDetailsFragment`, `FavoritesFragment`.
- Lista e favoritos hospedam Compose por `ComposeView` e configuram estratégia de descarte compatível com o ciclo de vida da view.
- Detalhes usa view binding sobre XML.

### Compose + StateFlow

`MoviesViewModel` expõe:

- `StateFlow<MoviesUiState>` para consulta, filtros, gêneros e estados não paginados;
- `Flow<PagingData<MovieUiModel>>` para o catálogo;
- métodos de evento como `onQueryChanged`, `onFilterChanged`, `clearFilters`, `retry`.

A tela coleta estado com API lifecycle-aware. Para a lista, utiliza `collectAsLazyPagingItems()` e interpreta `loadState.refresh` e `loadState.append`.

`FavoritesViewModel` expõe `StateFlow<FavoritesUiState>` derivado do Flow do DAO/repositório.

### XML + LiveData

`MovieDetailsViewModel` expõe um único estado observável:

```kotlin
sealed interface MovieDetailsUiState {
    data object Loading : MovieDetailsUiState
    data class Content(val movie: MovieDetailsUiModel, val isFavorite: Boolean) : MovieDetailsUiState
    data class Error(val message: UiText) : MovieDetailsUiState
}
```

O estado público é `LiveData<MovieDetailsUiState>`. Internamente, Coroutines/Flow podem ser combinados e convertidos para LiveData. O Fragment observa com `viewLifecycleOwner` e possui uma única função de renderização por estado.

## Pesquisa e filtros

Fluxo recomendado:

```text
query input
  → debounce
  → distinctUntilChanged
  → combine(filters)
  → flatMapLatest { repository.pagedMovies(query, filters) }
  → cachedIn(viewModelScope)
```

A regra de compatibilidade dos filtros pertence ao repositório ou a uma política explícita do domínio, não a condicionais espalhadas pela UI.

## Erros

Categorias mínimas:

- sem conexão;
- timeout;
- não autorizado;
- limite de requisições;
- HTTP inesperado;
- serialização/dados inválidos;
- desconhecido.

A camada de dados preserva informações úteis para log/teste. A UI recebe mensagem localizada e ação possível. Nunca mostre a exceção bruta ao usuário.

## Dispatchers e concorrência

- Retrofit suspende sem bloquear a Main thread.
- Room fornece APIs suspensas/Flow.
- CPU ou transformação pesada usa dispatcher injetável.
- Não envolva chamadas suspensas em `withContext(IO)` sem necessidade demonstrada.
- Jobs iniciados pelo ViewModel pertencem ao `viewModelScope`.
- Observação em Fragment respeita o ciclo da view.

## Injeção com Koin

Módulos propostos:

- `networkModule`: JSON, OkHttp, Retrofit, API.
- `databaseModule`: Room e DAO.
- `repositoryModule`: implementações dos contratos.
- `viewModelModule`: ViewModels de cada feature.

Evite um arquivo de DI gigante. Também não crie um módulo Koin por classe.

## Por que não usar RemoteMediator na v1

O requisito exige paginação e Room, mas não exige catálogo offline. `PagingSource` remoto cumpre paginação; Room cumpre persistência dos favoritos. Um `RemoteMediator` exigiria tabelas de catálogo, chaves remotas, política de invalidação e estratégia de consistência sem acrescentar valor proporcional ao desafio.

Pode ser adicionado depois como evolução documentada, sem ser condição de entrega.

## Dependências proibidas entre camadas

- `domain` não importa `android.*`, Retrofit, Room, Coil ou Koin; de AndroidX, somente `PagingData` é permitido no contrato paginado.
- DTO não chega ao ViewModel.
- Entity Room não chega à UI.
- Fragment/Composable não chama API ou DAO diretamente.
- Repository não conhece componentes visuais.
- Um ViewModel não recebe Fragment, Activity, Context concreto ou NavController.

## Referências oficiais

- Android — Guide to app architecture: https://developer.android.com/topic/architecture
- Android — UI layer: https://developer.android.com/topic/architecture/ui-layer
- Android — StateFlow and SharedFlow: https://developer.android.com/kotlin/flow/stateflow-and-sharedflow
- Android — Compose interoperability APIs: https://developer.android.com/develop/ui/compose/migrate/interoperability-apis
- Android — Using Compose in Views: https://developer.android.com/develop/ui/compose/migrate/interoperability-apis/compose-in-views
- Android — Paging overview: https://developer.android.com/topic/libraries/architecture/paging/v3-overview
