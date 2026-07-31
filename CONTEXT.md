# Contexto atual do projeto

Atualizado em: **2026-07-31**

Este arquivo é mutável. Ele registra o estado presente e não repete regras permanentes de `AGENTS.md` nem especificações completas de `docs/context/`.

## Estado

**Fase atual:** Fase 1 — Bootstrap implementada e validada por build, testes e lint; abertura em dispositivo ainda não verificada.

**Código Android:** módulo único `app` inicializado com Activity, NavHost, destinos placeholder, Material 3 e Koin.

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
- Subagentes reportam progresso; somente o agente principal consolida este arquivo.

## Próxima ação

Abrir o app em dispositivo/emulador para concluir a validação manual da **Fase 1**. Depois, iniciar a **Fase 2 — Núcleo de rede e descoberta** de `docs/execution/PLAN.md`.

## Escolhas provisórias do bootstrap

Use valores provisórios consistentes caso o responsável não defina outros:

- Nome exibido: `TMDB Movies`
- Nome técnico do projeto: `TmdbMovies`
- `applicationId`: `com.example.tmdbmovies`
- Tema visual: Material 3 com suporte a claro/escuro

Não bloqueie a implementação por essas escolhas; elas podem ser renomeadas antes da entrega final.

## Riscos conhecidos

- O ambiente atual não fornece `adb` ou dispositivo/emulador; a inicialização em runtime ainda precisa de verificação manual.
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

2026-07-31 — Fase 1 implementada
- Concluído: projeto Gradle, módulo app, catálogo de versões, Compose/Material 3, Koin, Activity, NavHost, destinos placeholder e configuração local segura do token.
- Decidido: Lifecycle ajustado de 2.11.0 para 2.10.0 para preservar compileSdk 36; KSP/Room validados por código exclusivo de teste.
- Pendente: abrir o app em dispositivo/emulador; depois iniciar a Fase 2.
- Evidência: `./gradlew assembleDebug testDebugUnitTest lintDebug` passou; 2 testes passaram e lint terminou com 0 erros.
```

Use este formato para entradas futuras:

```text
YYYY-MM-DD — Fase/decisão
- Concluído:
- Decidido:
- Pendente:
- Evidência: comando ou teste relevante
```
