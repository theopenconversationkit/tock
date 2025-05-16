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

import { Injectable, NgModule, Optional, SkipSelf } from '@angular/core';
import { AuthModule } from './auth/auth.module';
import { SettingsService } from './settings.service';
import { CommonModule } from '@angular/common';
import { RestModule } from './rest/rest.module';
import { StateService } from './state.service';
import { ApplicationService } from './applications.service';
import { CoreConfig } from './core.config';
import { ApplicationResolver } from './application.resolver';
import { DialogService } from './dialog.service';
import { UserRole } from '../model/auth';

@Injectable()
export class NlpCoreConfig implements CoreConfig {
  /** url of the configuration menu */
  configurationUrl: string = '/applications';
  /** url of the display dialogs if it exists */
  displayDialogUrl: string = 'a';
  /** url to answer to sentence if it exists */
  answerToSentenceUrl: string;
  /** url map for each default rights */
  roleMap: Map<UserRole, string[]> = new Map([
    [UserRole.nlpUser, ['/nlp']],
    [UserRole.admin, ['/configuration']],
    [UserRole.technicalAdmin, ['/configuration']]
  ]);
}

@NgModule({
  imports: [CommonModule, RestModule, AuthModule],
  declarations: [],
  providers: [
    {
      provide: CoreConfig,
      useClass: NlpCoreConfig
    },
    SettingsService,
    StateService,
    ApplicationService,
    ApplicationResolver,
    DialogService
  ],
  exports: []
})
export class CoreModule {
  constructor(@Optional() @SkipSelf() parentModule: CoreModule) {
    if (parentModule) {
      throw new Error('CoreModule is already loaded. Import it in the AppModule only');
    }
  }
}
