# Contexto atual do projeto

Atualizado em: **2026-07-31**

Este arquivo é mutável. Ele registra o estado presente e não repete regras permanentes de `AGENTS.md` nem especificações completas de `docs/context/`.

## Estado

**Fase atual:** Fase 2 — Núcleo de rede e descoberta concluída; próximo incremento é a Fase 3 — Lista paginada.

**Código Android:** módulo único `app` com bootstrap de UI/navegação e núcleo remoto de descoberta integrado ao Koin; os destinos ainda são placeholders.

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
- O contrato inicial de `MovieRepository` expõe `pagedMovies(query, filters)` sem implementação; `Pager`, `PagingSource` e repository concreto pertencem à Fase 3.
- OkHttp permanece em `4.12.0`, a versão resolvida pelo Retrofit `3.0.0`; não há logging HTTP nesta fase.
- Subagentes reportam progresso; somente o agente principal consolida este arquivo.

## Próxima ação

Iniciar a **Fase 3 — Lista paginada** de `docs/execution/PLAN.md`, implementando primeiro `PagingSource` e repository e validando esses contratos antes da UI.

## Escolhas provisórias do bootstrap

Use valores provisórios consistentes caso o responsável não defina outros:

- Nome exibido: `TMDB Movies`
- Nome técnico do projeto: `TmdbMovies`
- `applicationId`: `com.example.tmdbmovies`
- Tema visual: Material 3 com suporte a claro/escuro

Não bloqueie a implementação por essas escolhas; elas podem ser renomeadas antes da entrega final.

## Riscos conhecidos

- O placeholder Compose da lista ainda não trata window insets e aparece sob a barra de status; corrigir junto à primeira implementação real da tela.
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
```

Use este formato para entradas futuras:

```text
YYYY-MM-DD — Fase/decisão
- Concluído:
- Decidido:
- Pendente:
- Evidência: comando ou teste relevante
```
