# TMDB Movies — Android Challenge

Aplicativo Android para listar, pesquisar, filtrar, detalhar e favoritar filmes usando a API do The Movie Database (TMDB).

Este repositório foi preparado para desenvolvimento assistido por agentes de IA. As regras permanentes ficam em `AGENTS.md`; o estado atual do trabalho fica em `CONTEXT.md`; decisões detalhadas ficam em `docs/context/`; e instruções especializadas ficam em `.agents/skills/`.

## Escopo funcional

- Lista paginada de filmes.
- Pesquisa por título.
- Filtros de descoberta por gênero, ordenação e nota mínima.
- Tela de detalhes com título, sinopse, data de lançamento e imagem.
- Favoritos persistidos localmente com Room.
- Estados explícitos de carregamento, vazio, erro e sucesso.
- Repetição de requisições com ação de tentar novamente.

## Decisões principais

- **Módulo único:** suficiente para o tamanho do desafio e mais fácil de avaliar.
- **Arquitetura em camadas:** a UI chama contratos de domínio implementados por dados; no código, UI e dados dependem do domínio.
- **UI híbrida intencional:**
  - lista, pesquisa, filtros e favoritos em Jetpack Compose;
  - detalhes em `Fragment` com layout XML;
  - navegação hospedada por uma única `Activity`.
- **Estado:** `StateFlow` nas telas Compose; `LiveData` na tela XML de detalhes.
- **Paginação:** `PagingSource` consumindo Retrofit. `RemoteMediator` não faz parte da primeira versão.
- **Room:** fonte de verdade apenas para favoritos na primeira versão.
- **DI:** Koin com módulos separados para rede, banco, repositórios e features.

A justificativa completa está em [`docs/context/ARCHITECTURE.md`](docs/context/ARCHITECTURE.md).

## Pré-requisitos

Baseline pesquisada em **30 de julho de 2026**:

- Android Studio Quail 2 | 2026.1.2 ou compatível.
- JDK 17.
- Android Gradle Plugin 9.3.0.
- Gradle 9.5.0.
- `compileSdk` e `targetSdk` 36.
- `minSdk` 23.

As versões das bibliotecas estão em [`docs/context/TECH_BASELINE.md`](docs/context/TECH_BASELINE.md). O agente responsável pelo bootstrap deve validá-las em conjunto antes de iniciar a implementação.

## Configuração da API

1. Crie uma conta no TMDB e gere um **API Read Access Token**.
2. Copie `local.properties.example` para `local.properties` e substitua os caminhos/valores locais:

```properties
sdk.dir=/caminho/absoluto/para/Android/sdk
TMDB_ACCESS_TOKEN=seu_token_aqui
```

3. O Gradle deve expor o valor apenas ao build local, por exemplo por `BuildConfig`, e um interceptor do OkHttp deve enviar:

```http
Authorization: Bearer <token>
```

Nunca grave o token em código-fonte, recursos, commits, logs ou arquivos de exemplo versionados.

## Como os agentes devem trabalhar

Leia nesta ordem:

1. `AGENTS.md`
2. `CONTEXT.md`
3. `docs/context/PRODUCT.md`
4. `docs/context/ARCHITECTURE.md`
5. o documento específico da tarefa
6. o `SKILL.md` aplicável

O fluxo recomendado é:

1. o agente principal escolhe uma fase em `docs/execution/PLAN.md`;
2. o arquiteto produz um plano restrito ao escopo;
3. apenas um agente de escrita atua em cada conjunto de arquivos compartilhados;
4. o revisor executa a quality gate;
5. o agente principal atualiza `CONTEXT.md` com decisões e pendências reais.

### Exemplos de delegação

- **Planejamento:** `android-architect`
- **Retrofit, Paging, Room e repositórios:** `data-layer-implementer`
- **Compose, XML, navegação e ViewModels:** `ui-layer-implementer`
- **Revisão final:** `qa-reviewer`

Os arquivos dos subagentes estão em `.codex/agents/`. O modelo padrão pode ser trocado em `.codex/config.toml` caso não esteja disponível no ambiente utilizado.

## Comandos de verificação

Depois que o projeto Gradle existir:

```bash
./gradlew assembleDebug
./gradlew testDebugUnitTest
./gradlew lintDebug
```

Quando houver dispositivo ou emulador configurado:

```bash
./gradlew connectedDebugAndroidTest
```

Uma fase só é considerada concluída após os testes relevantes, lint sem erros bloqueantes, ausência de segredo versionado e atualização do contexto.

## Estrutura planejada

```text
app/src/main/java/<application_id>/
├── core/
│   ├── common/
│   ├── database/
│   ├── network/
│   └── ui/
├── data/
│   ├── local/
│   ├── remote/
│   └── repository/
├── domain/
│   ├── model/
│   └── repository/
├── feature/
│   ├── details/
│   ├── favorites/
│   └── movies/
└── di/
```

Use cases só devem ser criados quando encapsularem uma regra, coordenarem mais de um repositório ou forem reutilizados. Não crie uma classe por operação apenas para aumentar o número de camadas.

## Atribuição do TMDB

O aplicativo deve incluir uma tela ou seção de créditos com o logotipo oficial do TMDB e o aviso exigido:

> This product uses the TMDB API but is not endorsed or certified by TMDB.

Consulte [`docs/context/TMDB_API.md`](docs/context/TMDB_API.md) antes de implementar recursos ou textos relacionados à API.

## Documentação do projeto

- [`AGENTS.md`](AGENTS.md): regras curtas e permanentes para qualquer agente.
- [`CONTEXT.md`](CONTEXT.md): estado atual, decisões já tomadas e próximos passos.
- [`docs/context/PRODUCT.md`](docs/context/PRODUCT.md): requisitos e critérios de aceite.
- [`docs/context/ARCHITECTURE.md`](docs/context/ARCHITECTURE.md): arquitetura e limites das camadas.
- [`docs/context/TMDB_API.md`](docs/context/TMDB_API.md): contrato externo, autenticação e política de filtros.
- [`docs/context/QUALITY.md`](docs/context/QUALITY.md): testes, lint, acessibilidade e definição de pronto.
- [`docs/context/TECH_BASELINE.md`](docs/context/TECH_BASELINE.md): baseline tecnológica datada.
- [`docs/execution/PLAN.md`](docs/execution/PLAN.md): sequência implementável por fases.

## Fontes técnicas

As referências oficiais consultadas estão listadas no final de cada documento de contexto. Ao atualizar uma decisão dependente de versão, registre a data e a fonte primária utilizada.
