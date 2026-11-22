# E-sports — Frontend

Angular 21 e Angular Material, com NgModules, roteamento, Reactive Forms e CSS. Sem SSR/SSG. Tema verde escuro e logo em `public/e-sports-logo.png`.

Consulte o [README principal](../README.md) para configurar o backend e os perfis.

Nesta pasta:

```powershell
npm ci
npm start
```

Abra `http://localhost:4200`, com a API disponível em `http://localhost:8080`. O CLI é instalado localmente por `npm ci`.

```powershell
npm test -- --watch=false
npm run build
```

Publique `package-lock.json`; `node_modules`, `.angular` e `dist` são locais. Não insira senhas no código Angular. As credenciais de login são mantidas em memória.

A URL da API está em `src/app/servicos/api.ts`. A configuração atual é local; ajustes de hospedagem são uma etapa futura. O aviso de tamanho do bundle está registrado em [docs/testes](../docs/testes/README.md).
