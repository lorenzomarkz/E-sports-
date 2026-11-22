import { Injectable, inject } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { API_URL } from './api';
export interface LinhaClassificacao { escudo?:string; corEscudo?:string; equipeId:number; equipe:string; jogos:number; vitorias:number; empates:number; derrotas:number; pontos:number|null; saldo:number; }
export interface JogoPublico { equipeAEscudo?:string; equipeACorEscudo?:string; equipeBCorEscudo?:string; equipeBEscudo?:string; id:number; equipeA:string; equipeB:string; dataHora:string; local:string; status:string; placarEquipeA:number|null; placarEquipeB:number|null; }
export interface Classificacao { nome:string; modalidade:string; jogo:string|null; disponivel:boolean; regra:string; aviso:string|null; classificacao:LinhaClassificacao[]; proximos:JogoPublico[]; resultados:JogoPublico[]; }
@Injectable({providedIn:'root'})
export class ClassificacaoService {
 private http=inject(HttpClient);
 consultar(id:number) { return this.http.get<Classificacao>(API_URL+'/torneios/'+id+'/classificacao'); }
}