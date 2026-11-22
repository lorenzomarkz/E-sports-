import { Injectable, inject } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { API_URL } from './api';
export interface Registro {
  id: number;
  nome: string;
  dataNascimento: string;
  equipeId: number | null;
}

export interface DesempenhoAtleta {
 atletaId:number; atletaNome:string; competicoes:DesempenhoCompeticao[];
}
export interface DesempenhoCompeticao {
 torneioId:number; torneioNome:string; modalidade:string; jogo:string|null; equipes:string[];
 partidasComEstatisticas:number; totais:Record<string,number|null>;
 historico:{partidaId:number;dataHora:string|null;confronto:string;equipe:string;legado:boolean;valores:Record<string,number|null>}[];
}

@Injectable({providedIn:'root'})
export class AtletasService {
 private http = inject(HttpClient);
 private url = API_URL + '/atletas';
 listar() { return this.http.get<Registro[]>(this.url); }
 desempenho(id:number) { return this.http.get<DesempenhoAtleta>(this.url+'/'+id+'/desempenho'); }
 buscarPorId(id:number) { return this.http.get<Registro>(this.url + '/' + id); }
 cadastrar(dados:Omit<Registro,'id'>) { return this.http.post<Registro>(this.url,dados); }
 atualizar(id:number,dados:Omit<Registro,'id'>) { return this.http.put<Registro>(this.url + '/' + id,dados); }
 excluir(id:number) { return this.http.delete<void>(this.url + '/' + id); }
}
