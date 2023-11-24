import { NgModule } from '@angular/core';
import { RouterModule, Routes } from '@angular/router';
import { IntentQAComponent } from '../intents/quality/intent-qa.component';
import { ModelBuildsComponent } from '../build/model-builds.component';
import { TestEntityErrorComponent } from '../test-nlp/test-entity-error.component';
import { TestIntentErrorComponent } from '../test-nlp/test-intent-error.component';
import { TestBuildsComponent } from '../test-nlp/test-builds.component';
import { LogStatsComponent } from '../logs/log-stats.component';
import { ApplicationResolver } from '../core-nlp/application.resolver';
import { QualityTabsComponent } from './quality-tabs.component';
import { AuthGuard } from '../core-nlp/auth/auth.guard';
import {LogCountComponent} from "../logs/log-count.component";

const routes: Routes = [
  {
    path: '',
    canActivate: [AuthGuard],
    component: QualityTabsComponent,
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
        component: TestIntentErrorComponent
      },
      {
        path: 'test-entity-errors',
        component: TestEntityErrorComponent
      },
      {
        path: 'model-builds',
        component: ModelBuildsComponent
      },
      {
        path: 'intent-quality',
        component: IntentQAComponent
      },
      {
        path: 'count-stats',
        component: LogCountComponent
      }
    ]
  }
];

@NgModule({
  imports: [RouterModule.forChild(routes)],
  exports: [RouterModule]
})
export class QualityRoutingModule {}
