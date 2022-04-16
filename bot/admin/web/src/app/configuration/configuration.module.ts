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

import { ApplicationsModule } from '../applications/applications.module';
import { Injectable, NgModule } from '@angular/core';
import { SharedModule } from '../shared-nlp/shared.module';
import { RouterModule, Routes } from '@angular/router';
import { ApplicationsComponent } from '../applications/applications/applications.component';
import { ApplicationComponent } from '../applications/application/application.component';
import { AuthGuard } from '../core-nlp/auth/auth.guard';
import { BotConfigurationsComponent } from './bot/bot-configurations.component';
import { ConfigurationTabsComponent } from './configuration-tabs.component';
import { ApplicationsResolver } from '../applications/applications.resolver';
import { CommonModule } from '@angular/common';
import { BotSharedModule } from '../shared/bot-shared.module';
import { BotConfigurationComponent } from './bot/bot-configuration.component';
import {
  NbAccordionModule,
  NbActionsModule,
  NbButtonModule,
  NbCardModule,
  NbIconModule,
  NbInputModule,
  NbRouteTabsetModule,
  NbSelectModule,
  NbSpinnerModule,
  NbTabsetModule,
  NbToastrModule,
  NbTooltipModule,
  NbStepperModule
} from '@nebular/theme';
import { NewBotComponent } from './bot/new-bot.component';
import { ReactiveFormsModule } from '@angular/forms';
import { UserLogsComponent } from '../applications/user/user-logs.component';
import { NamespacesComponent } from '../applications/namespace/namespaces.component';
import { BotSharedService } from '../shared/bot-shared.service';
import { ApplicationConfig } from '../applications/application.config';
import { SelectBotConfigurationDialogComponent } from './bot/selection-dialog/select-bot-configuration-dialog.component';

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
      }
    ]
  }
];

@NgModule({
  imports: [RouterModule.forChild(routes)],
  exports: [RouterModule]
})
export class BotConfigurationRoutingModule {}

@Injectable()
export class BotApplicationConfig implements ApplicationConfig {
  constructor(private botSharedService: BotSharedService) {}

  /** is it allowed to create namespace? **/
  canCreateNamespace(): boolean {
    return (
      this.botSharedService.configuration && !this.botSharedService.configuration.botApiSupport
    );
  }
}

@NgModule({
  declarations: [
    ConfigurationTabsComponent,
    BotConfigurationsComponent,
    BotConfigurationComponent,
    NewBotComponent,
    SelectBotConfigurationDialogComponent
  ],
  imports: [
    CommonModule,
    SharedModule,
    BotSharedModule,
    ReactiveFormsModule,
    BotConfigurationRoutingModule,
    ApplicationsModule,
    NbTabsetModule,
    NbCardModule,
    NbRouteTabsetModule,
    NbButtonModule,
    NbInputModule,
    NbActionsModule,
    NbTooltipModule,
    NbSpinnerModule,
    NbAccordionModule,
    NbToastrModule.forRoot(),
    NbIconModule,
    NbSelectModule,
    NbStepperModule
  ],
  providers: [
    {
      provide: ApplicationConfig,
      useClass: BotApplicationConfig
    }
  ],
  bootstrap: []
})
export class BotConfigurationModule {}
