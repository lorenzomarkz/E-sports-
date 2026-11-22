import { Injectable, inject } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { API_URL } from './api';
export interface Estatistica {
 participacaoId?:number|null; atletaNome?:string;
 id: number; atletaId: number; partidaId: number;
 gols: number | null; pontos: number | null; abates: number | null; assistencias: number | null;
}
@Injectable({providedIn:'root'})
export class EstatisticasService {
 private http = inject(HttpClient);
 listar(partidaId:number) { return this.http.get<Estatistica[]>(API_URL + '/estatisticas', {params:{partidaId}}); }
 cadastrar(dados:Omit<Estatistica,'id'>) { return this.http.post<Estatistica>(API_URL + '/estatisticas',dados); }
 excluir(id:number) { return this.http.delete<void>(API_URL + '/estatisticas/' + id); }
}