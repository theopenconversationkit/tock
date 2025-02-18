import { NgModule } from '@angular/core';
import { RouterModule, Routes } from '@angular/router';
import { ApplicationResolver } from '../core-nlp/application.resolver';
import { AuthGuard } from '../core-nlp/auth/auth.guard';
import { RagExcludedComponent } from './rag-excluded/rag-excluded.component';
import { RagSettingsComponent } from './rag-settings/rag-settings.component';
import { RagTabsComponent } from './rag-tabs.component';

const routes: Routes = [
  {
    path: '',
    canActivate: [AuthGuard],
    component: RagTabsComponent,
    resolve: {
      application: ApplicationResolver
    },
    children: [
      {
        path: 'exclusions',
        component: RagExcludedComponent
      },
      {
        path: 'settings',
        component: RagSettingsComponent
      }
    ]
  }
];
@NgModule({
  imports: [RouterModule.forChild(routes)],
  exports: [RouterModule],
  declarations: []
})
export class RagRoutingModule {}
