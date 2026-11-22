# Explicação do diagrama de classes — E-sports

O [diagrama UML](diagrama-classes.md) apresenta as entidades que o backend usa para armazenar competições, participantes e resultados. Os nomes e tipos foram conferidos no código Java; o documento não acrescenta classes ou campos ao sistema.

## Função de cada entidade

### Modalidade

Representa a categoria esportiva cadastrada. Possui `id`, `nome` e `descricao`. Torneios e equipes fazem referência a ela. A modalidade não contém listas de equipes ou torneios na classe Java.

### Torneio

Representa uma competição. Guarda nome, descrição, jogo, datas de início e fim, prazo de inscrição, local e limite de equipes, além do identificador. O atributo `modalidade` identifica o esporte; `jogo` é uma informação textual usada nas competições eletrônicas, não uma entidade separada.

Pode ser referenciado por várias inscrições, partidas e participações de atletas. Essas referências ficam nas respectivas entidades, sem coleções no Torneio.

### Equipe

Representa um grupo esportivo. Armazena nome, cidade, nome do responsável, modalidade, código de escudo e cor do escudo. O código `escudo` permanece para compatibilidade com os registros existentes, mesmo que o formulário atual permita escolher somente a cor.

Uma equipe pode ser inscrita em diferentes torneios, aparecer como equipe A ou B nas partidas e ser usada em participações de atletas. O responsável é um campo textual; não existe uma entidade Responsavel.

### Atleta

Representa o cadastro individual, com nome e data de nascimento. O atributo `equipe` foi mantido como vínculo legado e pode ficar vazio. Ele não define sozinho em quais competições o atleta participa.

O mesmo cadastro pode ser usado em várias ParticipacaoAtleta, inclusive em modalidades diferentes. Não é necessário cadastrar novamente a pessoa a cada competição.

### Inscricao

Relaciona uma equipe a um torneio. Guarda a data da inscrição e o status: `PENDENTE`, `APROVADA` ou `REJEITADA`.

O vínculo representa a solicitação da equipe, não a participação individual de seus atletas. O service verifica duplicidade, compatibilidade da modalidade, prazo e vagas. A entidade Inscricao não declara uma restrição `@UniqueConstraint` para equipe e torneio; não foi acrescentada essa restrição ao diagrama como se já existisse no mapeamento.

### Partida

Representa um confronto com data/hora, local, status e dois placares. Possui uma referência ao torneio e duas referências diferentes à mesma classe Equipe: `equipeA` e `equipeB`.

Os campos `placarEquipeA` e `placarEquipeB` são reutilizados de acordo com a modalidade, como gols, pontos, sets ou mapas. Não existem entidades separadas para cada tipo de placar. O service impede confronto contra a mesma equipe e verifica as inscrições aprovadas. Os status possíveis são `AGENDADA`, `EM_ANDAMENTO` e `FINALIZADA`.

### ParticipacaoAtleta

É a associação explícita entre um atleta, uma equipe e um torneio. As três referências são obrigatórias. Por exemplo, o mesmo atleta pode representar uma equipe em uma competição de Futsal e outra equipe em uma competição de Basquete.

A restrição única `(atleta_id, torneio_id)` permite várias competições por atleta, mas impede duas participações na mesma competição. Assim, ele não pode ser associado a duas equipes diferentes dentro do mesmo torneio.

O service exige compatibilidade da modalidade e inscrição aprovada da equipe. Não existe uma referência JPA direta de ParticipacaoAtleta para Inscricao: essa condição é verificada por consulta no serviço.

### EstatisticaAtleta

Armazena os números individuais de um atleta em uma partida: `gols`, `pontos`, `abates` e `assistencias`. São campos `Integer`, que podem ficar nulos quando não informados ou não aplicáveis. Não há atributo `mortes` implementado.

As referências `atleta` e `partida` têm colunas obrigatórias. A restrição única `(atleta_id, partida_id)` impede dois registros individuais para o mesmo atleta no mesmo confronto.

O atributo `participacao` liga os novos registros à equipe e à competição do atleta. Essa coluna aceita nulo para preservar estatísticas antigas. O service exige participação válida nos novos cadastros; isso não transforma automaticamente os registros antigos em participações. O histórico legado continua identificado como sem associação.

Os totais de desempenho são calculados a partir dos registros existentes. Não há uma entidade ou campo persistente chamado Desempenho, Classificacao ou Indicador no modelo representado.

## Relacionamentos e cardinalidades

Cada linha abaixo corresponde a um atributo `@ManyToOne` real. Para uma entidade referenciada, podem existir zero ou muitos registros apontando para ela.

| Atributo na origem | Destino | Referência na persistência |
|---|---|---|
| `Torneio.modalidade` | Modalidade | `0..1` |
| `Equipe.modalidade` | Modalidade | `0..1` |
| `Atleta.equipe` | Equipe | `0..1`, vínculo legado |
| `Inscricao.torneio` | Torneio | `0..1` |
| `Inscricao.equipe` | Equipe | `0..1` |
| `Partida.torneio` | Torneio | `0..1` |
| `Partida.equipeA` | Equipe | `0..1` |
| `Partida.equipeB` | Equipe | `0..1` |
| `ParticipacaoAtleta.atleta` | Atleta | `1` |
| `ParticipacaoAtleta.equipe` | Equipe | `1` |
| `ParticipacaoAtleta.torneio` | Torneio | `1` |
| `EstatisticaAtleta.atleta` | Atleta | `1` |
| `EstatisticaAtleta.partida` | Partida | `1` |
| `EstatisticaAtleta.participacao` | ParticipacaoAtleta | `0..1`, para preservar o legado |

### Mapeamento JPA e regras dos serviços

Nas referências marcadas como `0..1`, as entidades não declaram `optional = false` nem `nullable = false` para a associação. Por isso o diagrama não as apresenta como obrigatórias na persistência. Isso **não significa** que a API permita cadastrar torneios sem modalidade, inscrições sem equipe ou partidas sem adversários: os services exigem esses dados nos fluxos correspondentes.

Em ParticipacaoAtleta, as três relações declaram tanto `optional = false` quanto `nullable = false`. Em EstatisticaAtleta, atleta e partida declaram `nullable = false`, embora `optional = false` não esteja escrito em `@ManyToOne`. O diagrama usa `1` nesses dois casos pela obrigatoriedade explícita das colunas.

A cardinalidade `0..*` indica a possibilidade de vários registros referenciadores. Ela não inventa campos `List` ou anotações `@OneToMany` nas classes referenciadas. Todas as associações são unidirecionais no código examinado.

## Como as entidades trabalham juntas

1. A modalidade identifica o esporte do torneio e da equipe.
2. Inscricao registra a entrada da equipe no torneio e sua aprovação.
3. ParticipacaoAtleta define qual atleta representa a equipe nessa competição.
4. Partida relaciona o torneio às duas equipes do confronto.
5. EstatisticaAtleta guarda os valores individuais de quem participa do confronto, conforme as validações dos serviços.

Inscricao e ParticipacaoAtleta são associações diferentes: a primeira trata da equipe na competição; a segunda trata do atleta representando uma equipe naquela competição. Não há uma associação direta `@ManyToMany` entre as classes. As entidades intermediárias tornam esses vínculos explícitos.

## Escopo e conferência

- Oito entidades JPA, 14 atributos de relacionamento e dois enums foram representados.
- Os atributos do diagrama correspondem às declarações dos arquivos Java, incluindo os campos legados.
- Os status são enums armazenados como texto; não são entidades ou tabelas próprias.
- Getters/setters Lombok e as camadas de API não foram incluídos para manter o desenho focado no domínio persistente.
- As regras descritas foram conferidas nos services existentes; nenhuma mudança foi feita nas entidades, no frontend ou no banco.

Fontes locais: [entidades](../../ecommerce-backend-4a-main/ecommerce-backend-4a-main/ecommerce/src/main/java/br/edu/unifio/ecommerce/entidades) e [services](../../ecommerce-backend-4a-main/ecommerce-backend-4a-main/ecommerce/src/main/java/br/edu/unifio/ecommerce/servicos).

[Ver o diagrama](diagrama-classes.md) · [Voltar ao README principal](../../README.md)
