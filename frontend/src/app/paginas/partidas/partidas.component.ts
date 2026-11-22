import { InscricoesService } from '../../servicos/inscricoes.service';
import { Registro as Equipe } from '../../servicos/equipes.service';
import { Registro as Torneio } from '../../servicos/torneios.service';
import { rotuloResultado, normalizar } from '../../servicos/modalidades-esportivas';
import { Component, inject, ChangeDetectorRef, DestroyRef } from '@angular/core';
import { FormControl, FormGroup, Validators } from '@angular/forms';
import { HttpErrorResponse } from '@angular/common/http';
import { takeUntilDestroyed } from '@angular/core/rxjs-interop';
import { finalize, switchMap, takeUntil, skip } from 'rxjs';
import { PartidasService, Registro } from '../../servicos/partidas.service';
import { AutenticacaoService } from '../../servicos/autenticacao.service';
import { mensagemErro } from '../../servicos/api';
import { TorneiosService } from '../../servicos/torneios.service';
import { EquipesService } from '../../servicos/equipes.service';

@Component({ selector:'app-partidas', standalone:false, templateUrl:'./partidas.component.html' })
export class PartidasComponent {
 readonly auth = inject(AutenticacaoService);
 private servico = inject(PartidasService);
 private cdr = inject(ChangeDetectorRef);
 private destroyRef = inject(DestroyRef);
 private torneiosServico = inject(TorneiosService);
private equipesServico = inject(EquipesService);
 ocupado = false;
 carregando = false;
 falhouLista = false;
  registros: Registro[] = [];
  torneios: Torneio[] = [];
  equipes: Equipe[] = [];
  idsAprovadas: number[] = [];
  carregandoEquipes = false;
  partidaEstatisticas: Registro | null = null;
  private inscricoesServico = inject(InscricoesService);
  get competicao(): Torneio | undefined { return this.torneios.find(t=>t.id===this.formulario.controls.torneioId.value); }
  get freeFire(): boolean { return this.competicao?.jogo === 'Free Fire'; }
  get rotulo(): string { return rotuloResultado(this.competicao?.modalidadeNome ?? '', this.competicao?.jogo); }
  get equipesAprovadas(): Equipe[] { return this.equipes.filter(e=>this.idsAprovadas.includes(e.id) && e.modalidadeId===this.competicao?.modalidadeId); }
  torneioDaPartida(partida: Registro): Torneio | undefined { return this.torneios.find(t=>t.id===partida.torneioId); }
  carregarAprovadas(id: number | null, limpar = true): void {
    this.idsAprovadas = [];
    if (limpar) { this.formulario.controls.equipeAId.reset(); this.formulario.controls.equipeBId.reset(); this.formulario.controls.placarEquipeA.reset(); this.formulario.controls.placarEquipeB.reset(); }
    this.carregandoEquipes = false;
    if (!id || !this.auth.organizador) return;
    this.carregandoEquipes = true;
    this.inscricoesServico.buscarPorTorneio(id).pipe(takeUntilDestroyed(this.destroyRef)).subscribe({
      next: dados => {
        if(this.formulario.controls.torneioId.value!==id) return;
        this.idsAprovadas = dados.filter(i=>i.status==='APROVADA').map(i=>i.equipeId!);
        this.carregandoEquipes = false; this.cdr.markForCheck();
      },
      error: erro => { if(this.formulario.controls.torneioId.value!==id) return; this.carregandoEquipes = false; this.falha(erro); }
    });
  }
  get colunas(): string[] { const campos = ["torneioId","equipeAId","equipeBId","dataHora","resultado","status","acoes"]; return this.auth.organizador ? campos : campos.filter(c => c !== "acoes"); }
  idEmEdicao: number | null = null;
  consultando = false;
  mensagem = '';
  formulario = new FormGroup({
    torneioId: new FormControl<number | null>(null, { validators: [Validators.required] }),
    equipeAId: new FormControl<number | null>(null, { validators: [Validators.required] }),
    equipeBId: new FormControl<number | null>(null, { validators: [Validators.required] }),
    dataHora: new FormControl('', { nonNullable: true, validators: [Validators.required] }),
    local: new FormControl('', { nonNullable: true, validators: [Validators.required, Validators.pattern(/\S/)] }),
    placarEquipeA: new FormControl<number | null>(null, { validators: [Validators.min(0), Validators.pattern(/^\d+$/)] }),
    placarEquipeB: new FormControl<number | null>(null, { validators: [Validators.min(0), Validators.pattern(/^\d+$/)] }),
    status: new FormControl('AGENDADA', { nonNullable: true, validators: [Validators.required] }),
  });


 constructor() {
  this.formulario.controls.torneioId.valueChanges.pipe(takeUntilDestroyed()).subscribe(id=>this.carregarAprovadas(id));
  this.auth.usuario$.pipe(takeUntilDestroyed()).subscribe(() => {
   this.registros = [];
   this.partidaEstatisticas = null;
   this.cancelar();
   this.torneios = [];
this.equipes = [];
   this.carregar();
   this.torneiosServico.listar().pipe(takeUntilDestroyed(this.destroyRef), takeUntil(this.auth.usuario$.pipe(skip(1)))).subscribe({
    next: dados => { this.torneios = dados; this.cdr.markForCheck(); },
    error: erro => this.falha(erro)
   });
if (this.auth.usuario) this.equipesServico.listar().pipe(takeUntilDestroyed(this.destroyRef), takeUntil(this.auth.usuario$.pipe(skip(1)))).subscribe({
    next: dados => { this.equipes = dados; this.cdr.markForCheck(); },
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
  if (this.freeFire) { this.mensagem = 'Free Fire terá classificação e pontos em uma etapa posterior.'; return; }
  if (!this.idsAprovadas.includes(this.formulario.controls.equipeAId.value!)
      || !this.idsAprovadas.includes(this.formulario.controls.equipeBId.value!)) {
    this.mensagem = 'Escolha duas equipes com inscrições APROVADAS nesta competição.'; return;
  }
  if (this.formulario.controls.equipeAId.value === this.formulario.controls.equipeBId.value) {
    this.mensagem = 'Uma equipe não pode jogar contra ela mesma.'; return;
  }
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
    this.formulario.patchValue(dados, {emitEvent:false});
    this.carregarAprovadas(dados.torneioId, false);
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
  this.idsAprovadas = [];
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
