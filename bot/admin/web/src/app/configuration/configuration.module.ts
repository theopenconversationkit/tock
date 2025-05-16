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

import { ApplicationsModule } from '../applications/applications.module';
import { Injectable, NgModule } from '@angular/core';
import { BotConfigurationsComponent } from './bot-configurations/bot-configurations.component';
import { ConfigurationTabsComponent } from './configuration-tabs.component';
import { CommonModule } from '@angular/common';
import { BotSharedModule } from '../shared/bot-shared.module';
import { BotConfigurationComponent } from './bot-configurations/bot-configuration/bot-configuration.component';
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
  NbStepperModule,
  NbFormFieldModule,
  NbRadioModule,
  NbCheckboxModule,
  NbAlertModule,
  NbToggleModule
} from '@nebular/theme';
import { NewBotComponent } from './bot-configurations/new-bot.component';
import { FormsModule, ReactiveFormsModule } from '@angular/forms';
import { BotSharedService } from '../shared/bot-shared.service';
import { ApplicationConfig } from '../applications/application.config';
import { SelectBotConfigurationDialogComponent } from './bot-configurations/selection-dialog/select-bot-configuration-dialog.component';
import { BotConfigurationRoutingModule } from './configuration-routing.module';
import { SynchronizationComponent } from './synchronization/synchronization.component';
import { SentenceGenerationSettingsComponent } from './sentence-generation-settings/sentence-generation-settings.component';
import { ObservabilitySettingsComponent } from './observability-settings/observability-settings.component';
import { VectorDbSettingsComponent } from './vector-db-settings/vector-db-settings.component';
import { CompressorSettingsComponent } from './compressor-settings/compressor-settings.component';

@Injectable()
export class BotApplicationConfig implements ApplicationConfig {
  constructor(private botSharedService: BotSharedService) {}

  /** is it allowed to create namespace? **/
  canCreateNamespace(): boolean {
    return this.botSharedService.configuration && !this.botSharedService.configuration.botApiSupport;
  }
}

@NgModule({
  declarations: [
    ConfigurationTabsComponent,
    BotConfigurationsComponent,
    BotConfigurationComponent,
    NewBotComponent,
    SelectBotConfigurationDialogComponent,
    SynchronizationComponent,
    SentenceGenerationSettingsComponent,
    ObservabilitySettingsComponent,
    VectorDbSettingsComponent,
    CompressorSettingsComponent
  ],
  imports: [
    CommonModule,
    FormsModule,
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
    NbStepperModule,
    NbFormFieldModule,
    NbRadioModule,
    NbCheckboxModule,
    NbAlertModule,
    NbToggleModule
  ],
  providers: [
    {
      provide: ApplicationConfig,
      useClass: BotApplicationConfig
    }
  ]
})
export class BotConfigurationModule {}
