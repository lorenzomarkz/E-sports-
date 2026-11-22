import { FormControl } from '@angular/forms';
import { switchMap, of, catchError, merge, timer, Subject } from 'rxjs';
import { Registro as Torneio } from '../../servicos/torneios.service';
import { Registro as Partida } from '../../servicos/partidas.service';
import { Registro as Equipe } from '../../servicos/equipes.service';
import { ClassificacaoService, Classificacao } from '../../servicos/classificacao.service';
import { Component, inject, ChangeDetectorRef, DestroyRef } from '@angular/core';
import { takeUntilDestroyed } from '@angular/core/rxjs-interop';
import { takeUntil, skip } from 'rxjs';
import { TorneiosService, IndicadoresDashboard } from '../../servicos/torneios.service';
import { EquipesService } from '../../servicos/equipes.service';
import { PartidasService } from '../../servicos/partidas.service';
import { AutenticacaoService } from '../../servicos/autenticacao.service';
import { mensagemErro } from '../../servicos/api';

@Component({selector:'app-dashboard', standalone:false, templateUrl:'./dashboard.component.html', styleUrl:'./dashboard.component.css'})
export class DashboardComponent {
 readonly auth = inject(AutenticacaoService);
 private torneios = inject(TorneiosService);
 private equipes = inject(EquipesService);
 private partidas = inject(PartidasService);
 private cdr = inject(ChangeDetectorRef);
 private destroyRef = inject(DestroyRef);
 totalTorneios: number | null = null;
 totalEquipes: number | null = null;
 totalPartidas: number | null = null;
 realizadas: number | null = null;
 restantes: number | null = null;
 erroTorneios = '';
 erroEquipes = '';
 erroPartidas = '';

 competicoes: Torneio[] = [];
 jogos: Partida[] = [];
 listaEquipes: Equipe[] = [];
 agendadas: number | null = null;
 selecao = new FormControl<number | null>(null);
 classificacao: Classificacao | null = null;
 erroClassificacao = '';
 carregandoClassificacao = false;
 private classificacoes = inject(ClassificacaoService);

 get proximas(): Partida[] {
  return this.jogos.filter(p => p.status === 'AGENDADA')
   .sort((a,b) => (a.dataHora ?? '').localeCompare(b.dataHora ?? '')).slice(0,5);
 }
 get resultados(): Partida[] {
  return this.jogos.filter(p => p.status === 'FINALIZADA')
   .sort((a,b) => (b.dataHora ?? '').localeCompare(a.dataHora ?? '')).slice(0,5);
 }
 get emAndamento(): Torneio[] {
  const hoje = new Date();
  const data = hoje.getFullYear()+'-'+String(hoje.getMonth()+1).padStart(2,'0')+'-'+String(hoje.getDate()).padStart(2,'0');
  return this.competicoes.filter(t => t.dataInicio <= data && t.dataFim >= data);
 }
 nomeEquipe(id: number | null): string { return this.listaEquipes.find(e => e.id === id)?.nome ?? 'Equipe '+(id ?? 'não informada'); }
 nomeCompeticao(id: number | null): string { return this.competicoes.find(t => t.id === id)?.nome ?? 'Competição não disponível'; }

 indicadores:IndicadoresDashboard|null=null;
 erroIndicadores='';
 carregandoIndicadores=false;
 private atualizacaoIndicadores=new Subject<void>();
 atualizarIndicadores():void { this.atualizacaoIndicadores.next(); }

 constructor() {
  merge(this.auth.usuario$,timer(30000,30000),this.atualizacaoIndicadores).pipe(
   switchMap(()=>{
    this.carregandoIndicadores=true;this.erroIndicadores='';
    return this.torneios.indicadores().pipe(catchError(()=>{
     this.erroIndicadores='Não foi possível atualizar os indicadores.';return of(null);
    }));
   }),takeUntilDestroyed()
  ).subscribe(dados=>{this.indicadores=dados;this.carregandoIndicadores=false;this.cdr.markForCheck();});

  this.selecao.valueChanges.pipe(
   switchMap(id => {
    this.classificacao=null; this.erroClassificacao=''; this.carregandoClassificacao=id!==null;
    return id===null ? of(null) : this.classificacoes.consultar(id).pipe(
     catchError(erro => { this.erroClassificacao=mensagemErro(erro); return of(null); })
    );
   }),takeUntilDestroyed()
  ).subscribe(dados => {this.classificacao=dados;this.carregandoClassificacao=false;this.cdr.markForCheck();});
  this.auth.usuario$.pipe(takeUntilDestroyed()).subscribe(() => {
   this.totalTorneios = this.totalEquipes = this.totalPartidas = this.realizadas = this.restantes = null;
   this.erroTorneios = this.erroEquipes = this.erroPartidas = '';
   this.competicoes=[]; this.jogos=[]; this.listaEquipes=[]; this.agendadas=null; this.selecao.setValue(null);
   this.torneios.listar().pipe(takeUntilDestroyed(this.destroyRef),takeUntil(this.auth.usuario$.pipe(skip(1)))).subscribe({
    next: dados => { this.competicoes=dados; this.totalTorneios = dados.length; this.cdr.markForCheck(); },
    error: erro => { this.erroTorneios = mensagemErro(erro); this.cdr.markForCheck(); }
   });
   if (this.auth.usuario) {
    this.equipes.listar().pipe(takeUntilDestroyed(this.destroyRef),takeUntil(this.auth.usuario$.pipe(skip(1)))).subscribe({
     next: dados => { this.listaEquipes=dados; this.totalEquipes = dados.length; this.cdr.markForCheck(); },
     error: erro => { this.erroEquipes = mensagemErro(erro); this.cdr.markForCheck(); }
    });
   } else { this.erroEquipes = 'Entre para consultar as equipes.'; }
   this.partidas.listar().pipe(takeUntilDestroyed(this.destroyRef),takeUntil(this.auth.usuario$.pipe(skip(1)))).subscribe({
    next: dados => {
     this.jogos=dados; this.agendadas=dados.filter(p => p.status === "AGENDADA").length; this.totalPartidas = dados.length;
     this.realizadas = dados.filter(p => p.status === 'FINALIZADA').length;
     this.restantes = dados.filter(p => p.status === 'AGENDADA' || p.status === 'EM_ANDAMENTO').length;
     this.cdr.markForCheck();
    },
    error: erro => { this.erroPartidas = mensagemErro(erro); this.cdr.markForCheck(); }
   });
  });
 }
}
