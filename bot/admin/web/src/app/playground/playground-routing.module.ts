import { NgModule } from '@angular/core';
import { RouterModule, Routes } from '@angular/router';
import { AuthGuard } from '../core-nlp/auth/auth.guard';
import { ApplicationResolver } from '../core-nlp/application.resolver';
import { PlaygroundComponent } from './playground.component';

const routes: Routes = [
  {
    path: '',
    canActivate: [AuthGuard],
    component: PlaygroundComponent,
    resolve: {
      application: ApplicationResolver
    }
  }
];

@NgModule({
  imports: [RouterModule.forChild(routes)],
  exports: [RouterModule]
})
export class PlaygroundRoutingModule {}
