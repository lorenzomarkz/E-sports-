# Testes da API com Postman

Coleção revisada em 23/11/2026 para as disciplinas Padrões de Desenvolvimento Web e Projeto Integrador. Os métodos, caminhos e campos foram comparados aos controllers, DTOs e services atuais. A coleção contém **80 requisições**, incluindo as 47 anteriores. Quantidade de requisições não significa quantidade de testes aprovados.

## Arquivos para importar

- [Coleção E-sports](../../ecommerce-backend-4a-main/ecommerce-backend-4a-main/ecommerce/postman/E-sports.postman_collection.json).
- [Ambiente local](../../ecommerce-backend-4a-main/ecommerce-backend-4a-main/ecommerce/postman/E-sports.postman_environment.json).
- [Evidências e resultados desta revisão](RESULTADOS_2026-11-23.md).

No Postman, use **Import**, selecione os dois JSONs e escolha o ambiente **E-sports - somente local isolado**. Use uma versão que ofereça `pm.execution.skipRequest()`. A coleção desabilita redirecionamentos; mantenha também a opção **Automatically follow redirects** desativada nas configurações do Postman.

## Ambiente seguro

Esta coleção completa **não deve ser executada no backend do Railway**, nem por meio de proxy ou túnel local para ele. O endereço local sozinho não garante que o banco também seja local.

Antes de iniciar o Runner, confira a configuração efetiva da instância Spring Boot: ela deve apontar para um banco exclusivo de testes, como `esports_testes`, sem dados reais. A configuração pode ser sobreposta somente no processo de testes pelas variáveis padrão do Spring:

| Variável do backend | Configuração para a instância isolada |
|---|---|
| `SPRING_DATASOURCE_URL` | `jdbc:mysql://localhost:3306/esports_testes` — banco previamente disponibilizado para testes |
| `SPRING_DATASOURCE_USERNAME` / `SPRING_DATASOURCE_PASSWORD` | Usuário e senha exclusivos desse banco; configurar somente localmente |
| `SPRING_JPA_HIBERNATE_DDL_AUTO` | `update` |
| `ESPORTS_ORGANIZADOR_SENHA` / `ESPORTS_CONSULTA_SENHA` | Senhas locais dos perfis de teste |
| `SERVER_PORT` | Porta livre, por exemplo `8081` |

Não há criação de banco nem execução de SQL nesta coleção. Não reutilize as credenciais da infraestrutura hospedada. Configure o ambiente antes de iniciar a aplicação e confira a conexão efetiva; não basta trocar apenas a URL no Postman.

No ambiente do Postman, preencha valores **locais, sem compartilhar ou exportar credenciais**:

| Variável | Finalidade |
|---|---|
| `url` | URL da API isolada, por exemplo `http://localhost:8081`, sem barra final |
| `usuario` / `senha` | Credenciais HTTP Basic do ORGANIZADOR da instância local |
| `usuarioConsulta` / `senhaConsulta` | Credenciais HTTP Basic do CONSULTA da instância local |
| `bancoIsoladoConfirmado` | Preencher `CONFIRMO_BANCO_ISOLADO` somente depois da conferência da conexão |

Usuários e senhas estão vazios no arquivo distribuído. Os campos de senha são do tipo `secret`; isso apenas oculta a apresentação, não torna seguro publicar um ambiente preenchido. URLs externas são bloqueadas pelo script, inclusive Railway. A confirmação do banco é manual, pois a API não expõe a configuração JDBC ao Postman.

## Ordem de execução

Execute a coleção inteira pelo **Collection Runner**, com uma iteração e sem arquivo externo de dados. Não reordene requisições nem preencha IDs manualmente.

1. **Cadastros fictícios:** modalidade temporária, localização de Futsal, competição, duas equipes com cor de escudo, atleta e inscrições PENDENTES. Aprova as duas equipes antes de cadastrar a partida. Consulta os próximos jogos antes da finalização.
2. **Consultar e atualizar CRUDs:** listagens, buscas por ID e alterações das seis entidades originais. Finaliza o confronto fictício em 3 a 1.
3. **Participações, estatísticas e resultados:** bloqueia estatística sem participação, cadastra a participação e 2 gols/1 assistência, consulta persistência e desempenho, valida duplicidades, campos incompatíveis e classificação pública. A chamada de geração verifica que o confronto existente não é duplicado; não representa uma simulação completa de campeonato.
4. **Acesso público e CONSULTA:** consultas sem autenticação e leitura de equipes pelo perfil CONSULTA.
5. **Erros esperados:** 400, 401, 403, 404 e 409. Inclui bloqueio de POST, PUT e DELETE para CONSULTA e de leitura de atletas, inscrições, participações e estatísticas por esse perfil.
6. **Excluir apenas fictícios:** estatística, participação, partida, inscrições, atleta, equipes, torneio e modalidades pertencentes à execução. Uma consulta final verifica se o diário ficou vazio.

As datas são calculadas no início conforme America/Sao_Paulo. O torneio começa em dez dias; o prazo de inscrição é o dia da execução. Evite atravessar a meia-noite durante o Runner.

O prefixo `TESTE_POSTMAN_` identifica os cadastros temporários. Futsal precisa ter o nome reconhecido pelas regras do sistema: se já existir, é usado apenas como referência, sem alteração ou exclusão; se não existir, é criado com o prefixo na descrição e registrado para limpeza. A modalidade temporária separada permite preservar os testes de CRUD de modalidades sem renomear Futsal.

## Endpoints recentes conferidos

| Operação | Método e caminho |
|---|---|
| Participações e participantes de um confronto | `GET /participacoes-atletas`, filtro opcional `?partidaId=...` |
| Cadastrar/remover participação | `POST /participacoes-atletas`, `DELETE /participacoes-atletas/{id}` |
| Estatísticas e filtro por partida | `GET /estatisticas`, filtro opcional `?partidaId=...` |
| Consultar/cadastrar/remover estatística | `GET /estatisticas/{id}`, `POST /estatisticas`, `DELETE /estatisticas/{id}` |
| Desempenho individual | `GET /atletas/{id}/desempenho` |
| Classificação, resultados e próximos jogos | `GET /torneios/{id}/classificacao` |
| Indicadores do Dashboard | `GET /torneios/indicadores` |
| Geração de partidas | `POST /partidas/torneio/{torneioId}/gerar`, corpo com `inicio` |

Não foram inventados PUT de estatísticas/participações, GET por ID de participação nem uma rota separada `/resultados`: esses endpoints não existem nos controllers atuais. Estatísticas recebem `atletaId` e `partidaId`; o serviço determina a participação válida.

## Proteção dos registros e recuperação

O script mantém a variável de coleção `diario`, com URL, prefixo e IDs retornados pelas criações. PUT e DELETE exigem um ID registrado por esta execução. IDs usados nos relacionamentos também são conferidos. A exceção é a modalidade Futsal preexistente, que só pode ser referenciada.

A limpeza ignora IDs que não foram criados. Um 204 remove o ID do diário; uma falha mantém o ID. Reiniciar a coleção com registros pendentes é bloqueado. O teste final exige um diário vazio. Os scripts não fazem exclusões por listagem geral, nome aproximado ou intervalo de IDs.

Se houver falha, conserve o mesmo ambiente e o diário. Pode-se executar a última pasta para remover os registros conhecidos, respeitando os relacionamentos. Não limpe as variáveis para tentar esconder a falha. Criações inesperadas em cenários negativos e partidas inesperadamente geradas também ficam registradas, mas exigem conferência manual dos IDs e dos vínculos antes da limpeza. Se a conexão cair antes de receber o ID de uma criação, a remoção automática não pode ser garantida: interrompa a limpeza, confira o prefixo no banco isolado e documente a ocorrência. Nunca execute limpeza geral.

## Evidências da execução

Ao executar o Runner, registre requisições executadas/ignoradas, asserções aprovadas/reprovadas, data, versão testada e situação final do diário. Requisições condicionais ignoradas não contam como testes aprovados. Não publique exportações com cabeçalhos Authorization, senhas ou dados pessoais.

Nesta revisão, a coleção foi validada **offline** e os testes existentes foram executados. A execução HTTP completa desta versão permanece pendente; os resultados reais estão no [relatório](RESULTADOS_2026-11-23.md).

Os endereços hospedados informados pelo grupo são [frontend Vercel](https://e-sports-lyart.vercel.app) e [backend Railway](https://e-sports-production-b1636b75.up.railway.app). Estão registrados como contexto da entrega, não como destinos do Runner nem como evidência de testes executados nesta revisão. Os testes manuais anteriores foram informados pelo grupo, sem nova execução pelo navegador nesta etapa.
