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
import {CommonModule} from "@angular/common";
import {InfiniteScrollModule} from "ngx-infinite-scroll";
import {SharedModule} from "../shared-nlp/shared.module";
import {AuthGuard} from "../core-nlp/auth/auth.guard";
import {MonitoringTabsComponent} from "./monitoring-tabs.component";
import {ApplicationResolver} from "../core-nlp/application.resolver";
import {UserTimelinesComponent} from "./users/user-timelines.component";
import {MonitoringService} from "./monitoring.service";
import {BotSharedModule} from "../shared/bot-shared.module";
import {MomentModule} from "angular2-moment";
import {DialogsComponent} from "./dialogs/dialogs.component";
import {MatDatepickerModule, MatNativeDateModule} from "@angular/material";


const routes: Routes = [
  {
    path: '',
    canActivate: [AuthGuard],
    component: MonitoringTabsComponent,
    resolve: {
      application: ApplicationResolver
    },
    children: [
      {
        path: '',
        component: UserTimelinesComponent
      },
      {
        path: 'users',
        component: UserTimelinesComponent
      },
      {
        path: 'dialogs',
        component: DialogsComponent
      }
    ]
  }
];
@NgModule({
  imports: [RouterModule.forChild(routes)],
  exports: [RouterModule]
})
export class MonitoringRoutingModule {
}

@NgModule({
  imports: [
    CommonModule,
    SharedModule,
    MonitoringRoutingModule,
    InfiniteScrollModule,
    MomentModule,
    BotSharedModule,
    MatDatepickerModule,
    MatNativeDateModule
  ],
  declarations: [
    MonitoringTabsComponent,
    UserTimelinesComponent,
    DialogsComponent
  ],
  exports: [],
  providers: [
    MonitoringService
  ],
  entryComponents: []
})
export class BotMonitoringModule {
}
