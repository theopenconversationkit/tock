import { NgModule } from '@angular/core';
import { RouterModule, Routes } from '@angular/router';

import { ApplicationResolver } from '../core-nlp/application.resolver';
import { AuthGuard } from '../core-nlp/auth/auth.guard';
import { ScenariosListComponent } from './scenarios-list/scenarios-list.component';
import {
  ScenarioEditorNavigationGuard,
  ScenariosEditComponent
} from './scenarios-edit/scenarios-edit.component';

const routes: Routes = [
  {
    path: '',
    canActivate: [AuthGuard],
    resolve: {
      application: ApplicationResolver
    },
    children: [
      {
        path: '',
        redirectTo: '',
        pathMatch: 'full',
        component: ScenariosListComponent
      },
      {
        path: ':id',
        component: ScenariosEditComponent,
        canDeactivate: [ScenarioEditorNavigationGuard]
      }
    ]
  }
];

@NgModule({
  imports: [RouterModule.forChild(routes)],
  exports: [RouterModule],
  declarations: []
})
export class ScenariosRoutingModule {}
