import { NgModule } from '@angular/core';
import { RouterModule, Routes } from '@angular/router';
import { ApplicationResolver } from '../core-nlp/application.resolver';
import { AuthGuard } from '../core-nlp/auth/auth.guard';
import { IndicatorsComponent } from './indicators/indicators.component';
import { MetricsBoardComponent } from './metrics-board/metrics-board.component';
import { MetricsTabsComponent } from './metrics-tabs/metrics-tabs.component';

const routes: Routes = [
  {
    path: '',
    canActivate: [AuthGuard],
    component: MetricsTabsComponent,
    resolve: {
      application: ApplicationResolver
    },
    children: [
      {
        path: '',
        component: MetricsBoardComponent
      },
      {
        path: 'board',
        component: MetricsBoardComponent
      },
      {
        path: 'indicators',
        component: IndicatorsComponent
      }
    ]
  }
];

@NgModule({
  imports: [RouterModule.forChild(routes)],
  exports: [RouterModule],
  declarations: []
})
export class MetricsRoutingModule {}
