import { Injectable, inject } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { API_URL } from './api';
export interface IndicadoresDashboard { competicoesAtivas:number; equipesInscritas:number; partidasRealizadas:number; }
export interface Registro {
  id: number;
  modalidadeNome?: string;
  jogo?: string | null;
  nome: string;
  descricao: string;
  modalidadeId: number | null;
  dataInicio: string;
  dataFim: string;
  prazoInscricao: string;
  limiteEquipes: number | null;
  local: string;
}

@Injectable({providedIn:'root'})
export class TorneiosService {
 private http = inject(HttpClient);
 private url = API_URL + '/torneios';
 gerar(id:number,inicio:string) { return this.http.post<{id:number}[]>(API_URL + "/partidas/torneio/" + id + "/gerar",{inicio}); }
 indicadores() { return this.http.get<IndicadoresDashboard>(this.url+'/indicadores'); }
 listar() { return this.http.get<Registro[]>(this.url); }
 buscarPorId(id:number) { return this.http.get<Registro>(this.url + '/' + id); }
 cadastrar(dados:Omit<Registro,'id'>) { return this.http.post<Registro>(this.url,dados); }
 atualizar(id:number,dados:Omit<Registro,'id'>) { return this.http.put<Registro>(this.url + '/' + id,dados); }
 excluir(id:number) { return this.http.delete<void>(this.url + '/' + id); }
}
