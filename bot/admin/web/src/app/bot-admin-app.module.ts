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

import {BrowserModule} from "@angular/platform-browser";
import {NgModule} from "@angular/core";
import {CoreModule} from "./core-nlp/core.module";
import {SharedModule} from "./shared-nlp/shared.module";
import {RouterModule, Routes} from "@angular/router";
import {BotAdminAppComponent} from "./bot-admin-app.component";
import {BrowserAnimationsModule} from "@angular/platform-browser/animations";
import {BotCoreModule} from "./core/bot-core.module";
import {HttpClientModule} from "@angular/common/http";
import {ThemeModule} from "./theme/theme.module";
import {
  NbDatepickerModule,
  NbDialogModule,
  NbMenuModule,
  NbSidebarModule,
  NbToastrModule,
  NbWindowModule,
  NbThemeModule
} from '@nebular/theme';

const routes: Routes = [
  {path: '', redirectTo: '/nlp/inbox', pathMatch: 'full'},
  {
    path: 'nlp',
    loadChildren: './nlp/nlp.module#BotNlpModule'
  },
  {
    path: 'quality',
    loadChildren: './quality/quality.module#BotQualityModule'
  },
  {
    path: 'configuration',
    loadChildren: './configuration/configuration.module#BotConfigurationModule'
  },
  {
    path: 'build',
    loadChildren: './bot/bot.module#BotModule'
  },
  {
    path: 'test',
    loadChildren: './test/test.module#BotTestModule'
  },
  {
    path: 'monitoring',
    loadChildren: './monitoring/monitoring.module#BotMonitoringModule'
  },
  {path: '**', redirectTo: '/nlp/inbox'}
];

@NgModule({
  imports: [RouterModule.forRoot(routes)],
  exports: [RouterModule]
})
export class BotAdminAppRoutingModule {
}

@NgModule({
  declarations: [BotAdminAppComponent],
  imports: [
    BrowserModule,
    BrowserAnimationsModule,
    HttpClientModule,
    CoreModule,
    BotCoreModule,
    SharedModule,
    BotAdminAppRoutingModule,

    ThemeModule.forRoot(),

    NbSidebarModule.forRoot(),
    NbMenuModule.forRoot(),
    NbDatepickerModule.forRoot(),
    NbDialogModule.forRoot(),
    NbWindowModule.forRoot(),
    NbToastrModule.forRoot(),
    NbThemeModule.forRoot(
              {
                name: 'default',
              })
  ],
  providers: [],
  bootstrap: [BotAdminAppComponent]
})
export class BotAdminAppModule {
}
