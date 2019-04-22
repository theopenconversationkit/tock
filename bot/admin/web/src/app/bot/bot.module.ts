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
import {BotSharedModule} from "../shared/bot-shared.module";
import {MomentModule} from "ngx-moment";
import {CreateStoryComponent} from "./story/create-story.component";
import {BotService} from "./bot-service";
import {BotTabsComponent} from "./bot-tabs.component";
import {SearchStoryComponent} from "./story/search-story.component";
import {NlpModule} from "../nlp-tabs/nlp.module"
import {ApplicationResolver} from "../core-nlp/application.resolver";
import {I18nComponent} from "./i18n/i18n.component";
import {FileUploadModule} from "ng2-file-upload";
import {I18nLabelComponent} from "./i18n/i18n-label.component";
import {FeatureComponent} from "./feature/feature.component";
import {FlowComponent} from "./flow/flow.component";
import {CytoComponent} from "./flow/cyto.component";
import {StoryComponent} from "./story/story.component";
import {AnswerComponent} from "./story/answer.component";
import {SimpleAnswerComponent} from "./story/simple-answer.component";
import {ScriptAnswerComponent} from "./story/script-answer.component";
import {AnswerDialogComponent} from "./story/answer-dialog.component";
import {StoryDialogComponent} from "./story/story-dialog.component";
import {MandatoryEntitiesDialogComponent} from "./story/mandatory-entities-dialog.component";

const routes: Routes = [
  {
    path: '',
    canActivate: [AuthGuard],
    component: BotTabsComponent,
    resolve: {
      application: ApplicationResolver
    },
    children: [
      {
        path: '',
        component: FlowComponent
      },
      {
        path: 'flow',
        component: FlowComponent
      },
      {
        path: 'story-create',
        component: CreateStoryComponent
      },
      {
        path: 'story-search',
        component: SearchStoryComponent
      },
      {
        path: 'i18n',
        component: I18nComponent
      },
      {
        path: 'feature-flipping',
        component: FeatureComponent
      }
    ]
  }
];

@NgModule({
  imports: [RouterModule.forChild(routes)],
  exports: [RouterModule]
})
export class BotRoutingModule {
}

@NgModule({
  imports: [
    CommonModule,
    SharedModule,
    BotSharedModule,
    BotRoutingModule,
    InfiniteScrollModule,
    MomentModule,
    NlpModule,
    FileUploadModule
  ],
  declarations: [
    BotTabsComponent,
    CreateStoryComponent,
    SearchStoryComponent,
    I18nComponent,
    I18nLabelComponent,
    FeatureComponent,
    FlowComponent,
    CytoComponent,
    StoryComponent,
    AnswerComponent,
    SimpleAnswerComponent,
    ScriptAnswerComponent,
    StoryDialogComponent,
    AnswerDialogComponent,
    MandatoryEntitiesDialogComponent
  ],
  exports: [],
  providers: [
    BotService
  ],
  entryComponents: [
    StoryDialogComponent,
    AnswerDialogComponent,
    MandatoryEntitiesDialogComponent
  ]
})
export class BotModule {
}
