/*
 * Copyright (C) 2017/2021 e-voyageurs technologies
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
import { SharedModule } from '../shared-nlp/shared.module';
import { CommonModule } from '@angular/common';
import { ApplicationsModule } from '../applications/applications.module';

import { MomentModule } from 'ngx-moment';
import { TestIntentErrorComponent } from '../test-nlp/test-intent-error.component';
import { TestEntityErrorComponent } from '../test-nlp/test-entity-error.component';
import { TestBuildsComponent } from '../test-nlp/test-builds.component';
import { QualityTabsComponent } from './quality-tabs.component';
import { QualityService } from './quality.service';
import { LogStatsComponent } from '../logs/log-stats.component';
import { ModelBuildsComponent } from '../build/model-builds.component';
import { IntentQAComponent } from '../intents/quality/intent-qa.component';
import {
  NbThemeModule,
  NbRouteTabsetModule,
  NbCardModule,
  NbButtonModule,
  NbSelectModule,
  NbTooltipModule,
  NbSpinnerModule,
  NbCheckboxModule
} from '@nebular/theme';
import { ThemeModule } from '../theme/theme.module';
import { NgxEchartsModule } from 'ngx-echarts';
import { NgbModule } from '@ng-bootstrap/ng-bootstrap';
import { QualityRoutingModule } from './quality-routing.module';
import { LogCountComponent } from '../logs/log-count.component';

export function importEcharts() {
  return import('echarts');
}

@NgModule({
  imports: [
    CommonModule,
    SharedModule,
    QualityRoutingModule,
    ApplicationsModule,
    MomentModule,
    NbThemeModule,
    ThemeModule,
    NbRouteTabsetModule,
    NbCardModule,
    NbCheckboxModule,
    NbButtonModule,
    NbSelectModule,
    NgxEchartsModule.forRoot({
      echarts: importEcharts
    }),
    NbTooltipModule,
    NgbModule,
    NbSpinnerModule
  ],
  declarations: [
    QualityTabsComponent,
    TestIntentErrorComponent,
    TestEntityErrorComponent,
    TestBuildsComponent,
    LogStatsComponent,
    LogCountComponent,
    ModelBuildsComponent,
    IntentQAComponent
  ],
  exports: [],
  providers: [QualityService]
})
export class QualityModule {}
