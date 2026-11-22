import { Injectable, inject } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { API_URL } from './api';
export interface ParticipacaoAtleta {
 id:number; atletaId:number; equipeId:number; torneioId:number;
 atletaNome:string; equipeNome:string; torneioNome:string;
}
@Injectable({providedIn:'root'})
export class ParticipacoesAtletasService {
 private http=inject(HttpClient);
 private url=API_URL+'/participacoes-atletas';
 listar() { return this.http.get<ParticipacaoAtleta[]>(this.url); }
 porPartida(partidaId:number) { return this.http.get<ParticipacaoAtleta[]>(this.url,{params:{partidaId}}); }
 cadastrar(dados:{atletaId:number;equipeId:number;torneioId:number}) { return this.http.post<ParticipacaoAtleta>(this.url,dados); }
 excluir(id:number) { return this.http.delete<void>(this.url+'/'+id); }
}
