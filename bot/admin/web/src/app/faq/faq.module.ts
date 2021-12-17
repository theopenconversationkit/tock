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
  NbCalendarRangeModule,
  NbCardModule,
  NbCheckboxModule,
  NbContextMenuModule,
  NbDatepickerModule,
  NbDialogModule,
  NbInputModule,
  NbListModule,
  NbMenuModule,
  NbRadioModule,
  NbRouteTabsetModule,
  NbSelectModule,
  NbSidebarModule,
  NbSpinnerModule,
  NbTooltipModule,
  NbUserModule
} from "@nebular/theme";
import {TrainComponent} from './train/train.component';
import {TrainHeaderComponent} from './train/train-header/train-header.component';
import {IntentsService} from "./common/intents.service";
import {SentencesService} from "./common/sentences.service";
import {ClickCaptureDirective} from './common/directives/click-capture.directive';
import {NgModelChangeDebouncedDirective} from './common/directives/ng-model-change-debounced.directive';

import {TrainToolbarComponent} from './train/train-toolbar/train-toolbar.component';
import {TrainSidebarComponent} from './train/train-sidebar/train-sidebar.component';
import {TrainGridItemComponent} from "./train/train-grid-item/train-grid-item.component";
import {TrainGridComponent} from "./train/train-grid/train-grid.component";
import { BotAnalyticsModule } from "../analytics/analytics.module";
import { QaComponent } from './qa/qa.component';
import { QaHeaderComponent } from './qa/qa-header/qa-header.component';
import { QaGridComponent } from './qa/qa-grid/qa-grid.component';
import { QaGridItemComponent } from './qa/qa-grid-item/qa-grid-item.component';
import { QaService } from "./common/qa.service";
import { InviewSidebarComponent } from './common/components/inview-sidebar/inview-sidebar.component';
import { QaSidebarImportContentComponent } from './qa/sidebars/qa-sidebar-import-content/qa-sidebar-import-content.component';
import { QaSidebarNewContentComponent } from './qa/sidebars/qa-sidebar-new-content/qa-sidebar-new-content.component';

const routes: Routes = [
  {
    path: 'train',
    component: TrainComponent,
    canActivate: [AuthGuard],
    resolve: {
      application: ApplicationResolver
    },
  },
  {
    path: 'qa',
    component: QaComponent,
    canActivate: [AuthGuard],
    resolve: {
      application: ApplicationResolver
    },
  }
];

@NgModule({
  imports: [RouterModule.forChild(routes)],
  exports: [RouterModule],
  declarations: []
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
    NbRadioModule,
    NbSidebarModule.forRoot(),
    BotAnalyticsModule
  ],
  declarations: [
    TrainComponent,
    TrainGridComponent,
    TrainHeaderComponent,
    TrainGridItemComponent,
    ClickCaptureDirective,
    NgModelChangeDebouncedDirective,
    TrainToolbarComponent,
    TrainSidebarComponent,
    QaComponent,
    QaHeaderComponent,
    QaGridComponent,
    QaGridItemComponent,
    InviewSidebarComponent,
    QaSidebarImportContentComponent,
    QaSidebarNewContentComponent

  ],
  exports: [],
  providers: [
    IntentsService,
    SentencesService,
    QaService
  ],
  entryComponents: []
})
export class FaqModule {
}
