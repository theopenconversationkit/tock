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
import { ApplicationResolver } from '../core-nlp/application.resolver';
import { AuthGuard } from '../core-nlp/auth/auth.guard';
import { RagExcludedComponent } from './rag-excluded/rag-excluded.component';
import { RagSettingsComponent } from './rag-settings/rag-settings.component';
import { RagTabsComponent } from './rag-tabs.component';

const routes: Routes = [
  {
    path: '',
    canActivate: [AuthGuard],
    component: RagTabsComponent,
    resolve: {
      application: ApplicationResolver
    },
    children: [
      {
        path: 'exclusions',
        component: RagExcludedComponent
      },
      {
        path: 'settings',
        component: RagSettingsComponent
      }
    ]
  }
];
@NgModule({
  imports: [RouterModule.forChild(routes)],
  exports: [RouterModule],
  declarations: []
})
export class RagRoutingModule {}
