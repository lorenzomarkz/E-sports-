import { InscricoesService, Registro as Inscricao } from '../../servicos/inscricoes.service';
import { TorneiosService, Registro as Torneio } from '../../servicos/torneios.service';
import { ActivatedRoute, Router } from '@angular/router';
import { CORES_ESCUDO } from '../../escudo.component';
import { Component, inject, ChangeDetectorRef, DestroyRef } from '@angular/core';
import { FormControl, FormGroup, Validators } from '@angular/forms';
import { HttpErrorResponse } from '@angular/common/http';
import { takeUntilDestroyed } from '@angular/core/rxjs-interop';
import { finalize, switchMap, takeUntil, skip } from 'rxjs';
import { EquipesService, Registro } from '../../servicos/equipes.service';
import { AutenticacaoService } from '../../servicos/autenticacao.service';
import { mensagemErro } from '../../servicos/api';
import { ModalidadesService } from '../../servicos/modalidades.service';

@Component({ selector:'app-equipes', standalone:false, templateUrl:'./equipes.component.html' })
export class EquipesComponent {
 readonly auth = inject(AutenticacaoService);
 private servico = inject(EquipesService);
 private cdr = inject(ChangeDetectorRef);
 private destroyRef = inject(DestroyRef);
 private modalidadesServico = inject(ModalidadesService);
 // Mantém o código legado ao editar; novos cadastros usam o modelo padrão.
 private escudoOriginal = 'VERDE';
 readonly coresEscudo = CORES_ESCUDO;
 private inscricoesApi=inject(InscricoesService);
 private torneiosApi=inject(TorneiosService);
 private rota=inject(ActivatedRoute);
 private router=inject(Router);
 inscricoes:Inscricao[]=[];
 competicoes:Torneio[]=[];
 competicaoInscricao=new FormControl<number|null>(null);
 get competicaoSelecionada():Torneio|undefined{return this.competicoes.find(t=>t.id===this.competicaoInscricao.value);}
 estadoInscricao(equipeId:number):string {
  const registro=this.inscricoes.find(i=>i.equipeId===equipeId&&i.torneioId===this.competicaoInscricao.value);
  return registro?.status==='APROVADA'?'INSCRITO':registro?.status==='PENDENTE'?'Aguardando aprovação':registro?.status==='REJEITADA'?'Inscrição rejeitada':'Solicitar inscrição';
 }
 ocupado = false;
 carregando = false;
 falhouLista = false;
  registros: Registro[] = [];
  modalidades: { id: number; nome: string }[] = [];
  get colunas(): string[] { const campos = ["nome","cidade","nomeResponsavel","modalidadeId","acoes"]; return this.auth.organizador ? campos : campos.filter(c => c !== "acoes"); }
  idEmEdicao: number | null = null;
  consultando = false;
  mensagem = '';
  formulario = new FormGroup({
    corEscudo: new FormControl('', { nonNullable:true, validators:[Validators.required] }),
    nome: new FormControl('', { nonNullable: true, validators: [Validators.required, Validators.pattern(/\S/)] }),
    cidade: new FormControl('', { nonNullable: true, validators: [Validators.required, Validators.pattern(/\S/)] }),
    nomeResponsavel: new FormControl('', { nonNullable: true, validators: [Validators.required, Validators.pattern(/\S/)] }),
    modalidadeId: new FormControl<number | null>(null, { validators: [Validators.required] }),
  });


 constructor() {
  this.competicaoInscricao.valueChanges.pipe(takeUntilDestroyed()).subscribe(id=>{
   if(id!==null)void this.router.navigate([], {relativeTo:this.rota,queryParams:{torneioId:id},queryParamsHandling:'merge',replaceUrl:true});
  });
  this.auth.usuario$.pipe(takeUntilDestroyed()).subscribe(() => {
   this.registros = [];
   this.inscricoes=[];this.competicoes=[];
   if(this.auth.organizador){
    this.inscricoesApi.listar().pipe(takeUntilDestroyed(this.destroyRef),takeUntil(this.auth.usuario$.pipe(skip(1)))).subscribe({
     next:dados=>{this.inscricoes=dados;this.cdr.markForCheck();},error:erro=>this.falha(erro)
    });
    this.torneiosApi.listar().pipe(takeUntilDestroyed(this.destroyRef),takeUntil(this.auth.usuario$.pipe(skip(1)))).subscribe({
     next:dados=>{this.competicoes=dados;const id=Number(this.rota.snapshot.queryParamMap.get('torneioId'));this.competicaoInscricao.setValue(dados.some(t=>t.id===id)?id:dados[0]?.id??null,{emitEvent:false});this.cdr.markForCheck();},error:erro=>this.falha(erro)
    });
   }
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
  const dados = {...this.formulario.getRawValue(), escudo:this.escudoOriginal};
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
    this.escudoOriginal = dados.escudo || 'VERDE';
    this.formulario.patchValue({...dados, corEscudo:dados.corEscudo || '#7ED957'});
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
  this.escudoOriginal = 'VERDE';
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
