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

import {RouterModule, Routes} from "@angular/router";
import {NgModule} from "@angular/core";
import {SharedModule} from "tock-nlp-admin/src/app/shared/shared.module";
import {AuthGuard} from "tock-nlp-admin/src/app/core/auth/auth.guard";
import {TestTabsComponent} from "./test-tabs.component";
import {ApplicationResolver} from "tock-nlp-admin/src/app/core/application.resolver";
import {BotDialogComponent} from "./dialog/bot-dialog.component";
import {CommonModule} from "@angular/common";
import {TestService} from "./test.service";
import {BotSharedModule} from "../shared/bot-shared.module";
import {TestPlanComponent} from "./plan/test-plan.component";
import {MomentModule} from "angular2-moment";

const routes: Routes = [
  {
    path: '',
    canActivate: [AuthGuard],
    component: TestTabsComponent,
    resolve: {
      application: ApplicationResolver
    },
    children: [
      {
        path: '',
        component: BotDialogComponent
      },
      {
        path: 'test',
        component: BotDialogComponent
      },
      {
        path: 'plan',
        component: TestPlanComponent
      }
    ]
  }
];
@NgModule({
  imports: [RouterModule.forChild(routes)],
  exports: [RouterModule]
})
export class BotTestRoutingModule {
}

@NgModule({
  imports: [
    CommonModule,
    SharedModule,
    BotTestRoutingModule,
    BotSharedModule,
    MomentModule
  ],
  declarations: [
    TestTabsComponent,
    BotDialogComponent,
    TestPlanComponent
  ],
  exports: [],
  providers: [
    TestService
  ],
  entryComponents: []
})
export class BotTestModule {
}
