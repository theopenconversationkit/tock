/*
 * Copyright (C) 2017/2020 e-voyageurs technologies
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

import {NgModule} from "@angular/core";
import {RouterModule, Routes} from "@angular/router";
import {TryComponent} from "../try/try.component";
import {SharedModule} from "../shared-nlp/shared.module";
import {NlpTabsComponent} from "./nlp-tabs.component";
import {InboxComponent} from "../inbox/inbox.component";
import {ArchiveComponent} from "../archive/archive.component";
import {IntentsComponent} from "../intents/intents.component";
import {SearchComponent} from "../search/search.component";
import {DisplayFullLogComponent, LogsComponent} from "../logs/logs.component";
import {CommonModule} from "@angular/common";
import {AuthGuard} from "../core-nlp/auth/auth.guard";
import {ApplicationsModule} from "../applications/applications.module";
import {HighlightComponent} from "../sentence-analysis/highlight/highlight.component";
import {SentenceAnalysisComponent} from "../sentence-analysis/sentence-analysis.component";
import {NlpService} from "./nlp.service";
import {CreateEntityDialogComponent} from "../sentence-analysis/create-entity-dialog/create-entity-dialog.component";
import {InfiniteScrollModule} from "ngx-infinite-scroll";
import {IntentDialogComponent} from "../sentence-analysis/intent-dialog/intent-dialog.component";
import {ApplicationResolver} from "../core-nlp/application.resolver";
import {SentencesScrollComponent} from "../sentences-scroll/sentences-scroll.component";

import {MomentModule} from "ngx-moment";
import {AddStateDialogComponent} from "../intents/add-state/add-state-dialog.component";
import {EntitiesComponent} from "../entities/entities.component";
import {EditEntitiesComponent} from "../sentence-analysis/entities/edit-entities.component";
import {EntityDetailsComponent} from "../entities/entity-details.component";
import {MatDatepickerModule, MatNativeDateModule} from "@angular/material";
import {FileUploadModule} from "ng2-file-upload";
import {ReviewRequestDialogComponent} from "../sentence-analysis/review-request-dialog/review-request-dialog.component";
import {
  NbAccordionModule,
  NbActionsModule,
  NbButtonModule,
  NbCardModule,
  NbCheckboxModule,
  NbInputModule,
  NbRouteTabsetModule,
  NbSelectModule,
  NbSpinnerModule,
  NbTabsetModule,
  NbTooltipModule
} from "@nebular/theme";
import {ThemeModule} from "../theme/theme.module";
import { NgJsonEditorModule } from 'ang-jsoneditor'

const routes: Routes = [
  {
    path: '',
    canActivate: [AuthGuard],
    component: NlpTabsComponent,
    resolve: {
      application: ApplicationResolver
    },
    children: [
      {
        path: '',
        component: InboxComponent
      },
      {
        path: 'try',
        component: TryComponent
      },
      {
        path: 'inbox',
        component: InboxComponent
      },
      {
        path: 'unknown',
        component: ArchiveComponent
      },
      {
        path: 'intents',
        component: IntentsComponent
      },
      {
        path: 'entities',
        component: EntitiesComponent
      },
      {
        path: 'search',
        component: SearchComponent
      },
      {
        path: 'logs',
        component: LogsComponent
      }
    ]
  }
];

@NgModule({
  imports: [RouterModule.forChild(routes)],
  exports: [RouterModule]
})
export class NlpRoutingModule {
}

@NgModule({
  imports: [
    CommonModule,
    SharedModule,
    NlpRoutingModule,
    ApplicationsModule,
    InfiniteScrollModule,
    MomentModule,
    MatDatepickerModule,
    MatNativeDateModule,
    FileUploadModule,
    MatNativeDateModule,
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
    NgJsonEditorModule
  ],
  declarations: [
    NlpTabsComponent,
    TryComponent,
    InboxComponent,
    ArchiveComponent,
    IntentsComponent,
    SearchComponent,
    LogsComponent,
    HighlightComponent,
    EditEntitiesComponent,
    SentenceAnalysisComponent,
    CreateEntityDialogComponent,
    IntentDialogComponent,
    SentencesScrollComponent,
    DisplayFullLogComponent,
    AddStateDialogComponent,
    EntitiesComponent,
    EntityDetailsComponent,
    ReviewRequestDialogComponent
  ],
  exports: [
    SentenceAnalysisComponent,
    HighlightComponent
  ],
  providers: [
    NlpService
  ],
  entryComponents: [
    CreateEntityDialogComponent,
    IntentDialogComponent,
    DisplayFullLogComponent,
    AddStateDialogComponent,
    ReviewRequestDialogComponent
  ]
})
export class NlpModule {
}
