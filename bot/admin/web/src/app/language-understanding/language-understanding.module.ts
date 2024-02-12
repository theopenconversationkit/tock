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
import { LanguageUnderstandingTabsComponent } from './language-understanding-tabs/language-understanding-tabs.component';
import {
  NbAccordionModule,
  NbButtonModule,
  NbCardModule,
  NbCheckboxModule,
  NbIconModule,
  NbInputModule,
  NbRouteTabsetModule,
  NbTabsetModule
} from '@nebular/theme';
import { LanguageUndestandingRoutingModule } from './language-understanding-routing.module';
import { SentencesInboxComponent } from './sentences/sentences-inbox/sentences-inbox.component';
import { SentencesSearchComponent } from './sentences/sentences-search/sentences-search.component';
import { SentencesUnknownComponent } from './sentences/sentences-unknown/sentences-unknown.component';
import { BotSharedModule } from '../shared/bot-shared.module';
import { SentenceNewComponent } from './sentences/sentence-new/sentence-new.component';
import { FormsModule } from '@angular/forms';

@NgModule({
  declarations: [
    LanguageUnderstandingTabsComponent,
    SentencesInboxComponent,
    SentencesSearchComponent,
    SentencesUnknownComponent,
    SentenceNewComponent
  ],
  imports: [
    CommonModule,
    FormsModule,
    LanguageUndestandingRoutingModule,
    BotSharedModule,
    NbTabsetModule,
    NbRouteTabsetModule,
    NbIconModule,
    NbButtonModule,
    NbCheckboxModule,
    NbCardModule,
    NbAccordionModule,
    NbInputModule
  ]
})
export class LanguageUnderstandingModule {}
