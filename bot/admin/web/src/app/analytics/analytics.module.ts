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

import { RouterModule, Routes } from '@angular/router';
import { NgModule, NO_ERRORS_SCHEMA } from '@angular/core';
import { CommonModule } from '@angular/common';
import { InfiniteScrollModule } from 'ngx-infinite-scroll';
import { SharedModule } from '../shared-nlp/shared.module';
import { AuthGuard } from '../core-nlp/auth/auth.guard';
import { AnalyticsTabsComponent } from './analytics-tabs.component';
import { ApplicationResolver } from '../core-nlp/application.resolver';
import { UsersComponent } from './users/users.component';
import { AnalyticsService } from './analytics.service';
import { BotSharedModule } from '../shared/bot-shared.module';
import { BotModule } from '../bot/bot.module';
import { NlpModule } from '../nlp-tabs/nlp.module';
import { MomentModule } from 'ngx-moment';
import { DialogsComponent } from './dialogs/dialogs.component';
import { MatNativeDateModule } from '@angular/material/core';
import { MatDatepickerModule } from '@angular/material/datepicker';
import {
  NbAccordionModule,
  NbButtonModule,
  NbCalendarModule,
  NbCardModule,
  NbCheckboxModule,
  NbContextMenuModule,
  NbDatepickerModule,
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
  NbRadioModule
} from '@nebular/theme';
import { ChartComponent } from './chart/chart.component';
import { ChartDialogComponent } from './chart-dialog/chart-dialog.component';
import { ActivityComponent } from './activity/activity.component';
import { BehaviorComponent } from './behavior/behavior.component';
import { GoogleChartsModule } from 'angular-google-charts';
import { FlowComponent } from './flow/flow.component';
import { CytoComponent } from './flow/cyto.component';
import { PreferencesComponent } from './preferences/preferences.component';
import { NgxEchartsModule } from 'ngx-echarts';

export function importEcharts() {
  return import('echarts');
}

const routes: Routes = [
  {
    path: '',
    canActivate: [AuthGuard],
    component: AnalyticsTabsComponent,
    resolve: {
      application: ApplicationResolver
    },
    children: [
      {
        path: '',
        component: ActivityComponent
      },
      {
        path: 'activity',
        component: ActivityComponent
      },
      {
        path: 'behavior',
        component: BehaviorComponent
      },
      {
        path: 'flow',
        component: FlowComponent
      },
      {
        path: 'dialogs',
        component: DialogsComponent
      },
      {
        path: 'users',
        component: UsersComponent
      },
      {
        path: 'preferences',
        component: PreferencesComponent
      }
    ]
  }
];

@NgModule({
  imports: [RouterModule.forChild(routes)],
  exports: [RouterModule]
})
export class AnalyticsRoutingModule {}

@NgModule({
  schemas: [NO_ERRORS_SCHEMA],
  imports: [
    CommonModule,
    SharedModule,
    AnalyticsRoutingModule,
    InfiniteScrollModule,
    MomentModule,
    BotSharedModule,
    BotModule,
    NlpModule,
    MatDatepickerModule,
    MatNativeDateModule,
    NbRouteTabsetModule,
    NbCheckboxModule,
    NbCardModule,
    NbTooltipModule,
    NbSpinnerModule,
    NbButtonModule,
    NbInputModule,
    NbSelectModule,
    NbCalendarModule,
    NbUserModule,
    NbDatepickerModule,
    NbListModule,
    NbAccordionModule,
    GoogleChartsModule,
    NbContextMenuModule,
    NbMenuModule.forRoot(),
    NbCalendarRangeModule,
    NbDialogModule.forRoot(),
    NbRadioModule,
    NgxEchartsModule.forRoot({
      echarts: importEcharts
    })
  ],
  declarations: [
    AnalyticsTabsComponent,
    DialogsComponent,
    FlowComponent,
    CytoComponent,
    UsersComponent,
    ChartComponent,
    ActivityComponent,
    BehaviorComponent,
    PreferencesComponent,
    ChartDialogComponent
  ],
  exports: [],
  providers: [AnalyticsService],
  entryComponents: []
})
export class BotAnalyticsModule {}
