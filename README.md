<img width="2557" height="1437" alt="image" src="https://github.com/user-attachments/assets/8de76f64-314c-4bb8-8d3d-86139c0f0b7a" /># E-sports

O E-sports é um sistema acadêmico para organizar competições esportivas e acompanhar equipes, inscrições, partidas e resultados. O objetivo é aplicar os conhecimentos de desenvolvimento web em uma aplicação com frontend Angular, API REST e banco de dados MySQL.

O projeto foi desenvolvido para **Padrões de Desenvolvimento Web** e **Projeto Integrador**, do **4º semestre de Engenharia de Software**.

Repositório de destino: [lorenzomarkz/E-sports-](https://github.com/lorenzomarkz/E-sports-).

## Aplicação publicada

O sistema E-sports está disponível na internet para avaliação das disciplinas Padrões de Desenvolvimento Web e Projeto Integrador.

- **Frontend (Angular / Vercel):** https://e-sports-lyart.vercel.app
- **Backend (Spring Boot / Railway):** https://e-sports-production-b1636b75.up.railway.app
- **Banco de dados:** MySQL hospedado no Railway.

O sistema pode ser acessado pelo navegador, sem necessidade de executar o projeto localmente.

Perfis disponíveis:
- **ORGANIZADOR:** gerencia os cadastros e as competições.
- **CONSULTA:** visualiza as informações permitidas.

As credenciais de teste serão fornecidas aos professores de forma privada.

As instruções com `localhost` neste README servem para execução e testes locais.

## Funcionalidades implementadas

- CRUDs de modalidades, torneios, equipes, atletas, inscrições e partidas.
- Inscrições pendentes, aprovadas ou rejeitadas, com prazo, vagas, compatibilidade de modalidade e bloqueio de duplicatas.
- Geração simples de partidas todos contra todos, com verificação de duplicatas e horários.
- Resultados por modalidade e classificação pública, incluindo próximos jogos.
- Escudo de formato único com 16 cores; preservação dos códigos legados.
- Participação do mesmo atleta em equipes e competições de modalidades diferentes, sem duplicar o cadastro.
- Estatísticas individuais por participação/partida e desempenho acumulado separado por competição.
- Dashboard com classificação, próximos jogos e três KPIs: competições dentro do período cadastrado, equipes distintas com inscrição aprovada e partidas finalizadas. Indicadores atualizados a cada 30 segundos.
- ORGANIZADOR administra o sistema; CONSULTA acompanha competições, equipes, partidas e classificação.


## Tecnologias utilizadas

- Backend: Java 21, Spring Boot 4.1.1, Maven Wrapper, Spring Web MVC, Spring Data JPA, Spring Security, MySQL 8 e Lombok.
- Frontend: Angular 21, Angular Material, TypeScript, CSS, NgModules (`standalone: false`), roteamento e Reactive Forms. Sem SSR/SSG.
- Testes: JUnit/Mockito e Vitest/Angular.
- Autenticação: HTTP Basic, BCrypt e dois perfis em memória. Sem JWT ou banco de usuários.

As versões estão no `pom.xml`, `frontend/package.json` e `frontend/package-lock.json`. O frontend foi desenvolvido com Node.js 24; o Angular CLI é uma dependência local.

## Ferramentas utilizadas no desenvolvimento

| Ferramenta | Finalidade |
|---|---|
| Visual Studio Code | Desenvolvimento e organização do código. |
| Java e Spring Boot | Desenvolvimento do backend e da API REST. |
| Angular e Angular Material | Desenvolvimento do frontend, páginas e formulários. |
| MySQL e MySQL Workbench | Armazenamento e gerenciamento dos dados. |
| Postman | Testes das requisições e endpoints. |
| Git e GitHub | Versionamento e armazenamento do projeto. |
| Figma | Modelagem de protótipos. |
| Adobe Express | Seleção das cores e da identidade visual. |
| Codex | Auxílio na programação, nos testes, na correção de erros e na organização do projeto. |

As ferramentas apoiam o trabalho dos integrantes. O Codex é apresentado como ferramenta de auxílio, não como único responsável pelo desenvolvimento.

## Integrantes do grupo

| Nome completo | RA |
|---|---|
| Lorenzo Signorini Marques | 272619 |
| Pedro Henrique Andrade de Almeida | 272499 |

**Curso:** Engenharia de Software — 4º semestre.

**Disciplinas:** Padrões de Desenvolvimento Web e Projeto Integrador.

## Perfis de usuário e acesso para avaliação

| Perfil | Permissões implementadas |
|---|---|
| ORGANIZADOR | Gerencia modalidades, competições, equipes, atletas, participações, inscrições, partidas, resultados e estatísticas. Pode aprovar/rejeitar inscrições, gerar confrontos e consultar o desempenho dos atletas. |
| CONSULTA (usuário comum) | Visualiza Dashboard, competições, equipes, partidas, resultados e classificação. Não pode cadastrar, editar ou excluir registros, nem acessar as páginas administrativas de modalidades, atletas e inscrições. |

Visitantes sem login podem acessar as informações públicas de torneios, modalidades, partidas, classificação e indicadores. A API exige autenticação para consultar equipes; dados administrativos de atletas, participações, inscrições e estatísticas são exclusivos do ORGANIZADOR. Embora GET de modalidades seja público na API, sua página de administração é restrita no Angular.

A autenticação usa HTTP Basic. Os logins definidos no código são `ORGANIZADOR` e `CONSULTA`, com senhas configuradas por variáveis de ambiente e codificadas com BCrypt no backend. O frontend mantém as credenciais em memória, sem localStorage. As proteções do backend continuam valendo para requisições diretas.

### Credenciais para os professores ⚠️⚠️⚠️⚠️⚠️ 

O arquivo destinado às credenciais de demonstração é [ACESSOS_TESTE.txt](ACESSOS_TESTE.txt).

**Preenchimento pendente:** o arquivo contém placeholders, não credenciais funcionais. Não foi confirmado que as contas/senhas atuais sejam exclusivas de demonstração, sem reutilização pessoal ou acesso à infraestrutura. Por isso, nenhuma senha foi copiada de configurações ou variáveis de ambiente. Antes de preencher, os responsáveis devem confirmar essas condições e que as credenciais correspondem ao ambiente de avaliação.

Esse arquivo não deve conter senhas do MySQL, tokens ou chaves privadas. Nenhum usuário ou senha foi criado ou alterado nesta atualização documental.

## Estrutura de pastas

```text
E-sports-/
├── README.md
├── ACESSOS_TESTE.txt
├── .gitignore
├── RELATORIO_REVISAO_FINAL.md
├── docs/
│   ├── README.md
│   ├── PUBLICACAO.md
│   ├── diagramas/
│   │   ├── README.md
│   │   ├── diagrama-classes.md
│   │   └── explicacao-diagrama.md
│   └── testes/README.md
├── frontend/
│   ├── public/                 # Logo e favicon
│   ├── src/app/                # Páginas, componentes, serviços HTTP e guardas
│   ├── angular.json
│   ├── package.json
│   └── package-lock.json
└── ecommerce-backend-4a-main/
    └── ecommerce-backend-4a-main/
        ├── README.md           # Referência original do professor
        ├── LICENSE
        └── ecommerce/
            ├── pom.xml
            ├── mvnw / mvnw.cmd
            ├── .mvn/wrapper/
            ├── src/main/
            ├── src/test/
            └── postman/
```

Os diretórios existentes não foram renomeados. Frontend e backend pertencem ao mesmo repositório, com builds independentes.

### Arquitetura

O Angular acessa a API REST com `HttpClient`. Controllers recebem DTOs, services aplicam as regras de negócio e repositories JPA acessam o MySQL. As entidades são Modalidade, Torneio, Equipe, Atleta, Inscricao, Partida, ParticipacaoAtleta e EstatisticaAtleta.

ParticipacaoAtleta define a equipe e a competição do atleta. Estatísticas novas usam esse vínculo, enquanto o histórico legado é preservado. Guardas Angular limitam a navegação, mas as permissões das operações também são verificadas no backend.

## Como executar o projeto

### Banco de dados e variáveis de ambiente

1. Disponibilize JDK 21 e configure `JAVA_HOME`. Confira `java -version` e `javac -version`.
2. Disponibilize Node.js 24 e npm. Confira `node --version` e `npm --version`.
3. Inicie o serviço MySQL e conecte-se pelo MySQL Workbench em `localhost:3306`. Confirme que o schema **esports** existe. Se for uma instalação nova, crie somente esse schema vazio. Preserve bancos existentes e faça backup antes de atualizar a aplicação.
4. Configure as variáveis abaixo no ambiente que executará o backend. Não grave valores nos arquivos versionados.

| Variável | Finalidade |
|---|---|
| `MYSQL_USUARIO` | Usuário autorizado no schema esports |
| `MYSQL_SENHA` | Senha desse usuário MySQL |
| `ESPORTS_ORGANIZADOR_SENHA` | Senha do perfil ORGANIZADOR |
| `ESPORTS_CONSULTA_SENHA` | Senha do perfil CONSULTA |

Abra um novo terminal após configurar variáveis do sistema. Um arquivo `.env` sozinho **não é carregado automaticamente** pela configuração atual.

A conexão utiliza `jdbc:mysql://localhost:3306/esports?serverTimezone=America/Sao_Paulo`. O `ddl-auto=update` atualiza a estrutura ao iniciar; não substitui backups ou uma estratégia de migração. Não use `create` ou `create-drop` em bancos com dados a preservar.

Somente em uma instalação nova, se o schema ainda não existir, o comando abaixo pode ser executado no MySQL Workbench. Ele é uma instrução de preparação, não foi executado nesta atualização do README:

```sql
CREATE DATABASE IF NOT EXISTS esports CHARACTER SET utf8mb4;
```

O usuário MySQL configurado precisa das permissões necessárias para acessar e atualizar a estrutura desse schema. Não restaure backups sobre bancos existentes para apenas iniciar o projeto.

### Backend — terminal 1

A partir da raiz, no PowerShell:

```powershell
cd ecommerce-backend-4a-main/ecommerce-backend-4a-main/ecommerce
.\mvnw.cmd spring-boot:run
```

API em `http://localhost:8080`. No Linux/macOS, use `./mvnw`, concedendo permissão de execução ao wrapper local se necessário. Encerre uma instância anterior antes de ocupar a mesma porta.

### Frontend — terminal 2

A partir da raiz:

```powershell
cd frontend
npm ci
npm start
```

Interface em `http://localhost:4200`. O Angular CLI é instalado localmente por `npm ci`. A URL da API está em `frontend/src/app/servicos/api.ts`; o CORS atual permite as origens locais da porta 4200.

Use Entrar com `ORGANIZADOR` ou `CONSULTA` e a senha configurada no backend. Não há senha padrão publicada. As credenciais do frontend ficam em memória, sem armazenamento no localStorage.

## Endpoints e documentação da API

| Acesso | Regra |
|---|---|
| GET de modalidades, torneios, partidas e suas subrotas | Público, incluindo classificação e indicadores |
| GET de equipes | Autenticado |
| GET de atletas, inscrições, estatísticas, participações e subrotas | ORGANIZADOR |
| POST, PUT e DELETE | ORGANIZADOR |

Rotas principais: `/modalidades`, `/torneios`, `/equipes`, `/atletas`, `/inscricoes`, `/partidas`, `/estatisticas` e `/participacoes-atletas`.

Consultas adicionais incluem `GET /atletas/{id}/desempenho`, `GET /torneios/indicadores` e `GET /torneios/{id}/classificacao`. As seis entidades principais têm CRUD REST; participações e estatísticas oferecem as operações implementadas nos controllers, sem pressupor PUT onde não existe.

Respostas: 200/201/204 para sucesso, 400 para dados inválidos, 401 sem autenticação válida, 403 sem permissão, 404 para inexistentes e 409 para conflitos.

### Operações disponíveis

| Recurso | Operações |
|---|---|
| Modalidades, torneios, equipes, atletas, inscrições e partidas | GET e POST na rota base; GET, PUT e DELETE em `/{id}`. |
| Estatísticas | GET e POST em `/estatisticas`; GET e DELETE em `/estatisticas/{id}`; filtro opcional `?partidaId=`. |
| Participações de atletas | GET e POST em `/participacoes-atletas`; DELETE em `/{id}`; filtro opcional `?partidaId=`. |
| Confrontos de um torneio | GET `/partidas/torneio/{torneioId}`. |
| Geração automática | POST `/partidas/torneio/{torneioId}/gerar`. |
| Inscrições de um torneio | GET `/inscricoes/torneio/{torneioId}`. |

Os DTOs recebem IDs dos relacionamentos, como `modalidadeId`, `torneioId`, `equipeId` e `atletaId`. Datas usam `AAAA-MM-DD`; data/hora usa `AAAA-MM-DDTHH:mm:ss`. PUT nos CRUDs recebe o DTO completo.

A documentação disponível é este resumo, os controllers/DTOs e a coleção Postman. Não há Swagger/OpenAPI configurado.

## Testes realizados e coleção Postman

Na pasta do backend:

```powershell
.\mvnw.cmd -B test
.\mvnw.cmd -B package
```

Na pasta `frontend`:

```powershell
npm test -- --watch=false
npm run build
```

Testes unitários/componente usam mocks e não alteram o MySQL. Relatórios JUnit são gerados em `target/surefire-reports`, fora do versionamento. Testes manuais/Postman podem gravar dados; use registros fictícios identificados e remova apenas os IDs criados na execução.

Veja [evidências de testes](docs/testes/README.md) e o [relatório histórico de revisão](RELATORIO_REVISAO_FINAL.md).

### Resultados já registrados

Conforme [docs/testes](docs/testes/README.md), a execução registrada em 21/11/2026 teve **125 testes backend e 58 testes frontend aprovados**, sem falhas, além dos builds aprovados. O build Angular apresentou aviso de tamanho do bundle inicial. Esses resultados são de uma execução anterior; não foram reexecutados nesta atualização documental.

O relatório de 20/11/2026 descreve testes HTTP e simulações da versão daquela data. Testes com mocks, verificações HTTP e testes visuais são evidências diferentes; não se deve inferir cobertura completa de navegador a partir dos testes unitários.

### Coleção Postman e relatórios

- [Coleção Postman](ecommerce-backend-4a-main/ecommerce-backend-4a-main/ecommerce/postman/E-sports.postman_collection.json).
- [Modelo de ambiente sem credenciais](ecommerce-backend-4a-main/ecommerce-backend-4a-main/ecommerce/postman/E-sports.postman_environment.json).
- [Documentação acadêmica e pendências](docs/README.md).
- [Preparação e segurança da publicação](docs/PUBLICACAO.md).

**A coleção é de uma etapa anterior:** contém 47 requisições, mas precisa de atualização dos campos de escudo/cor e dos fluxos posteriores antes de usar o Runner. Não foi validada novamente contra a versão atual. Preencha senhas somente localmente; não publique o ambiente preenchido.

## Diagrama de classes e documentação técnica

O modelo foi conferido com as entidades JPA do backend e está disponível em dois documentos:

- [Diagrama de classes UML em Mermaid](docs/diagramas/diagrama-classes.md): oito entidades, seus atributos e relacionamentos, além dos dois enums de status.
- [Explicação do diagrama](docs/diagramas/explicacao-diagrama.md): função das entidades, cardinalidades, restrições e vínculos legados preservados.

A [pasta docs](docs/README.md) reúne a organização dos materiais e as pendências. A explicação do modelo de entidades está disponível acima. A documentação técnica complementar de DTOs, endpoints e regras e o documento de requisitos/ERS final do E-sports ainda precisam ser completados pelos integrantes após revisão.

Também permanecem pendentes as evidências visuais finais do fluxo autenticado e responsivo e a documentação do ambiente hospedado.

## Materiais de estudo e referências técnicas

Os links abaixo são **referências técnicas complementares e materiais de estudo**. Sua inclusão não comprova que tenham sido consultados durante a implementação nem que tenham resolvido erros específicos do E-sports. Alguns materiais usam versões diferentes das adotadas no projeto.

### Videoaulas — Java e Spring Boot

| Material | Assunto |
|---|---|
| [Criando um CRUD com Java Spring Boot e MySQL — Build & Run](https://www.youtube.com/watch?v=Tnl4YnB6E54) | Desenvolvimento de APIs REST e operações CRUD. |
| [Spring Boot CRUD Operations using Spring Data JPA & MySQL — Prasant Codes](https://www.youtube.com/watch?v=mV36FawKbpI) | Camadas Controller, Service e Repository e persistência de dados. |
| [Spring Security Basic Authentication Explained — CodeSnippet by Chetan](https://www.youtube.com/watch?v=qGZab30KDuQ) | Autenticação HTTP Basic e proteção dos endpoints. |

### Videoaulas — Angular

| Material | Assunto |
|---|---|
| [CRUD Angular + Spring — Atualizar Curso: Populando o Formulário — Loiane Groner](https://www.youtube.com/watch?v=uZwqA1DZXMU) | Preenchimento de formulários para editar registros. |
| [CRUD Angular + Spring — Validações de Formulário no Front-end — Loiane Groner](https://www.youtube.com/watch?v=As70gDHoagE) | Validação de campos em formulários Angular. |
| [Connecting Angular with Create Employee REST API — Java Guides](https://www.youtube.com/watch?v=bh-HDtx-ppw) | Integração de cadastro entre Angular e Spring Boot. |
| [Connecting Angular with Update Employee REST API — Java Guides](https://www.youtube.com/watch?v=-Kz0hsW2k68) | Atualização de registros pela API REST. |
| [Connecting Angular with Delete Employee REST API — Java Guides](https://www.youtube.com/watch?v=gS_2aVA0H3A) | Exclusão de registros utilizando requisições HTTP. |

Repositório didático: [CRUD Angular + Spring — Loiane Groner](https://github.com/loiane/crud-angular-spring). Material complementar com exemplos de aplicação Angular integrada a uma API Spring; não é o projeto-base do professor usado no E-sports.

### Fóruns e discussões técnicas

| Discussão | Assunto |
|---|---|
| [Comunicação entre Angular e Spring Boot — CORS](https://stackoverflow.com/questions/53528643/) | Comunicação entre frontend e backend em portas diferentes no ambiente local. |
| [Relacionamentos muitos-para-muitos com atributos adicionais em JPA](https://stackoverflow.com/questions/70644399/) | Modelagem de entidades intermediárias e relacionamentos com informações adicionais. |
| [Angular Material — mat-select e Reactive Forms](https://stackoverflow.com/questions/56187371/) | Seleção de valores existentes ao editar formulários reativos. |
| [Problemas de CORS com Spring Boot](https://stackoverflow.com/questions/64892592/) | Erros de comunicação entre Angular e Spring Boot mesmo após configurar CORS. |
| [Relacionamentos JPA com colunas extras](https://stackoverflow.com/questions/46789899/) | Associação entre entidades com informações adicionais e problemas no mapeamento. |
| [Angular Material — seleção de valores em formulários](https://stackoverflow.com/questions/62860153/) | Preenchimento de mat-select com valores recebidos de serviços. |

Os seis links foram acessados e seus assuntos conferidos nesta revisão documental. São discussões da comunidade, não documentação normativa nem justificativa para copiar configurações sem avaliar o contexto do projeto.

### Documentações oficiais

| Documentação | Conteúdo |
|---|---|
| [Angular](https://angular.dev/) | Conceitos e recursos do framework. |
| [Angular Reactive Forms](https://angular.dev/guide/forms/reactive-forms) | Formulários reativos e seus controles. |
| [Angular HttpClient](https://angular.dev/guide/http/making-requests) | Requisições HTTP e comunicação com APIs. |
| [Angular Material](https://material.angular.dev/) | Componentes de interface. |
| [Spring Boot](https://docs.spring.io/spring-boot/) | Configuração e desenvolvimento de aplicações Spring Boot. |
| [Spring Security](https://docs.spring.io/spring-security/reference/) | Autenticação e autorização. |
| [Spring Data JPA](https://spring.io/guides/gs/accessing-data-jpa/) | Guia de acesso e persistência de dados com JPA. |
| [MySQL](https://dev.mysql.com/doc/) | Documentação do banco de dados. |
| [Postman](https://learning.postman.com/) | Requisições, coleções, ambientes e testes de APIs. |

Conferência dos links em 21/11/2026: os títulos/autores dos oito vídeos foram confirmados pelos metadados públicos do YouTube, sem avaliação integral das aulas. Os seis fóruns correspondem aos temas informados; os sites oficiais e o repositório didático responderam à consulta. Links externos podem mudar; use as versões das documentações compatíveis com o projeto.


## Disciplinas e materiais disponibilizados pelos professores

O projeto segue os requisitos das disciplinas **Padrões de Desenvolvimento Web** e **Projeto Integrador**, do 4º semestre de Engenharia de Software.

### Padrões de Desenvolvimento Web

[Material do professor — Backend do Projeto Integrador](https://hackmd.io/@srdelfino/SyMXGnkAzg): orientações para o backend, API REST, arquitetura em camadas, persistência e testes com Postman.

[Repositório-base indicado no material](https://github.com/sergiodelfino/ecommerce-backend-4a/tree/main/ecommerce).

O backend foi adaptado do **ecommerce-backend-4a**, disponibilizado pelo professor para Padrões de Desenvolvimento Web. Foram preservados a estrutura Maven, os wrappers, o pacote `br.edu.unifio.ecommerce`, o [README original](ecommerce-backend-4a-main/ecommerce-backend-4a-main/README.md) e a [licença fornecida com a base](ecommerce-backend-4a-main/ecommerce-backend-4a-main/LICENSE).

O nome `ecommerce` permanece em diretórios, pacote e artefato Maven para preservar a origem. O endereço da base consta no material de Padrões de Desenvolvimento Web indicado abaixo.

### Projeto Integrador

Conforme as orientações de entrega informadas pelo grupo, o projeto final envolve aplicação Angular, Dashboard com indicadores, operações CRUD, diagrama de classes e hospedagem. O diagrama de classes está disponível nos links acima; a hospedagem permanece pendente.

O enunciado final de Projeto Integrador e o documento de requisitos do E-sports ainda precisam ser adicionados ao repositório. Nenhum endereço foi inventado para esses materiais.

### Publicação e hospedagem

Esta etapa preparou arquivos, sem commit, push ou hospedagem. Antes do commit, conferir `git status` e o diff a ser enviado. O `.gitignore` exclui dependências, builds, logs, backups e configurações locais; não elimina segredos já presentes em um histórico externo.

Para hospedar, configurar URLs, origens CORS e banco do destino, além de HTTPS para HTTP Basic. A configuração atual é de desenvolvimento local. Essas mudanças não fazem parte desta preparação documental.

