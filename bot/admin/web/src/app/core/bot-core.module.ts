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

import {CommonModule} from "@angular/common";
import {NgModule, Optional, SkipSelf} from "@angular/core";
import {BotConfigurationService} from "./bot-configuration.service";
import {ApplicationConfig} from "../core-nlp/application.config";

@NgModule({
  imports: [CommonModule],
  declarations: [],
  exports: [],
  providers: [
    {
      provide: ApplicationConfig,
      useValue: {
        configurationUrl: "/configuration",
        displayDialogUrl: "/monitoring/dialogs",
        answerToSentenceUrl : "/build/intent-create"
      }
      },
    BotConfigurationService
  ]
})
export class BotCoreModule {

  constructor(@Optional() @SkipSelf() parentModule: BotCoreModule) {
    if (parentModule) {
      throw new Error(
        'BotCoreModule is already loaded. Import it in the AppModule only');
    }
  }

}
