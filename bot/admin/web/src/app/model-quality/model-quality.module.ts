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
