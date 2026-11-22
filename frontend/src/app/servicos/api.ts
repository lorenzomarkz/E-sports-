import { HttpErrorResponse } from '@angular/common/http';
export const API_URL = 'http://localhost:8080';
export function mensagemErro(erro: HttpErrorResponse): string {
  if (erro.status === 0) return 'Não foi possível conectar à API. Confira se o backend está em execução.';
  if (erro.status === 401) return 'Entre com usuário e senha válidos para acessar estes dados.';
  if (erro.status === 403) return 'Seu usuário não tem permissão para esta operação.';
  return erro.error?.mensagem ?? (erro.status === 404 ? 'Registro não encontrado.' : 'Não foi possível concluir a operação.');
}
