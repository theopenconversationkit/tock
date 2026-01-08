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

import { BrowserModule } from '@angular/platform-browser';
import { NgModule } from '@angular/core';
import { CoreModule } from './core-nlp/core.module';
import { BotAdminAppComponent } from './bot-admin-app.component';
import { BrowserAnimationsModule } from '@angular/platform-browser/animations';
import { BotCoreModule } from './core/bot-core.module';
import { HttpClientModule } from '@angular/common/http';
import { ThemeModule } from './theme/theme.module';
import {
  NbDatepickerModule,
  NbDialogModule,
  NbMenuModule,
  NbSidebarModule,
  NbToastrModule,
  NbWindowModule,
  NbThemeModule,
  NbIconLibraries,
  NbTimepickerModule
} from '@nebular/theme';

import { APP_BASE_HREF, PlatformLocation } from '@angular/common';
import { BotService } from './bot/bot-service';
import { BotAdminAppRoutingModule } from './bot-admin-app-routing.module';
import { NlpService } from './core-nlp/nlp.service';

@NgModule({
  declarations: [BotAdminAppComponent],
  imports: [
    BrowserModule,
    BrowserAnimationsModule,
    HttpClientModule,
    CoreModule,

    BotCoreModule,
    BotAdminAppRoutingModule,

    ThemeModule.forRoot(),

    NbSidebarModule.forRoot(),
    NbMenuModule.forRoot(),
    NbDatepickerModule.forRoot(),
    NbTimepickerModule.forRoot(),
    NbDialogModule.forRoot(),
    NbWindowModule.forRoot(),
    NbToastrModule.forRoot(),
    NbThemeModule.forRoot({ name: 'default' })
  ],
  providers: [
    {
      provide: APP_BASE_HREF,
      useFactory: (s: PlatformLocation) => s.getBaseHrefFromDOM(),
      deps: [PlatformLocation]
    },
    BotService,
    NlpService
  ],
  bootstrap: [BotAdminAppComponent]
})
export class BotAdminAppModule {
  constructor(private iconLibraries: NbIconLibraries) {
    this.iconLibraries.registerFontPack('bootstrap-icons', { iconClassPrefix: 'bi' });
    this.iconLibraries.setDefaultPack('bootstrap-icons');
  }
}
