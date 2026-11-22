# Preparação para publicação — 21/11/2026

## Organização e Git

Frontend e backend permanecem nos diretórios existentes, sob uma única raiz Git. Nenhum arquivo de código, entidade, configuração da aplicação ou regra de negócio foi alterado nesta preparação.

A pasta recebida não continha `.git`, inclusive nas subpastas. A consulta `git ls-remote` ao destino informado terminou com sucesso e retornou zero referências: não havia branches/commits remotos anunciados na verificação. Foi inicializado um repositório local na raiz, branch `main`, com `origin` apontando para `https://github.com/lorenzomarkz/E-sports-.git`.

Não foram feitos `git add`, commit, push, reset, rebase ou alteração do histórico remoto. Como a cópia local não trazia histórico do professor, não é possível reconstruí-lo a partir destes arquivos. O README e a licença originais da base continuam preservados. Se outra cópia com histórico aparecer, reconciliar antes do primeiro commit, sem sobrescrever esse histórico.

## Arquivos candidatos à publicação

- README principal, `.gitignore`, relatório histórico e documentação em `docs`.
- Frontend: fontes, testes, logo/favicon, configurações Angular/TypeScript, `package.json` e `package-lock.json`.
- Backend: fontes, testes, `application.properties` com referências a variáveis de ambiente, `pom.xml`, wrappers Maven e `.mvn/wrapper/maven-wrapper.properties`.
- Coleção Postman e modelo de ambiente com senhas e IDs vazios.
- README e licença do projeto-base fornecidos pelo professor, além dos READMEs de execução atualizados.

## Exclusões verificadas

O `.gitignore` principal foi conferido com `git check-ignore` para dependências, builds e caches (`node_modules`, `target`, `dist`, `.angular`), logs, temporários, `.env`/variantes, backups SQL/dumps, arquivos compactados de backup, chaves/certificados privados e configurações locais de IDE.

As pastas `.vscode` ficam fora da publicação, incluindo configurações MCP locais. A pasta legada `ecommerce-backend-4a-main/ecommerce-backend-4a-main/.devcontainer` foi excluída porque seu script de inicialização contém uma credencial fixa. Ela foi preservada no computador, sem execução nem alteração; não faz parte das instruções atuais de execução.

O `.gitignore` não apaga os arquivos locais. Modelos `.env.example`/`.env.sample` são permitidos, mas somente devem conter nomes e valores fictícios/vazios. Novos arquivos sensíveis exigem revisão antes de qualquer commit.

## Verificação de segurança

- Varredura textual dos arquivos candidatos por padrões de tokens, chaves privadas, credenciais em URLs e atribuições literais de senha/segredo: nenhum achado de credencial real nos candidatos examinados.
- Comparação com as três senhas disponíveis nas variáveis de ambiente do projeto: nenhuma ocorrência nos arquivos candidatos; valores não foram exibidos.
- `application.properties`: usuário e senha MySQL e senhas de autenticação são fornecidos pelo ambiente, não publicados como valores fixos.
- Postman: JSON válido; campos de usuário/senha e IDs estão vazios no ambiente. A URL local é configuração pública, não credencial.
- Testes contêm credenciais explicitamente fictícias para mocks; não são senhas do ambiente.
- A credencial fixa identificada na configuração legada não integra os arquivos candidatos graças à exclusão da pasta.

Esta verificação é uma busca textual e comparação de valores conhecidos, não uma garantia de ausência de todo segredo possível ou auditoria de históricos externos. Relatórios brutos de testes, backups e arquivos locais continuam fora do Git.

## Materiais e pendências

A coleção Postman está disponível, mas seus 47 exemplos antecedem os campos escudo/cor e os endpoints mais recentes. Precisa de atualização antes de ser apresentada como suíte executável da versão final. Nenhuma requisição Postman foi executada nesta preparação.

O relatório principal de revisão é histórico (20/11/2026). As evidências mais recentes estão resumidas em [testes/README.md](testes/README.md), com distinção entre resultados já executados e documentação preparada agora.

Ainda faltam diagrama de classes, documentação técnica detalhada/ERS final, identificação acadêmica completa, evidências visuais finais e informações de hospedagem. Ver [lista de materiais](README.md).

## Antes de autorizar commit e push

1. Revisar README, créditos e materiais acadêmicos.
2. Atualizar a coleção Postman e validar seus exemplos em ambiente apropriado.
3. Conferir novamente `git status`, candidatos e segredos; o ambiente Postman não deve estar preenchido.
4. Verificar se o remoto recebeu novos commits e reconciliar antes de publicar.
5. Somente após autorização do usuário, preparar o commit e depois o push. Nenhuma publicação ocorreu nesta etapa.
