# Qualidade e testes

## Objetivo

Assegurar comportamento previsível, código legível e evidência automatizada para os pontos que o desafio avalia: arquitetura, organização, UX e gerenciamento de estados.

## Pirâmide de testes

### Unidade

Cobrir:

- mapeamento DTO → domínio e entity → domínio;
- política de endpoint e filtros;
- cálculo de `prevKey`/`nextKey` e erros no PagingSource;
- repositórios com fontes falsas;
- ViewModels: loading, sucesso, vazio, erro, retry, mudança de consulta/filtro e favorito;
- debounce/cancelamento usando scheduler de teste, sem espera real;
- mensagens/fallbacks derivados de dados ausentes.

Ferramentas esperadas:

- `kotlinx-coroutines-test`;
- `androidx.paging:paging-testing`;
- fakes manuais pequenos; biblioteca de mocking somente se reduzir complexidade real.

### Instrumentados

Cobrir:

- DAO Room em banco em memória: upsert, remoção, ordenação e observação;
- migração quando a versão do schema passar de 1;
- tela Compose em loading, vazio, erro e conteúdo;
- ação de retry;
- seleção de filtro e limpeza;
- navegação lista → detalhes → voltar;
- renderização XML de detalhes e favorito.

### Manuais

Checklist antes da entrega:

- token ausente produz orientação segura e não crash opaco;
- modo avião e conexão lenta;
- rolagem até novas páginas;
- nova busca enquanto a anterior está em andamento;
- rotação/recriação;
- tema escuro em modo normal e alto contraste; tema claro somente se a preferência entrar na fase;
- fonte ampliada;
- filme sem pôster, data ou sinopse;
- persistência de favoritos após encerrar o processo;
- TalkBack ou inspeção de acessibilidade nos fluxos principais.
- Home com banner e carrosséis em rede rápida, lenta e offline;
- grid de favoritos com 2 e 3 colunas, ordenação e remoção;
- barra inferior nos três destinos principais e retorno correto em busca/detalhes;
- contraste dos tokens de `docs/context/DESIGN_SYSTEM.md` e legibilidade sobre backdrops;
- ausência de promessa de conta, assistidos ou séries antes dessas features existirem.

## Matriz por feature

| Feature | Unidade | Instrumentado | Manual |
|---|---|---|---|
| Lista paginada | PagingSource, ViewModel | Compose/load states | rolagem e retry |
| Pesquisa | debounce, cancelamento, política | campo e vazio | digitação rápida |
| Filtros | validação e recriação do Pager | chips/sheet e limpar | combinações suportadas |
| Detalhes | repository e ViewModel | Fragment XML | campos ausentes |
| Favoritos | repository | Room + UI | reinício do app |
| Discover editorial | regras/fontes por seção | seções, favorito e navegação | carrosséis e imagens |
| Perfil local | estado de preferências | navegação e conteúdo | créditos e links |

## Padrões de código

- APIs públicas pequenas; visibilidade mínima necessária.
- Estado imutável e nomes que descrevem intenção.
- Funções curtas quando isso melhora a leitura, sem fragmentação artificial.
- Não usar `!!` para dados da API.
- Não capturar `Exception` genérica sem mapear, registrar ou relançar corretamente.
- Não usar `GlobalScope`.
- Não iniciar coroutine em Composable para lógica de negócio.
- Não usar mutable collections como estado público.
- Não duplicar estado derivável, especialmente loading do Paging.
- Recursos de string para texto de UI.

## Revisão de arquitetura

O revisor deve procurar:

- imports indevidos entre camadas;
- API/DAO chamados diretamente por UI;
- DTO ou Entity vazando para UI;
- ViewModel segurando Context/Fragment/View;
- token hardcoded ou impresso;
- Flow coletado sem respeito ao lifecycle;
- múltiplos agentes alterando arquivos compartilhados sem coordenação;
- filtros locais incompletos sobre paginação;
- perda de estado ao navegar ou rotacionar;
- erros silenciosos e retry inexistente;
- testes que apenas repetem implementação sem validar comportamento.

## Qualidade visual e acessibilidade

- Material 3 e espaçamento consistente.
- Alvos de toque adequados.
- Contraste aceitável nos dois temas.
- Não depender somente de cor para indicar seleção/erro.
- Loading não deve cobrir conteúdo já disponível durante append.
- Erro de append aparece inline e mantém itens carregados.
- Placeholder evita salto excessivo de layout.
- Texto suporta escala de fonte e não corta informação crítica.
- Pôsteres preservam proporção 2:3 e espaço durante placeholder/erro.
- Backdrops possuem gradiente suficiente sem esconder o assunto principal.
- Grid, carrosséis e banner são verificados em retrato, paisagem e fonte a 200%.
- Componentes e tokens seguem `docs/context/DESIGN_SYSTEM.md` sem literais visuais duplicados nas features.

## Gates por pull request ou fase

Execute a verificação padrão definida em `AGENTS.md` quando o projeto existir.

Quando houver ambiente Android:

```bash
./gradlew connectedDebugAndroidTest
```

O relatório final deve listar os comandos executados e os que não puderam ser executados, com motivo concreto.

## Definição de pronto da entrega

- Todas as jornadas obrigatórias funcionam.
- Estados de loading, vazio, erro e sucesso estão representados.
- Favoritos persistem.
- Paginação e retry funcionam.
- Busca cancela trabalho obsoleto.
- Filtros não prometem capacidade que o endpoint não oferece.
- Testes relevantes passam.
- Lint sem erros bloqueantes.
- Nenhum segredo ou artefato local foi versionado.
- README permite configurar e executar do zero.
- Créditos e atribuição do TMDB estão presentes.
