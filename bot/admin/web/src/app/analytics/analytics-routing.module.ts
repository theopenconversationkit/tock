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
import { AuthGuard } from '../core-nlp/auth/auth.guard';
import { AnalyticsTabsComponent } from './analytics-tabs.component';
import { ApplicationResolver } from '../core-nlp/application.resolver';
import { ActivityComponent } from './activity/activity.component';
import { BehaviorComponent } from './behavior/behavior.component';
import { FlowComponent } from './flow/flow.component';
import { DialogsComponent } from './dialogs/dialogs.component';
import { UsersComponent } from './users/users.component';
import { PreferencesComponent } from './preferences/preferences.component';
import { SatisfactionComponent } from './satisfaction/satisfaction.component';
import { DialogComponent } from './dialog/dialog.component';

const routes: Routes = [
  {
    path: '',
    canActivate: [AuthGuard],
    component: AnalyticsTabsComponent,
    resolve: {
      application: ApplicationResolver
    },
    children: [
      {
        path: '',
        component: ActivityComponent
      },
      {
        path: 'activity',
        component: ActivityComponent
      },
      {
        path: 'behavior',
        component: BehaviorComponent
      },
      {
        path: 'flow',
        component: FlowComponent
      },
      {
        path: 'dialogs',
        component: DialogsComponent
      },
      {
        path: 'dialogs/:namespace/:applicationId/:dialogId',
        component: DialogComponent
      },
      {
        path: 'users',
        component: UsersComponent
      },
      {
        path: 'preferences',
        component: PreferencesComponent
      },
      {
        path: 'satisfaction',
        component: SatisfactionComponent
      }
    ]
  }
];

@NgModule({
  imports: [RouterModule.forChild(routes)],
  exports: [RouterModule]
})
export class AnalyticsRoutingModule {}
