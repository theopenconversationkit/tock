/*
 * Copyright (C) 2017/2021 e-voyageurs technologies
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

import { RouterModule, Routes } from '@angular/router';
import { NgModule } from '@angular/core';
import { SharedModule } from '../shared-nlp/shared.module';
import { AuthGuard } from '../core-nlp/auth/auth.guard';
import { TestTabsComponent } from './test-tabs.component';
import { ApplicationResolver } from '../core-nlp/application.resolver';
import { BotDialogComponent, DisplayNlpStatsComponent } from './dialog/bot-dialog.component';
import { CommonModule } from '@angular/common';
import { TestService } from './test.service';
import { BotSharedModule } from '../shared/bot-shared.module';
import { TestPlanComponent } from './plan/test-plan.component';
import { MomentModule } from 'ngx-moment';
import {
  NbAccordionModule,
  NbActionsModule,
  NbButtonModule,
  NbCardModule,
  NbRouteTabsetModule,
  NbSelectModule,
  NbTooltipModule,
  NbInputModule
} from '@nebular/theme';
import { ReactiveFormsModule } from '@angular/forms';

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
        redirectTo: 'test',
        pathMatch: 'full'
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
export class BotTestRoutingModule {}

@NgModule({
    imports: [
        CommonModule,
        SharedModule,
        BotTestRoutingModule,
        BotSharedModule,
        MomentModule,
        NbRouteTabsetModule,
        NbCardModule,
        NbButtonModule,
        NbActionsModule,
        NbSelectModule,
        NbTooltipModule,
        NbAccordionModule,
        NbInputModule,
        ReactiveFormsModule
    ],
    declarations: [TestTabsComponent, BotDialogComponent, TestPlanComponent, DisplayNlpStatsComponent],
    exports: [],
    providers: [TestService]
})
export class BotTestModule {}
