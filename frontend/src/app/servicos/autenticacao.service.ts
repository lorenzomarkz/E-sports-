import { Injectable, inject } from '@angular/core';
import { HttpClient, HttpHeaders } from '@angular/common/http';
import { BehaviorSubject, tap } from 'rxjs';
import { API_URL } from './api';

@Injectable({ providedIn: 'root' })
export class AutenticacaoService {
  private http = inject(HttpClient);
  private autorizacao = '';
  readonly usuario$ = new BehaviorSubject<string>('');
  get usuario(): string { return this.usuario$.value; }
  get organizador(): boolean { return this.usuario === 'ORGANIZADOR'; }
  get cabecalho(): string { return this.autorizacao; }

  entrar(usuario: string, senha: string) {
    const bytes = new TextEncoder().encode(usuario + ':' + senha);
    const basic = 'Basic ' + btoa(Array.from(bytes, byte => String.fromCharCode(byte)).join(''));
    // A leitura protegida valida as credenciais sem alterar dados.
    return this.http.get(API_URL + '/equipes', {
      headers: new HttpHeaders({ Authorization: basic })
    }).pipe(tap(() => {
      this.autorizacao = basic;
      this.usuario$.next(usuario);
    }));
  }

  sair(): void {
    this.autorizacao = '';
    this.usuario$.next('');
  }
}
