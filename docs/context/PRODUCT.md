# Produto e critérios de aceite

## Objetivo

Entregar um aplicativo Android simples, funcional e demonstrável que permita descobrir filmes pelo TMDB, consultar detalhes e manter favoritos locais. A avaliação deve conseguir perceber arquitetura coerente, legibilidade, estado previsível, boa experiência e tratamento de falhas.

## Usuários e jornadas

### 1. Explorar filmes

Como usuário, quero abrir o aplicativo e ver filmes paginados para descobrir títulos sem esperar todo o catálogo carregar.

**Critérios de aceite**

- A primeira página é solicitada automaticamente.
- Cada item apresenta no mínimo pôster, título e data ou ano quando disponível.
- A rolagem solicita novas páginas sem bloquear a UI.
- Loading inicial, loading de próxima página, erro inicial, erro de próxima página, lista vazia e conteúdo são distinguíveis.
- Há ação de tentar novamente após falha.
- Filmes sem imagem ou data utilizam fallback visual/textual adequado.

### 2. Pesquisar

Como usuário, quero pesquisar por título para encontrar um filme específico.

**Critérios de aceite**

- O campo de busca aceita texto, limpar e nova tentativa.
- A busca usa debounce aproximado de 300–500 ms e cancela a consulta anterior quando uma nova consulta prevalece.
- Consulta em branco volta ao modo de descoberta.
- A tela informa quando nenhum resultado foi encontrado.
- A consulta é preservada durante recriação da tela pelo ViewModel/SavedStateHandle quando aplicável.

### 3. Filtrar resultados

Como usuário, quero refinar a descoberta para receber resultados mais relevantes.

**Filtros da primeira versão**

- gênero;
- ordenação suportada pelo discover;
- nota mínima;
- ano, quando suportado pelo endpoint ativo;
- limpar todos.

**Critérios de aceite**

- Alterar um filtro cria uma nova fonte paginada e volta à primeira página.
- O estado dos filtros fica visível e removível.
- A UI desabilita ou oculta filtros que o endpoint de pesquisa não suporta de forma global.
- Nenhum filtro local é aplicado apenas sobre páginas já carregadas como se representasse todo o catálogo.

### 4. Ver detalhes

Como usuário, quero selecionar um filme e consultar informações completas.

**Critérios de aceite**

- A navegação envia o `movieId`, não um DTO completo.
- A tela consulta detalhes pelo ID.
- Exibe título, sinopse, data de lançamento e imagem.
- Exibe loading, erro recuperável e conteúdo.
- Campos ausentes têm fallback legível.
- A tela permite favoritar/desfavoritar e reflete o estado persistido.
- A implementação visual desta feature usa XML e observa LiveData.

### 5. Gerenciar favoritos

Como usuário, quero salvar filmes para consultar depois, inclusive após fechar o aplicativo.

**Critérios de aceite**

- Favoritar realiza upsert local idempotente.
- Desfavoritar remove pelo ID do TMDB.
- A lista de favoritos observa Room por Flow.
- Favoritos permanecem após reinício do processo.
- Um favorito pode ser aberto na tela de detalhes.
- A UI reage imediatamente à alteração sem depender de nova chamada remota.

## Requisitos de experiência

- Material 3 e layout adaptável a celulares em retrato; paisagem não pode quebrar o conteúdo.
- Feedback visual claro para toque, seleção de filtro e favorito.
- Conteúdo acessível: contraste, área mínima de toque, descrições de imagens informativas e suporte a tamanho de fonte aumentado.
- Mensagens de erro orientam a próxima ação sem expor stack trace ou código interno desnecessário.
- A navegação para trás preserva busca, filtros e posição de lista quando tecnicamente viável.

## Requisitos não funcionais

- Kotlin idiomático, Coroutines e Flow com cancelamento estruturado.
- Sem trabalho bloqueante na Main thread.
- Dependências injetadas; não usar service locator manual em features.
- Segredos fora do controle de versão.
- Testes de unidade para regras e ViewModels; testes instrumentados para Room e UI crítica.
- Código formatado, nomes claros e comentários apenas para explicar decisões não óbvias.

## Fora do escopo da primeira versão

- Login de usuário e sincronização de favoritos com conta TMDB.
- Reprodução de trailers.
- Avaliações, listas personalizadas ou recomendações.
- Download offline de imagens.
- Cache completo do catálogo com `RemoteMediator`.
- Multi-módulo, design system próprio ou arquitetura multiplataforma.
- Analytics, push notification e publicação em loja.

## Critério de entrega

O avaliador deve conseguir clonar, inserir um token em `local.properties`, compilar, executar e validar todas as jornadas acima sem editar código-fonte.
