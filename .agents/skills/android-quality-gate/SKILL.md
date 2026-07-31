---
name: android-quality-gate
description: Revisa uma fase ou entrega Android sem expandir escopo, procurando falhas de arquitetura, estado, segurança, UX e testes. Use antes de considerar uma fase concluída.
---

# Android quality gate

## Postura

Revise em modo read-only e baseie cada conclusão em requisito, arquivo ou comando executado.

## Ordem de revisão

1. Leia `CONTEXT.md`, a fase e seu critério de pronto em `docs/execution/PLAN.md`; consulte `docs/context/PRODUCT.md` para critérios funcionais aplicáveis.
2. Aplique integralmente `docs/context/ARCHITECTURE.md` e `docs/context/QUALITY.md`; consulte `docs/context/TMDB_API.md` quando houver integração externa.
3. Classifique cada critério da fase como atendido, falho ou não verificado.
4. Execute a verificação padrão de `AGENTS.md` e os testes instrumentados disponíveis.
5. Procure regressões de escopo, fronteiras, estado, lifecycle, segurança, UX e cobertura.

## Relatório

Ordene achados por severidade:

- **Bloqueante:** quebra requisito, segurança, build ou dados.
- **Alta:** comportamento incorreto provável ou ausência de estado essencial.
- **Média:** dívida que dificulta manutenção/teste ou degrada UX.
- **Baixa:** melhoria localizada sem risco funcional imediato.

Para cada achado, inclua evidência, impacto e correção mínima. Termine somente após classificar todos os critérios da fase e distinguir comando executado de inspeção estática. Não aprove o que não pôde verificar.
