# Contexto atual do projeto

Atualizado em: **2026-08-03**

Este arquivo é mutável. Ele registra o estado presente e não repete regras permanentes de `AGENTS.md` nem especificações completas de `docs/context/`.

## Estado

**Fase atual:** Fase 11 — Quality gate final de filmes é a próxima fase; a Fase 10 foi concluída com validação automatizada, checklist manual no AVD, README e screenshots finais.

**Código Android:** módulo único `app` com descoberta, pesquisa e filtros paginados reais em Compose, detalhes reais em Fragment XML, favoritos persistentes com Room, Retrofit/Paging/Coil e navegação por ID.

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
- Falhas paginadas chegam à UI como `AppErrorException`, preservando a categoria `AppError` e a causa técnica sem exibir mensagem bruta.
- `MoviesViewModel` transforma domínio em `MovieUiModel`, usa `cachedIn(viewModelScope)` e deixa loading/erro/retry sob responsabilidade dos `LoadState` do Paging.
- A tela da lista trata refresh/append loading e erro, vazio, conteúdo, retry, fallback de imagem/data, insets seguros e navegação apenas com `movieId`.
- Consulta vazia usa `discover/movie` com gênero, ordenação, nota mínima e ano; consulta preenchida usa `search/movie` somente com texto e ano, conforme política explícita de compatibilidade no domínio.
- `MoviesViewModel` preserva consulta e filtros no `SavedStateHandle`, aplica debounce de 400 ms, cancela fluxos obsoletos com `flatMapLatest` e cria uma nova geração de Paging por requisição efetiva.
- Gêneros vêm de `genre/movie/list` como `AppResult`, com loading, erro independente e retry; a UI mantém filtros exclusivos de descoberta ocultos/inativos durante pesquisa sem apagá-los.
- A tela Compose oferece busca/limpar, filtros de gênero, ordenação, nota mínima e ano, chips removíveis, limpar todos e retorno ao topo quando a requisição efetiva muda.
- Room é a fonte de verdade para favoritos; `FavoriteRepository` expõe lista e estado por `Flow` e operações idempotentes por `AppResult`.
- Upserts preservam o `favoritedAt` original em transação, mantendo a ordenação estável mesmo quando salvar é repetido.
- Lista e detalhes combinam o estado local sem nova chamada remota; detalhes converte `MovieDetails` explicitamente em `Movie` antes de persistir.
- A tela Compose de favoritos trata loading, erro, vazio, conteúdo, remoção e navegação para detalhes por `movieId`.
- O fluxo remoto de `PagingData` é cacheado antes de ser combinado com o `Flow` do Room; cachear somente depois do `combine` reutilizava um `pageEventFlow` não cacheado e encerrava o app ao alterar favorito na lista.
- `MovieRepository.movieDetails(movieId)` consulta `GET movie/{movie_id}` em `pt-BR`, converte DTO em `MovieDetails` e entrega falhas tipadas por `AppResult`.
- `MovieDetailsViewModel` recebe o ID por `SavedStateHandle`, expõe um único `LiveData<MovieDetailsUiState>` e preserva conteúdo durante recriação da Activity.
- `MovieDetailsFragment` usa view binding, observa com `viewLifecycleOwner` e renderiza loading, erro recuperável, conteúdo, fallbacks de título/sinopse/data/imagem e retry.
- O manifesto declara `android.permission.INTERNET`; sua ausência causava o encerramento do processo quando o OkHttp iniciava a primeira chamada à TMDB.
- AndroidX Test foi ajustado para `ext.junit 1.3.0` e Espresso `3.7.0`; o Espresso 3.6.1 era incompatível com Android 17 por usar `InputManager.getInstance` via reflexão.
- OkHttp permanece em `4.12.0`, a versão resolvida pelo Retrofit `3.0.0`; não há logging HTTP nesta fase.
- No Android Studio, o Gradle JDK deve usar o Embedded JDK em `/Applications/Android Studio.app/Contents/jbr/Contents/Home`; a referência local `jbr-25` estava indefinida.
- Subagentes reportam progresso; somente o agente principal consolida este arquivo.
- A direção visual dark-first, os tokens, a navegação Início/Favoritos/Perfil e os componentes estão em `docs/context/DESIGN_SYSTEM.md`.
- Perfil é local e contém somente dados/preferências reais e créditos; autenticação e sincronização TMDB continuam fora do escopo.
- A revisão visual foi decomposta nas Fases 7–10; a Fase 11 é o quality gate de filmes e séries permanecem evolução opcional final na Fase 12.
- O tema dark-first está aplicado em Compose e Views; a Activity controla a navegação inferior Início/Favoritos/Perfil e a oculta em Busca/Detalhes.
- A busca existente foi movida para um destino próprio sem alterar seus contratos de Paging; Perfil é um destino local navegável com versão, escopo e atribuição textual do TMDB.
- A Home usa fontes editoriais independentes para trending semanal, now playing, top rated e clássicos; cada seção possui loading, vazio, erro e retry isolados.
- Clássicos usa `discover/movie` com `vote_average.desc`, nota mínima 7,0, no mínimo 1.000 votos e lançamento até 2005-12-31; não há filtragem local de páginas.
- O banner é estático e selecionável entre os cinco primeiros filmes em alta; a seleção é preservada no `SavedStateHandle` e favoritos continuam reativos via Room.
- Favoritos possui busca local, grid adaptável e cinco ordenações derivadas na UI; “adicionados recentemente” preserva a ordem emitida pelo Room e nenhuma ordenação altera `favoritedAt`.
- Favoritos usa três colunas fixas; em telas compactas de 432 dp os cards ficam com cerca de 133 dp, equivalentes aos 132 dp da Home, e encolhem proporcionalmente em larguras menores.
- Busca e ordenação de Favoritos são preservadas em `SavedStateHandle`; o estado vazio retorna à Home e a remoção continua imediata pela fonte local.
- Perfil observa a quantidade real de favoritos e exibe versão, logo oficial aprovado e aviso obrigatório do TMDB, sem simular conta ou atividade.
- Ao trocar de aba, a navegação remove destinos filhos restaurados e termina no destino de nível superior selecionado, preservando o estado salvo da aba sem reabrir Detalhes.
- Detalhes preserva Fragment XML, View Binding e LiveData e usa somente o backdrop com gradiente como imagem principal, seguido por metadados, nota real, favorito e sinopse; o pôster permanece disponível para listas/favoritos e `vote_average` continua nullável em todas as fronteiras.
- Busca preserva Paging, filtros e cancelamento, com cards e estados alinhados aos tokens finais; botões tonais usam a superfície secundária em Compose e Views.
- Screenshots finais com dados reais ficam em `docs/screenshots/` e o README documenta escopo, setup, execução, testes, arquitetura e limites.

## Próxima ação

Executar a **Fase 11 — Quality gate final de filmes** em modo de revisão: dependências e fronteiras, segredo/logging, gates limpos, critérios de aceite e limitações reais.

## Escolhas provisórias do bootstrap

Use valores provisórios consistentes caso o responsável não defina outros:

- Nome exibido: `TMDB Movies`
- Nome técnico do projeto: `TmdbMovies`
- `applicationId`: `com.example.tmdbmovies`
- Tema visual: Material 3 com suporte a claro/escuro

Não bloqueie a implementação por essas escolhas; elas podem ser renomeadas antes da entrega final.

## Riscos conhecidos

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

2026-07-31 — Fase 5 concluída
- Concluído: endpoints e paginação de pesquisa, gêneros, política de filtros por endpoint, três ordenações, debounce/cancelamento, estado preservado e UI Compose de busca/filtros com limpar, chips, vazio, erro e retry.
- Decidido: centralizar compatibilidade em `MovieFilters.compatibleWithQuery`; manter gênero/ordenação/nota como preferências latentes durante pesquisa e aplicar somente ano no `search/movie`; usar debounce de 400 ms.
- Pendente: iniciar a Fase 6 — Favoritos com Room; validar manualmente rotação, digitação rápida, combinações de filtros e fonte ampliada durante o polimento/checklist final.
- Evidência: `./gradlew assembleDebug testDebugUnitTest lintDebug connectedDebugAndroidTest` passou com 36 testes unitários, lint sem erros e 8 testes instrumentados no Pixel_9/API 37; descoberta e pesquisa real por “Alien” foram validadas manualmente no AVD.

2026-07-31 — Fase 6 concluída
- Concluído: banco/DAO/mapeadores Room, repository local, favorito reativo na lista e detalhes, tela Compose de favoritos, estado vazio, remoção e navegação por ID.
- Decidido: preservar `favoritedAt` em upsert repetido para idempotência e ordenação estável; manter Room como única fonte de verdade e combinar seu Flow ao Paging sem nova requisição remota.
- Pendente: executar na Fase 7 a validação visual/manual de encerramento real do processo, tema, paisagem, fonte ampliada e acessibilidade.
- Evidência: `./gradlew assembleDebug testDebugUnitTest lintDebug` passou com 42 testes unitários e lint sem erros; `./gradlew connectedDebugAndroidTest` passou com 12 testes no Pixel_9/API 37, incluindo DAO em memória, fechamento/reabertura de banco em disco e UI Compose de favoritos.

2026-07-31 — Regressão de favorito na lista corrigida
- Concluído: corrigido crash ao remover favorito diretamente do card paginado e adicionado teste instrumentado com `Pager`/`PagingSource` reais.
- Decidido: aplicar `cachedIn(viewModelScope)` ao fluxo paginado antes do `combine` com favoritos, preservando uma geração coletável novamente a cada emissão do Room.
- Pendente: nenhum risco adicional desta correção; permanece o checklist manual da Fase 7.
- Evidência: o teste reproduziu `Attempt to collect twice from pageEventFlow` antes da correção e passou depois; gates padrão passaram com 42 testes unitários, lint sem erros e 13 instrumentados no Pixel_9/API 37; Logcat final sem `AndroidRuntime`.

2026-08-03 — Direção visual e plano revisados
- Concluído: especificação visual criada; produto, API, qualidade, plano e perfil de UI alinhados à nova Home de filmes, favoritos em grid e Perfil local.
- Decidido: documentação de design é contexto do produto, não uma skill nem biblioteca própria; séries ficam após o quality gate de filmes; Perfil não implica conta TMDB.
- Pendente: implementar a Fase 7 e validar a fundação visual antes dos novos endpoints da Home.
- Evidência: contratos confrontados com a documentação oficial de trending, now playing, top rated, discover/search/details de TV, autenticação de usuário e append to response do TMDB.

2026-08-03 — Fase 7 concluída
- Concluído: tokens dark-first aplicados em Compose/Views, navegação inferior Início/Favoritos/Perfil, ícones com contorno/preenchimento por estado, Perfil local e Busca em destino próprio.
- Decidido: manter a barra inferior na Activity e ocultá-la em Busca/Detalhes; reutilizar a feature paginada existente na Busca; deixar banner e carrosséis exclusivamente para a Fase 8.
- Pendente: implementar o Discover editorial; adicionar logo oficial do TMDB e resumo real da coleção na Fase 9.
- Evidência: `./gradlew assembleDebug testDebugUnitTest lintDebug connectedDebugAndroidTest` passou; 15 testes instrumentados passaram no Pixel_9/API 37; Home, Busca, Detalhes e Perfil foram inspecionados no AVD.

2026-08-03 — Fase 8 concluída
- Concluído: fontes editoriais independentes, estado agregado com retry por seção, banner selecionável, carrosséis de pôsteres, favoritos reativos e navegação para Busca/Detalhes.
- Decidido: clássicos usa corte até 2005-12-31, nota mínima 7,0, mínimo de 1.000 votos e ordenação por nota; o banner não avança automaticamente.
- Pendente: grid, quantidade e ordenação de Favoritos, resumo real e logo oficial do TMDB no Perfil na Fase 9.
- Evidência: endpoints e regra editorial cobertos por MockWebServer; ViewModel e UI cobrem sucesso, vazio, erro isolado, retry, seleção e favorito; gate completo e inspeção no Pixel_9/API 37 executados.

2026-08-03 — Fase 9 concluída
- Concluído: Favoritos em grid adaptável com quantidade, busca, ordenação local e estado vazio acionável; Perfil com contagem real, versão, logo e atribuição do TMDB.
- Decidido: preservar a ordem do Room para recentes e derivar as demais ordenações no ViewModel; não incluir preferência de aparência sem implementação ponta a ponta.
- Pendente: polimento visual de Detalhes/Busca, checklist transversal, README e screenshots na Fase 10.
- Evidência: testes de ViewModel, Compose, Perfil e navegação aprovados; telas vazia de Favoritos e Perfil inspecionadas no Pixel_9/API 37; gate completo executado.

2026-08-03 — Fase 10 concluída
- Concluído: Detalhes XML com hierarquia cinematográfica e nota real; Busca polida sem alterar Paging/filtros; fallbacks, mensagens e semântica revisados; README e quatro screenshots reais finalizados.
- Decidido: mapear somente `vote_average` do contrato de detalhes já existente, sem `append_to_response`; manter backdrop decorativo e pôster informativo; usar superfície secundária para ações tonais nos dois toolkits.
- Pendente: executar a revisão read-only da Fase 11; o cenário de token ausente ficou coberto por contrato/código, mas não foi recompilado manualmente sem o token local nesta fase.
- Evidência: `./gradlew assembleDebug testDebugUnitTest lintDebug connectedDebugAndroidTest` passou, com 20 instrumentados no Pixel_9/API 37; AVD confirmou rede real/lenta, offline/retry, fonte 200%, paisagem rolável, descrições acessíveis e favorito persistente após encerramento do processo; quality gate da fase sem achado bloqueante.

2026-08-03 — Detalhes simplificado para banner único
- Concluído: removida a capa/pôster da tela de Detalhes; backdrop, gradiente, retorno, título, metadados, favorito e sinopse foram preservados.
- Decidido: o pôster continua no domínio e nas demais telas, mas Detalhes usa somente o banner para evitar repetição visual.
- Pendente: nenhum escopo adicional; permanece a Fase 11 como próxima ação.
- Evidência: `./gradlew assembleDebug testDebugUnitTest lintDebug connectedDebugAndroidTest` passou com 20 instrumentados; inspeção no Pixel_9/API 37 confirmou somente o banner na árvore visual/acessível, sem espaço reservado para o pôster.
```

Use este formato para entradas futuras:

```text
YYYY-MM-DD — Fase/decisão
- Concluído:
- Decidido:
- Pendente:
- Evidência: comando ou teste relevante
```
