/*
 * Copyright (C) 2017/2025 SNCF Connect & Tech
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

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
