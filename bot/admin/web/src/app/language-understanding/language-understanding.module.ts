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
import { LanguageUnderstandingTabsComponent } from './language-understanding-tabs.component';
import {
  NbAccordionModule,
  NbAutocompleteModule,
  NbButtonModule,
  NbCardModule,
  NbCheckboxModule,
  NbFormFieldModule,
  NbIconModule,
  NbInputModule,
  NbRouteTabsetModule,
  NbSelectModule,
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
import { EntitiesComponent } from './entities/entities.component';
import { EntityDetailsComponent } from './entities/entity-details.component';
import { FileUploadModule } from 'ng2-file-upload';
import { IntentsComponent } from './intents/intents.component';
import { IntentsFiltersComponent } from './intents/intents-filters/intents-filters.component';
import { IntentsListComponent } from './intents/intents-list/intents-list.component';

import { AddStateDialogComponent } from './intents/add-state/add-state-dialog.component';
import { AddSharedIntentDialogComponent } from './intents/add-shared-intent/add-shared-intent-dialog.component';
import { IntentDialogComponent } from './intent-dialog/intent-dialog.component';

@NgModule({
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
    NbSelectModule,
    NbAutocompleteModule,
    NgJsonEditorModule,
    MomentModule,
    InfiniteScrollModule,
    FileUploadModule
  ],
  declarations: [
    LanguageUnderstandingTabsComponent,
    SentencesInboxComponent,
    SentencesSearchComponent,
    SentencesUnknownComponent,
    SentenceNewComponent,
    IntentsLogsComponent,
    DisplayIntentFullLogComponent,
    EntitiesComponent,
    EntityDetailsComponent,
    IntentsComponent,
    IntentsFiltersComponent,
    IntentsListComponent,

    AddStateDialogComponent,
    AddSharedIntentDialogComponent,
    IntentDialogComponent
  ]
})
export class LanguageUnderstandingModule {}
