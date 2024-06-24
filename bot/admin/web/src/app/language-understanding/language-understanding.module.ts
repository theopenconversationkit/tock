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
import { CommonModule } from '@angular/common';
import { LanguageUnderstandingTabsComponent } from './language-understanding-tabs.component';
import {
  NbAccordionModule,
  NbButtonModule,
  NbCardModule,
  NbCheckboxModule,
  NbFormFieldModule,
  NbIconModule,
  NbInputModule,
  NbRouteTabsetModule,
  NbSpinnerModule,
  NbTabsetModule,
  NbToggleModule,
  NbTooltipModule
} from '@nebular/theme';
import { LanguageUndestandingRoutingModule } from './language-understanding-routing.module';
import { SentencesInboxComponent } from './sentences/sentences-inbox/sentences-inbox.component';
import { SentencesSearchComponent } from './sentences/sentences-search/sentences-search.component';
import { SentencesUnknownComponent } from './sentences/sentences-unknown/sentences-unknown.component';
import { BotSharedModule } from '../shared/bot-shared.module';
import { SentenceNewComponent } from './sentences/sentence-new/sentence-new.component';
import { FormsModule, ReactiveFormsModule } from '@angular/forms';
import { IntentsLogsComponent } from './intents-logs/intents-logs.component';
import { DisplayIntentFullLogComponent } from './intents-logs/display-intents-full-log/display-intents-full-log.component';
import { NgJsonEditorModule } from 'ang-jsoneditor';
import { MomentModule } from 'ngx-moment';
import { InfiniteScrollModule } from 'ngx-infinite-scroll';

@NgModule({
  declarations: [
    LanguageUnderstandingTabsComponent,
    SentencesInboxComponent,
    SentencesSearchComponent,
    SentencesUnknownComponent,
    SentenceNewComponent,
    IntentsLogsComponent,
    DisplayIntentFullLogComponent
  ],
  imports: [
    CommonModule,
    FormsModule,
    ReactiveFormsModule,
    LanguageUndestandingRoutingModule,
    BotSharedModule,
    NbTabsetModule,
    NbRouteTabsetModule,
    NbIconModule,
    NbButtonModule,
    NbCheckboxModule,
    NbCardModule,
    NbAccordionModule,
    NbInputModule,
    NbFormFieldModule,
    NbSpinnerModule,
    NbToggleModule,
    NbTooltipModule,
    NgJsonEditorModule,
    MomentModule,
    InfiniteScrollModule
  ]
})
export class LanguageUnderstandingModule {}
