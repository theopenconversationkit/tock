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

import { CommonModule, DatePipe } from '@angular/common';
import { NgModule } from '@angular/core';
import { FormsModule, ReactiveFormsModule } from '@angular/forms';
import { TranslocoModule, provideTranslocoScope } from '@jsverse/transloco';
import {
  NbButtonModule,
  NbCardModule,
  NbCheckboxModule,
  NbFormFieldModule,
  NbIconModule,
  NbInputModule,
  NbOptionModule,
  NbSelectModule,
  NbSpinnerModule,
  NbTooltipModule
} from '@nebular/theme';

import { KnowledgeBaseRoutingModule } from './knowledge-base-routing.module';
import { KnowledgeBaseEntriesBoardComponent } from './entries-board/entries-board.component';
import { KnowledgeBaseSyncBannerComponent } from './entries-board/sync-banner/sync-banner.component';
import { KnowledgeBaseEntriesImportComponent } from './entries-import/entries-import.component';
import { KnowledgeBaseEntryDetailComponent } from './entry-detail/entry-detail.component';
import { KnowledgeBaseRetrievalTestComponent } from './entry-detail/retrieval-test/retrieval-test.component';
import { KnowledgeBaseMockService } from './services/knowledge-base-mock.service';
import { KnowledgeBaseService } from './services/knowledge-base.service';
import { BotSharedModule } from '../shared/bot-shared.module';

@NgModule({
  declarations: [
    KnowledgeBaseEntriesBoardComponent,
    KnowledgeBaseSyncBannerComponent,
    KnowledgeBaseEntryDetailComponent,
    KnowledgeBaseEntriesImportComponent,
    KnowledgeBaseRetrievalTestComponent
  ],
  imports: [
    CommonModule,
    FormsModule,
    ReactiveFormsModule,
    BotSharedModule,
    TranslocoModule,
    KnowledgeBaseRoutingModule,
    NbButtonModule,
    NbCardModule,
    NbCheckboxModule,
    NbFormFieldModule,
    NbIconModule,
    NbInputModule,
    NbOptionModule,
    NbSelectModule,
    NbSpinnerModule,
    NbTooltipModule
  ],
  providers: [
    DatePipe,
    provideTranslocoScope({ scope: 'knowledge-base', alias: 'knowledge-base' }),

    // Implementation switch. Components only ever inject the abstract KnowledgeBaseService.
    // Moving to the real backend is: replace the two lines below with
    //   { provide: KnowledgeBaseService, useClass: KnowledgeBaseRestService }
    // The board's demo scenario selector injects KnowledgeBaseMockService optionally, so it
    // disappears on its own once the mock is no longer provided.
    KnowledgeBaseMockService,
    { provide: KnowledgeBaseService, useExisting: KnowledgeBaseMockService }
  ]
})
export class KnowledgeBaseModule {}
