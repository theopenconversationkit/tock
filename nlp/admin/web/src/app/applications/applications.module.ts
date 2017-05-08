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

import {SharedModule} from "../shared/shared.module";
import {CommonModule} from "@angular/common";
import {NgModule} from "@angular/core";
import {RouterModule, Routes} from "@angular/router";
import {AuthGuard} from "../core/auth/auth.guard";
import {ApplicationsComponent} from "./applications/applications.component";
import {ApplicationsResolver} from "./applications.resolver";
import {ApplicationComponent} from "./application/application.component";
import {ApplicationUploadComponent} from "./application-upload/application-upload.component";
import {FileUploadModule} from "ng2-file-upload";

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
    ApplicationsRoutingModule,
    FileUploadModule
  ],
  declarations: [
    ApplicationsComponent,
    ApplicationComponent,
    ApplicationUploadComponent
  ],
  providers: [
    ApplicationsResolver
  ],
  entryComponents: []
})
export class ApplicationsModule {

}
