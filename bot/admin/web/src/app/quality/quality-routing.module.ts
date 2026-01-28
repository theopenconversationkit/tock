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

import { SamplesBoardComponent } from './samples/samples-board/samples-board.component';
import { QualityTabsComponent } from './quality-tabs.component';
import { SampleDetailComponent } from './samples/sample-detail/sample-detail.component';
// import { DatasetsBoardComponent } from './datatsets/datasets-board/datasets-board.component';
// import { DatasetDetailComponent } from './datatsets/dataset-detail/dataset-detail.component';

const routes: Routes = [
  {
    path: '',
    canActivate: [AuthGuard],
    component: QualityTabsComponent,
    resolve: {
      application: ApplicationResolver
    },
    children: [
      {
        path: '',
        component: SamplesBoardComponent
      },
      {
        path: 'samples',
        component: SamplesBoardComponent
      },
      {
        path: 'samples/samples-board',
        component: SamplesBoardComponent
      },
      {
        path: 'samples/detail/:id',
        component: SampleDetailComponent
      }
      // {
      //   path: 'datasets',
      //   component: DatasetsBoardComponent
      // },
      // {
      //   path: 'datasets/datasets-board',
      //   component: DatasetsBoardComponent
      // },
      // {
      //   path: 'datasets/detail/:id',
      //   component: DatasetDetailComponent
      // }
    ]
  }
];

@NgModule({
  imports: [RouterModule.forChild(routes)],
  exports: [RouterModule],
  declarations: []
})
export class QualityRoutingModule {}
