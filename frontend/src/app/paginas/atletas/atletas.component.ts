import { ParticipacoesAtletasService, ParticipacaoAtleta } from '../../servicos/participacoes-atletas.service';
import { TorneiosService, Registro as Torneio } from '../../servicos/torneios.service';
import { Component, inject, ChangeDetectorRef, DestroyRef } from '@angular/core';
import { FormControl, FormGroup, Validators } from '@angular/forms';
import { HttpErrorResponse } from '@angular/common/http';
import { takeUntilDestroyed } from '@angular/core/rxjs-interop';
import { finalize, takeUntil, skip } from 'rxjs';
import { AtletasService, Registro, DesempenhoAtleta, DesempenhoCompeticao } from '../../servicos/atletas.service';
import { AutenticacaoService } from '../../servicos/autenticacao.service';
import { mensagemErro } from '../../servicos/api';
import { EquipesService, Registro as Equipe } from '../../servicos/equipes.service';

@Component({ selector:'app-atletas', standalone:false, templateUrl:'./atletas.component.html' })
export class AtletasComponent {
 readonly auth = inject(AutenticacaoService);
 private servico = inject(AtletasService);
 private cdr = inject(ChangeDetectorRef);
 private destroyRef = inject(DestroyRef);
 private equipesServico = inject(EquipesService);
 escudoEquipe(id:number|null):string { return this.equipes.find(e=>e.id===id)?.escudo ?? "VERDE"; }
 corEquipe(id:number|null):string { return this.equipes.find(e=>e.id===id)?.corEscudo ?? "#7ED957"; }
 ocupado = false;
 carregando = false;
 falhouLista = false;
  registros: Registro[] = [];
  equipes: Equipe[] = [];
  colunas = ["nome","dataNascimento","equipeId","participacoes","acoes"];
  idEmEdicao: number | null = null;
  consultando = false;
  mensagem = '';
  formulario = new FormGroup({
    nome: new FormControl('', { nonNullable: true, validators: [Validators.required, Validators.pattern(/\S/)] }),
    dataNascimento: new FormControl('', { nonNullable: true, validators: [Validators.required] }),
    equipeId: new FormControl<number | null>(null),
  });


 private participacoesServico=inject(ParticipacoesAtletasService);
 private torneiosServico=inject(TorneiosService);
 participacoes:ParticipacaoAtleta[]=[];
 torneios:Torneio[]=[];
 carregandoParticipacoes=false;
 falhouParticipacoes=false;
 formularioParticipacao=new FormGroup({
  atletaId:new FormControl<number|null>(null,Validators.required),
  equipeId:new FormControl<number|null>(null,Validators.required),
  torneioId:new FormControl<number|null>(null,Validators.required)
 });
 get competicoesCompativeis():Torneio[] {
  const equipe=this.equipes.find(e=>e.id===this.formularioParticipacao.controls.equipeId.value);
  return equipe ? this.torneios.filter(t=>t.modalidadeId===equipe.modalidadeId) : [];
 }
 resumoParticipacoes(id:number):string {
  if(this.carregandoParticipacoes)return 'Carregando participações…';
  if(this.falhouParticipacoes)return 'Não foi possível consultar participações.';
  return this.participacoes.filter(p=>p.atletaId===id).map(p=>p.torneioNome+' — '+p.equipeNome).join('; ') || 'Sem participação: associação necessária';
 }
 carregarParticipacoes():void {
  this.carregandoParticipacoes=true;this.falhouParticipacoes=false;
  this.participacoesServico.listar().pipe(takeUntilDestroyed(this.destroyRef),takeUntil(this.auth.usuario$.pipe(skip(1))),
   finalize(()=>{this.carregandoParticipacoes=false;this.cdr.markForCheck();})).subscribe({
   next:dados=>{this.participacoes=dados;},error:erro=>{this.falhouParticipacoes=true;this.falha(erro);}
  });
 }
 salvarParticipacao():void {
  if(!this.auth.organizador||this.ocupado)return;
  if(this.formularioParticipacao.invalid){this.formularioParticipacao.markAllAsTouched();return;}
  const dados=this.formularioParticipacao.getRawValue();
  this.ocupado=true;
  this.participacoesServico.cadastrar({atletaId:dados.atletaId!,equipeId:dados.equipeId!,torneioId:dados.torneioId!})
   .pipe(takeUntilDestroyed(this.destroyRef),finalize(()=>{this.ocupado=false;this.cdr.markForCheck();})).subscribe({
    next:()=>{this.mensagem='Participação cadastrada.';this.formularioParticipacao.reset();this.carregarParticipacoes();},error:erro=>this.falha(erro)
   });
 }
 excluirParticipacao(p:ParticipacaoAtleta):void {
  if(!this.auth.organizador||this.ocupado||!window.confirm('Remover esta participação?'))return;
  this.ocupado=true;
  this.participacoesServico.excluir(p.id).pipe(takeUntilDestroyed(this.destroyRef),finalize(()=>{this.ocupado=false;this.cdr.markForCheck();})).subscribe({
   next:()=>{this.mensagem='Participação removida.';this.carregarParticipacoes();},error:erro=>this.falha(erro)
  });
 }
 desempenho:DesempenhoAtleta|null=null;
 carregandoDesempenho=false;
 erroDesempenho='';
 competicaoDesempenho=new FormControl<number|null>(null);
 private consultaDesempenho=0;
 readonly rotulosEstatisticas:Record<string,string>={gols:'Gols',pontos:'Pontos',abates:'Abates',assistencias:'Assistências'};
 get desempenhoSelecionado():DesempenhoCompeticao|undefined {
  return this.desempenho?.competicoes.find(c=>c.torneioId===this.competicaoDesempenho.value);
 }
 get camposDesempenho():string[] { return Object.keys(this.desempenhoSelecionado?.totais ?? {}); }
 get colunasHistorico():string[] { return ['data','partida','equipe',...this.camposDesempenho]; }
 carregarDesempenho():void {
  if(!this.auth.organizador || !this.consultando || this.idEmEdicao===null)return;
  const id=this.idEmEdicao;const consulta=++this.consultaDesempenho;
  const selecionada=this.competicaoDesempenho.value;
  this.desempenho=null;this.erroDesempenho='';this.carregandoDesempenho=true;
  this.servico.desempenho(id).pipe(takeUntilDestroyed(this.destroyRef),takeUntil(this.auth.usuario$.pipe(skip(1))),
   finalize(()=>{if(consulta===this.consultaDesempenho){this.carregandoDesempenho=false;this.cdr.markForCheck();}})).subscribe({
   next:dados=>{if(consulta!==this.consultaDesempenho)return;this.desempenho=dados;
    this.competicaoDesempenho.setValue(dados.competicoes.some(c=>c.torneioId===selecionada)?selecionada:dados.competicoes[0]?.torneioId??null);},
   error:erro=>{if(consulta===this.consultaDesempenho)this.erroDesempenho=mensagemErro(erro);}
  });
 }
 constructor() {
  this.auth.usuario$.pipe(takeUntilDestroyed()).subscribe(() => {
   this.registros = [];
   this.cancelar();
   this.equipes = []; this.participacoes=[]; this.torneios=[]; this.formularioParticipacao.reset();
   if(this.auth.organizador) {
    this.carregarParticipacoes();
    this.torneiosServico.listar().pipe(takeUntilDestroyed(this.destroyRef),takeUntil(this.auth.usuario$.pipe(skip(1)))).subscribe({next:dados=>{this.torneios=dados;this.cdr.markForCheck();},error:erro=>this.falha(erro)});
   }
   this.carregar();
   if (this.auth.usuario) this.equipesServico.listar().pipe(takeUntilDestroyed(this.destroyRef), takeUntil(this.auth.usuario$.pipe(skip(1)))).subscribe({
    next: dados => { this.equipes = dados; this.cdr.markForCheck(); },
    error: erro => this.falha(erro)
   });
  });
 }

 carregar(): void {
  if (!this.auth.usuario) { this.carregando = false; this.falhouLista = true; this.mensagem = 'Entre para consultar estes dados.'; return; }
  this.carregando = true;
  this.falhouLista = false;
  this.servico.listar().pipe(takeUntilDestroyed(this.destroyRef), takeUntil(this.auth.usuario$.pipe(skip(1))),
   finalize(() => { this.carregando = false; this.cdr.markForCheck(); })
  ).subscribe({
   next: dados => { this.registros = dados; },
   error: erro => { this.falhouLista = true; this.falha(erro); }
  });
 }

 salvar(): void {
  if (!this.auth.organizador || this.ocupado || this.consultando) return;
  if (this.formulario.invalid) {
   this.formulario.markAllAsTouched();
   this.mensagem = 'Revise os campos destacados antes de continuar.';
   return;
  }
  this.ocupado = true;
  const dados = this.formulario.getRawValue();
  const operacao = this.idEmEdicao === null ? this.servico.cadastrar(dados) : this.servico.atualizar(this.idEmEdicao, dados);
  operacao.pipe(takeUntilDestroyed(this.destroyRef), finalize(() => { this.ocupado = false; this.cdr.markForCheck(); })).subscribe({
   next: () => { this.cancelar(); this.mensagem = 'Registro salvo com sucesso.'; this.carregar(); },
   error: erro => this.falha(erro)
  });
 }

 editar(registro: Registro): void { this.abrir(registro.id, false); }
 consultar(registro: Registro): void { this.abrir(registro.id, true); }

 private abrir(id: number, consulta: boolean): void {
  if (this.ocupado || !this.auth.organizador) return;
  this.ocupado = true;
  this.servico.buscarPorId(id).pipe(takeUntilDestroyed(this.destroyRef),
   finalize(() => { this.ocupado = false; this.cdr.markForCheck(); })
  ).subscribe({
   next: dados => {
    this.cancelar();
    this.idEmEdicao = dados.id;
    this.formulario.patchValue(dados);
    this.consultando = consulta;
    if (consulta) {this.formulario.disable();this.carregarDesempenho();}
   },
   error: erro => this.falha(erro)
  });
 }

 excluir(registro: Registro): void {
  if (!this.auth.organizador || this.ocupado || !window.confirm('Excluir este registro?')) return;
  this.ocupado = true;
  this.servico.excluir(registro.id).pipe(takeUntilDestroyed(this.destroyRef),
   finalize(() => { this.ocupado = false; this.cdr.markForCheck(); })
  ).subscribe({
   next: () => { this.cancelar(); this.mensagem = 'Registro excluído.'; this.carregar(); },
   error: erro => this.falha(erro)
  });
 }

 cancelar(): void {
  this.consultaDesempenho++;this.desempenho=null;this.carregandoDesempenho=false;this.erroDesempenho='';this.competicaoDesempenho.reset();
  this.formulario.enable();
  this.formulario.reset();
  this.idEmEdicao = null;
  this.consultando = false;
  this.mensagem = '';
 }

 private falha(erro: HttpErrorResponse): void {
  this.mensagem = mensagemErro(erro);
  this.cdr.markForCheck();
 }
 
 nomeRelacionado(opcoes: {id:number;nome:string}[], id:number|null): string {
  return opcoes.find(opcao => opcao.id === id)?.nome ?? (id === null ? 'Não informado' : '#' + id);
 }
}
