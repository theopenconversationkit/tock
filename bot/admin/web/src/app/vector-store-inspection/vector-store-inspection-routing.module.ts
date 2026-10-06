import { NgModule } from '@angular/core';
import { RouterModule, Routes } from '@angular/router';

import { DiagnosticComponent } from './diagnostic/diagnostic.component';
import { ExplorationComponent } from './exploration/exploration.component';
import { AuthGuard } from '../core-nlp/auth/auth.guard';
import { ApplicationResolver } from '../core-nlp/application.resolver';
import { VectorStoreInspectionTabsComponent } from './vector-store-inspection-tabs.component';

const routes: Routes = [
  {
    path: '',
    canActivate: [AuthGuard],
    component: VectorStoreInspectionTabsComponent,
    resolve: {
      application: ApplicationResolver
    },
    children: [
      { path: '', redirectTo: 'exploration', pathMatch: 'full' },
      { path: 'exploration', component: ExplorationComponent },
      { path: 'diagnostic', component: DiagnosticComponent }
    ]
  }
];

@NgModule({
  imports: [RouterModule.forChild(routes)],
  exports: [RouterModule]
})
export class VectorStoreInspectionRoutingModule {}
