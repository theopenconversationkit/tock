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
import { CommonModule } from '@angular/common';
import { InfiniteScrollModule } from 'ngx-infinite-scroll';
import { SharedModule } from '../shared-nlp/shared.module';
import { AuthGuard } from '../core-nlp/auth/auth.guard';
import { BotSharedModule } from '../shared/bot-shared.module';
import { BotConfigurationModule } from '../configuration/configuration.module';
import { MomentModule } from 'ngx-moment';
import { CreateStoryComponent } from './story/create-story.component';
import { BotService } from './bot-service';
import { BotTabsComponent } from './bot-tabs.component';
import { BackButtonHolder, SearchStoryComponent, SearchStoryNavigationGuard } from './story/search-story.component';
import { NlpModule } from '../nlp-tabs/nlp.module';
import { ApplicationResolver } from '../core-nlp/application.resolver';
import { I18nComponent } from './i18n/i18n.component';
import { FileUploadModule } from 'ng2-file-upload';
import { I18nLabelComponent } from './i18n/i18n-label.component';
import { FeatureComponent } from './feature/feature.component';
import { StoryComponent } from './story/story.component';
import { AnswerComponent } from './story/answer.component';
import { SimpleAnswerComponent } from './story/simple-answer.component';
import { ScriptAnswerComponent } from './story/script-answer.component';
import { AnswerDialogComponent } from './story/answer-dialog.component';
import { StoryDialogComponent } from './story/story-dialog.component';
import { MandatoryEntitiesDialogComponent } from './story/mandatory-entities-dialog.component';
import { StepDialogComponent } from './story/step-dialog.component';
import { StepsComponent } from './story/steps.component';
import { StepComponent } from './story/step.component';
import { MediaDialogComponent } from './story/media/media-dialog.component';
import { ThemeModule } from '../theme/theme.module';
import {
  NbAccordionModule,
  NbBadgeModule,
  NbButtonModule,
  NbCardModule,
  NbCheckboxModule,
  NbFormFieldModule,
  NbIconModule,
  NbInputModule,
  NbRadioModule,
  NbRouteTabsetModule,
  NbSelectModule,
  NbSpinnerModule,
  NbTooltipModule
} from '@nebular/theme';
import { ApplicationFeatureComponent } from './feature/application-feature.component';
import { StoryRuleComponent } from './feature/story-rule.component';
import { SelectEntityDialogComponent } from './story/select-entity-dialog.component';
import { StoryRuntimeSettingsComponent } from './feature/story-runtime-settings.component';
import { StoryTagComponent } from './story/story-tag.component';
import { MatCardModule } from '@angular/material/card';
import { MatIconModule } from '@angular/material/icon';
import { MatGridListModule } from '@angular/material/grid-list';
import { I18nExportComponent } from './i18n/i18n-export.component';
import { I18nImportComponent } from './i18n/i18n-import.component';
import { ApplicationFeaturesTableComponent } from './feature/application-features-table.component';

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
        redirectTo: 'story-create',
        pathMatch: 'full'
      },
      {
        path: 'story-create',
        component: CreateStoryComponent
      },
      {
        path: 'story-search',
        component: SearchStoryComponent,
        canDeactivate: [SearchStoryNavigationGuard]
      },
      {
        path: 'i18n',
        component: I18nComponent
      },
      {
        path: 'story-rules',
        component: FeatureComponent
      }
    ]
  }
];

@NgModule({
  imports: [RouterModule.forChild(routes)],
  exports: [RouterModule]
})
export class BotRoutingModule {}

@NgModule({
    imports: [
        CommonModule,
        SharedModule,
        BotSharedModule,
        BotRoutingModule,
        InfiniteScrollModule,
        MomentModule,
        NlpModule,
        FileUploadModule,
        BotConfigurationModule,
        ThemeModule,
        NbRouteTabsetModule,
        NbBadgeModule,
        NbCardModule,
        NbSpinnerModule,
        NbCheckboxModule,
        NbAccordionModule,
        NbSelectModule,
        NbTooltipModule,
        NbInputModule,
        NbRadioModule,
        MatCardModule,
        MatIconModule,
        MatGridListModule,
        NbFormFieldModule,
        NbIconModule,
        NbButtonModule
    ],
    declarations: [
        BotTabsComponent,
        CreateStoryComponent,
        SearchStoryComponent,
        I18nComponent,
        I18nLabelComponent,
        I18nExportComponent,
        I18nImportComponent,
        FeatureComponent,
        StoryComponent,
        AnswerComponent,
        SimpleAnswerComponent,
        ScriptAnswerComponent,
        StoryDialogComponent,
        AnswerDialogComponent,
        MandatoryEntitiesDialogComponent,
        SelectEntityDialogComponent,
        StepDialogComponent,
        StepComponent,
        StepsComponent,
        MediaDialogComponent,
        ApplicationFeatureComponent,
        ApplicationFeaturesTableComponent,
        StoryRuleComponent,
        StoryRuntimeSettingsComponent,
        StoryTagComponent
    ],
    exports: [],
    providers: [BotService, BackButtonHolder, SearchStoryNavigationGuard]
})
export class BotModule {
  constructor() {}
}
