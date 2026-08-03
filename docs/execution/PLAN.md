# Plano de execução

Cada fase deve produzir um incremento compilável. Não antecipe estruturas de fases futuras sem necessidade concreta.

## Fase 0 — Contexto para agentes

**Objetivo:** deixar requisitos, arquitetura, API, qualidade e responsabilidades claras.

**Entregas**

- `README.md`
- `AGENTS.md`
- `CONTEXT.md`
- documentos de contexto
- skills
- configuração e perfis de subagentes

**Pronto quando**

- arquivos não se contradizem;
- cada informação possui um local principal;
- nenhum segredo foi incluído.

## Fase 1 — Bootstrap Android

**Responsável sugerido:** agente principal com revisão do `android-architect`.

**Entregas**

- projeto Gradle e módulo `app`;
- catálogo de versões;
- JDK/SDK configurados;
- `Application` e Koin inicializados;
- Activity, NavHost e destinos vazios;
- tema Material 3;
- leitura segura de `TMDB_ACCESS_TOKEN`;
- `.gitignore` e exemplo de configuração sem segredo;
- testes e lint executáveis.

**Pronto quando**

```bash
./gradlew assembleDebug testDebugUnitTest lintDebug
```

passa e o app abre sem crash.

## Fase 2 — Núcleo de rede e descoberta

**Responsável sugerido:** `data-layer-implementer`.

**Entregas**

- modelos de domínio necessários à lista;
- Retrofit/OkHttp/serialization;
- `TmdbApi` com discover;
- DTOs e mapeadores da lista;
- hierarquia de erros;
- contrato paginado do repositório;
- testes de mapeamento e erro.

**Dependência:** Fase 1.

**Pronto quando**

- token é enviado sem aparecer em logs;
- fixtures cobrem nulls e datas vazias;
- chamada falsa/teste prova parsing e mapeamento de discover.

## Fase 3 — Lista paginada

**Responsáveis sugeridos:** `data-layer-implementer` e depois `ui-layer-implementer`, nunca editando os mesmos arquivos em paralelo.

**Entregas**

- `PagingSource` para discover;
- repository com `Pager`;
- `MoviesViewModel`;
- tela Compose com cards;
- loading refresh/append;
- erro refresh/append e retry;
- vazio e fallback de imagem;
- navegação para detalhes por ID.

**Pronto quando**

- múltiplas páginas carregam;
- append falho preserva itens;
- testes do PagingSource e estados principais passam.

## Fase 4 — Detalhes em XML

**Responsáveis sugeridos:** `data-layer-implementer` e depois `ui-layer-implementer`.

**Entregas**

- endpoint, DTO e mapeamento de detalhes;
- repository de detalhes;
- `MovieDetailsViewModel` com LiveData;
- Fragment com view binding e layout XML;
- loading, erro, conteúdo e retry;
- título, sinopse, data e imagem;
- restauração/navegação correta.

**Pronto quando**

- Fragment observa com `viewLifecycleOwner`;
- nenhum DTO chega à UI;
- teste de ViewModel e teste UI crítico passam.

## Fase 5 — Pesquisa e filtros

**Responsáveis sugeridos:** dados primeiro, UI depois.

**Entregas**

- endpoints, DTOs e mapeadores de search e gêneros;
- busca com debounce, cancelamento e paginação;
- discover com filtros;
- política de filtros por endpoint;
- UI de filtros e limpar;
- preservação do estado.

**Pronto quando**

- consulta obsoleta é cancelada;
- mudar filtro reinicia paginação;
- filtros incompatíveis não aparecem como ativos na pesquisa;
- vazio e erro funcionam nos dois modos.

## Fase 6 — Favoritos com Room

**Responsável sugerido:** dados primeiro, UI depois.

**Entregas**

- banco, entidade, DAO e mapeadores;
- repository local;
- favoritar/desfavoritar em lista e detalhes;
- tela Compose de favoritos;
- estado vazio;
- navegação para detalhes;
- testes de DAO e ViewModels.

**Pronto quando**

- favorito sobrevive ao reinício;
- alteração é refletida sem nova chamada remota;
- operações são idempotentes.

## Fase 7 — Fundação visual e navegação

**Responsável sugerido:** `ui-layer-implementer` com revisão do `qa-reviewer`.

**Entregas**

- tokens dark-first de `docs/context/DESIGN_SYSTEM.md` em Compose e Views;
- componentes compartilhados necessários por duas ou mais telas;
- barra inferior com Início, Favoritos e Perfil;
- destinos placeholder navegáveis de Perfil e busca separada, sem prometer funções ausentes;
- manutenção dos testes de navegação e estados existentes.

**Pronto quando**

- tema e navegação são consistentes entre Compose e XML;
- nenhum literal visual relevante fica duplicado nas features;
- os três destinos principais preservam estado e não quebram os fluxos existentes;
- build, testes e lint passam.

## Fase 8 — Discover editorial de filmes

**Responsáveis sugeridos:** `data-layer-implementer` nos contratos/endpoints e depois `ui-layer-implementer`.

**Entregas**

- fontes para trending semanal, now playing, top rated e clássicos via discover;
- estado agregado da Home com falha isolada por seção e retry;
- banner selecionável com swipe e avanço automático, além de carrosséis horizontais;
- favorito reativo e navegação para detalhes;
- acesso à busca existente em tela própria;
- testes de endpoint, mapeamento, ViewModel e UI.

**Pronto quando**

- cada seção informa sua fonte/regra e não bloqueia as demais ao falhar;
- conteúdo, vazio, erro, retry e fallbacks são verificáveis;
- nenhuma filtragem local de páginas promete representar o catálogo;
- acessibilidade e fonte ampliada passam nos componentes principais.

## Fase 9 — Favoritos e Perfil

**Responsável sugerido:** `ui-layer-implementer`; dados somente se um campo persistido realmente faltar.

**Entregas**

- favoritos em grid adaptável com quantidade e ordenação local;
- estado vazio com ação para Início;
- tela de Perfil local com resumo real da coleção, versão e créditos do TMDB;
- preferência de aparência somente se implementada de ponta a ponta;
- testes de ordenação, navegação, remoção e conteúdo do perfil.

**Pronto quando**

- ordenação não altera `favoritedAt` nem depende de rede;
- Perfil não simula conta, avatar, assistidos ou sincronização;
- atribuição e logo do TMDB atendem às diretrizes oficiais;
- grid funciona em retrato, paisagem e fonte ampliada.

## Fase 10 — Detalhes, busca e polimento transversal

**Responsável sugerido:** `ui-layer-implementer` com revisão do `qa-reviewer`.

**Entregas**

- detalhes XML alinhados à hierarquia visual definida, preservando LiveData;
- busca dedicada reaproveitando paginação, filtros e cancelamento existentes;
- acessibilidade, placeholders, fallbacks e mensagens localizadas;
- README final com screenshots e instruções;
- ícone/nome final, se desejado.

Trailer e elenco são opcionais e exigem uma decisão de escopo antes de adicionar contratos de API.

**Pronto quando**

- checklist manual de `docs/context/QUALITY.md` foi executado;
- não há overflow ou texto crítico cortado nos cenários testados;
- fluxos existentes continuam cobertos e os gates padrão passam.

## Fase 11 — Quality gate final de filmes

**Responsável sugerido:** `qa-reviewer` em modo read-only; correções voltam ao agente dono da camada.

**Atividades**

- revisar dependências e fronteiras;
- procurar segredo e logging indevido;
- executar build, testes e lint;
- executar instrumentados se possível;
- comparar implementação com todos os critérios de aceite;
- registrar limitações reais.

**Comandos**

```bash
./gradlew clean assembleDebug testDebugUnitTest lintDebug
./gradlew connectedDebugAndroidTest  # quando houver ambiente Android
```

**Pronto quando**

- nenhuma falha bloqueante permanece;
- README reproduz o setup;
- `CONTEXT.md` descreve o estado final sem histórico irrelevante.

## Fase 12 — Séries (evolução opcional final)

Iniciar somente após a Fase 11 e mediante confirmação de escopo. Tratar séries como domínio próprio, com discover, pesquisa, detalhes e favoritos deliberadamente modelados. Não incluir temporadas, episódios, progresso assistido ou autenticação por implicação.
