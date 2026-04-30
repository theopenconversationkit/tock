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
import { BotSharedModule } from '../shared/bot-shared.module';
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
  NbAlertModule,
  NbBadgeModule,
  NbCheckboxModule,
  NbProgressBarModule
} from '@nebular/theme';
import { FormsModule, ReactiveFormsModule } from '@angular/forms';
import { SamplesBoardComponent } from './samples/samples-board/samples-board.component';
import { QualityTabsComponent } from './quality-tabs.component';
import { QualityRoutingModule } from './quality-routing.module';
import { SampleCreateComponent } from './samples/sample-create/sample-create.component';
import { SampleDetailComponent } from './samples/sample-detail/sample-detail.component';
import { InfiniteScrollModule } from 'ngx-infinite-scroll';
import { DatasetsBoardComponent } from './datatsets/datasets-board/datasets-board.component';
import { DatasetCreateComponent } from './datatsets/dataset-create/dataset-create.component';
import { DatasetDetailComponent } from './datatsets/dataset-detail/dataset-detail.component';
import { DatasetDetailSettingsDiffComponent } from './datatsets/dataset-detail/settings-diff/settings-diff.component';
import { DatasetDetailEntryComponent } from './datatsets/dataset-detail/dataset-detail-entry/dataset-detail-entry.component';
import { DatasetsBoardEntryComponent } from './datatsets/datasets-board/dataset-board-entry/datasets-board-entry.component';
import { SampleCreateFromRunComponent } from './datatsets/sample-create-from-run/sample-create-from-run.component';

@NgModule({
  declarations: [
    QualityTabsComponent,
    SamplesBoardComponent,
    SampleCreateComponent,
    SampleDetailComponent,
    SampleCreateFromRunComponent,
    DatasetsBoardComponent,
    DatasetsBoardEntryComponent,
    DatasetCreateComponent,
    DatasetDetailComponent,
    DatasetDetailSettingsDiffComponent,
    DatasetDetailEntryComponent
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
    NbSpinnerModule,
    NbAutocompleteModule,
    NbAlertModule,
    NbBadgeModule,
    NbCheckboxModule,
    NbProgressBarModule,
    InfiniteScrollModule,
    QualityRoutingModule
  ]
})
export class QualityModule {}
