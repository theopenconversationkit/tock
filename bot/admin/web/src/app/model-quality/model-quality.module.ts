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
import { CommonModule } from '@angular/common';

import { ModelQualityRoutingModule } from './model-quality-routing.module';
import { ModelQualityTabsComponent } from './model-quality-tabs.component';
import {
  NbButtonModule,
  NbCardModule,
  NbCheckboxModule,
  NbDatepickerModule,
  NbFormFieldModule,
  NbIconModule,
  NbInputModule,
  NbRouteTabsetModule,
  NbSelectModule,
  NbSpinnerModule,
  NbToggleModule,
  NbTooltipModule
} from '@nebular/theme';
import { LogStatsComponent } from './log-stats/log-stats.component';
import { FormsModule, ReactiveFormsModule } from '@angular/forms';
import { QualityService } from './quality.service';
import { NgxEchartsModule } from 'ngx-echarts';
import { BotSharedModule } from '../shared/bot-shared.module';
import { TestBuildsComponent } from './test-builds/test-builds.component';
import { TestIntentErrorsComponent } from './test-intent-errors/test-intent-errors.component';
import { MomentModule } from 'ngx-moment';
import { TestEntityErrorsComponent } from './test-entity-errors/test-entity-errors.component';

import { ModelBuildsComponent } from './model-builds/model-builds.component';
import { IntentQualityComponent } from './intent-quality/intent-quality.component';
import { CountStatsComponent } from './count-stats/count-stats.component';

@NgModule({
  declarations: [
    ModelQualityTabsComponent,
    LogStatsComponent,
    TestBuildsComponent,
    TestIntentErrorsComponent,
    TestEntityErrorsComponent,
    ModelBuildsComponent,
    IntentQualityComponent,
    CountStatsComponent
  ],
  imports: [
    CommonModule,
    FormsModule,
    ReactiveFormsModule,
    BotSharedModule,
    ModelQualityRoutingModule,
    NbRouteTabsetModule,
    NbCardModule,
    NbInputModule,
    NbSelectModule,
    NbFormFieldModule,
    NbToggleModule,
    NbSpinnerModule,
    NbDatepickerModule,
    NbIconModule,
    NbButtonModule,
    NbTooltipModule,
    NbCheckboxModule,
    MomentModule,
    NgxEchartsModule.forRoot({
      echarts: () => import('echarts')
    })
  ],
  providers: [QualityService]
})
export class ModelQualityModule {}
