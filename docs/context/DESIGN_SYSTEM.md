# Direção visual e componentes

## Papel deste documento

Este arquivo é a fonte de verdade visual do aplicativo. Ele descreve tokens, hierarquia, componentes e comportamento adaptável. Não cria uma biblioteca ou módulo de design system: os recursos permanecem no módulo `app` e só devem ser extraídos para `core/ui` quando forem usados por duas ou mais features.

A referência é uma combinação de navegação direta e coleção pessoal inspiradas no TV Time com a hierarquia de imagem e metadados do TMDB. A implementação deve ter identidade própria e não copiar marcas, logos ou layouts pixel a pixel.

## Princípios

1. Pôsteres e backdrops são o primeiro nível da hierarquia.
2. Verde indica ação ou estado selecionado; não é decoração generalizada.
3. Cada tela possui uma ação primária evidente e poucos controles simultâneos.
4. Conteúdo real, fallbacks e estados de erro devem preservar a composição.
5. A interface é dark-first. A primeira entrega pode ser somente escura se isso for registrado no plano; suporte claro não deve produzir uma versão sem direção visual.

## Tokens

### Cores

| Papel | Valor |
|---|---|
| Background principal | `#0B0D0E` |
| Superfície dos cards | `#151819` |
| Superfície secundária | `#202425` |
| Destaque | `#65D46E` |
| Texto principal | `#F4F6F5` |
| Texto secundário | `#A7AEAA` |
| Divisores | `#2A2E2F` |
| Estrela/nota | `#FFC94A` |

As cores devem ser expostas por `MaterialTheme.colorScheme` ou tokens semânticos, nunca repetidas como literais em telas. Verde sobre superfícies escuras deve passar por verificação de contraste para texto e ícones. Estado selecionado também precisa de forma, preenchimento, ícone ou rótulo; não pode depender apenas da cor.

### Tipografia

| Uso | Tamanho | Peso |
|---|---:|---|
| Título de tela | 28–32 sp | Bold |
| Título de seção | 20–22 sp | SemiBold |
| Título de card | 14–16 sp | Medium |
| Metadados | 12–14 sp | Regular |
| Título em detalhes | 30–36 sp | Bold |
| Sinopse | 15–16 sp | Regular, altura de linha confortável |

Não fixe altura de contêiner de texto que corte conteúdo com fonte ampliada. Títulos de card podem usar no máximo duas linhas com reticências; informação crítica deve continuar disponível na tela de detalhes e para acessibilidade.

### Forma e espaçamento

| Token | Valor |
|---|---:|
| Margem horizontal | 16 dp |
| Espaço entre seções | 24 dp |
| Espaço entre cards | 12 dp |
| Raio de card | 14 dp |
| Raio de botão | 24 dp |
| Área mínima de toque | 48 dp |
| Pôster | proporção 2:3 |

Use insets seguros do sistema. Dimensões podem crescer em telas largas; não devem encolher os alvos de toque.

### Ícones

Preferir Material Symbols Rounded ou equivalentes Material já disponíveis: início, coração vazio/cheio, busca, voltar, estrela, reproduzir, ordenar e chevron. Ícones interativos exigem rótulo acessível. Coração sobre imagem usa superfície circular escura translúcida para manter legibilidade.

## Navegação

A primeira versão possui três destinos de nível superior:

- `Início`: Discover de filmes;
- `Favoritos`: coleção local;
- `Perfil`: informações e preferências locais, incluindo créditos do TMDB.

A busca é uma tela própria acessada pela lupa da Home. A barra inferior fica visível apenas nos destinos de nível superior; detalhes e busca usam navegação de retorno. O item ativo combina verde, ícone preenchido e rótulo.

`Perfil` não representa uma conta TMDB. Não exibir avatar pessoal, login, sincronização ou dados remotos de usuário enquanto autenticação não fizer parte do escopo.

## Telas

### Discover de filmes

Ordem vertical:

1. top bar com nome curto do app e busca;
2. banner de destaque com backdrop, gradiente, categoria, título, ano, nota, favorito e indicador do carrossel;
3. `Em alta nesta semana`;
4. `Nos cinemas`;
5. `Clássicos imperdíveis`;
6. `Mais bem avaliados`;
7. navegação inferior.

As seções usam carrosséis horizontais de `MoviePosterCard`. `Ver todos` abre uma lista paginada reutilizando a busca/filtros existentes com o contexto da seção. O banner não deve avançar automaticamente na primeira implementação; isso evita problemas de acessibilidade e estado sem acrescentar valor essencial.

`Clássicos imperdíveis` é uma curadoria algorítmica, não uma categoria oficial do TMDB. Deve usar regra explícita e testável, como data limite, nota mínima e quantidade mínima de votos. O chip de década é derivado do ano.

### Favoritos

Cabeçalho com título, quantidade, busca local e ordenação. Conteúdo em grid adaptável de pôsteres:

- 2 colunas em largura compacta;
- 3 colunas quando houver espaço suficiente sem reduzir pôsteres ou toque;
- coração preenchido para remoção, com confirmação visual imediata;
- título, ano e nota quando persistidos.

Ordenação em bottom sheet: adicionados recentemente, título A–Z, maior nota, mais recentes e mais antigos. A ordenação é local e não altera `favoritedAt`.

O estado vazio contém ícone, título, mensagem e ação `Explorar filmes`, que volta para Início.

### Perfil local

Tela enxuta, sem identidade fictícia de usuário:

- título `Perfil` ou `Meu MovieBox` após definição do nome final;
- resumo local da coleção, como quantidade de favoritos;
- preferência de aparência disponível de fato;
- seção `Sobre` com versão do app;
- créditos, logo e aviso obrigatório do TMDB;
- links externos somente quando tiverem destino real e seguro.

Não incluir `Continue assistindo`, `Assistidos`, watchlist ou estatísticas que o app ainda não persiste.

### Detalhes e busca

A linguagem visual definida aqui também se aplica às telas existentes de detalhes e busca. A revisão de detalhes deve usar backdrop com gradiente, pôster, título, metadados, nota, favorito e sinopse. Trailer e elenco só entram quando endpoints e contratos correspondentes fizerem parte de uma fase explícita.

## Componentes reutilizáveis

- `MoviePosterCard`: pôster 2:3, favorito, título, ano e nota;
- `FeaturedMovieBanner`: backdrop, categoria, título, metadados e favorito;
- `SectionHeader`: título e ação opcional;
- `FavoriteButton`: estado vazio/selecionado com texto;
- `RatingBadge`: estrela amarela e nota localizada;
- `MovieMetadataRow`: metadados separados semanticamente;
- `EmptyFavoritesState`: ícone, mensagem e ação;
- `MovieBottomBar`: destinos principais.

Componentes devem receber modelos de UI e callbacks; não acessam ViewModel, repository, API ou DAO diretamente.

## Imagens e conteúdo

- backdrop usa tamanho apropriado e `ContentScale.Crop` com ponto focal central por padrão;
- pôster mantém 2:3 e fallback com o mesmo espaço reservado;
- gradiente garante leitura sem apagar a imagem inteira;
- nota ausente não é exibida como zero;
- ano, imagem ou sinopse ausentes usam fallback localizado;
- imagem decorativa usa descrição nula; pôster informativo descreve o título sem duplicar texto anunciado no mesmo elemento clicável.

## Critérios visuais de aceite

- a primeira dobra da Home comunica filme em destaque e próximo conteúdo sem parecer congestionada;
- nenhum texto ou controle crítico é cortado com fonte a 200%;
- navegação e favoritos são operáveis por TalkBack e área mínima de 48 dp;
- pôsteres não mudam de tamanho durante loading/erro;
- contraste é verificado nas combinações usadas, inclusive verde e texto secundário;
- grid e carrosséis funcionam em retrato, paisagem e largura ampliada;
- loading, vazio, erro, conteúdo e retry mantêm a identidade visual;
- screenshots finais usam dados reais ou fixtures identificadas, nunca a arte conceitual como se fosse saída do app.
