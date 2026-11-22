import { EscudoComponent } from './escudo.component';
import { ClassificacaoComponent } from './paginas/classificacao/classificacao.component';
import { EstatisticasComponent } from './paginas/partidas/estatisticas.component';
import { provideHttpClient, withInterceptors } from '@angular/common/http';
import { autenticacaoInterceptor } from './servicos/autenticacao.interceptor';
import { NgModule, provideBrowserGlobalErrorListeners } from '@angular/core';
import { BrowserModule } from '@angular/platform-browser';
import { ReactiveFormsModule } from '@angular/forms';
import { MatToolbarModule } from '@angular/material/toolbar';
import { MatSidenavModule } from '@angular/material/sidenav';
import { MatCardModule } from '@angular/material/card';
import { MatTableModule } from '@angular/material/table';
import { MatFormFieldModule } from '@angular/material/form-field';
import { MatInputModule } from '@angular/material/input';
import { MatSelectModule } from '@angular/material/select';
import { MatButtonModule } from '@angular/material/button';
import { AppRoutingModule } from './app-routing.module';
import { App } from './app.component';
import { DashboardComponent } from './paginas/dashboard/dashboard.component';
import { ModalidadesComponent } from './paginas/modalidades/modalidades.component';
import { TorneiosComponent } from './paginas/torneios/torneios.component';
import { EquipesComponent } from './paginas/equipes/equipes.component';
import { AtletasComponent } from './paginas/atletas/atletas.component';
import { InscricoesComponent } from './paginas/inscricoes/inscricoes.component';
import { PartidasComponent } from './paginas/partidas/partidas.component';
@NgModule({
 declarations: [App, EscudoComponent, ClassificacaoComponent, EstatisticasComponent, DashboardComponent, ModalidadesComponent, TorneiosComponent, EquipesComponent, AtletasComponent, InscricoesComponent, PartidasComponent],
 imports: [BrowserModule, AppRoutingModule, ReactiveFormsModule, MatToolbarModule, MatSidenavModule, MatCardModule, MatTableModule, MatFormFieldModule, MatInputModule, MatSelectModule, MatButtonModule],
 providers: [provideBrowserGlobalErrorListeners(), provideHttpClient(withInterceptors([autenticacaoInterceptor]))],
 bootstrap: [App]
})
export class AppModule {}
