import { NgModule } from '@angular/core';
import { RouterModule, Routes } from '@angular/router';
import { ApplicationResolver } from '../core-nlp/application.resolver';
import { AuthGuard } from '../core-nlp/auth/auth.guard';
import { FaqManagementComponent } from './faq-management/faq-management.component';
import { FaqTabsComponent } from './faq-tabs/faq-tabs.component';
import { FaqTrainingComponent } from './faq-training/faq-training.component';

const routes: Routes = [
  {
    path: '',
    canActivate: [AuthGuard],
    component: FaqTabsComponent,
    resolve: {
      application: ApplicationResolver
    },
    children: [
      {
        path: 'training',
        component: FaqTrainingComponent
      },
      {
        path: 'management',
        component: FaqManagementComponent
      }
    ]
  }
];

@NgModule({
  imports: [RouterModule.forChild(routes)],
  exports: [RouterModule],
  declarations: []
})
export class FaqRoutingModule {}
