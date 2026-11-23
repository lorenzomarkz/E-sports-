// Validação offline dos scripts Postman. Não envia HTTP nem acessa banco.
const fs = require('node:fs');
const path = require('node:path');
const vm = require('node:vm');
const assert = require('node:assert/strict');
const { test } = require('node:test');
const raiz = path.resolve(__dirname, '../..');
const pasta = path.join(raiz, 'ecommerce-backend-4a-main/ecommerce-backend-4a-main/ecommerce/postman');
const colecao = JSON.parse(fs.readFileSync(path.join(pasta, 'E-sports.postman_collection.json'), 'utf8'));
const ambiente = JSON.parse(fs.readFileSync(path.join(pasta, 'E-sports.postman_environment.json'), 'utf8'));
const itens = colecao.item.flatMap(p => p.item);
const codigo = tipo => colecao.event.find(e => e.listen === tipo).script.exec.join('\n');
function contexto(nome, ajustes = {}) {
  const item = itens.find(i => i.name === nome);
  const valores = { url: 'http://localhost:8080', bancoIsoladoConfirmado: 'CONFIRMO_BANCO_ISOLADO', prefixo: 'TESTE_POSTMAN_OFFLINE', equipeId: '12', ...ajustes };
  const diario = { url: 'http://localhost:8080', prefixo: 'TESTE_POSTMAN_OFFLINE', registros: [{ recurso: 'equipes', chave: 'equipeId', id: '12' }], futsalLeitura: null };
  const colecaoVars = { diario: JSON.stringify(diario) };
  const acesso = objeto => ({ get: k => objeto[k], set: (k, v) => { objeto[k] = v; }, unset: k => { delete objeto[k]; } });
  const resultado = { ignorada: false, valores, colecaoVars, diario };
  resultado.pm = {
    environment: acesso(valores), collectionVariables: acesso(colecaoVars), variables: acesso(valores),
    info: { requestName: nome },
    request: { method: item.request.method, url: { toString: () => item.request.url }, body: item.request.body },
    execution: { skipRequest: () => { resultado.ignorada = true; } }
  };
  resultado.salvarDiario = () => { colecaoVars.diario = JSON.stringify(diario); };
  resultado.executar = tipo => vm.runInNewContext(codigo(tipo), { pm: resultado.pm, Intl, Date });
  return resultado;
}
test('JSON importável, 80 requisições e scripts JavaScript válidos', () => {
  assert.equal(colecao.info.schema, 'https://schema.getpostman.com/json/collection/v2.1.0/collection.json');
  assert.equal(itens.length, 80);
  for (const obj of [colecao, ...itens]) for (const e of obj.event || []) new vm.Script(e.script.exec.join('\n'));
});
test('Ambiente sem credenciais e confirmação desabilitada', () => {
  for (const chave of ['usuario', 'senha', 'usuarioConsulta', 'senhaConsulta', 'bancoIsoladoConfirmado'])
    assert.equal(ambiente.values.find(v => v.key === chave).value, '');
});
for (const url of ['https://e-sports-production-b1636b75.up.railway.app', 'https://example.org', 'http://localhost:8080@evil.org', 'http://localhost:8080/api', 'http://127.0.0.1.evil.org:8080']) {
  test('Bloqueia destino: ' + url, () => {
    const c = contexto('Listar equipes', { url });
    assert.throws(() => c.executar('prerequest')); assert.equal(c.ignorada, true);
  });
}
test('Exige confirmação de banco isolado', () => {
  const c = contexto('Listar equipes', { bancoIsoladoConfirmado: '' });
  assert.throws(() => c.executar('prerequest')); assert.equal(c.ignorada, true);
});
test('Bloqueia troca de servidor durante execução', () => {
  const c = contexto('Listar equipes', { url: 'http://localhost:8081' });
  assert.throws(() => c.executar('prerequest')); assert.equal(c.ignorada, true);
});
test('Bloqueia ID de outro registro', () => {
  const c = contexto('Excluir fictício equipes', { equipeId: '999' });
  c.executar('prerequest'); assert.equal(c.ignorada, true);
});
test('Permite excluir somente ID registrado como criado', () => {
  const c = contexto('Excluir fictício equipes');
  c.executar('prerequest'); assert.equal(c.ignorada, false);
});
test('Bloqueia variáveis sobrepostas por arquivo de dados', () => {
  const c = contexto('Excluir fictício equipes');
  c.pm.variables = { get: k => k === 'equipeId' ? '999' : c.valores[k] };
  assert.throws(() => c.executar('prerequest')); assert.equal(c.ignorada, true);
});
test('Não reinicia com limpeza pendente', () => {
  const c = contexto('Cadastrar modalidades');
  assert.throws(() => c.executar('prerequest')); assert.equal(c.ignorada, true);
});
test('Inicia nova execução somente com diário vazio', () => {
  const c = contexto('Cadastrar modalidades'); c.diario.registros = []; c.salvarDiario();
  c.executar('prerequest'); assert.equal(c.ignorada, false);
  assert.match(c.valores.prefixo, /^TESTE_POSTMAN_/);
});
test('Futsal preexistente não é criado novamente nem excluído', () => {
  for (const nome of ['Criar Futsal somente se ausente', 'Excluir Futsal somente se criado nesta execução']) {
    const c = contexto(nome, { futsalId: '77' }); c.diario.futsalLeitura = '77'; c.salvarDiario();
    c.executar('prerequest'); assert.equal(c.ignorada, true);
  }
});
test('Registra ID retornado por criação bem-sucedida', () => {
  const c = contexto('Cadastrar participação do atleta');
  c.pm.response = { code: 201, json: () => ({ id: 91 }) }; c.executar('test');
  assert.equal(c.valores.participacaoId, '91');
  assert.equal(JSON.parse(c.colecaoVars.diario).registros.at(-1).recurso, 'participacoes-atletas');
});
test('Registra criação inesperada em teste negativo', () => {
  const c = contexto('409 - estatística sem participação');
  c.pm.response = { code: 201, json: () => ({ id: 92 }) }; c.executar('test');
  assert.equal(JSON.parse(c.colecaoVars.diario).registros.at(-1).chave, 'inesperado_estatisticas_92');
});
test('Exclusão confirmada remove somente o ID correto do diário', () => {
  const c = contexto('Excluir fictício equipes'); c.pm.response = { code: 204 }; c.executar('test');
  assert.equal(JSON.parse(c.colecaoVars.diario).registros.length, 0);
});
test('Falha na exclusão mantém o ID para recuperação', () => {
  const c = contexto('Excluir fictício equipes'); c.pm.response = { code: 409 }; c.executar('test');
  assert.equal(JSON.parse(c.colecaoVars.diario).registros.length, 1);
});
