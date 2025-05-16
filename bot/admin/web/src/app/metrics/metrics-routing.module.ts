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
import { ApplicationResolver } from '../core-nlp/application.resolver';
import { AuthGuard } from '../core-nlp/auth/auth.guard';
import { IndicatorsComponent } from './indicators/indicators.component';
import { MetricsBoardComponent } from './metrics-board/metrics-board.component';
import { MetricsTabsComponent } from './metrics-tabs.component';

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
