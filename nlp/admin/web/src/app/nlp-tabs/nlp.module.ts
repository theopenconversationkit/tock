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
import { TryComponent } from '../try/try.component';
import { SharedModule } from '../shared-nlp/shared.module';
import { NlpTabsComponent } from './nlp-tabs.component';
import { InboxComponent } from '../inbox/inbox.component';
import { ArchiveComponent } from '../archive/archive.component';
import { IntentsComponent } from '../intents/intents.component';
import { IntentsFiltersComponent } from '../intents/intents-filters/intents-filters.component';
import { IntentsListComponent } from '../intents/intents-list/intents-list.component';
import { SearchComponent } from '../search/search.component';
import { SearchFilterComponent } from '../search/filter/search-filter.component';
import { DisplayFullLogComponent, LogsComponent } from '../logs/logs.component';
import { CommonModule } from '@angular/common';
import { ApplicationsModule } from '../applications/applications.module';
import { HighlightComponent } from '../sentence-analysis/highlight/highlight.component';
import { SentenceAnalysisComponent } from '../sentence-analysis/sentence-analysis.component';
import { NlpService } from './nlp.service';
import { CreateEntityDialogComponent } from '../sentence-analysis/create-entity-dialog/create-entity-dialog.component';
import { InfiniteScrollModule } from 'ngx-infinite-scroll';
import { IntentDialogComponent } from '../sentence-analysis/intent-dialog/intent-dialog.component';
import { SentencesScrollComponent } from '../sentences-scroll/sentences-scroll.component';

import { MomentModule } from 'ngx-moment';
import { AddStateDialogComponent } from '../intents/add-state/add-state-dialog.component';
import { AddSharedIntentDialogComponent } from '../intents/add-shared-intent/add-shared-intent-dialog.component';
import { EntitiesComponent } from '../entities/entities.component';
import { EditEntitiesComponent } from '../sentence-analysis/entities/edit-entities.component';
import { EntityDetailsComponent } from '../entities/entity-details.component';
import { FileUploadModule } from 'ng2-file-upload';
import { ReviewRequestDialogComponent } from '../sentence-analysis/review-request-dialog/review-request-dialog.component';
import {
  NbAccordionModule,
  NbActionsModule,
  NbAutocompleteModule,
  NbButtonModule,
  NbCardModule,
  NbCheckboxModule,
  NbFormFieldModule,
  NbInputModule,
  NbRouteTabsetModule,
  NbSelectModule,
  NbSpinnerModule,
  NbTabsetModule,
  NbTooltipModule,
  NbTreeGridModule
} from '@nebular/theme';
import { ThemeModule } from '../theme/theme.module';
import { NgJsonEditorModule } from 'ang-jsoneditor';
import { ReactiveFormsModule } from '@angular/forms';
import { NlpRoutingModule } from './nlp-routing.module';

@NgModule({
  imports: [
    CommonModule,
    SharedModule,
    NlpRoutingModule,
    ApplicationsModule,
    InfiniteScrollModule,
    MomentModule,
    FileUploadModule,
    ThemeModule,
    NbTabsetModule,
    NbRouteTabsetModule,
    NbAccordionModule,
    NbCardModule,
    NbCheckboxModule,
    NbSpinnerModule,
    NbActionsModule,
    NbSelectModule,
    NbButtonModule,
    NbTooltipModule,
    NbInputModule,
    NgJsonEditorModule,
    NbTreeGridModule,
    NbAutocompleteModule,
    ReactiveFormsModule,
    NbFormFieldModule
  ],
  declarations: [
    NlpTabsComponent,
    TryComponent,
    InboxComponent,
    ArchiveComponent,
    IntentsComponent,
    SearchComponent,
    SearchFilterComponent,
    LogsComponent,
    HighlightComponent,
    EditEntitiesComponent,
    SentenceAnalysisComponent,
    CreateEntityDialogComponent,
    IntentDialogComponent,
    SentencesScrollComponent,
    DisplayFullLogComponent,
    AddStateDialogComponent,
    AddSharedIntentDialogComponent,
    EntitiesComponent,
    EntityDetailsComponent,
    ReviewRequestDialogComponent,
    IntentsFiltersComponent,
    IntentsListComponent
  ],
  exports: [SentenceAnalysisComponent, HighlightComponent, SentencesScrollComponent],
  providers: [NlpService]
})
export class NlpModule {}
