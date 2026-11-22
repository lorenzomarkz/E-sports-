import { AtletasComponent } from './paginas/atletas/atletas.component';
import { EstatisticasComponent } from './paginas/partidas/estatisticas.component';
import { EscudoComponent, CORES_ESCUDO } from './escudo.component';
import { EquipesComponent } from './paginas/equipes/equipes.component';
import { ClassificacaoComponent } from './paginas/classificacao/classificacao.component';
import { TorneiosComponent } from './paginas/torneios/torneios.component';
import { InscricoesComponent } from './paginas/inscricoes/inscricoes.component';
import { PartidasComponent } from './paginas/partidas/partidas.component';
import { rotuloResultado } from './servicos/modalidades-esportivas';
import { DashboardComponent } from './paginas/dashboard/dashboard.component';
import { TestBed } from '@angular/core/testing';
import { Router } from '@angular/router';
import { HttpClient } from '@angular/common/http';
import { HttpTestingController, provideHttpClientTesting } from '@angular/common/http/testing';
import { AppModule } from './app.module';
import { App } from './app.component';
import { routes } from './app-routing.module';
import { ModalidadesComponent } from './paginas/modalidades/modalidades.component';
import { AutenticacaoService } from './servicos/autenticacao.service';
import { API_URL } from './servicos/api';

describe('E-sports', () => {
 let http: HttpTestingController;
 beforeEach(async () => {
  await TestBed.configureTestingModule({ imports: [AppModule], providers:[provideHttpClientTesting()] }).compileComponents();
  http = TestBed.inject(HttpTestingController);
 });
 afterEach(() => http.verify());
 function responderListas() {
  http.match(req => req.method === 'GET').forEach(req => req.flush([]));
 }
 function entrar() {
  TestBed.inject(AutenticacaoService).entrar('ORGANIZADOR','senha-ficticia-unitaria').subscribe();
  http.expectOne(API_URL + '/equipes').flush([]);
 }
 it('cria layout do organizador com logo original e oito links', async () => {
  entrar();
  const fixture = TestBed.createComponent(App);
  fixture.detectChanges();
  responderListas();
  await fixture.whenStable();
  expect(fixture.componentInstance).toBeTruthy();
  expect(fixture.nativeElement.querySelector('.marca img').getAttribute('src')).toBe('/e-sports-logo.png');
  expect(fixture.nativeElement.querySelectorAll('nav a').length).toBe(8);
 });
 for (const rota of routes.filter(rota => rota.component)) {
  it('abre a página ' + rota.path, async () => {
   entrar();
   const fixture = TestBed.createComponent(App);
   fixture.detectChanges();
   responderListas();
   await TestBed.inject(Router).navigateByUrl('/' + rota.path);
   responderListas();
   await fixture.whenStable();
   expect(fixture.nativeElement.querySelector('main h2')).toBeTruthy();
   expect(fixture.nativeElement.querySelector('h1').textContent).not.toBe('');
  });
 }
 it('valida e cadastra modalidade, atualizando a lista pela API', () => {
  entrar();
  const fixture = TestBed.createComponent(ModalidadesComponent);
  responderListas();
  const pagina = fixture.componentInstance;
  pagina.salvar();
  expect(pagina.formulario.invalid).toBe(true);
  http.expectNone(req => req.method === 'POST');
  pagina.formulario.controls.nome.setValue('Modalidade de teste');
  pagina.salvar();
  const post = http.expectOne(req => req.method === 'POST' && req.url === API_URL + '/modalidades');
  expect(post.request.body.nome).toBe('Modalidade de teste');
  expect(post.request.headers.has('Authorization')).toBe(true);
  post.flush({id:1,nome:'Modalidade de teste',descricao:''});
  http.expectOne(API_URL + '/modalidades').flush([{id:1,nome:'Modalidade de teste',descricao:''}]);
  expect(pagina.registros.length).toBe(1);
  expect(pagina.mensagem).toContain('sucesso');
 });
 it('consulta, edita e exclui pela API', () => {
  entrar();
  const fixture = TestBed.createComponent(ModalidadesComponent);
  responderListas();
  const pagina = fixture.componentInstance;
  const registro = {id:7,nome:'Teste',descricao:''};
  pagina.consultar(registro);
  http.expectOne(API_URL + '/modalidades/7').flush(registro);
  expect(pagina.formulario.disabled).toBe(true);
  pagina.editar(registro);
  http.expectOne(API_URL + '/modalidades/7').flush(registro);
  pagina.formulario.controls.nome.setValue('Atualizado');
  pagina.salvar();
  http.expectOne(req => req.method === 'PUT').flush({...registro,nome:'Atualizado'});
  http.expectOne(API_URL + '/modalidades').flush([{...registro,nome:'Atualizado'}]);
  const confirmar = vi.spyOn(window,'confirm').mockReturnValue(true);
  pagina.excluir(registro);
  http.expectOne(req => req.method === 'DELETE').flush(null);
  http.expectOne(API_URL + '/modalidades').flush([]);
  expect(pagina.registros).toEqual([]);
  confirmar.mockRestore();
 });
 it('exibe erro da API sem simular sucesso', () => {
  entrar();
  const fixture = TestBed.createComponent(ModalidadesComponent);
  responderListas();
  const pagina = fixture.componentInstance;
  pagina.formulario.controls.nome.setValue('Teste');
  pagina.salvar();
  http.expectOne(req => req.method === 'POST').flush({mensagem:'Conflito de teste'},{status:409,statusText:'Conflict'});
  expect(pagina.mensagem).toBe('Conflito de teste');
  expect(pagina.registros).toEqual([]);
  expect(pagina.ocupado).toBe(false);
 });
 it('não envia credenciais para origem externa e limpa ao sair', () => {
  entrar();
  const cliente=TestBed.inject(HttpClient);
  cliente.get('https://example.invalid/teste').subscribe();
  const externo=http.expectOne('https://example.invalid/teste');
  expect(externo.request.headers.has('Authorization')).toBe(false);
  externo.flush({});
  const auth=TestBed.inject(AutenticacaoService);
  auth.sair();
  expect(auth.cabecalho).toBe('');
  expect(auth.usuario).toBe('');
 });

 it('calcula os indicadores pelos dados e status reais retornados', () => {
  entrar();
  const fixture=TestBed.createComponent(DashboardComponent);
  http.expectOne(API_URL + '/torneios/indicadores').flush({competicoesAtivas:1,equipesInscritas:2,partidasRealizadas:1});
  http.expectOne(API_URL + '/torneios').flush([{id:1},{id:2}]);
  http.expectOne(API_URL + '/equipes').flush([{id:1},{id:2},{id:3}]);
  http.expectOne(API_URL + '/partidas').flush([
   {id:1,status:'FINALIZADA'}, {id:2,status:'AGENDADA'}, {id:3,status:'EM_ANDAMENTO'}
  ]);
  const pagina=fixture.componentInstance;
  expect(pagina.totalTorneios).toBe(2);
  expect(pagina.totalEquipes).toBe(3);
  expect(pagina.totalPartidas).toBe(3);
  expect(pagina.realizadas).toBe(1);
  expect(pagina.restantes).toBe(2);
  expect(pagina.agendadas).toBe(1);
 });
 it('não envia cadastro de usuário CONSULTA', () => {
  const auth=TestBed.inject(AutenticacaoService);
  auth.entrar('CONSULTA','senha-ficticia-unitaria').subscribe();
  http.expectOne(API_URL + '/equipes').flush([]);
  const fixture=TestBed.createComponent(ModalidadesComponent);
  responderListas();
  fixture.componentInstance.formulario.controls.nome.setValue('Teste');
  fixture.componentInstance.salvar();
  http.expectNone(req => req.method === 'POST');
  expect(auth.organizador).toBe(false);
 });

 it('exige jogo ao selecionar E-sports', () => {
  entrar();
  const fixture=TestBed.createComponent(TorneiosComponent);
  responderListas();
  const f=fixture.componentInstance.formulario;
  f.controls.modalidadeNome.setValue('E-sports');
  expect(f.controls.jogo.hasError('required')).toBe(true);
  f.controls.jogo.setValue('Valorant');
  expect(f.controls.jogo.valid).toBe(true);
  f.controls.modalidadeNome.setValue('Futebol');
  expect(f.controls.jogo.value).toBe('');
  expect(f.controls.jogo.valid).toBe(true);
 });
 it('filtra competições pela modalidade da equipe', () => {
  entrar();
  const fixture=TestBed.createComponent(InscricoesComponent);
  responderListas();
  const pagina=fixture.componentInstance;
  pagina.equipes=[{id:1,nome:'Equipe teste',cidade:'',nomeResponsavel:'',modalidadeId:2}];
  pagina.torneios=[
   {id:10,nome:'Compatível',modalidadeId:2,descricao:'',dataInicio:'',dataFim:'',prazoInscricao:'',limiteEquipes:2,local:''},
   {id:11,nome:'Outra modalidade',modalidadeId:3,descricao:'',dataInicio:'',dataFim:'',prazoInscricao:'',limiteEquipes:2,local:''}
  ];
  pagina.formulario.controls.equipeId.setValue(1);
  expect(pagina.competicoesCompativeis.map(t=>t.id)).toEqual([10]);
 });
 it('mostra apenas equipes aprovadas na competição selecionada', () => {
  entrar();
  const fixture=TestBed.createComponent(PartidasComponent);
  responderListas();
  const pagina=fixture.componentInstance;
  pagina.torneios=[{id:10,nome:'Teste',modalidadeId:2,descricao:'',dataInicio:'',dataFim:'',prazoInscricao:'',limiteEquipes:2,local:''}];
  pagina.equipes=[{id:1,nome:'A',cidade:'',nomeResponsavel:'',modalidadeId:2},{id:2,nome:'B',cidade:'',nomeResponsavel:'',modalidadeId:2}];
  pagina.formulario.controls.torneioId.setValue(10);
  http.expectOne(API_URL+'/inscricoes/torneio/10').flush([{id:1,equipeId:1,status:'APROVADA'},{id:2,equipeId:2,status:'PENDENTE'}]);
  expect(pagina.equipesAprovadas.map(e=>e.id)).toEqual([1]);
 });
 it('consulta classificação sem autenticação', () => {
  const pagina=TestBed.createComponent(ClassificacaoComponent).componentInstance;
  responderListas();
  pagina.selecao.setValue(7);
  const pedido=http.expectOne(API_URL+'/torneios/7/classificacao');
  expect(pedido.request.headers.has('Authorization')).toBe(false);
  pedido.flush({nome:'Competição de teste',modalidade:'Futebol',disponivel:true,classificacao:[],proximos:[],resultados:[]});
  expect(pagina.dados?.nome).toBe('Competição de teste');
  expect(pagina.carregando).toBe(false);
 });
 it('envia geração ao backend sem inventar confrontos no frontend', () => {
  entrar();
  const pagina=TestBed.createComponent(TorneiosComponent).componentInstance;
  responderListas();
  pagina.abrirGeracao({id:7,nome:'Teste',modalidadeId:1,descricao:'',dataInicio:'2030-01-01',dataFim:'2030-01-02',prazoInscricao:'2029-12-31',limiteEquipes:4,local:''});
  pagina.gerar();
  const pedido=http.expectOne(API_URL+'/partidas/torneio/7/gerar');
  expect(pedido.request.method).toBe('POST');
  expect(pedido.request.body.inicio).toBe('2030-01-01T08:00');
  pedido.flush([{id:1},{id:2},{id:3}]);
  expect(pagina.mensagem).toContain('3 partida(s)');
  expect(pagina.ocupado).toBe(false);
 });
 it('adapta o significado do placar à modalidade e ao jogo', () => {
  expect(rotuloResultado('Futebol')).toBe('Gols');
  expect(rotuloResultado('Basquete')).toBe('Pontos');
  expect(rotuloResultado('Vôlei')).toBe('Sets vencidos');
  expect(rotuloResultado('E-sports','Rocket League')).toBe('Gols');
  expect(rotuloResultado('E-sports','League of Legends')).toBe('Vitórias na série');
  expect(rotuloResultado('E-sports','Valorant')).toBe('Mapas vencidos');
  expect(rotuloResultado('E-sports','Counter-Strike 2')).toBe('Mapas vencidos');
 });
 function entrarConsulta() {
  TestBed.inject(AutenticacaoService).entrar('CONSULTA','senha-ficticia-unitaria').subscribe();
  http.expectOne(API_URL+'/equipes').flush([]);
 }
 it('CONSULTA vê somente quatro links', async () => {
  entrarConsulta();
  const fixture=TestBed.createComponent(App);
  fixture.detectChanges(); responderListas(); await fixture.whenStable();
  const links=Array.from(fixture.nativeElement.querySelectorAll('nav a') as NodeListOf<HTMLAnchorElement>).map(a=>a.getAttribute('href'));
  expect(links).toEqual(['/dashboard','/competicoes','/equipes','/partidas']);
 });
 for (const rota of ['atletas','inscricoes','modalidades']) {
  it('redireciona CONSULTA ao tentar acessar '+rota, async () => {
   entrarConsulta();
   const fixture=TestBed.createComponent(App);fixture.detectChanges();responderListas();
   await TestBed.inject(Router).navigateByUrl('/'+rota);
   responderListas();await fixture.whenStable();
   expect(TestBed.inject(Router).url).toBe('/dashboard');
  });
 }
 for (const componente of [EquipesComponent, TorneiosComponent, PartidasComponent]) {
  it('oculta ferramentas administrativas de CONSULTA em '+componente.name, () => {
   entrarConsulta();
   const fixture=TestBed.createComponent(componente as typeof EquipesComponent);
   responderListas();fixture.detectChanges();
   expect(fixture.nativeElement.querySelector('form')).toBeNull();
   expect(fixture.nativeElement.querySelector('app-estatisticas')).toBeNull();
   expect(fixture.nativeElement.textContent).not.toMatch(/Cadastrar|Limpar formulário|Consultar|Excluir|Editar|Gerar partidas/);
   if(componente!==TorneiosComponent) expect(fixture.nativeElement.textContent).not.toContain('Ações');
  });
 }
 it('carrega classificação resumida e limpa ao sair', () => {
  entrarConsulta();
  const pagina=TestBed.createComponent(DashboardComponent).componentInstance;
  responderListas();
  pagina.selecao.setValue(1);
  http.expectOne(API_URL+'/torneios/1/classificacao').flush({nome:'Teste',disponivel:true,classificacao:[],proximos:[],resultados:[]});
  expect(pagina.classificacao?.nome).toBe('Teste');
  TestBed.inject(AutenticacaoService).sair();responderListas();
  expect(pagina.listaEquipes).toEqual([]);
  expect(pagina.classificacao).toBeNull();
 });
 it('exige somente cor e envia modelo padrão no cadastro de equipe', () => {
  entrar();
  const pagina=TestBed.createComponent(EquipesComponent).componentInstance;
  responderListas();
  pagina.formulario.patchValue({nome:'Equipe teste',cidade:'Cidade',nomeResponsavel:'Responsável',modalidadeId:1});
  expect(pagina.formulario.invalid).toBe(true);
  expect(pagina.formulario.invalid).toBe(true);
  pagina.formulario.controls.corEscudo.setValue('#EF75B0');
  pagina.salvar();
  const pedido=http.expectOne(req=>req.method==='POST' && req.url===API_URL+'/equipes');
  expect(pedido.request.body.escudo).toBe('VERDE');
  expect(pedido.request.body.corEscudo).toBe('#EF75B0');
  pedido.flush({id:7,...pedido.request.body});
  http.expectOne(API_URL+'/equipes').flush([]);
 });
 it('mostra nomes e escudos nas partidas sem depender de login', () => {
  const fixture=TestBed.createComponent(PartidasComponent);
  http.expectOne(API_URL+'/partidas').flush([{id:1,torneioId:1,equipeAId:4,equipeBId:18,equipeANome:'Equipe Azul',equipeBNome:'Equipe Roxa',equipeAEscudo:'AZUL',equipeBEscudo:'ROXO',status:'AGENDADA',dataHora:'2030-01-01T08:00:00'}]);
  responderListas();fixture.detectChanges();
  expect(fixture.nativeElement.textContent).toContain('Equipe Azul');
  expect(fixture.nativeElement.textContent).toContain('Equipe Roxa');
  expect(fixture.nativeElement.textContent).not.toContain('#18');
  expect(fixture.nativeElement.querySelectorAll('app-escudo').length).toBe(2);
 });
 it('unifica a rota de torneios para CONSULTA', async () => {
  entrarConsulta();
  const fixture=TestBed.createComponent(App);fixture.detectChanges();responderListas();
  await TestBed.inject(Router).navigateByUrl('/torneios');
  responderListas();await fixture.whenStable();
  expect(TestBed.inject(Router).url).toBe('/competicoes');
  expect(fixture.nativeElement.querySelector('main h2').textContent).toBe('Competições');
  expect(fixture.nativeElement.querySelector('main form')).toBeNull();
 });
 it('oferece 16 cores e garante contraste da faixa', () => {
  const escudo=TestBed.createComponent(EscudoComponent).componentInstance;
  expect(CORES_ESCUDO.length).toBe(16);
  escudo.corEscudo='#F5F5F5';
  expect(escudo.cor).toBe('#F5F5F5');
  expect(escudo.contraste).toBe('#111A17');
  escudo.corEscudo='#222222';
  expect(escudo.contraste).toBe('#FFFFFF');
  escudo.corEscudo=undefined;
  expect(escudo.cor).toBe('#7ED957');
 });
 it('mostra prévia antes de salvar e recupera a cor ao editar', () => {
  entrar();
  const fixture=TestBed.createComponent(EquipesComponent);
  responderListas();
  const equipe={id:7,nome:'Teste',cidade:'Cidade',nomeResponsavel:'Responsável',modalidadeId:1,escudo:'ROXO',corEscudo:'#55C5F4'};
  fixture.componentInstance.editar(equipe);
  http.expectOne(API_URL+'/equipes/7').flush(equipe);
  fixture.detectChanges();
  expect(fixture.componentInstance.formulario.controls.corEscudo.value).toBe('#55C5F4');
  const previa=fixture.nativeElement.querySelector('[aria-label="Prévia do escudo"] .escudo') as HTMLElement;
  expect(previa.style.background).toBe('rgb(85, 197, 244)');
  http.expectNone(req=>req.method==='POST'||req.method==='PUT');
 });
 for(const [modalidade,jogo,campos] of [
  ['Futebol','',['gols','assistencias']],['Futsal','',['gols','assistencias']],
  ['Handebol','',['gols','assistencias']],['Basquete','',['pontos','assistencias']],
  ['Vôlei','',['pontos']],['E-sports','Rocket League',['gols','assistencias']],
  ['E-sports','Valorant',['abates','assistencias']],['E-sports','League of Legends',['abates','assistencias']],
  ['E-sports','Counter-Strike 2',['abates','assistencias']]
 ] as [string,string,string[]][]) {
  it('exibe campos e colunas corretos para '+modalidade+' '+jogo, () => {
   entrar();
   const fixture=TestBed.createComponent(EstatisticasComponent);
   fixture.componentInstance.partida={id:1,torneioId:1,equipeAId:1,equipeBId:2,dataHora:'2030-01-01',local:'Teste',status:'AGENDADA',placarEquipeA:null,placarEquipeB:null};
   fixture.componentInstance.torneio={id:1,nome:'Teste',modalidadeNome:modalidade,jogo,modalidadeId:1,dataInicio:'',dataFim:'',prazoInscricao:'',limiteEquipes:4,local:'',descricao:''};
   fixture.detectChanges();responderListas();
   expect(fixture.componentInstance.colunas).toEqual(['atleta',...campos,'acoes']);
   for(const campo of ['gols','pontos','abates','assistencias']){
    expect(!!fixture.nativeElement.querySelector('input[formControlName="'+campo+'"]')).toBe(campos.includes(campo));
   }
  });
 }
 for(const [status,rotulo] of [['PENDENTE','Aguardando aprovação'],['APROVADA','INSCRITO'],['REJEITADA','Inscrição rejeitada']]) {
  it('restaura estado '+status+' da inscrição carregada', () => {
   entrar();const pagina=TestBed.createComponent(EquipesComponent).componentInstance;
   responderListas();
   pagina.competicaoInscricao.setValue(10,{emitEvent:false});
   pagina.inscricoes=[{id:1,equipeId:3,torneioId:10,dataInscricao:'2026-11-20',status}];
   expect(pagina.estadoInscricao(3)).toBe(rotulo);
   expect(pagina.estadoInscricao(4)).toBe('Solicitar inscrição');
  });
 }
 it('atualiza a solicitação imediatamente e impede segundo POST', () => {
  entrar();const pagina=TestBed.createComponent(InscricoesComponent).componentInstance;
  responderListas();
  pagina.formulario.patchValue({equipeId:3,torneioId:10,dataInscricao:'2026-11-20',status:'PENDENTE'});
  pagina.salvar();
  http.expectOne(req=>req.method==='POST'&&req.url===API_URL+'/inscricoes').flush({id:1,equipeId:3,torneioId:10,dataInscricao:'2026-11-20',status:'PENDENTE'});
  expect(pagina.textoSolicitacao).toBe('Aguardando aprovação');
  http.expectOne(API_URL+'/inscricoes').flush([{id:1,equipeId:3,torneioId:10,dataInscricao:'2026-11-20',status:'PENDENTE'}]);
  pagina.salvar();http.expectNone(req=>req.method==='POST');
 });
 it('recarrega inscrições e competição selecionada a partir da API', () => {
  entrar();const pagina=TestBed.createComponent(EquipesComponent).componentInstance;
  http.expectOne(API_URL+'/inscricoes').flush([{id:1,equipeId:3,torneioId:10,status:'APROVADA',dataInscricao:'2026-11-20'}]);
  http.expectOne(API_URL+'/torneios').flush([{id:10,nome:'Teste',modalidadeId:1}]);
  http.expectOne(API_URL+'/equipes').flush([{id:3,nome:'Equipe',modalidadeId:1,escudo:'VERDE',corEscudo:'#7ED957'}]);
  http.expectOne(API_URL+'/modalidades').flush([{id:1,nome:'Futsal'}]);
  expect(pagina.competicaoInscricao.value).toBe(10);
  expect(pagina.estadoInscricao(3)).toBe('INSCRITO');
 });
 it('filtra competições pela equipe escolhida e reutiliza o atleta cadastrado', () => {
  entrar();const pagina=TestBed.createComponent(AtletasComponent).componentInstance;
  http.expectOne(API_URL+'/equipes').flush([{id:3,nome:'Futsal',modalidadeId:1},{id:4,nome:'Basquete',modalidadeId:2}]);
  http.expectOne(API_URL+'/torneios').flush([{id:10,nome:'Copa Futsal',modalidadeId:1},{id:20,nome:'Copa Basquete',modalidadeId:2}]);
  responderListas();
  pagina.formularioParticipacao.patchValue({atletaId:7,equipeId:3,torneioId:10});
  expect(pagina.competicoesCompativeis.map(t=>t.id)).toEqual([10]);
  pagina.salvarParticipacao();
  const post=http.expectOne(API_URL+'/participacoes-atletas');
  expect(post.request.body).toEqual({atletaId:7,equipeId:3,torneioId:10});
  post.flush({id:1,atletaId:7,equipeId:3,torneioId:10});responderListas();
  pagina.formularioParticipacao.patchValue({atletaId:7,equipeId:4,torneioId:20});
  expect(pagina.competicoesCompativeis.map(t=>t.id)).toEqual([20]);
  pagina.salvarParticipacao();
  const outro=http.expectOne(API_URL+'/participacoes-atletas');expect(outro.request.body.atletaId).toBe(7);
  outro.flush({id:2,atletaId:7,equipeId:4,torneioId:20});responderListas();
  http.expectNone(req=>req.method==='POST'&&req.url===API_URL+'/atletas');
 });
 it('identifica atletas antigos sem criar participação automaticamente', () => {
  entrar();const pagina=TestBed.createComponent(AtletasComponent).componentInstance;responderListas();
  expect(pagina.resumoParticipacoes(7)).toContain('associação necessária');
  http.expectNone(req=>req.method==='POST');
 });
 it('carrega somente participantes do confronto para estatísticas e mantém nome legado', () => {
  entrar();const fixture=TestBed.createComponent(EstatisticasComponent);const pagina=fixture.componentInstance;
  pagina.partida={id:12,torneioId:10,equipeAId:3,equipeBId:4,dataHora:'2030-01-01',local:'Teste',status:'AGENDADA',placarEquipeA:null,placarEquipeB:null};
  pagina.ngOnChanges();
  http.expectOne(req=>req.url===API_URL+'/participacoes-atletas'&&req.params.get('partidaId')==='12').flush([{id:1,atletaId:7,atletaNome:'Atleta participante',equipeId:3,torneioId:10}]);
  http.expectOne(req=>req.url===API_URL+'/estatisticas').flush([{id:9,atletaId:8,atletaNome:'Atleta antigo',partidaId:12,participacaoId:null,gols:1}]);
  expect(pagina.atletas).toEqual([{id:7,nome:'Atleta participante'}]);
  fixture.detectChanges();expect(fixture.nativeElement.textContent).toContain('Atleta antigo');
  expect(fixture.nativeElement.textContent).toContain('Registro antigo, sem participação associada');
  http.expectNone(API_URL+'/atletas');
 });
 it('preserva modelo legado ao editar cor e mostra a cor após recriar a página', () => {
  entrar();const fixture=TestBed.createComponent(EquipesComponent);responderListas();
  const pagina=fixture.componentInstance;
  const equipe={id:7,nome:'Teste',cidade:'Cidade',nomeResponsavel:'Responsável',modalidadeId:1,escudo:'ROXO',corEscudo:'#55C5F4'};
  pagina.editar(equipe);http.expectOne(API_URL+'/equipes/7').flush(equipe);
  pagina.formulario.controls.corEscudo.setValue('#F28C32');fixture.detectChanges();
  expect(fixture.nativeElement.querySelector('[formControlName="escudo"]')).toBeNull();
  expect(fixture.nativeElement.textContent).not.toContain('Modelo do escudo');
  pagina.salvar();const put=http.expectOne(req=>req.method==='PUT');
  expect(put.request.body.escudo).toBe('ROXO');expect(put.request.body.corEscudo).toBe('#F28C32');
  const salvo={...equipe,corEscudo:'#F28C32'};put.flush(salvo);
  http.expectOne(API_URL+'/equipes').flush([salvo]);fixture.destroy();
  const recarregada=TestBed.createComponent(EquipesComponent);
  http.expectOne(API_URL+'/equipes').flush([salvo]);responderListas();recarregada.detectChanges();
  const escudo=recarregada.nativeElement.querySelector('td .escudo') as HTMLElement;
  expect(escudo.style.background).toBe('rgb(242, 140, 50)');
 });
 it('consulta desempenho por competição e atualiza os totais em nova consulta', () => {
  entrar();const fixture=TestBed.createComponent(AtletasComponent);const pagina=fixture.componentInstance;responderListas();
  const atleta={id:7,nome:'Atleta teste',dataNascimento:'2000-01-01',equipeId:null};
  pagina.consultar(atleta);http.expectOne(API_URL+'/atletas/7').flush(atleta);
  const futsal={torneioId:10,torneioNome:'Copa Futsal',modalidade:'Futsal',jogo:null,equipes:['Equipe verde'],partidasComEstatisticas:2,totais:{gols:3,assistencias:null},historico:[]};
  const basquete={torneioId:20,torneioNome:'Copa Basquete',modalidade:'Basquete',jogo:null,equipes:['Equipe azul'],partidasComEstatisticas:1,totais:{pontos:8,assistencias:2},historico:[]};
  http.expectOne(API_URL+'/atletas/7/desempenho').flush({atletaId:7,atletaNome:'Atleta teste',competicoes:[futsal,basquete]});fixture.detectChanges();
  expect(pagina.desempenhoSelecionado?.totais['gols']).toBe(3);expect(pagina.camposDesempenho).toEqual(['gols','assistencias']);
  expect(fixture.nativeElement.textContent).toContain('Partidas com estatísticas registradas: 2');
  pagina.competicaoDesempenho.setValue(20);fixture.detectChanges();expect(pagina.camposDesempenho).toEqual(['pontos','assistencias']);
  pagina.carregarDesempenho();http.expectOne(API_URL+'/atletas/7/desempenho').flush({atletaId:7,atletaNome:'Atleta teste',competicoes:[futsal,{...basquete,totais:{pontos:12,assistencias:2}}]});
  expect(pagina.competicaoDesempenho.value).toBe(20);expect(pagina.desempenhoSelecionado?.totais['pontos']).toBe(12);
 });
 it('não exibe desempenho atrasado depois de cancelar a consulta', () => {
  entrar();const pagina=TestBed.createComponent(AtletasComponent).componentInstance;responderListas();
  const atleta={id:7,nome:'Teste',dataNascimento:'2000-01-01',equipeId:null};pagina.consultar(atleta);http.expectOne(API_URL+'/atletas/7').flush(atleta);
  const pedido=http.expectOne(API_URL+'/atletas/7/desempenho');pagina.cancelar();pedido.flush({atletaId:7,atletaNome:'Teste',competicoes:[]});expect(pagina.desempenho).toBeNull();
 });
 it('mostra três KPIs reais e permite atualizar sem acesso a inscrições', () => {
  entrarConsulta();const fixture=TestBed.createComponent(DashboardComponent);const pagina=fixture.componentInstance;
  http.expectOne(API_URL+'/torneios/indicadores').flush({competicoesAtivas:2,equipesInscritas:7,partidasRealizadas:5});responderListas();fixture.detectChanges();
  expect(Array.from(fixture.nativeElement.querySelectorAll('.indicador strong')).map((n:any)=>n.textContent.trim())).toEqual(['2','7','5']);
  expect(fixture.nativeElement.querySelectorAll('.colunas-dashboard > .coluna').length).toBe(2);
  pagina.atualizarIndicadores();http.expectOne(API_URL+'/torneios/indicadores').flush({competicoesAtivas:3,equipesInscritas:8,partidasRealizadas:6});fixture.detectChanges();
  expect(pagina.indicadores?.partidasRealizadas).toBe(6);http.expectNone(API_URL+'/inscricoes');
 });
 it('atualiza os KPIs automaticamente e cancela o temporizador ao sair da página', () => {
  vi.useFakeTimers();
  try {
   const fixture=TestBed.createComponent(DashboardComponent);responderListas();
   vi.advanceTimersByTime(30000);
   http.expectOne(API_URL+'/torneios/indicadores').flush({competicoesAtivas:1,equipesInscritas:4,partidasRealizadas:2});
   expect(fixture.componentInstance.indicadores?.equipesInscritas).toBe(4);fixture.destroy();
   vi.advanceTimersByTime(30000);http.expectNone(API_URL+'/torneios/indicadores');
  } finally {vi.useRealTimers();}
 });
 it('distingue erro de carregamento de indicadores com valor zero', () => {
  const fixture=TestBed.createComponent(DashboardComponent);const pagina=fixture.componentInstance;
  http.expectOne(API_URL+'/torneios/indicadores').flush({}, {status:500,statusText:'Erro'});responderListas();fixture.detectChanges();
  expect(pagina.indicadores).toBeNull();expect(pagina.erroIndicadores).not.toBe('');
  pagina.atualizarIndicadores();http.expectOne(API_URL+'/torneios/indicadores').flush({competicoesAtivas:0,equipesInscritas:0,partidasRealizadas:0});fixture.detectChanges();
  expect(Array.from(fixture.nativeElement.querySelectorAll('.indicador strong')).map((n:any)=>n.textContent.trim())).toEqual(['0','0','0']);expect(pagina.erroIndicadores).toBe('');
 });
});
