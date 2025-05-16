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

import { RouterModule, Routes } from '@angular/router';
import { AuthGuard } from '../core-nlp/auth/auth.guard';
import { ApplicationResolver } from '../core-nlp/application.resolver';
import { EntitiesComponent } from './entities/entities.component';
import { NgModule } from '@angular/core';
import { SentencesInboxComponent } from './sentences/sentences-inbox/sentences-inbox.component';
import { SentencesUnknownComponent } from './sentences/sentences-unknown/sentences-unknown.component';
import { SentencesSearchComponent } from './sentences/sentences-search/sentences-search.component';
import { LanguageUnderstandingTabsComponent } from './language-understanding-tabs.component';
import { SentenceNewComponent } from './sentences/sentence-new/sentence-new.component';
import { IntentsLogsComponent } from './intents-logs/intents-logs.component';
import { IntentsComponent } from './intents/intents.component';

const routes: Routes = [
  {
    path: '',
    canActivate: [AuthGuard],
    component: LanguageUnderstandingTabsComponent,
    resolve: {
      application: ApplicationResolver
    },
    children: [
      {
        path: '',
        redirectTo: 'inbox',
        pathMatch: 'full'
      },
      {
        path: 'try',
        component: SentenceNewComponent
      },
      {
        path: 'inbox',
        component: SentencesInboxComponent
      },
      {
        path: 'unknown',
        component: SentencesUnknownComponent
      },
      {
        path: 'search',
        component: SentencesSearchComponent
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
        path: 'logs',
        component: IntentsLogsComponent
      }
    ]
  }
];

@NgModule({
  imports: [RouterModule.forChild(routes)],
  exports: [RouterModule]
})
export class LanguageUndestandingRoutingModule {}
