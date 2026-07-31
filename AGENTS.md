# Instruções para agentes

Mantenha aqui somente regras permanentes. Contexto mutável pertence a `CONTEXT.md`; especificações, a `docs/context/`; procedimentos, às skills.

## Contexto e escopo

1. Leia `CONTEXT.md` e a fase ativa em `docs/execution/PLAN.md`.
2. Antes de alterar código, leia `docs/context/PRODUCT.md` e `docs/context/ARCHITECTURE.md`.
3. Carregue somente os documentos e skills aplicáveis: API TMDB, qualidade ou baseline tecnológica.

- Implemente apenas o escopo da fase ativa e prefira a menor solução testável.
- UI e dados dependem dos contratos de domínio; o domínio não depende do framework Android nem de Retrofit, Room, Coil ou Koin. `PagingData` é a única exceção AndroidX permitida no contrato paginado.
- Converta DTO, entidade Room, domínio e UI nas respectivas fronteiras.
- Não antecipe multi-módulo, `RemoteMediator`, cache geral, autenticação de usuário ou design system próprio.
- Justifique bibliotecas novas e confira `docs/context/TECH_BASELINE.md`.
- Nunca exponha o token do TMDB em código, recursos, logs, testes ou commits.

## Alterações e delegação

- Paralelize leitura; não permita escrita concorrente nos mesmos arquivos.
- Dê a cada subagente escopo, arquivos permitidos, critérios de aceite e validação.
- Não reformate arquivos não relacionados nem reverta mudanças sem evidência.
- Em conflito entre documentação e código, interrompa a expansão do escopo e proponha uma correção consistente.
- Subagentes reportam progresso e decisões; somente o agente principal atualiza `CONTEXT.md`.

## Verificação e entrega

Quando o projeto Gradle existir, execute:

```bash
./gradlew assembleDebug
./gradlew testDebugUnitTest
./gradlew lintDebug
```

Execute instrumentados para Room, navegação, Compose ou Fragment quando houver ambiente. Ao terminar, informe:

1. arquivos alterados;
2. comportamento implementado;
3. testes e comandos executados;
4. limitações ou riscos restantes;
5. atualização feita em `CONTEXT.md` pelo agente principal.
