import { inject } from '@angular/core';
import { CanActivateFn, Router } from '@angular/router';
import { AutenticacaoService } from './autenticacao.service';

export const somenteOrganizador: CanActivateFn = () =>
  inject(AutenticacaoService).organizador || inject(Router).createUrlTree(['/dashboard']);
