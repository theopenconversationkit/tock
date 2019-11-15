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

import {SharedModule} from "../shared-nlp/shared.module";
import {CommonModule} from "@angular/common";
import {NgModule} from "@angular/core";
import {RouterModule, Routes} from "@angular/router";
import {AuthGuard} from "../core-nlp/auth/auth.guard";
import {ApplicationsComponent} from "./applications/applications.component";
import {ApplicationsResolver} from "./applications.resolver";
import {ApplicationComponent} from "./application/application.component";
import {ApplicationUploadComponent} from "./application-upload/application-upload.component";
import {FileUploadModule} from "ng2-file-upload";
import {ApplicationAdvancedOptionsComponent} from "./application-advanced-options/application-advanced-options.component";
import {
  NbAccordionModule,
  NbActionsModule,
  NbButtonModule,
  NbCardModule,
  NbCheckboxModule,
  NbRadioModule,
  NbSelectModule,
  NbSpinnerModule,
  NbTooltipModule
} from "@nebular/theme";
import {DisplayUserDataComponent, UserLogsComponent} from "./user/user-logs.component";
import {MomentModule} from "ngx-moment";

const routes: Routes = [
  {
    path: '',
    canActivate: [AuthGuard],
    resolve: {
      applications: ApplicationsResolver
    },
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
      },
      {
        path: 'users/logs',
        component: UserLogsComponent
      }]
  }
];

@NgModule({
  imports: [RouterModule.forChild(routes)],
  exports: [RouterModule]
})
export class ApplicationsRoutingModule {
}

@NgModule({
  imports: [
    CommonModule,
    SharedModule,
    MomentModule,
    ApplicationsRoutingModule,
    FileUploadModule,
    NbCardModule,
    NbActionsModule,
    NbButtonModule,
    NbTooltipModule,
    NbCheckboxModule,
    NbSelectModule,
    NbAccordionModule,
    NbSpinnerModule,
    NbRadioModule
  ],
  declarations: [
    ApplicationsComponent,
    ApplicationComponent,
    ApplicationAdvancedOptionsComponent,
    ApplicationUploadComponent,
    UserLogsComponent,
    DisplayUserDataComponent
  ],
  providers: [
    ApplicationsResolver
  ],
  entryComponents: [DisplayUserDataComponent]
})
export class ApplicationsModule {

}
