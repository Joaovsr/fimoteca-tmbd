---
name: tmdb-integration
description: Implementa integração com a API TMDB. Use ao adicionar ou alterar autenticação, endpoint, DTO, paginação, busca, filtro, imagem ou erro externo.
---

# TMDB integration

## Fluxo

1. Leia `docs/context/TMDB_API.md` e confirme na documentação oficial qualquer contrato ausente ou divergente.
2. Identifique endpoint, parâmetros suportados, resposta e erros relevantes.
3. Defina DTO mínimo e nullável e uma fixture com campos ausentes.
4. Implemente parsing, mapeamento e a menor alteração necessária em API, repository ou `PagingSource`.
5. Mapeie falhas técnicas para o contrato de erro do domínio.
6. Teste sucesso, vazio, limites de página e falhas relevantes ao endpoint.
7. Verifique que tipos externos não vazam, que o token não aparece em código ou fixtures e que Authorization está redigido nos logs.

## Conclusão

Conclua somente quando o contrato alterado estiver coberto por parsing, mapeamento, erro e segurança aplicáveis. Informe ao agente principal endpoint, parâmetros, testes e divergências; não edite `CONTEXT.md` como subagente.
