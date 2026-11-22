import { Component, inject, ChangeDetectorRef, DestroyRef } from '@angular/core';
import { FormControl, FormGroup, Validators } from '@angular/forms';
import { HttpErrorResponse } from '@angular/common/http';
import { takeUntilDestroyed } from '@angular/core/rxjs-interop';
import { finalize, switchMap, takeUntil, skip } from 'rxjs';
import { TorneiosService, Registro } from '../../servicos/torneios.service';
import { AutenticacaoService } from '../../servicos/autenticacao.service';
import { mensagemErro } from '../../servicos/api';
import { MODALIDADES, JOGOS, normalizar } from '../../servicos/modalidades-esportivas';
import { ModalidadesService } from '../../servicos/modalidades.service';

@Component({ selector:'app-torneios', standalone:false, templateUrl:'./torneios.component.html' })
export class TorneiosComponent {
 readonly auth = inject(AutenticacaoService);
 private servico = inject(TorneiosService);
 private cdr = inject(ChangeDetectorRef);
 private destroyRef = inject(DestroyRef);
 private modalidadesServico = inject(ModalidadesService);
 geracaoTorneio: Registro|null=null;
 inicioGeracao=new FormControl('',{nonNullable:true,validators:[Validators.required]});
 abrirGeracao(t:Registro):void {
  this.geracaoTorneio=t;
  this.inicioGeracao.setValue(t.dataInicio+'T08:00');
 }
 gerar():void {
  if(!this.auth.organizador||this.ocupado||!this.geracaoTorneio||this.inicioGeracao.invalid)return;
  this.ocupado=true;
  this.servico.gerar(this.geracaoTorneio.id,this.inicioGeracao.value).pipe(takeUntilDestroyed(this.destroyRef),
    finalize(()=>{this.ocupado=false;this.cdr.markForCheck();})).subscribe({
   next:partidas=>{this.mensagem=partidas.length ? partidas.length+' partida(s) gerada(s). Consulte a página Partidas.' : 'Todos os confrontos já estão cadastrados. Nenhuma duplicata foi criada.';this.geracaoTorneio=null;},
   error:erro=>this.falha(erro)
  });
 }
 ocupado = false;
 carregando = false;
 falhouLista = false;
  registros: Registro[] = [];
  jogos = JOGOS;
  get opcoesModalidades(): string[] {
    return [...MODALIDADES, ...this.modalidades.map(m => m.nome).filter(nome => !MODALIDADES.some(p => normalizar(p) === normalizar(nome)))];
  }
  get eletronico(): boolean { return normalizar(this.formulario.controls.modalidadeNome.value) === 'e-sports'; }

  modalidades: { id: number; nome: string }[] = [];
  colunas = ["nome","modalidadeId","dataInicio","dataFim","limiteEquipes","acoes"];
  idEmEdicao: number | null = null;
  consultando = false;
  mensagem = '';
  formulario = new FormGroup({
    nome: new FormControl('', { nonNullable: true, validators: [Validators.required, Validators.pattern(/\S/)] }),
    descricao: new FormControl('', { nonNullable: true, validators: [] }),
    modalidadeId: new FormControl<number | null>(null),
    modalidadeNome: new FormControl('', {nonNullable:true, validators:[Validators.required]}),
    jogo: new FormControl('', {nonNullable:true}),
    dataInicio: new FormControl('', { nonNullable: true, validators: [Validators.required] }),
    dataFim: new FormControl('', { nonNullable: true, validators: [Validators.required] }),
    prazoInscricao: new FormControl('', { nonNullable: true, validators: [Validators.required] }),
    limiteEquipes: new FormControl<number | null>(null, { validators: [Validators.required, Validators.min(1), Validators.pattern(/^\d+$/)] }),
    local: new FormControl('', { nonNullable: true, validators: [Validators.required, Validators.pattern(/\S/)] }),
  });


 constructor() {
  this.formulario.controls.modalidadeNome.valueChanges.pipe(takeUntilDestroyed()).subscribe(nome => {
   this.formulario.controls.modalidadeId.setValue(this.modalidades.find(m => normalizar(m.nome) === normalizar(nome))?.id ?? null);
   this.formulario.controls.jogo.setValue('');
   this.formulario.controls.jogo.setValidators(normalizar(nome) === 'e-sports' ? [Validators.required] : []);
   this.formulario.controls.jogo.updateValueAndValidity();
  });
  this.auth.usuario$.pipe(takeUntilDestroyed()).subscribe(() => {
   this.registros = [];
   this.cancelar();
   this.modalidades = [];
   this.carregar();
   this.modalidadesServico.listar().pipe(takeUntilDestroyed(this.destroyRef), takeUntil(this.auth.usuario$.pipe(skip(1)))).subscribe({
    next: dados => { this.modalidades = dados; this.cdr.markForCheck(); },
    error: erro => this.falha(erro)
   });
  });
 }

 carregar(): void {
  
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
  if (this.ocupado || (!consulta && !this.auth.organizador)) return;
  this.ocupado = true;
  this.servico.buscarPorId(id).pipe(takeUntilDestroyed(this.destroyRef),
   finalize(() => { this.ocupado = false; this.cdr.markForCheck(); })
  ).subscribe({
   next: dados => {
    this.cancelar();
    this.idEmEdicao = dados.id;
    const nome = dados.modalidadeNome ?? this.nomeRelacionado(this.modalidades, dados.modalidadeId);
    this.formulario.patchValue({...dados, modalidadeNome: MODALIDADES.find(opcao => normalizar(opcao) === normalizar(nome)) ?? nome, jogo: dados.jogo ?? ''}, {emitEvent:false});
    this.formulario.controls.jogo.setValidators(this.eletronico ? [Validators.required] : []);
    this.formulario.controls.jogo.updateValueAndValidity();
    this.consultando = consulta;
    if (consulta) this.formulario.disable();
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
