import { NgModule } from '@angular/core';
import { RouterModule, Routes } from '@angular/router';
import { AuthGuard } from '../core-nlp/auth/auth.guard';
import { ApplicationResolver } from '../core-nlp/application.resolver';
import { ModelQualityTabsComponent } from './model-quality-tabs.component';
import { LogStatsComponent } from './log-stats/log-stats.component';
import { TestBuildsComponent } from './test-builds/test-builds.component';
import { TestIntentErrorsComponent } from './test-intent-errors/test-intent-errors.component';
import { TestEntityErrorsComponent } from './test-entity-errors/test-entity-errors.component';
import { ModelBuildsComponent } from './model-builds/model-builds.component';
import { IntentQualityComponent } from './intent-quality/intent-quality.component';
import { CountStatsComponent } from './count-stats/count-stats.component';

const routes: Routes = [
  {
    path: '',
    canActivate: [AuthGuard],
    component: ModelQualityTabsComponent,
    resolve: {
      application: ApplicationResolver
    },
    children: [
      {
        path: '',
        redirectTo: 'test-builds',
        pathMatch: 'full'
      },
      {
        path: 'log-stats',
        component: LogStatsComponent
      },
      {
        path: 'test-builds',
        component: TestBuildsComponent
      },
      {
        path: 'test-intent-errors',
        component: TestIntentErrorsComponent
      },
      {
        path: 'test-entity-errors',
        component: TestEntityErrorsComponent
      },
      {
        path: 'model-builds',
        component: ModelBuildsComponent
      },
      {
        path: 'intent-quality',
        component: IntentQualityComponent
      },
      {
        path: 'count-stats',
        component: CountStatsComponent
      }
    ]
  }
];

@NgModule({
  imports: [RouterModule.forChild(routes)],
  exports: [RouterModule]
})
export class ModelQualityRoutingModule {}
