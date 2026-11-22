import { AutenticacaoService } from '../../servicos/autenticacao.service';
import { Component, inject, ChangeDetectorRef, DestroyRef } from '@angular/core';
import { FormControl } from '@angular/forms';
import { takeUntilDestroyed } from '@angular/core/rxjs-interop';
import { switchMap, of, catchError, tap } from 'rxjs';
import { ActivatedRoute } from '@angular/router';
import { TorneiosService, Registro } from '../../servicos/torneios.service';
import { ClassificacaoService, Classificacao } from '../../servicos/classificacao.service';
import { mensagemErro } from '../../servicos/api';
@Component({selector:'app-classificacao',standalone:false,templateUrl:'./classificacao.component.html'})
export class ClassificacaoComponent {
 readonly auth=inject(AutenticacaoService);
 get competicao():Registro|undefined { return this.torneios.find(t=>t.id===this.selecao.value); }
 private api=inject(ClassificacaoService);
 private torneiosApi=inject(TorneiosService);
 private cdr=inject(ChangeDetectorRef);
 private route=inject(ActivatedRoute);
 private destroyRef=inject(DestroyRef);
 torneios:Registro[]=[];
 selecao=new FormControl<number|null>(null);
 dados:Classificacao|null=null;
 carregando=false;
 mensagem='';
 colunas=['equipe','jogos','vitorias','empates','derrotas','pontos','saldo'];
 colunasJogos=['data','equipes','resultado','status','local'];
 constructor() {
  this.selecao.valueChanges.pipe(
   tap(()=>{this.dados=null;this.mensagem='';this.carregando=true;}),
   switchMap(id=> id===null ? of(null) : this.api.consultar(id).pipe(catchError(erro=>{this.mensagem=mensagemErro(erro);return of(null);}))),
   takeUntilDestroyed()
  ).subscribe(dados=>{this.dados=dados;this.carregando=false;this.cdr.markForCheck();});
  this.torneiosApi.listar().pipe(takeUntilDestroyed()).subscribe({
   next:dados=>{this.torneios=dados;const id=Number(this.route.snapshot.queryParamMap.get('torneioId'));if(dados.some(t=>t.id===id))this.selecao.setValue(id);this.cdr.markForCheck();},
   error:erro=>{this.mensagem=mensagemErro(erro);this.cdr.markForCheck();}
  });
 }
 atualizar():void { this.selecao.setValue(this.selecao.value); }
}