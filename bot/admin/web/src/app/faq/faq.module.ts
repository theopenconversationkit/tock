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

import {RouterModule, Routes} from "@angular/router";
import {NgModule} from "@angular/core";
import {CommonModule} from "@angular/common";
import {InfiniteScrollModule} from "ngx-infinite-scroll";
import {SharedModule} from "../shared-nlp/shared.module";
import {AuthGuard} from "../core-nlp/auth/auth.guard";
import {ApplicationResolver} from "../core-nlp/application.resolver";
import {BotSharedModule} from "../shared/bot-shared.module";
import {BotModule} from "../bot/bot.module";
import {NlpModule} from '../nlp-tabs/nlp.module';
import {MomentModule} from "ngx-moment";
import {MatNativeDateModule} from "@angular/material/core";
import {MatDatepickerModule} from "@angular/material/datepicker";
import {
  NbAccordionModule,
  NbButtonModule,
  NbCalendarModule,
  NbCardModule,
  NbCheckboxModule,
  NbContextMenuModule,
  NbDatepickerModule,
  NbInputModule,
  NbListModule,
  NbMenuModule,
  NbRouteTabsetModule,
  NbSelectModule,
  NbSpinnerModule,
  NbTooltipModule,
  NbUserModule,
  NbCalendarRangeModule,
  NbDialogModule,
  NbRadioModule
} from "@nebular/theme";
import {TrainComponent} from './train/train.component';
import { TrainGridComponent } from './train/train-grid/train-grid.component';
import { TrainHeaderComponent } from './train/train-header/train-header.component';
import { TrainGridItemComponent } from './train/train-grid-item/train-grid-item.component';
import { TrainGridItemBtnComponent } from './train/train-grid-item-btn/train-grid-item-btn.component';
import {IntentsService} from "./common/intents.service";

const routes: Routes = [
  {
    path: 'train',
    component: TrainComponent,
    canActivate: [AuthGuard],
    resolve: {
      application: ApplicationResolver
    },
  }
];

@NgModule({
  imports: [RouterModule.forChild(routes)],
  exports: [RouterModule],
  declarations: [TrainGridItemBtnComponent]
})
export class FaqRoutingModule {
}

@NgModule({
  imports: [
    CommonModule,
    SharedModule,
    FaqRoutingModule,
    InfiniteScrollModule,
    MomentModule,
    BotSharedModule,
    BotModule,
    NlpModule,
    MatDatepickerModule,
    MatNativeDateModule,
    NbRouteTabsetModule,
    NbCheckboxModule,
    NbCardModule,
    NbTooltipModule,
    NbSpinnerModule,
    NbButtonModule,
    NbInputModule,
    NbSelectModule,
    NbCalendarModule,
    NbUserModule,
    NbDatepickerModule,
    NbListModule,
    NbAccordionModule,
    NbContextMenuModule,
    NbMenuModule.forRoot(),
    NbCalendarRangeModule,
    NbDialogModule.forRoot(),
    NbRadioModule
  ],
  declarations: [
    TrainComponent,TrainGridComponent,TrainHeaderComponent,TrainGridItemComponent
  ],
  exports: [],
  providers: [
    IntentsService
  ],
  entryComponents: []
})
export class FaqModule {
}
