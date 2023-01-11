import { NgModule } from '@angular/core';
import { RouterModule, Routes } from '@angular/router';

import { ScenariosResolver } from './scenarios.resolver';
import { AuthGuard } from '../core-nlp/auth/auth.guard';
import { ScenariosListComponent } from './scenarios-list/scenarios-list.component';
import { ScenarioDesignerNavigationGuard, ScenarioDesignerComponent } from './scenario-designer/scenario-designer.component';
import { CurrentApplicationAndConfigurationsGuard } from '../shared/guards';

const routes: Routes = [
  {
    path: '',
    canActivate: [AuthGuard, CurrentApplicationAndConfigurationsGuard],
    resolve: {
      scenariosGroups: ScenariosResolver
    },
    children: [
      {
        path: '',
        redirectTo: '',
        pathMatch: 'full',
        component: ScenariosListComponent
      },
      {
        path: ':scenarioGroupId/:scenarioVersionId',
        component: ScenarioDesignerComponent,
        canDeactivate: [ScenarioDesignerNavigationGuard]
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
