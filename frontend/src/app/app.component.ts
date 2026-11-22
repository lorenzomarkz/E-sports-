import { takeUntilDestroyed } from '@angular/core/rxjs-interop';
import { FormControl, FormGroup, Validators } from '@angular/forms';
import { AutenticacaoService } from './servicos/autenticacao.service';
import { mensagemErro } from './servicos/api';
import { Component, HostListener, inject, ChangeDetectorRef } from '@angular/core';
import { Router } from '@angular/router';
@Component({ selector: 'app-root', standalone: false, templateUrl: './app.component.html', styleUrl: './app.component.css' })
export class App {
 private router = inject(Router);
 readonly auth = inject(AutenticacaoService);
 private cdr = inject(ChangeDetectorRef);
 mostrarLogin = false;
 entrando = false;
 erroLogin = '';
 login = new FormGroup({
  usuario: new FormControl('', {nonNullable:true, validators:[Validators.required]}),
  senha: new FormControl('', {nonNullable:true, validators:[Validators.required]})
 });

 entrar(): void {
  if (this.login.invalid || this.entrando) { this.login.markAllAsTouched(); return; }
  this.entrando = true;
  this.erroLogin = '';
  const dados = this.login.getRawValue();
  this.auth.entrar(dados.usuario.trim(), dados.senha).subscribe({
   next: () => { this.login.reset(); this.mostrarLogin = false; this.entrando = false; this.cdr.markForCheck(); },
   error: erro => { this.login.controls.senha.reset(); this.erroLogin = mensagemErro(erro); this.entrando = false; this.cdr.markForCheck(); }
  });
 }

 fecharLogin(): void { this.mostrarLogin = false; this.login.reset(); this.erroLogin = ''; }

 celular = window.innerWidth < 900;
 menuAberto = !this.celular;
 private todosLinks = [{rota:"/classificacao",titulo:"Classificação pública",simbolo:"▤"},{"rota":"/dashboard","titulo":"Dashboard","simbolo":"▦"},{"rota":"/modalidades","titulo":"Modalidades","simbolo":"◈"},{"rota":"/torneios","titulo":"Torneios","simbolo":"⚑"},{"rota":"/equipes","titulo":"Equipes","simbolo":"◉"},{"rota":"/atletas","titulo":"Atletas","simbolo":"◎"},{"rota":"/inscricoes","titulo":"Inscrições","simbolo":"☑"},{"rota":"/partidas","titulo":"Partidas","simbolo":"▤"}];
 get links() {
  if (this.auth.usuario === 'CONSULTA') return [
   {rota:'/dashboard',titulo:'Dashboard',simbolo:''},
   {rota:'/competicoes',titulo:'Competições',simbolo:''},
   {rota:'/equipes',titulo:'Equipes',simbolo:''},
   {rota:'/partidas',titulo:'Partidas/Resultados',simbolo:''}
  ];
  return this.todosLinks.filter(link => this.auth.organizador || !['/modalidades','/atletas','/inscricoes'].includes(link.rota)); }
 constructor() {
  this.auth.usuario$.pipe(takeUntilDestroyed()).subscribe(() => {
   if (!this.auth.organizador && ['/modalidades','/atletas','/inscricoes'].includes(this.router.url.split('?')[0])) {
    void this.router.navigateByUrl('/dashboard');
   }
   if (this.auth.usuario === 'CONSULTA' && this.router.url.split('?')[0] === '/torneios') void this.router.navigateByUrl('/competicoes');
   this.cdr.markForCheck();
  });
 }
 get titulo(): string { if (this.router.url.startsWith('/competicoes') || (this.auth.usuario === 'CONSULTA' && this.router.url.startsWith('/classificacao'))) return 'Competições'; return this.links.find(link => this.router.url.split('?')[0] === link.rota)?.titulo ?? 'Dashboard'; }
 @HostListener('window:resize')
 ajustarMenu(): void {
   const celular = window.innerWidth < 900;
   if (celular !== this.celular) { this.celular = celular; this.menuAberto = !celular; }
 }
 navegar(): void { if (this.celular) this.menuAberto = false; }
}
