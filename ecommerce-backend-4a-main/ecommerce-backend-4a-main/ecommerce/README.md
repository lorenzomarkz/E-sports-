# E-sports — Backend

API Spring Boot adaptada do projeto **ecommerce-backend-4a**, fornecido pelo professor em Padrões de Desenvolvimento Web. A estrutura Maven, o pacote `br.edu.unifio.ecommerce`, o [README original](../README.md) e a [licença da base](../LICENSE) foram preservados.

Consulte o [README principal](../../../README.md) para configurar ambiente, banco, variáveis e permissões.

Nesta pasta, com Java 21:

```powershell
.\mvnw.cmd -B test
.\mvnw.cmd -B package
.\mvnw.cmd spring-boot:run
```

Os testes usam mocks e não conectam ao MySQL. A aplicação iniciada acessa `esports` com `ddl-auto=update`; preserve um backup antes de atualizar. Dados administrativos de atletas, inscrições, estatísticas e participações são exclusivos do ORGANIZADOR.

## Postman

A pasta `postman` contém uma coleção com 47 requisições e um ambiente com credenciais vazias. **Atualize a coleção antes de executar o Runner:** ela antecede os campos obrigatórios de escudo/cor e os endpoints de participação, estatísticas, desempenho e indicadores. Não tratar os exemplos como uma suíte atual validada.

Veja [evidências de testes](../../../docs/testes/README.md) e [pendências acadêmicas](../../../docs/README.md). Nunca publique o ambiente com senhas preenchidas.
