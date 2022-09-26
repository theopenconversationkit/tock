import { NgModule } from '@angular/core';
import { RouterModule, Routes } from '@angular/router';
import { ApplicationResolver } from '../core-nlp/application.resolver';
import { AuthGuard } from '../core-nlp/auth/auth.guard';
import { FaqManagementComponent } from './faq-management/faq-management.component';
import { FaqTrainingComponent } from './faq-training/faq-training.component';

const routes: Routes = [
  {
    path: 'training',
    component: FaqTrainingComponent,
    canActivate: [AuthGuard],
    resolve: {
      application: ApplicationResolver
    }
  },
  {
    path: 'management',
    component: FaqManagementComponent,
    canActivate: [AuthGuard],
    resolve: {
      application: ApplicationResolver
    }
  }
];

@NgModule({
  imports: [RouterModule.forChild(routes)],
  exports: [RouterModule],
  declarations: []
})
export class FaqRoutingModule {}
