# Contexto atual do projeto

Atualizado em: **2026-07-31**

Este arquivo é mutável. Ele registra o estado presente e não repete regras permanentes de `AGENTS.md` nem especificações completas de `docs/context/`.

## Estado

**Fase atual:** Fase 5 — Pesquisa e filtros é a próxima fase a implementar; a Fase 4 foi concluída com testes unitários e instrumentados no AVD.

**Código Android:** módulo único `app` com descoberta paginada real em Compose, detalhes reais em Fragment XML, Retrofit/Paging/Coil e navegação por ID; pesquisa, filtros e favoritos ainda não foram implementados.

## Decisões confirmadas

- Aplicativo Android nativo em Kotlin.
- Um único módulo `app` na primeira versão.
- `minSdk 23`, `compileSdk 36`, `targetSdk 36`, JDK 17.
- Lifecycle fixado em `2.10.0`, pois `2.11.0` exige `compileSdk` 37 e conflita com a baseline de API 36.
- Uma única Activity com navegação entre Fragments.
- Lista, pesquisa, filtros e favoritos em Compose hospedado por `ComposeView`.
- Detalhes do filme em Fragment com layout XML.
- `StateFlow` nas features Compose e `LiveData` na feature XML.
- Retrofit + Kotlin serialization para TMDB.
- Paging 3 com `PagingSource` remoto; sem `RemoteMediator` na primeira versão.
- Room apenas para favoritos na primeira versão.
- Koin para injeção de dependências.
- Coil para imagens.
- Pesquisa e filtros seguem a política descrita em `docs/context/TMDB_API.md`.
- Dependências de código seguem `UI → domínio ← dados`; `PagingData` é a única exceção AndroidX aceita no contrato de domínio.
- Operações não paginadas usam falhas tipadas por `AppResult`/`AppError`.
- `MovieRepository.pagedMovies(query, filters)` é implementado por `TmdbMovieRepository` com `Pager` remoto de 20 itens, sem placeholders e sem `RemoteMediator`.
- A Fase 3 aceita somente consulta vazia; query não vazia falha explicitamente até o endpoint search ser implementado na Fase 5.
- Falhas paginadas chegam à UI como `AppErrorException`, preservando a categoria `AppError` e a causa técnica sem exibir mensagem bruta.
- `MoviesViewModel` transforma domínio em `MovieUiModel`, usa `cachedIn(viewModelScope)` e deixa loading/erro/retry sob responsabilidade dos `LoadState` do Paging.
- A tela da lista trata refresh/append loading e erro, vazio, conteúdo, retry, fallback de imagem/data, insets seguros e navegação apenas com `movieId`.
- `MovieRepository.movieDetails(movieId)` consulta `GET movie/{movie_id}` em `pt-BR`, converte DTO em `MovieDetails` e entrega falhas tipadas por `AppResult`.
- `MovieDetailsViewModel` recebe o ID por `SavedStateHandle`, expõe um único `LiveData<MovieDetailsUiState>` e preserva conteúdo durante recriação da Activity.
- `MovieDetailsFragment` usa view binding, observa com `viewLifecycleOwner` e renderiza loading, erro recuperável, conteúdo, fallbacks de título/sinopse/data/imagem e retry.
- O manifesto declara `android.permission.INTERNET`; sua ausência causava o encerramento do processo quando o OkHttp iniciava a primeira chamada à TMDB.
- AndroidX Test foi ajustado para `ext.junit 1.3.0` e Espresso `3.7.0`; o Espresso 3.6.1 era incompatível com Android 17 por usar `InputManager.getInstance` via reflexão.
- OkHttp permanece em `4.12.0`, a versão resolvida pelo Retrofit `3.0.0`; não há logging HTTP nesta fase.
- No Android Studio, o Gradle JDK deve usar o Embedded JDK em `/Applications/Android Studio.app/Contents/jbr/Contents/Home`; a referência local `jbr-25` estava indefinida.
- Subagentes reportam progresso; somente o agente principal consolida este arquivo.

## Próxima ação

Implementar a **Fase 5 — Pesquisa e filtros**: endpoints de search e gêneros, política de filtros por endpoint, debounce/cancelamento, novo Pager por consulta/filtro e UI com preservação de estado.

## Escolhas provisórias do bootstrap

Use valores provisórios consistentes caso o responsável não defina outros:

- Nome exibido: `TMDB Movies`
- Nome técnico do projeto: `TmdbMovies`
- `applicationId`: `com.example.tmdbmovies`
- Tema visual: Material 3 com suporte a claro/escuro

Não bloqueie a implementação por essas escolhas; elas podem ser renomeadas antes da entrega final.

## Riscos conhecidos

- Os endpoints search e discover não aceitam exatamente o mesmo conjunto de filtros; a UI não pode sugerir que filtros incompatíveis foram aplicados globalmente.
- A disponibilidade do modelo configurado para subagentes depende do ambiente do usuário; remova ou substitua a linha de modelo caso necessário.

## Registro de progresso

Adicione entradas curtas, somente após mudanças significativas:

```text
2026-07-31 — Fase 0 concluída
- Concluído: regras, skills, subagentes, arquitetura e proteção de arquivos locais revisados.
- Decidido: contratos e ownership registrados acima.
- Pendente: executar a Fase 1 e validar a baseline no primeiro build.
- Evidência: skills e perfis validados estruturalmente; Gradle ainda não existe.

2026-07-31 — Fase 1 concluída
- Concluído: projeto Gradle, módulo app, catálogo de versões, Compose/Material 3, Koin, Activity, NavHost, destinos placeholder e configuração local segura do token.
- Decidido: Lifecycle ajustado de 2.11.0 para 2.10.0 para preservar compileSdk 36; KSP/Room validados por código exclusivo de teste.
- Pendente: corrigir window insets ao implementar a tela real da lista; iniciar a Fase 2.
- Evidência: `./gradlew assembleDebug testDebugUnitTest lintDebug` passou; 2 testes passaram; lint terminou com 0 erros; APK instalado no AVD Pixel_9 e `MainActivity` abriu sem crash em cold start de 1.349 ms.

2026-07-31 — Fase 2 concluída
- Concluído: domínio mínimo da lista, contrato paginado, Retrofit/OkHttp/serialization, autenticação Bearer, endpoint discover, DTOs, mapeadores e hierarquia de erros integrados ao Koin.
- Decidido: manter `PagingSource`, `Pager` e implementação do repository para a Fase 3; fixar OkHttp 4.12.0, alinhado à dependência transitiva do Retrofit 3.0.0; não adicionar logger HTTP.
- Pendente: implementar a Fase 3 e corrigir window insets junto à tela real da lista.
- Evidência: `./gradlew testDebugUnitTest` passou com 8 testes; `./gradlew assembleDebug lintDebug` passou com 0 erros; chamada Retrofit simulada validou parsing, mapeamento, query e headers sem segredo real.

2026-07-31 — Fase 3 concluída
- Concluído: `PagingSource`, repository/Pager, Koin, ViewModel, tela Compose da lista, cards Coil, seis estados de Paging, retry, fallbacks, insets seguros, navegação por `movieId` e validação manual da lista real no AVD.
- Decidido: não duplicar `LoadState` no ViewModel; manter busca/filtros completos para a Fase 5; atualizar AndroidX Test para ext.junit 1.3.0 e Espresso 3.7.0 por compatibilidade com Android 17; declarar `android.permission.INTERNET` no manifesto.
- Pendente: iniciar a Fase 4 — Detalhes em XML.
- Evidência: `./gradlew assembleDebug testDebugUnitTest lintDebug` passou com 17 testes unitários e lint sem erros; `./gradlew connectedDebugAndroidTest` passou com 3 testes Compose no Pixel_9/API 37; Logcat identificou a ausência da permissão de internet, a correção compilou com sucesso e a lista de filmes foi confirmada manualmente no AVD.

2026-07-31 — Fase 4 concluída
- Concluído: endpoint e DTO de detalhes, mapeamento para domínio, repository com falhas tipadas, ViewModel com LiveData/SavedStateHandle e Fragment XML com view binding, loading, erro, conteúdo, retry e fallbacks.
- Decidido: mapear HTTP 404 como `AppError.NotFound`; manter favoritos fora desta fase; usar Coil Views para o pôster XML e `core-testing` apenas em testes de LiveData.
- Pendente: iniciar a Fase 5 — Pesquisa e filtros; validar manualmente detalhes reais, modo avião, paisagem, fonte ampliada e filme sem metadados durante o polimento/checklist final.
- Evidência: `./gradlew assembleDebug testDebugUnitTest lintDebug connectedDebugAndroidTest` passou com 23 testes unitários, lint sem erros e 4 testes instrumentados no Pixel_9/API 37, incluindo erro/retry/conteúdo/recriação do Fragment XML.
```

Use este formato para entradas futuras:

```text
YYYY-MM-DD — Fase/decisão
- Concluído:
- Decidido:
- Pendente:
- Evidência: comando ou teste relevante
```
