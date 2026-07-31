# Contexto atual do projeto

Atualizado em: **2026-07-31**

Este arquivo é mutável. Ele registra o estado presente e não repete regras permanentes de `AGENTS.md` nem especificações completas de `docs/context/`.

## Estado

**Fase atual:** Fase 0 concluída; próximo incremento é a Fase 1 — Bootstrap.

**Código Android:** ainda não inicializado.

## Decisões confirmadas

- Aplicativo Android nativo em Kotlin.
- Um único módulo `app` na primeira versão.
- `minSdk 23`, `compileSdk 36`, `targetSdk 36`, JDK 17.
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

Executar a **Fase 1 — Bootstrap** de `docs/execution/PLAN.md`:

- criar projeto Gradle;
- configurar catálogo de versões e plugins;
- criar `Application`, `MainActivity` e grafo de navegação;
- configurar Koin;
- adicionar token local seguro;
- provar build e testes vazios.

## Pendências que exigem escolha no bootstrap

Use valores provisórios consistentes caso o responsável não defina outros:

- Nome exibido: `TMDB Movies`
- Nome técnico do projeto: `TmdbMovies`
- `applicationId`: `com.example.tmdbmovies`
- Tema visual: Material 3 com suporte a claro/escuro

Não bloqueie a implementação por essas escolhas; elas podem ser renomeadas antes da entrega final.

## Riscos conhecidos

- Compatibilidade conjunta entre AGP 9.3, built-in Kotlin, KSP, Room e plugins deve ser validada pelo primeiro build.
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
```

Use este formato para entradas futuras:

```text
YYYY-MM-DD — Fase/decisão
- Concluído:
- Decidido:
- Pendente:
- Evidência: comando ou teste relevante
```
