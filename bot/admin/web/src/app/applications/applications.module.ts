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
import { Injectable, NgModule } from '@angular/core';
import { CommonModule } from '@angular/common';
import { ApplicationsComponent } from './applications/applications.component';
import { ApplicationsResolver } from './applications.resolver';
import { ApplicationComponent } from './application/application.component';
import { ApplicationUploadComponent } from './application-upload/application-upload.component';
import { FileUploadModule } from 'ng2-file-upload';
import { ApplicationAdvancedOptionsComponent } from './application-advanced-options/application-advanced-options.component';
import {
  NbAccordionModule,
  NbActionsModule,
  NbButtonModule,
  NbCardModule,
  NbCheckboxModule,
  NbDialogModule,
  NbFormFieldModule,
  NbIconModule,
  NbInputModule,
  NbRadioModule,
  NbRouteTabsetModule,
  NbSelectModule,
  NbSpinnerModule,
  NbTabsetModule,
  NbToggleModule,
  NbTooltipModule
} from '@nebular/theme';
import { UserLogsComponent } from './user/user-logs.component';
import { DisplayUserDataComponent } from './user/display-user-data/display-user-data.component';
import { MomentModule } from 'ngx-moment';
import { ConfigurationTabsComponent } from './configuration-tabs.component';
import { NamespacesComponent } from './namespace/namespaces.component';
import { NgJsonEditorModule } from 'ang-jsoneditor';
import { ApplicationConfig } from './application.config';
import { CreateNamespaceComponent } from './namespace/create-namespace/create-namespace.component';
import { ApplicationsRoutingModule } from './applications-routing.module';
import { FormsModule, ReactiveFormsModule } from '@angular/forms';
import { BotSharedModule } from '../shared/bot-shared.module';

@Injectable()
export class NlpApplicationConfig implements ApplicationConfig {
  /** is it allowed to create namespace? **/
  canCreateNamespace(): boolean {
    return true;
  }
}

@NgModule({
  imports: [
    CommonModule,
    BotSharedModule,
    MomentModule,
    ApplicationsRoutingModule,
    NbTabsetModule,
    NbCardModule,
    NbRouteTabsetModule,
    FileUploadModule,
    NbCardModule,
    NbActionsModule,
    NbButtonModule,
    NbTooltipModule,
    NbCheckboxModule,
    NbSelectModule,
    NbFormFieldModule,
    NbAccordionModule,
    NbSpinnerModule,
    NbRadioModule,
    NbIconModule,
    NbToggleModule,
    NbInputModule,
    NgJsonEditorModule,
    NbDialogModule.forRoot(),
    FormsModule,
    ReactiveFormsModule
  ],
  declarations: [
    ApplicationsComponent,
    ApplicationComponent,
    ApplicationAdvancedOptionsComponent,
    ApplicationUploadComponent,
    UserLogsComponent,
    DisplayUserDataComponent,
    ConfigurationTabsComponent,
    NamespacesComponent,
    CreateNamespaceComponent
  ],
  providers: [
    {
      provide: ApplicationConfig,
      useClass: NlpApplicationConfig
    },
    ApplicationsResolver
  ]
})
export class ApplicationsModule {}
