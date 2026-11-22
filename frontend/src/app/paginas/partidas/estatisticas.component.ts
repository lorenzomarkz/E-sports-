import { Component, Input, OnChanges, inject, ChangeDetectorRef, DestroyRef } from '@angular/core';
import { FormControl, FormGroup, Validators } from '@angular/forms';
import { finalize } from 'rxjs';
import { takeUntilDestroyed } from '@angular/core/rxjs-interop';
import { Registro as Partida } from '../../servicos/partidas.service';
import { Registro as Torneio } from '../../servicos/torneios.service';
import { ParticipacoesAtletasService } from '../../servicos/participacoes-atletas.service';
import { EstatisticasService, Estatistica } from '../../servicos/estatisticas.service';
import { AutenticacaoService } from '../../servicos/autenticacao.service';
import { mensagemErro } from '../../servicos/api';
import { normalizar } from '../../servicos/modalidades-esportivas';

@Component({selector:'app-estatisticas',standalone:false,templateUrl:'./estatisticas.component.html'})
export class EstatisticasComponent implements OnChanges {
 @Input({required:true}) partida!: Partida;
 @Input() torneio?: Torneio;
 readonly auth=inject(AutenticacaoService);
 private api=inject(EstatisticasService);
 private participacoesApi=inject(ParticipacoesAtletasService);
 private cdr=inject(ChangeDetectorRef);
 private destroyRef=inject(DestroyRef);
 atletas: {id:number;nome:string}[]=[];
 registros: Estatistica[]=[];
 ocupado=false;
 mensagem='';
 get permiteAssistencias(): boolean { return !!this.tipo && normalizar(this.torneio?.modalidadeNome ?? '') !== 'volei'; }
 get colunas(): string[] {
  const campos=['atleta'];
  if(this.tipo==='gols')campos.push('gols');
  if(this.tipo==='pontos')campos.push('pontos');
  if(this.tipo==='jogos')campos.push('abates');
  if(this.permiteAssistencias)campos.push('assistencias');
  if(this.auth.organizador)campos.push('acoes');
  return campos;
 }
 formulario=new FormGroup({
  atletaId:new FormControl<number|null>(null,Validators.required),
  gols:new FormControl<number|null>(null,[Validators.min(0),Validators.pattern(/^\d+$/)]),
  pontos:new FormControl<number|null>(null,[Validators.min(0),Validators.pattern(/^\d+$/)]),
  abates:new FormControl<number|null>(null,[Validators.min(0),Validators.pattern(/^\d+$/)]),
  assistencias:new FormControl<number|null>(null,[Validators.min(0),Validators.pattern(/^\d+$/)])
 });
 get tipo(): string {
  const modalidade=normalizar(this.torneio?.modalidadeNome ?? '');
  if (this.torneio?.jogo==='Free Fire') return '';
  if (this.torneio?.jogo==='Rocket League') return 'gols';
  if (modalidade==='e-sports') return 'jogos';
  if (['basquete','volei'].includes(modalidade)) return 'pontos';
  if (['futebol','futsal','handebol'].includes(modalidade)) return 'gols';
  return '';
 }
 ngOnChanges(): void {
  this.formulario.reset(); this.mensagem=''; this.registros=[]; this.atletas=[];
  const id=this.partida.id;
  this.carregar();
  this.participacoesApi.porPartida(id).pipe(takeUntilDestroyed(this.destroyRef)).subscribe({
   next: dados=>{ if(this.partida.id!==id)return; this.atletas=dados.map(p=>({id:p.atletaId,nome:p.atletaNome}));this.cdr.markForCheck(); },
   error: erro=>{this.mensagem=mensagemErro(erro);this.cdr.markForCheck();}
  });
 }
 carregar(): void {
  const id=this.partida.id;
  this.api.listar(id).pipe(takeUntilDestroyed(this.destroyRef)).subscribe({
   next:dados=>{if(this.partida.id!==id)return;this.registros=dados;this.cdr.markForCheck();},
   error:erro=>{this.mensagem=mensagemErro(erro);this.cdr.markForCheck();}
  });
 }
 nome(id:number):string {return this.atletas.find(a=>a.id===id)?.nome ?? '#'+id;}
 salvar():void {
  if(!this.auth.organizador||this.ocupado||!this.tipo)return;
  const f=this.formulario.getRawValue();
  if(this.formulario.invalid || (this.tipo==='gols'&&f.gols===null) || (this.tipo==='pontos'&&f.pontos===null)
     || (this.tipo==='jogos'&&f.abates===null&&f.assistencias===null)){
    this.formulario.markAllAsTouched();this.mensagem='Selecione o atleta e informe estatísticas inteiras não negativas.';return;
  }
  this.ocupado=true;
  this.api.cadastrar({atletaId:f.atletaId!,partidaId:this.partida.id,
    gols:this.tipo==='gols'?f.gols:null,pontos:this.tipo==='pontos'?f.pontos:null,
    abates:this.tipo==='jogos'?f.abates:null,assistencias:this.permiteAssistencias?f.assistencias:null})
   .pipe(takeUntilDestroyed(this.destroyRef),finalize(()=>{this.ocupado=false;this.cdr.markForCheck();})).subscribe({
    next:()=>{this.mensagem='Estatística cadastrada.';this.formulario.reset();this.carregar();},
    error:erro=>{this.mensagem=mensagemErro(erro);}
   });
 }
 excluir(registro:Estatistica):void {
  if(!this.auth.organizador||this.ocupado||!window.confirm('Excluir esta estatística?'))return;
  this.ocupado=true;
  this.api.excluir(registro.id).pipe(takeUntilDestroyed(this.destroyRef),finalize(()=>{this.ocupado=false;this.cdr.markForCheck();})).subscribe({
   next:()=>{this.mensagem='Estatística excluída.';this.carregar();},error:erro=>this.mensagem=mensagemErro(erro)
  });
 }
}