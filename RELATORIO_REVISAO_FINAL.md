# Relatório da revisão final — E-sports

Data: 20/11/2026. Ambiente local: Angular 21/Material, Java 21, Spring Boot, HTTP Basic e MySQL.

## 1. Resumo geral

O fluxo principal foi exercitado por HTTP em quatro competições temporárias: cadastro, inscrição, aprovação, geração de confrontos, resultados, estatísticas e classificação. Foram corrigidos erros reproduzidos e acrescentados testes de regressão. O projeto não foi recriado, não recebeu bibliotecas adicionais e não teve testes removidos ou desativados.

A revisão funcional local terminou com builds e testes aprovados. Isso não equivale a homologação visual completa nem a aprovação de uma instalação em produção. As limitações estão nas seções 16 e 17.

Estrutura preservada: frontend Angular com NgModules e Reactive Forms; backend Maven separado em controllers, DTOs, services, repositories e entidades. A conexão verificada foi exclusivamente `localhost:3306/esports`, com `ddl-auto=update` e credenciais por variáveis de ambiente. Nenhum comando SQL de limpeza ou recriação foi executado.

## 2. Problemas identificados

Cinco falhas foram reproduzidas por HTTP antes das correções:

1. Cadastro manual aceitava confronto duplicado (201 em vez de 409).
2. Edição manual aceitava sobreposição de horário de uma equipe (200 em vez de 409).
3. Edição aceitava partida fora do período do torneio (200 em vez de 400).
4. Estatística de Futsal com assistências era rejeitada (400 em vez de 201).
5. Basquete aceitava resultado final empatado (200 em vez de 400).

A inspeção de código também encontrou: estado de inscrição pouco claro após solicitar/retornar à tela; colunas de estatísticas incompatíveis com o esporte; Rocket League tratado como jogo de abates; ausência de limite da soma de gols/pontos individuais em relação ao placar final.

## 3. Problemas corrigidos

- Cadastro e edição de partidas agora verificam adversários duplicados, intervalo de duas horas por equipe e período do torneio. A geração existente foi preservada.
- Resultados finais de Basquete e Rocket League não aceitam empate. Empates continuam permitidos em Futebol, Futsal e Handebol.
- Estatísticas respeitam o esporte: gols e assistências; pontos e assistências no Basquete; somente pontos no Vôlei; abates e assistências nos jogos correspondentes. Rocket League usa gols e assistências.
- Em partidas finalizadas, a soma dos gols/pontos registrados não pode exceder o placar da equipe. A validação ocorre ao cadastrar estatística e ao alterar/finalizar partida. Estatísticas parciais continuam permitidas; não são inventados valores faltantes. Pontos do Vôlei não são comparados a sets e abates não são comparados a mapas.
- Equipes e inscrições consultam o estado persistido: “Solicitar inscrição”, “Aguardando aprovação”, “INSCRITO” e “Inscrição rejeitada”. A seleção da competição na página Equipes permanece na URL ao recarregar. O retorno do cadastro atualiza imediatamente a lista e bloqueia uma segunda solicitação.
- Formulário e tabela de estatísticas mostram apenas os campos correspondentes à modalidade. A prévia do escudo foi reposicionada para preservar o alinhamento do formulário de equipes.

## 4. Arquivos alterados

Backend, sob `ecommerce-backend-4a-main/ecommerce-backend-4a-main/ecommerce/`:

- `src/main/java/br/edu/unifio/ecommerce/servicos/PartidaServico.java`
- `src/main/java/br/edu/unifio/ecommerce/servicos/RegrasEsportivas.java`
- `src/main/java/br/edu/unifio/ecommerce/servicos/EstatisticaAtletaServico.java`
- `src/test/java/br/edu/unifio/ecommerce/servicos/PartidaServicoTests.java`
- `src/test/java/br/edu/unifio/ecommerce/servicos/GeracaoPartidasServicoTests.java`
- `src/test/java/br/edu/unifio/ecommerce/servicos/RegrasEsportivasTests.java`
- `src/test/java/br/edu/unifio/ecommerce/servicos/EstatisticaAtletaServicoTests.java`

Frontend:

- `frontend/src/app/paginas/equipes/equipes.component.ts` e `.html`
- `frontend/src/app/paginas/inscricoes/inscricoes.component.ts` e `.html`
- `frontend/src/app/paginas/partidas/estatisticas.component.ts` e `.html`
- `frontend/src/app/app.component.spec.ts`

Criado: `RELATORIO_REVISAO_FINAL.md`. Artefatos de build foram regenerados. O roteiro HTTP e seus registros de execução ficaram na pasta temporária local, fora do código da aplicação e sem credenciais gravadas.

## 5. Simulação — Futsal

Torneio temporário ID 15; período 2026-11-23 a 2026-11-26; prazo 2026-11-22. Quatro equipes, doze atletas, quatro inscrições aprovadas e seis confrontos únicos. Cada equipe tinha três atletas. Foram usadas cores verde-lima, azul, branco e preto e opções distintas de escudo.

Duas partidas finalizadas e quatro agendadas, com quatro registros individuais compatíveis. Resultados: 3 × 1 e 2 × 2; vitória vale 3 pontos, empate 1 e derrota 0.

Conferidos por HTTP: inscrição inicialmente PENDENTE mesmo quando o cliente envia APROVADA, rejeição/aprovação persistidas, bloqueio de duplicata, segunda geração sem novas partidas, ausência de sobreposição, resultados e recálculo da classificação. Todos os registros dessa simulação foram removidos ao final.

## 6. Simulação — Basquete

Torneio temporário ID 16; período 2026-11-24 a 2026-11-27; prazo 2026-11-22. Quatro equipes, doze atletas, quatro inscrições aprovadas e seis confrontos únicos. Cada equipe tinha três atletas. Foram usadas cores verde-lima, azul, branco e preto e opções distintas de escudo.

Duas partidas finalizadas e quatro agendadas, com quatro registros individuais compatíveis. Resultados: 80 × 70 e 60 × 65; vitória vale 2 pontos e derrota 1, conforme a regra existente.

Conferidos por HTTP: inscrição inicialmente PENDENTE mesmo quando o cliente envia APROVADA, rejeição/aprovação persistidas, bloqueio de duplicata, segunda geração sem novas partidas, ausência de sobreposição, resultados e recálculo da classificação. Todos os registros dessa simulação foram removidos ao final.

## 7. Simulação — Vôlei

Torneio temporário ID 17; período 2026-11-25 a 2026-11-28; prazo 2026-11-22. Quatro equipes, doze atletas, quatro inscrições aprovadas e seis confrontos únicos. Cada equipe tinha três atletas. Foram usadas cores verde-lima, azul, branco e preto e opções distintas de escudo.

Duas partidas finalizadas e quatro agendadas, com quatro registros individuais compatíveis. Resultados: 3 × 1 e 2 × 3 em sets; ordenação por vitórias, sem pontuação de futebol.

Conferidos por HTTP: inscrição inicialmente PENDENTE mesmo quando o cliente envia APROVADA, rejeição/aprovação persistidas, bloqueio de duplicata, segunda geração sem novas partidas, ausência de sobreposição, resultados e recálculo da classificação. Todos os registros dessa simulação foram removidos ao final.

## 8. Simulação — Valorant

Torneio temporário ID 18; período 2026-11-26 a 2026-11-29; prazo 2026-11-22. Quatro equipes, doze atletas, quatro inscrições aprovadas e seis confrontos únicos. Cada equipe tinha três atletas. Foram usadas cores verde-lima, azul, branco e preto e opções distintas de escudo.

Duas partidas finalizadas e quatro agendadas, com quatro registros individuais compatíveis. Resultados: 2 × 1 e 0 × 2 em mapas; ordenação por vitórias, sem pontuação de futebol.

Conferidos por HTTP: inscrição inicialmente PENDENTE mesmo quando o cliente envia APROVADA, rejeição/aprovação persistidas, bloqueio de duplicata, segunda geração sem novas partidas, ausência de sobreposição, resultados e recálculo da classificação. Todos os registros dessa simulação foram removidos ao final.

As datas foram calculadas a partir da execução, com prazos abertos. Os resultados foram lançados antecipadamente apenas nos registros fictícios, como simulação; não representam partidas reais. Futsal e Basquete reutilizaram modalidades existentes. Vôlei e E-sports foram criadas temporariamente com nome canônico exigido pelas regras e identificação TESTE_REVISAO na descrição, e depois removidas. Os demais cadastros nomeados receberam o prefixo TESTE_REVISAO.

## 9. Inscrições

HTTP aprovado nas quatro modalidades: criação PENDENTE, aprovação, rejeição, persistência por nova consulta, duplicata inclusive após rejeição, limite de vagas, prazo expirado mesmo com tentativa de retroagir data e incompatibilidade entre modalidades.

Testes Angular verificam os rótulos dos três estados, bloqueio de nova solicitação após sucesso e recuperação do estado pela API. O organizador pode rever uma inscrição rejeitada pelo fluxo de edição existente; o sistema não cria uma segunda inscrição para o mesmo vínculo.

A atualização visual em sessão autenticada não foi executada no navegador: sua cobertura nesta revisão é por testes de componente com mocks e HTTP real.

## 10. Partidas

Geração todos contra todos: seis pares para quatro equipes, sem duplicatas; repetir geração retorna lista vazia. Intervalo mínimo de duas horas entre partidas da mesma equipe conferido nas quatro tabelas.

Também verificados: equipes iguais, equipe de outra competição, placar negativo, finalização sem placares, impedimento de excluir torneio com partidas, transição de status e atualização posterior de resultado. A classificação foi consultada novamente após modificar e restaurar resultado fictício. As cinco reproduções iniciais passaram no reteste.

Testes unitários cobrem adicionalmente as validações da agenda manual e as regras de geração. A revisão não é um teste de concorrência de vários organizadores salvando ao mesmo tempo.

## 11. Estatísticas individuais

Cadastrados e consultados registros compatíveis nas quatro competições. Testados atleta/partida, duplicidade, campos incompatíveis e excesso de gols/pontos em relação ao resultado final. Dados fictícios foram removidos em primeiro lugar na limpeza.

Os testes parametrizados cobrem Futebol, Futsal, Handebol, Basquete, Vôlei, Rocket League e jogos eletrônicos nas regras pertinentes. As colunas Angular foram testadas por modalidade. Mortes não existem no modelo atual e não foram adicionadas. Free Fire permanece fora desta etapa.

## 12. Classificação e Dashboard

A conferência HTTP recalculou os resultados esperados independentemente para as quatro competições. Partidas agendadas não pontuam; alteração de resultado reflete na classificação; nomes, escudos e cores retornam na resposta pública. Jogos futuros são ordenados por data.

No navegador, sem login, foram observados:

- Dashboard com classificação à esquerda, próximas partidas à direita e últimos resultados abaixo; nomes e escudos dos registros reais da simulação.
- Seleção de Futsal no Dashboard: equipe 1 com uma vitória, um empate e quatro pontos.
- Classificação pública de Valorant com vitórias/derrotas, pontos indicados por “—”, quatro próximos jogos e dois resultados finalizados.
- Logo e tema verde escuro preservados; recarga da página pública continua acessível sem login. Sem parâmetro de competição na URL, a recarga volta à seleção vazia, comportamento atual.

Essa conferência visual foi em desktop e não cobriu todas as páginas nem os formulários autenticados. Não foi feita captura de tela móvel nem teste de todos os tamanhos de janela.

## 13. Segurança dos perfis e escudos

HTTP Basic preservado. Para modalidades, torneios, equipes, atletas, inscrições, partidas e estatísticas:

- ORGANIZADOR: GET autorizado e operações necessárias da simulação realizadas.
- CONSULTA: GET permitido em modalidades, torneios, equipes e partidas; atletas, inscrições e estatísticas retornam 403. Modalidades permanece oculta/protegida na navegação administrativa do Angular, embora o GET da API seja público por configuração existente.
- CONSULTA: POST, PUT e DELETE retornam 403 em todas essas rotas.
- Visitante: GET público de modalidades, torneios e partidas; demais GET protegidos retornam 401. Escritas sem autenticação retornam 401.
- Classificação dos quatro torneios acessível sem autenticação.
- Foram observados 200, 201, 204, 400, 401, 403, 404 e 409 nos cenários correspondentes.

Escudo e cor ausentes são rejeitados; alteração pelo organizador foi persistida e conferida por nova consulta. As opções de cores, validações e fallback de legado têm cobertura automatizada. Cores claras e escuras foram observadas nas páginas públicas. As operações proibidas foram testadas diretamente por HTTP, sem depender de esconder botões.

Menus, guardas e ausência de ferramentas administrativas possuem testes Angular; não foi realizado login no navegador como CONSULTA ou ORGANIZADOR nesta revisão. Não foram expostas senhas nem enviados dados a serviços externos.

## 14. Resultados e quantidade de testes

| Verificação final | Aprovados | Reprovados |
|---|---:|---:|
| Backend — JUnit/Mockito, oito classes | 94 | 0 |
| Frontend — Vitest/Angular | 49 | 0 |
| HTTP — preparação das quatro competições | 226 | 0 |
| HTTP — reteste das cinco falhas | 5 | 0 |
| HTTP — fluxo, regras, persistência e segurança após correções | 261 | 0 |
| Comparação integral antes/depois pelas sete APIs | 7 | 0 |

Total automatizado unitário/componente: **143 aprovados, zero falhas**. Não acessa o MySQL real. Os testes HTTP acessaram somente esports, usando registros temporários.

Histórico da exploração HTTP: três verificações passaram e cinco falharam antes das correções. Somando todas as fases HTTP, incluindo tentativas iniciais e comparações, são **502 verificações aprovadas e cinco falhas históricas corrigidas**; não são 502 cenários distintos. Na rodada de validação final e no reteste não restaram falhas.

Uma execução intermediária de JUnit falhou porque um teste antigo considerava empate final de Basquete válido. A expectativa foi corrigida para a regra exigida, preservando os testes de empate permitido nos esportes apropriados e acrescentando regressão específica. Nenhum teste foi desativado.

Build Angular: **sucesso** (`npm run build`). Aviso: pacote inicial de 785,99 kB excede o orçamento de aviso de 500 kB, sem exceder o limite de erro. Não foi aumentado o orçamento para ocultar o aviso.

Spring Boot: **BUILD SUCCESS** (`mvnw.cmd -B test`, incluindo compilação com Java 21). Há aviso do Mockito sobre carregamento dinâmico de agente, sem falha no Java 21 usado.

Frontend: `npm test -- --watch=false`, **49 passed**. Navegador: conferências assistidas descritas na seção 12, sem contabilizá-las como testes automatizados de ponta a ponta.

## 15. Preservação dos dados

Antes dos cadastros, foi salvo um retrato autenticado das sete coleções da API. Depois de excluir somente IDs retornados nesta execução, as sete coleções foram comparadas integralmente, ordenadas por ID: todos os campos expostos pela API permaneceram iguais.

| Coleção | Antes | Depois |
| modalidades | 2 | 2 |
| torneios | 2 | 2 |
| atletas | 0 | 0 |
| equipes | 4 | 4 |
| inscricoes | 4 | 4 |
| estatisticas | 0 | 0 |
| partidas | 1 | 1 |

Foram criados **140 registros temporários**, incluindo os cenários negativos auxiliares e registros removidos durante os próprios testes. Todos foram removidos pelos IDs registrados, respeitando dependências: estatísticas, partidas, inscrições, atletas, equipes, torneios e modalidades. Antes de excluir, foi conferida a existência do registro e a marca TESTE_REVISAO nos cadastros nomeados. Nenhum registro anterior foi utilizado em PUT ou DELETE.

A preservação foi comprovada nos campos expostos pelas APIs, não por comparação física de arquivos MySQL ou dump completo. Contadores automáticos de IDs avançam naturalmente após criar/excluir testes e não foram restaurados.

### IDs temporários removidos

- **modalidades (2):** 11, 12.
- **torneios (12):** 15, 16, 17, 18, 19, 20, 21, 22, 23, 24, 25, 26.
- **equipes (20):** 24, 25, 26, 27, 28, 29, 30, 31, 32, 33, 34, 35, 36, 37, 38, 39, 40, 41, 42, 43.
- **atletas (48):** 6, 7, 8, 9, 10, 11, 12, 13, 14, 15, 16, 17, 18, 19, 20, 21, 22, 23, 24, 25, 26, 27, 28, 29, 30, 31, 32, 33, 34, 35, 36, 37, 38, 39, 40, 41, 42, 43, 44, 45, 46, 47, 48, 49, 50, 51, 52, 53.
- **inscricoes (16):** 23, 24, 25, 26, 27, 28, 29, 30, 31, 32, 33, 34, 35, 36, 37, 38.
- **partidas (25):** 14, 15, 16, 17, 18, 19, 20, 21, 22, 23, 24, 25, 26, 27, 28, 29, 30, 31, 32, 33, 34, 35, 36, 37, 38.
- **estatisticas (17):** 4, 5, 6, 7, 8, 9, 10, 11, 12, 13, 14, 15, 16, 17, 18, 19, 20.

## 16. Pendências e limites

- Fazer homologação visual autenticada das páginas administrativas, com ORGANIZADOR e CONSULTA, especialmente inscrição, edição e estatísticas. Os testes de componente/HTTP não substituem essa avaliação completa.
- Conferir responsividade em celular e diferentes navegadores. A revisão visual desta execução foi apenas das páginas públicas em desktop.
- Os seis códigos de escudo e as 16 cores foram preservados. O componente atual usa a mesma silhueta básica; não foram redesenhados seis símbolos diferentes nesta revisão.
- Otimizar o tamanho inicial Angular quando preparar produção; o aviso de bundle permanece.
- Não houve teste de carga/concorrência, falhas de rede durante salvamento, infraestrutura externa ou reinicialização do MySQL. Persistência foi verificada por novas requisições; não por reinício do servidor de banco.
- Não foram implementados classificação de Free Fire, mortes ou regras profissionais adicionais de desempate. A classificação permanece simples e documentada pelo próprio sistema.
- Formulários e colunas de todas as modalidades têm testes de componente/regras, mas simulações HTTP completas foram restritas às quatro modalidades solicitadas.

## 17. Preparação para GitHub e hospedagem

**Entrega acadêmica local:** fluxo principal aprovado nas simulações e regressões executadas, com as limitações visuais acima.

**GitHub:** antes de publicar, revisar arquivos não rastreados e histórico para segredos, conferir `.gitignore` para `.env`, builds, caches e dados locais e atualizar README/Postman conforme os campos atuais. Esta revisão não equivale a uma auditoria de todo o histórico Git. Nenhuma credencial foi adicionada nas alterações.

**Hospedagem:** ainda não pronta para publicação direta. A API do frontend aponta para localhost:8080, o CORS permite origens locais e o banco usa localhost. Será necessário configurar URLs/origens/credenciais do ambiente de destino, HTTPS obrigatório para HTTP Basic, variáveis de ambiente no servidor e estratégia de backup/evolução do schema. `ddl-auto=update` e logs SQL devem ser avaliados no perfil de produção. Essas alterações de infraestrutura não foram realizadas nesta revisão.

Não houve publicação no GitHub nem hospedagem. Não há falha conhecida pendente nos cenários funcionais efetivamente retestados; os itens acima continuam sem homologação completa.
