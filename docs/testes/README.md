# Evidências de testes

## Última execução registrada — 21/11/2026

| Verificação | Resultado |
|---|---|
| JUnit/Mockito backend | 125 aprovados; 0 falhas, erros ou ignorados |
| Vitest/Angular frontend | 58 aprovados; 0 falhas |
| Compilação backend | BUILD SUCCESS |
| Build Angular | Aprovado, com aviso de bundle inicial acima de 500 kB |
| Indicadores por HTTP real | Totais conferidos para visitante, CONSULTA e ORGANIZADOR, sem gravações |

São os resultados da execução anterior dos KPIs. Nesta preparação documental, os 11 XML locais em `target/surefire-reports` foram conferidos e totalizam 125 testes. A evidência frontend é a saída registrada na sessão de desenvolvimento; não há relatório frontend exportado permanente no repositório. Não foram reexecutados testes somente para alterar documentação.

## Materiais

- [Relatório histórico de revisão](../../RELATORIO_REVISAO_FINAL.md), datado de 20/11/2026: suas contagens correspondem à versão daquele dia.
- Testes fonte: `frontend/src/app/app.component.spec.ts` e `ecommerce-backend-4a-main/ecommerce-backend-4a-main/ecommerce/src/test/java/`.
- Relatórios brutos JUnit: `target/surefire-reports`, excluídos do Git por conterem propriedades e caminhos do ambiente.
- [Coleção Postman](../../ecommerce-backend-4a-main/ecommerce-backend-4a-main/ecommerce/postman/E-sports.postman_collection.json): JSON válido, 47 requisições; desatualizada em campos e endpoints recentes. Não executada nesta preparação.

## Reproduzir

Backend, na pasta do pom.xml: `.\mvnw.cmd -B test`.

Frontend, na pasta frontend: `npm test -- --watch=false`.

Ambos usam mocks, sem conexão ao MySQL real. Testes unitários/componente não equivalem a verificações HTTP reais ou testes de navegador.

## Pendências

Adicionar evidências visuais finais do fluxo autenticado/responsivo, exportar o resultado frontend e atualizar o Postman. Remover credenciais, dados pessoais e propriedades do ambiente de relatórios compartilhados. Não declarar cobertura percentual ou testes não medidos/executados.
