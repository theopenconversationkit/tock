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
import { RagTabsComponent } from './rag-tabs.component';
import {
  NbAccordionModule,
  NbAlertModule,
  NbAutocompleteModule,
  NbButtonModule,
  NbCardModule,
  NbCheckboxModule,
  NbFormFieldModule,
  NbIconModule,
  NbInputModule,
  NbRadioModule,
  NbRouteTabsetModule,
  NbSelectModule,
  NbSpinnerModule,
  NbToggleModule,
  NbTooltipModule
} from '@nebular/theme';
import { RagSettingsComponent } from './rag-settings/rag-settings.component';
import { BotSharedModule } from '../shared/bot-shared.module';
import { RagRoutingModule } from './rag-routing.module';
import { FormsModule, ReactiveFormsModule } from '@angular/forms';
import { RagExcludedComponent } from './rag-excluded/rag-excluded.component';

@NgModule({
  imports: [
    CommonModule,
    BotSharedModule,
    RagRoutingModule,
    FormsModule,
    ReactiveFormsModule,
    NbRouteTabsetModule,
    NbSelectModule,
    NbToggleModule,
    NbRadioModule,
    NbSpinnerModule,
    NbCardModule,
    NbButtonModule,
    NbInputModule,
    NbIconModule,
    NbAccordionModule,
    NbTooltipModule,
    NbCheckboxModule,
    NbAlertModule,
    NbAutocompleteModule,
    NbFormFieldModule
  ],
  declarations: [RagTabsComponent, RagSettingsComponent, RagExcludedComponent],
  providers: []
})
export class RagModule {}
