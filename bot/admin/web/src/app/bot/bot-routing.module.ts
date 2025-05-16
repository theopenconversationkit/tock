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
import { RouterModule, Routes } from '@angular/router';
import { FeatureComponent } from './feature/feature.component';
import { I18nComponent } from './i18n/i18n.component';
import { EditStoryComponent } from './story/edit-story/edit-story.component';
import { SearchStoryComponent } from './story/search-story/search-story.component';
import { CreateStoryComponent } from './story/create-story/create-story.component';
import { ApplicationResolver } from '../core-nlp/application.resolver';
import { BotTabsComponent } from './bot-tabs.component';
import { AuthGuard } from '../core-nlp/auth/auth.guard';
import { DocumentsStoryComponent } from './story/documents-story/documents-story.component';

const routes: Routes = [
  {
    path: '',
    canActivate: [AuthGuard],
    component: BotTabsComponent,
    resolve: {
      application: ApplicationResolver
    },
    children: [
      {
        path: '',
        redirectTo: 'story-create',
        pathMatch: 'full'
      },
      {
        path: 'story-create',
        component: CreateStoryComponent
      },
      {
        path: 'story-search',
        component: SearchStoryComponent
      },
      {
        path: 'story-edit/:storyId',
        component: EditStoryComponent
      },
      {
        path: 'i18n',
        component: I18nComponent
      },
      {
        path: 'story-rules',
        component: FeatureComponent
      },
      {
        path: 'story-documents',
        component: DocumentsStoryComponent
      }
    ]
  }
];

@NgModule({
  imports: [RouterModule.forChild(routes)],
  exports: [RouterModule]
})
export class BotRoutingModule {}
