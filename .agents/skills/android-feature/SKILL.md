---
name: android-feature
description: Orquestra uma feature Android vertical deste projeto. Use ao implementar lista, detalhes, pesquisa, filtros ou favoritos envolvendo uma ou mais camadas.
---

# Android feature

## Fluxo

1. Leia `CONTEXT.md`, a fase em `docs/execution/PLAN.md` e os critérios da feature em `docs/context/PRODUCT.md`.
2. Consulte `docs/context/ARCHITECTURE.md` e `docs/context/QUALITY.md`; quando houver UI, leia também `docs/context/DESIGN_SYSTEM.md`; para API TMDB, use também `tmdb-integration`.
3. Delimite critérios de aceite, contratos necessários, arquivos e validação. Pare se uma dependência anterior não existir ou houver contradição não resolvida.
4. Implemente uma fatia compilável. Quando separar dados e UI entre agentes, conclua e valide o contrato de dados antes da escrita da UI.
5. Adicione testes para as regras e transições alteradas.
6. Execute a verificação padrão de `AGENTS.md` e testes instrumentados quando aplicáveis.

## Limites

- Não crie abstrações ou estruturas para fases futuras.
- Preserve o ownership dos agentes e as fronteiras documentadas.
- Cubra sucesso, ausência de dados, erro e retry; inclua cancelamento, persistência ou recriação quando relevantes.
- Trate acessibilidade e textos de usuário no mesmo incremento de UI.

## Conclusão

Conclua somente quando cada critério da fatia estiver implementado, testado ou registrado como limitação objetiva. Entregue ao agente principal comportamento, arquivos, testes, riscos e decisões duráveis; não edite `CONTEXT.md` como subagente.
