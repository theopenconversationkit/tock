import { NgModule } from '@angular/core';
import { CommonModule } from '@angular/common';
import { MetricsBoardComponent } from './metrics-board/metrics-board.component';
import { MetricsRoutingModule } from './metrics-routing.module';
import { BotSharedModule } from '../shared/bot-shared.module';
import { MetricsTabsComponent } from './metrics-tabs/metrics-tabs.component';
import {
  NbAccordionModule,
  NbButtonModule,
  NbCardModule,
  NbDatepickerModule,
  NbFormFieldModule,
  NbIconModule,
  NbInputModule,
  NbRouteTabsetModule,
  NbSelectModule,
  NbTooltipModule,
  NbTagModule,
  NbSpinnerModule,
  NbAutocompleteModule,
  NbAlertModule
} from '@nebular/theme';
import { IndicatorsComponent } from './indicators/indicators.component';
import { NgxEchartsModule } from 'ngx-echarts';
import { FormsModule, ReactiveFormsModule } from '@angular/forms';
import { IndicatorsEditComponent } from './indicators/indicators-edit/indicators-edit.component';
import { IndicatorsFiltersComponent } from './indicators/indicators-filters/indicators-filters.component';
import { AnalyticsService } from '../analytics/analytics.service';
import { MetricsByStoriesComponent } from './metrics-board/metrics-by-stories/metrics-by-stories.component';

@NgModule({
  declarations: [
    MetricsBoardComponent,
    MetricsTabsComponent,
    MetricsByStoriesComponent,
    IndicatorsComponent,
    IndicatorsEditComponent,
    IndicatorsFiltersComponent
  ],
  imports: [
    ReactiveFormsModule,
    CommonModule,
    FormsModule,
    BotSharedModule,
    NbRouteTabsetModule,
    NbCardModule,
    NbIconModule,
    NbButtonModule,
    NbTooltipModule,
    NbAccordionModule,
    NbInputModule,
    NbFormFieldModule,
    NbSelectModule,
    NbDatepickerModule,
    NbTagModule,
    MetricsRoutingModule,
    NbSpinnerModule,
    NbAutocompleteModule,
    NbAlertModule,
    NgxEchartsModule.forRoot({
      echarts: () => import('echarts')
    })
  ],
  providers: [AnalyticsService]
})
export class MetricsModule {}
