import { HttpInterceptorFn } from '@angular/common/http';
import { inject } from '@angular/core';
import { AutenticacaoService } from './autenticacao.service';
import { API_URL } from './api';

export const autenticacaoInterceptor: HttpInterceptorFn = (requisicao, proximo) => {
  const auth = inject(AutenticacaoService);
  // Nunca enviar credenciais para outros endereços.
  if (requisicao.url.startsWith(API_URL + '/') && auth.cabecalho && !requisicao.headers.has('Authorization')) {
    requisicao = requisicao.clone({ setHeaders: { Authorization: auth.cabecalho } });
  }
  return proximo(requisicao);
};
