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

import {SharedModule} from "../../shared-nlp/shared.module";
import {NgModule} from "@angular/core";
import {LoginComponent} from "./login/login.component";
import {AuthService} from "./auth.service";
import {AuthGuard} from "./auth.guard";
import {RouterModule, Routes} from "@angular/router";
import {CommonModule} from "@angular/common";
import {RestModule} from "../rest/rest.module";
import {NbCardModule, NbCheckboxModule} from "@nebular/theme";

const authRoutes: Routes = [
  {
    path: 'login',
    component: LoginComponent
  }
];

@NgModule({
  imports: [RouterModule.forChild(authRoutes)],
  exports: [RouterModule]
})
export class AuthRoutingModule {
}

@NgModule({
  imports: [
    CommonModule,
    SharedModule,
    RestModule,
    AuthRoutingModule,
    NbCardModule,
    NbCheckboxModule
  ],
  declarations: [
    LoginComponent
  ],
  providers: [
    AuthService, AuthGuard
  ]
})
export class AuthModule {
}
