/*
 * Copyright (C) 2017/2022 e-voyageurs technologies
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
  NbPopoverModule,
  NbRadioModule,
  NbRouteTabsetModule,
  NbSelectModule,
  NbSidebarModule,
  NbSpinnerModule,
  NbTagModule,
  NbTooltipModule,
  NbUserModule
} from "@nebular/theme";
import {TrainComponent} from './train/train.component';
import {TrainHeaderComponent} from './train/train-header/train-header.component';
import {IntentsService} from "./common/intents.service";
import {SentencesService} from "./common/sentences.service";
import {ClickCaptureDirective} from './common/directives/click-capture.directive';
import {NgModelChangeDebouncedDirective} from './common/directives/ng-model-change-debounced.directive';
import {DelayDirective} from './common/directives/delay.directive';

import {TrainToolbarComponent} from './train/train-toolbar/train-toolbar.component';
import {TrainSidepanelComponent} from './train/train-sidepanel/train-sidepanel.component';
import {TrainGridItemComponent} from "./train/train-grid-item/train-grid-item.component";
import {TrainGridComponent} from "./train/train-grid/train-grid.component";
import {BotAnalyticsModule} from "../analytics/analytics.module";
import {FaqDefinitionComponent} from "./faq-definition/faq-definition.component";
import {FaqHeaderComponent} from "./faq-definition/faq-header/faq-header.component";
import {FaqGridComponent} from "./faq-definition/faq-grid/faq-grid.component";
import {FaqGridItemComponent} from "./faq-definition/faq-grid-item/faq-grid-item.component";
import {FaqDefinitionService} from "./common/faq-definition.service";
import {InviewSidepanelComponent} from './common/components/inview-sidepanel/inview-sidepanel.component';
import {FaqSidepanelImportContentComponent} from "./faq-definition/sidepanels/faq-sidepanel-import-content/faq-sidepanel-import-content.component";
import {FaqSidepanelEditorContentComponent} from "./faq-definition/sidepanels/faq-sidepanel-editor-content/faq-sidepanel-editor-content.component";
import {ReactiveFormsModule} from "@angular/forms";
import {EditUtteranceComponent} from './common/components/edit-utterance/edit-utterance.component';
import {FaqDefinitionSidepanelEditorService} from "./faq-definition/sidepanels/faq-definition-sidepanel-editor.service";

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
    component: FaqDefinitionComponent,
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
    NbPopoverModule,
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
    BotAnalyticsModule,
    ReactiveFormsModule,
    NbTagModule
  ],
  declarations: [
    TrainComponent,
    TrainGridComponent,
    TrainHeaderComponent,
    TrainGridItemComponent,
    ClickCaptureDirective,
    NgModelChangeDebouncedDirective,
    DelayDirective,
    TrainToolbarComponent,
    TrainSidepanelComponent,
    FaqDefinitionComponent,
    FaqHeaderComponent,
    FaqGridComponent,
    FaqGridItemComponent,
    InviewSidepanelComponent,
    FaqSidepanelImportContentComponent,
    FaqSidepanelEditorContentComponent,
    EditUtteranceComponent
  ],
  exports: [],
  providers: [
    IntentsService,
    SentencesService,
    FaqDefinitionService,
    FaqDefinitionSidepanelEditorService
  ],
})
export class FaqModule {
}
