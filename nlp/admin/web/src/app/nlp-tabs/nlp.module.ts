/*
 * Copyright (C) 2017 VSCT
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

import {NgModule} from "@angular/core";
import {RouterModule, Routes} from "@angular/router";
import {TryComponent} from "../try/try.component";
import {SharedModule} from "../shared/shared.module";
import {NlpTabsComponent} from "./nlp-tabs.component";
import {InboxComponent} from "../inbox/inbox.component";
import {ArchiveComponent} from "../archive/archive.component";
import {IntentsComponent} from "../intents/intents.component";
import {SearchComponent} from "../search/search.component";
import {DisplayFullLogComponent, LogsComponent} from "../logs/logs.component";
import {CommonModule} from "@angular/common";
import {AuthGuard} from "../core/auth/auth.guard";
import {ApplicationsModule} from "../applications/applications.module";
import {HighlightComponent} from "../sentence-analysis/highlight/highlight.component";
import {SentenceAnalysisComponent} from "../sentence-analysis/sentence-analysis.component";
import {NlpService} from "./nlp.service";
import {CreateEntityDialogComponent} from "../sentence-analysis/create-entity-dialog/create-entity-dialog.component";
import {InfiniteScrollModule} from "ngx-infinite-scroll";
import {CreateIntentDialogComponent} from "../sentence-analysis/create-intent-dialog/create-intent-dialog.component";
import {ApplicationResolver} from "../core/application.resolver";
import {SentencesScrollComponent} from "../sentences-scroll/sentences-scroll.component";

import {MomentModule} from "angular2-moment";
import {AddStateDialogComponent} from "../intents/add-state/add-state-dialog.component";
import {EntitiesComponent} from "../entities/entities.component";
import {EditEntitiesComponent} from "../sentence-analysis/entities/edit-entities.component";
import {EntityDetailsComponent} from "../entities/entity-details.component";

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
        path: 'archive',
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
    MomentModule
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
    CreateIntentDialogComponent,
    SentencesScrollComponent,
    DisplayFullLogComponent,
    AddStateDialogComponent,
    EntitiesComponent,
    EntityDetailsComponent
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
    CreateIntentDialogComponent,
    DisplayFullLogComponent,
    AddStateDialogComponent
  ]
})
export class NlpModule {
}
