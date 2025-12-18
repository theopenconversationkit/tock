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
import { NO_ERRORS_SCHEMA, NgModule } from '@angular/core';
import { CommonModule } from '@angular/common';
import { InfiniteScrollModule } from 'ngx-infinite-scroll';
import { AnalyticsTabsComponent } from './analytics-tabs.component';
import { UsersComponent } from './users/users.component';
import { AnalyticsService } from './analytics.service';
import { BotSharedModule } from '../shared/bot-shared.module';
import { BotModule } from '../bot/bot.module';
import { MomentModule } from 'ngx-moment';
import { DialogsComponent } from './dialogs/dialogs.component';
import {
  NbAccordionModule,
  NbButtonModule,
  NbCalendarModule,
  NbCardModule,
  NbCheckboxModule,
  NbContextMenuModule,
  NbDatepickerModule,
  NbTimepickerModule,
  NbInputModule,
  NbListModule,
  NbMenuModule,
  NbRouteTabsetModule,
  NbSelectModule,
  NbSpinnerModule,
  NbTooltipModule,
  NbUserModule,
  NbCalendarRangeModule,
  NbDialogModule,
  NbRadioModule,
  NbToggleModule,
  NbIconModule,
  NbFormFieldModule,
  NbPopoverModule
} from '@nebular/theme';
import { FormsModule, ReactiveFormsModule } from '@angular/forms';
import { ChartComponent } from './chart/chart.component';
import { ChartDialogComponent } from './chart-dialog/chart-dialog.component';
import { ActivityComponent } from './activity/activity.component';
import { BehaviorComponent } from './behavior/behavior.component';
import { FlowComponent } from './flow/flow.component';
import { PreferencesComponent } from './preferences/preferences.component';
import { NgxEchartsModule } from 'ngx-echarts';
import { SatisfactionComponent } from './satisfaction/satisfaction.component';
import { ActivateSatisfactionComponent } from './satisfaction/activate-satisfaction/activate-satisfaction.component';
import { SatisfactionDetailsComponent } from './satisfaction/satisfaction-details/satisfaction-details.component';
import { AnalyticsRoutingModule } from './analytics-routing.module';
import { DialogsListComponent } from './dialogs/dialogs-list/dialogs-list.component';
import { DialogComponent } from './dialog/dialog.component';
import { DialogsListFiltersComponent } from './dialogs/dialogs-list/dialogs-list-filters/dialogs-list-filters.component';

@NgModule({
  schemas: [NO_ERRORS_SCHEMA],
  imports: [
    CommonModule,
    FormsModule,
    ReactiveFormsModule,
    AnalyticsRoutingModule,
    InfiniteScrollModule,
    MomentModule,
    BotSharedModule,
    BotModule,
    NbFormFieldModule,
    NbRouteTabsetModule,
    NbCheckboxModule,
    NbCardModule,
    NbTooltipModule,
    NbSpinnerModule,
    NbButtonModule,
    NbInputModule,
    NbSelectModule,
    NbFormFieldModule,
    NbCalendarModule,
    NbUserModule,
    NbDatepickerModule,
    NbTimepickerModule.forRoot(),
    NbListModule,
    NbAccordionModule,
    NbContextMenuModule,
    NbMenuModule.forRoot(),
    NbCalendarRangeModule,
    NbDialogModule.forRoot(),
    NbRadioModule,
    NbToggleModule,
    NbIconModule,
    NgxEchartsModule.forRoot({
      echarts: () => import('echarts')
    }),
    NbPopoverModule
  ],
  declarations: [
    AnalyticsTabsComponent,
    DialogsComponent,
    FlowComponent,
    UsersComponent,
    ChartComponent,
    ActivityComponent,
    BehaviorComponent,
    PreferencesComponent,
    ChartDialogComponent,
    SatisfactionComponent,
    ActivateSatisfactionComponent,
    SatisfactionDetailsComponent,
    DialogsListComponent,
    DialogsListFiltersComponent,
    DialogComponent
  ],
  exports: [],
  providers: [AnalyticsService]
})
export class BotAnalyticsModule {}
