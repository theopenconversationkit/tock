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

import { CommonModule } from '@angular/common';
import { Injectable, NgModule, Optional, SkipSelf } from '@angular/core';
import { BotConfigurationService } from './bot-configuration.service';
import { CoreConfig } from '../core-nlp/core.config';
import { UserRole } from '../model/auth';

@Injectable()
export class BotCoreConfig implements CoreConfig {
  /** url of the configuration menu */
  configurationUrl: string = '/configuration/new';
  /** url of the display dialogs if it exists */
  displayDialogUrl: string = '/analytics/dialogs';
  /** url to answer to sentence if it exists */
  answerToSentenceUrl: string = '/build/story-create';
  /** url map for each default rights */
  roleMap: Map<UserRole, string[]> = new Map([
    [UserRole.nlpUser, ['/nlp', '/configuration']],
    [UserRole.botUser, ['/build', '/configuration']],
    [UserRole.admin, ['/configuration']],
    [UserRole.technicalAdmin, ['/configuration']]
  ]);
}

@NgModule({
  imports: [CommonModule],
  declarations: [],
  exports: [],
  providers: [
    {
      provide: CoreConfig,
      useClass: BotCoreConfig
    },
    BotConfigurationService
  ]
})
export class BotCoreModule {
  constructor(@Optional() @SkipSelf() parentModule: BotCoreModule) {
    if (parentModule) {
      throw new Error('BotCoreModule is already loaded. Import it in the AppModule only');
    }
  }
}
