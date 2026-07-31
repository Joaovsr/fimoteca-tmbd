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

## Fase 7 — Polimento

**Responsável sugerido:** `ui-layer-implementer` com revisão do `qa-reviewer`.

**Entregas**

- tema claro/escuro;
- acessibilidade e tamanhos de fonte;
- placeholders/fallbacks;
- mensagens localizadas;
- créditos e atribuição do TMDB;
- README final com screenshots e instruções;
- ícone/nome final, se desejado.

**Pronto quando**

- checklist manual de `docs/context/QUALITY.md` foi executado;
- não há overflow ou texto crítico cortado nos cenários testados.

## Fase 8 — Quality gate final

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
