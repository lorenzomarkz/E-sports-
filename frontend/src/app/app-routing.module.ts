import { inject } from '@angular/core';
import { Router } from '@angular/router';
import { AutenticacaoService } from './servicos/autenticacao.service';
import { somenteOrganizador } from './servicos/organizador.guard';
import { ClassificacaoComponent } from './paginas/classificacao/classificacao.component';
import { NgModule } from '@angular/core';
import { RouterModule, Routes } from '@angular/router';
import { DashboardComponent } from './paginas/dashboard/dashboard.component';
import { ModalidadesComponent } from './paginas/modalidades/modalidades.component';
import { TorneiosComponent } from './paginas/torneios/torneios.component';
import { EquipesComponent } from './paginas/equipes/equipes.component';
import { AtletasComponent } from './paginas/atletas/atletas.component';
import { InscricoesComponent } from './paginas/inscricoes/inscricoes.component';
import { PartidasComponent } from './paginas/partidas/partidas.component';
export const routes: Routes = [
 { path: '', redirectTo: 'dashboard', pathMatch: 'full' },
 { path: 'dashboard', component: DashboardComponent },
{ path: 'modalidades', canActivate: [somenteOrganizador], component: ModalidadesComponent },
{ path: 'torneios', canActivate: [() => inject(AutenticacaoService).usuario !== 'CONSULTA' || inject(Router).createUrlTree(['/competicoes'])], component: TorneiosComponent },
{ path: 'equipes', component: EquipesComponent },
{ path: 'atletas', canActivate: [somenteOrganizador], component: AtletasComponent },
{ path: 'inscricoes', canActivate: [somenteOrganizador], component: InscricoesComponent },
{ path: 'partidas', component: PartidasComponent },
 { path: 'competicoes', component: ClassificacaoComponent },
 { path: 'classificacao', component: ClassificacaoComponent },
 { path: '**', redirectTo: 'dashboard' }
];
@NgModule({ imports: [RouterModule.forRoot(routes)], exports: [RouterModule] })
export class AppRoutingModule {}
