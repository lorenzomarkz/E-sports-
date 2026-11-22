import { ActivatedRoute } from '@angular/router';
import { hoje } from '../../servicos/modalidades-esportivas';
import { Registro as Equipe } from '../../servicos/equipes.service';
import { Registro as Torneio } from '../../servicos/torneios.service';
import { Component, inject, ChangeDetectorRef, DestroyRef } from '@angular/core';
import { FormControl, FormGroup, Validators } from '@angular/forms';
import { HttpErrorResponse } from '@angular/common/http';
import { takeUntilDestroyed } from '@angular/core/rxjs-interop';
import { finalize, switchMap, takeUntil, skip } from 'rxjs';
import { InscricoesService, Registro } from '../../servicos/inscricoes.service';
import { AutenticacaoService } from '../../servicos/autenticacao.service';
import { mensagemErro } from '../../servicos/api';
import { EquipesService } from '../../servicos/equipes.service';
import { TorneiosService } from '../../servicos/torneios.service';

@Component({ selector:'app-inscricoes', standalone:false, templateUrl:'./inscricoes.component.html' })
export class InscricoesComponent {
 readonly auth = inject(AutenticacaoService);
 private servico = inject(InscricoesService);
 private cdr = inject(ChangeDetectorRef);
 private destroyRef = inject(DestroyRef);
 private equipesServico = inject(EquipesService);
private torneiosServico = inject(TorneiosService);
 escudoEquipe(id:number|null):string { return this.equipes.find(e=>e.id===id)?.escudo ?? "VERDE"; }
 corEquipe(id:number|null):string { return this.equipes.find(e=>e.id===id)?.corEscudo ?? "#7ED957"; }
 ocupado = false;
 carregando = false;
 falhouLista = false;
  registros: Registro[] = [];
  equipes: Equipe[] = [];
  torneios: Torneio[] = [];
 private route = inject(ActivatedRoute);
 get competicoesCompativeis(): Torneio[] { const equipe=this.equipes.find(e=>e.id===this.formulario.controls.equipeId.value); return equipe ? this.torneios.filter(t=>t.modalidadeId===equipe.modalidadeId) : []; }
 get inscricaoAtual():Registro|undefined {
  return this.registros.find(i=>i.equipeId===this.formulario.controls.equipeId.value && i.torneioId===this.formulario.controls.torneioId.value);
 }
 get textoSolicitacao():string {
  const status=this.inscricaoAtual?.status;
  return status==='APROVADA'?'INSCRITO':status==='PENDENTE'?'Aguardando aprovação':status==='REJEITADA'?'Inscrição rejeitada':'Solicitar inscrição';
 }
  colunas = ["equipeId","torneioId","dataInscricao","status","acoes"];
  idEmEdicao: number | null = null;
  consultando = false;
  mensagem = '';
  formulario = new FormGroup({
    equipeId: new FormControl<number | null>(null, { validators: [Validators.required] }),
    torneioId: new FormControl<number | null>(null, { validators: [Validators.required] }),
    dataInscricao: new FormControl(hoje(), { nonNullable: true, validators: [Validators.required] }),
    status: new FormControl('PENDENTE', { nonNullable: true, validators: [Validators.required] }),
  });


 constructor() {
 this.formulario.controls.equipeId.valueChanges.pipe(takeUntilDestroyed()).subscribe(() => this.formulario.controls.torneioId.setValue(null));
  this.auth.usuario$.pipe(takeUntilDestroyed()).subscribe(() => {
   this.registros = [];
   this.cancelar();
   this.equipes = [];
this.torneios = [];
   this.carregar();
   if (this.auth.usuario) this.equipesServico.listar().pipe(takeUntilDestroyed(this.destroyRef), takeUntil(this.auth.usuario$.pipe(skip(1)))).subscribe({
    next: dados => { this.equipes = dados;
 const equipeId = Number(this.route.snapshot.queryParamMap.get('equipeId'));
 if (this.equipes.some(e=>e.id===equipeId) && this.idEmEdicao===null) {
 this.formulario.controls.equipeId.setValue(equipeId);
 this.formulario.controls.torneioId.setValue(Number(this.route.snapshot.queryParamMap.get('torneioId')) || null);
 }
 this.cdr.markForCheck(); },
    error: erro => this.falha(erro)
   });
this.torneiosServico.listar().pipe(takeUntilDestroyed(this.destroyRef), takeUntil(this.auth.usuario$.pipe(skip(1)))).subscribe({
    next: dados => { this.torneios = dados; this.cdr.markForCheck(); },
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
  if (this.idEmEdicao===null && this.inscricaoAtual) { this.mensagem=this.textoSolicitacao;return; }
  if (this.formulario.invalid) {
   this.formulario.markAllAsTouched();
   this.mensagem = 'Revise os campos destacados antes de continuar.';
   return;
  }
  this.ocupado = true;
  const dados = this.formulario.getRawValue();
  if (this.idEmEdicao === null) dados.status = "PENDENTE";
  const operacao = this.idEmEdicao === null ? this.servico.cadastrar(dados) : this.servico.atualizar(this.idEmEdicao, dados);
  operacao.pipe(takeUntilDestroyed(this.destroyRef), finalize(() => { this.ocupado = false; this.cdr.markForCheck(); })).subscribe({
   next: registro => {
    const nova=this.idEmEdicao===null;this.cancelar();
    this.registros=[...this.registros.filter(i=>i.id!==registro.id),registro];
    if(nova)this.formulario.patchValue({equipeId:registro.equipeId,torneioId:registro.torneioId},{emitEvent:false});
    this.mensagem='Registro salvo com sucesso.';this.carregar();
   },
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
    this.formulario.patchValue(dados, {emitEvent:false});
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
 
 alterarStatus(registro: Registro, status: 'APROVADA' | 'REJEITADA'): void {
  if (!this.auth.organizador || this.ocupado) return;
  this.ocupado = true;
  this.servico.buscarPorId(registro.id).pipe(
   switchMap(atual => this.servico.atualizar(atual.id, { ...atual, status })),
   takeUntilDestroyed(this.destroyRef),
   finalize(() => { this.ocupado = false; this.cdr.markForCheck(); })
  ).subscribe({ next: () => { this.mensagem = 'Status atualizado.'; this.carregar(); }, error: erro => this.falha(erro) });
 }

 nomeRelacionado(opcoes: {id:number;nome:string}[], id:number|null): string {
  return opcoes.find(opcao => opcao.id === id)?.nome ?? (id === null ? 'Não informado' : '#' + id);
 }
}
