import { Injectable, inject } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { API_URL } from './api';
export interface Registro {
  id: number;
  equipeANome?: string; equipeBNome?: string; equipeAEscudo?: string; equipeACorEscudo?: string; equipeBCorEscudo?: string; equipeBEscudo?: string;
  torneioId: number | null;
  equipeAId: number | null;
  equipeBId: number | null;
  dataHora: string;
  local: string;
  placarEquipeA: number | null;
  placarEquipeB: number | null;
  status: string;
}

@Injectable({providedIn:'root'})
export class PartidasService {
 private http = inject(HttpClient);
 private url = API_URL + '/partidas';
 listar() { return this.http.get<Registro[]>(this.url); }
 buscarPorId(id:number) { return this.http.get<Registro>(this.url + '/' + id); }
 cadastrar(dados:Omit<Registro,'id'>) { return this.http.post<Registro>(this.url,dados); }
 atualizar(id:number,dados:Omit<Registro,'id'>) { return this.http.put<Registro>(this.url + '/' + id,dados); }
 excluir(id:number) { return this.http.delete<void>(this.url + '/' + id); }
}
