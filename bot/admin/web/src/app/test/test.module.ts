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
import { TestTabsComponent } from './test-tabs.component';
import { BotDialogComponent } from './dialog/bot-dialog.component';
import { CommonModule } from '@angular/common';
import { TestService } from './test.service';
import { BotSharedModule } from '../shared/bot-shared.module';
import { TestPlanComponent } from './plan/test-plan.component';
import { MomentModule } from 'ngx-moment';
import {
  NbAccordionModule,
  NbActionsModule,
  NbButtonModule,
  NbCardModule,
  NbRouteTabsetModule,
  NbSelectModule,
  NbTooltipModule,
  NbInputModule,
  NbSpinnerModule,
  NbAutocompleteModule,
  NbIconModule,
  NbToggleModule
} from '@nebular/theme';
import { FormsModule, ReactiveFormsModule } from '@angular/forms';
import { NlpService } from '../core-nlp/nlp.service';
import { NlpStatsDisplayComponent } from './dialog/nlp-stats-display/nlp-stats-display.component';
import { BotTestRoutingModule } from './test-routing.module';

@NgModule({
  imports: [
    CommonModule,
    FormsModule,
    BotTestRoutingModule,
    BotTestRoutingModule,
    BotSharedModule,
    MomentModule,
    NbRouteTabsetModule,
    NbCardModule,
    NbButtonModule,
    NbActionsModule,
    NbSelectModule,
    NbTooltipModule,
    NbAccordionModule,
    NbInputModule,
    ReactiveFormsModule,
    NbSpinnerModule,
    NbAutocompleteModule,
    NbCardModule,
    NbIconModule,
    NbToggleModule
  ],
  declarations: [TestTabsComponent, BotDialogComponent, TestPlanComponent, NlpStatsDisplayComponent],
  exports: [],
  providers: [TestService, NlpService]
})
export class BotTestModule {}
