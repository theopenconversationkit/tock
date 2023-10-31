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

import { NgModule } from '@angular/core';
import { NlpAdminAppComponent } from './nlp-admin-app.component';
import { SharedModule } from './shared-nlp/shared.module';
import { BrowserModule } from '@angular/platform-browser';
import { CoreModule } from './core-nlp/core.module';
import { BrowserAnimationsModule } from '@angular/platform-browser/animations';
import { HttpClientModule } from '@angular/common/http';
import { ThemeModule } from './theme/theme.module';
import {
  NbDatepickerModule,
  NbDialogModule,
  NbMenuModule,
  NbSidebarModule,
  NbToastrModule,
  NbWindowModule,
  NbThemeModule
} from '@nebular/theme';
import { APP_BASE_HREF, PlatformLocation } from '@angular/common';
import { NlpAdminAppRoutingModule } from './nlp-admin-app-routing.module';

@NgModule({
  declarations: [NlpAdminAppComponent],
  imports: [
    BrowserModule,
    BrowserAnimationsModule,
    HttpClientModule,
    CoreModule,
    SharedModule,
    NlpAdminAppRoutingModule,
    ThemeModule.forRoot(),
    NbSidebarModule.forRoot(),
    NbMenuModule.forRoot(),
    NbDatepickerModule.forRoot(),
    NbDialogModule.forRoot(),
    NbWindowModule.forRoot(),
    NbToastrModule.forRoot(),
    NbThemeModule.forRoot({
      name: 'default'
    })
  ],
  providers: [
    {
      provide: APP_BASE_HREF,
      useFactory: (s: PlatformLocation) => s.getBaseHrefFromDOM(),
      deps: [PlatformLocation]
    }
  ],
  bootstrap: [NlpAdminAppComponent]
})
export class NlpAdminAppModule {}
