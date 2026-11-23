# Revisão da documentação de testes — 23/11/2026

## Resultado real desta execução

| Grupo | Aprovados | Reprovados | Ignorados | Forma de execução |
|---|---:|---:|---:|---|
| Backend JUnit/Mockito | 125 | 0 | 0 | `mvnw.cmd test`, Java 21, BUILD SUCCESS |
| Frontend Angular/Vitest | 58 | 0 | 0 | `npm.cmd test -- --watch=false`, saída 0 |
| Scripts e segurança da coleção | 19 | 0 | 0 | `node --test docs/testes/validar-colecao.cjs`, offline |
| **Total automatizado executado** | **202** | **0** | **0** | Sem banco real |
| Coleção via HTTP/Postman Runner | — | — | — | Não executada nesta revisão |
| Navegador e hospedagem | — | — | — | Não executados nesta revisão |

Os testes backend usam objetos e repositórios simulados com Mockito, sem subir contexto JPA. Os testes frontend usam HttpTestingController. Os testes da coleção usam o módulo nativo `vm` do Node para exercitar scripts com um objeto `pm` simulado; isso não testa a API HTTP real nem substitui o Runner.

Não foi iniciado backend conectado a banco. Nenhuma requisição de teste foi enviada ao Railway, nenhum dado MySQL foi criado, alterado ou excluído. Por isso não houve registros temporários para limpar nesta execução. A confirmação da limpeza da coleção sobre um banco isolado ainda depende da execução HTTP.

## Evidências disponíveis

- [Resumo de resultados](evidencias-2026-11-23/resultados.json): contagens backend extraídas dos XMLs Surefire gerados nesta execução, sem copiar propriedades do sistema ou caminhos pessoais; resumo observado do frontend.
- [Saída TAP dos 19 testes offline](evidencias-2026-11-23/colecao-offline.tap).
- [Trecho real da saída Vitest](evidencias-2026-11-23/frontend-vitest.txt).
- [Script reproduzível de validação offline](validar-colecao.cjs).
- [Guia de importação e execução segura](GUIA_POSTMAN.md).

Os XMLs completos do Maven ficam em `target/surefire-reports` localmente e não foram incluídos no repositório, pois podem conter propriedades e caminhos do computador. O aviso do Mockito sobre carregamento dinâmico do agente no Java 21 apareceu, mas não causou falhas. Não foram desativados ou alterados testes existentes.

### Backend por classe

| Classe | Testes | Falhas/erros |
|---|---:|---:|
| AtletaParticipacaoTests | 2 | 0 |
| ClassificacaoServicoTests | 14 | 0 |
| DesempenhoAtletaTests | 16 | 0 |
| EquipeEscudoTests | 22 | 0 |
| EstatisticaAtletaServicoTests | 18 | 0 |
| GeracaoPartidasServicoTests | 9 | 0 |
| InscricaoServicoTests | 10 | 0 |
| ParticipacaoAtletaServicoTests | 10 | 0 |
| PartidaServicoTests | 9 | 0 |
| RegrasEsportivasTests | 10 | 0 |
| TorneioServicoTests | 5 | 0 |

## Revisão da coleção

Foram conferidos estaticamente os métodos e caminhos das **80 requisições**, comparando-os às **43 rotas declaradas nos controllers**. Os corpos JSON foram analisados com substituições de exemplo, e os scripts JavaScript passaram pela análise de sintaxe. Isso confirma a correspondência estrutural, não 80 respostas HTTP bem-sucedidas.

As 47 requisições anteriores foram preservadas, incluindo CRUDs, acesso público, CONSULTA e erros 400/401/403/404/409. Foram corrigidos os seguintes problemas da coleção antiga:

- Cadastro de partidas antes de haver duas equipes inscritas e aprovadas.
- Ausência do campo obrigatório `corEscudo` nas requisições de equipes.
- Modalidade genérica que não permitia testar estatísticas e classificação esportivas. Foi mantida para seu próprio CRUD e separado o uso controlado de Futsal.
- Ausência de cobertura para participações, estatísticas, desempenho, resultados/classificação e indicadores.
- Falta de bloqueio de destino externo e confirmação do banco isolado.
- Controle de IDs sem diário persistente de pendências; agora novas execuções são bloqueadas enquanto houver IDs a limpar.

Há **33 requisições adicionais**. Participações são testadas antes das estatísticas. A classificação e os próximos jogos são consultados sem autenticação. PUT e DELETE para CONSULTA usam somente equipes temporárias. A geração é exercitada no caso de confronto já existente, esperando lista vazia; a geração de um campeonato completo continua coberta pelos testes unitários existentes, não por uma nova simulação HTTP nesta revisão.

Os 19 testes offline verificam, entre outros pontos, bloqueio de Railway e outros destinos externos, URL disfarçada, troca de ambiente, ausência de confirmação, variáveis sobrepostas, ID desconhecido, proteção da modalidade reutilizada, captura de IDs e retenção do diário quando uma exclusão falha.

## Arquivos e preservação

Alterados:

- `ecommerce-backend-4a-main/ecommerce-backend-4a-main/ecommerce/postman/E-sports.postman_collection.json`.
- `ecommerce-backend-4a-main/ecommerce-backend-4a-main/ecommerce/postman/E-sports.postman_environment.json`.

Criados: este relatório, `GUIA_POSTMAN.md`, `validar-colecao.cjs` e os três arquivos da pasta `evidencias-2026-11-23` citados acima.

Os READMEs existentes, o diagrama de classes, Java, Angular, regras de negócio e configurações da aplicação foram preservados. Os JSONs distribuídos contêm apenas referências a variáveis para autenticação, com credenciais vazias no ambiente. Não houve commit ou push.

## Pendências para a entrega

1. Disponibilizar e confirmar uma instância local com banco exclusivo de testes e executar a coleção completa no Postman conforme o guia. Nesta revisão não foi confirmado um banco isolado para autorizar a execução HTTP.
2. Anexar a contagem real de asserções e requisições dessa execução, distinguindo falhas de requisições condicionais ignoradas. Confirmar que o diário terminou vazio.
3. Se necessário para a avaliação, anexar evidências manuais do navegador com versão/data, sem credenciais ou dados pessoais. O grupo informou testes manuais prévios, mas não foram inventados registros ou contagens desses testes.
4. Os READMEs foram mantidos intactos conforme solicitado. Referências antigas à coleção de 47 requisições devem ser lidas como históricas; a versão revisada e suas limitações estão descritas neste documento e no guia.

A documentação está preparada para importação e execução local. Não se declara que a coleção revisada passou contra a API real ou contra a hospedagem.
