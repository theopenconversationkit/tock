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
import { ApplicationsComponent } from '../applications/applications/applications.component';
import { ApplicationComponent } from '../applications/application/application.component';
import { AuthGuard } from '../core-nlp/auth/auth.guard';
import { BotConfigurationsComponent } from './bot-configurations/bot-configurations.component';
import { ConfigurationTabsComponent } from './configuration-tabs.component';
import { ApplicationsResolver } from '../applications/applications.resolver';
import { NewBotComponent } from './bot-configurations/new-bot.component';
import { UserLogsComponent } from '../applications/user/user-logs.component';
import { NamespacesComponent } from '../applications/namespace/namespaces.component';
import { SynchronizationComponent } from './synchronization/synchronization.component';
import { SentenceGenerationSettingsComponent } from './sentence-generation-settings/sentence-generation-settings.component';
import { ObservabilitySettingsComponent } from './observability-settings/observability-settings.component';
import { VectorDbSettingsComponent } from './vector-db-settings/vector-db-settings.component';
import { CompressorSettingsComponent } from './compressor-settings/compressor-settings.component';

const routes: Routes = [
  {
    path: '',
    canActivate: [AuthGuard],
    component: ConfigurationTabsComponent,
    resolve: {
      application: ApplicationsResolver
    },
    children: [
      {
        path: '',
        component: ApplicationsComponent,
        resolve: {
          application: ApplicationsResolver
        }
      },
      {
        path: 'bot',
        component: BotConfigurationsComponent,
        resolve: {
          application: ApplicationsResolver
        }
      },
      {
        path: 'create',
        component: ApplicationComponent
      },
      {
        path: 'nlp',
        children: [
          {
            path: '',
            component: ApplicationsComponent
          },
          {
            path: 'edit/:id',
            component: ApplicationComponent
          },
          {
            path: 'create',
            component: ApplicationComponent
          }
        ],
        resolve: {
          application: ApplicationsResolver
        }
      },
      {
        path: 'new',
        component: NewBotComponent,
        resolve: {
          application: ApplicationsResolver
        }
      },
      {
        path: 'users/logs',
        component: UserLogsComponent
      },
      {
        path: 'namespaces',
        component: NamespacesComponent
      },
      {
        path: 'synchronization',
        component: SynchronizationComponent
      },
      {
        path: 'sentence-generation-settings',
        component: SentenceGenerationSettingsComponent
      },
      {
        path: 'observability-settings',
        component: ObservabilitySettingsComponent
      },
      {
        path: 'vector-db-settings',
        component: VectorDbSettingsComponent
      },
      {
        path: 'compressor-settings',
        component: CompressorSettingsComponent
      }
    ]
  }
];

@NgModule({
  imports: [RouterModule.forChild(routes)],
  exports: [RouterModule]
})
export class BotConfigurationRoutingModule {}
