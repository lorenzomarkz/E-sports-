import { Injectable, inject } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { API_URL } from './api';
export interface Registro {
  id: number;
  nome: string;
  descricao: string;
}

@Injectable({providedIn:'root'})
export class ModalidadesService {
 private http = inject(HttpClient);
 private url = API_URL + '/modalidades';
 listar() { return this.http.get<Registro[]>(this.url); }
 buscarPorId(id:number) { return this.http.get<Registro>(this.url + '/' + id); }
 cadastrar(dados:Omit<Registro,'id'>) { return this.http.post<Registro>(this.url,dados); }
 atualizar(id:number,dados:Omit<Registro,'id'>) { return this.http.put<Registro>(this.url + '/' + id,dados); }
 excluir(id:number) { return this.http.delete<void>(this.url + '/' + id); }
}
