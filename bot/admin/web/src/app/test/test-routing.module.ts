import { NgModule } from '@angular/core';
import { RouterModule, Routes } from '@angular/router';
import { ApplicationResolver } from '../core-nlp/application.resolver';
import { AuthGuard } from '../core-nlp/auth/auth.guard';
import { BotDialogComponent } from './dialog/bot-dialog.component';
import { TestPlanComponent } from './plan/test-plan.component';
import { TestTabsComponent } from './test-tabs.component';

const routes: Routes = [
  {
    path: '',
    canActivate: [AuthGuard],
    component: TestTabsComponent,
    resolve: {
      application: ApplicationResolver
    },
    children: [
      {
        path: '',
        redirectTo: 'test',
        pathMatch: 'full'
      },
      {
        path: 'test',
        component: BotDialogComponent
      },
      {
        path: 'plan',
        component: TestPlanComponent
      }
    ]
  }
];
@NgModule({
  imports: [RouterModule.forChild(routes)],
  exports: [RouterModule]
})
export class BotTestRoutingModule {}
