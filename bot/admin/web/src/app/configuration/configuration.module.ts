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

import {ApplicationsModule} from "tock-nlp-admin/src/app/applications/applications.module";
import {NgModule} from "@angular/core";
import {SharedModule} from "tock-nlp-admin/src/app/shared/shared.module";
import {RouterModule, Routes} from "@angular/router";
import {ApplicationsComponent} from "tock-nlp-admin/src/app/applications/applications/applications.component";
import {ApplicationComponent} from "tock-nlp-admin/src/app/applications/application/application.component";
import {AuthGuard} from "tock-nlp-admin/src/app/core/auth/auth.guard";
import {BotConfigurationComponent} from "./bot/bot-configuration.component";
import {ConfigurationTabsComponent} from "./configuration-tabs.component";
import {ApplicationsResolver} from "tock-nlp-admin/src/app/applications/applications.resolver";
import {CommonModule} from "@angular/common";
import {BotSharedModule} from "../shared/bot-shared.module";

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
        component: ApplicationsComponent
      },
      {
        path: 'bot',
        component: BotConfigurationComponent,
        resolve: {
          application: ApplicationsResolver
        }
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
        ]
      }
    ]
  }
];
@NgModule({
  imports: [RouterModule.forChild(routes)],
  exports: [RouterModule]
})
export class BotConfigurationRoutingModule {
}

@NgModule({
  declarations: [
    ConfigurationTabsComponent,
    BotConfigurationComponent
  ],
  imports: [
    CommonModule,
    SharedModule,
    BotSharedModule,
    BotConfigurationRoutingModule,
    ApplicationsModule
  ],
  providers: [],
  bootstrap: []
})
export class BotConfigurationModule {
}

