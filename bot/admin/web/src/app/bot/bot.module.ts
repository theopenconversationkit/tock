/*
 * Copyright (C) 2017/2025 SNCF Connect & Tech
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

import { NgModule } from '@angular/core';
import { CommonModule } from '@angular/common';
import { InfiniteScrollModule } from 'ngx-infinite-scroll';
import { BotSharedModule } from '../shared/bot-shared.module';
import { BotConfigurationModule } from '../configuration/configuration.module';
import { MomentModule } from 'ngx-moment';
import { CreateStoryComponent } from './story/create-story/create-story.component';
import { BotTabsComponent } from './bot-tabs.component';
import { SearchStoryComponent } from './story/search-story/search-story.component';
import { I18nComponent } from './i18n/i18n.component';
import { FileUploadModule } from 'ng2-file-upload';
import { FeatureComponent } from './feature/feature.component';
import { StoryComponent } from './story/story.component';
import { AnswerComponent, AnswerDialogComponent, ScriptAnswerComponent, SimpleAnswerComponent } from './story/answer';
import { StoryDialogComponent } from './story/story-dialog/story-dialog.component';
import { MandatoryEntitiesDialogComponent } from './story/mandatory-entities/mandatory-entities-dialog.component';
import { StepComponent, StepDialogComponent, StepsComponent } from './story/action';
import { MediaDialogComponent } from './story/media/media-dialog.component';
import { ThemeModule } from '../theme/theme.module';
import {
  NbAccordionModule,
  NbAlertModule,
  NbAutocompleteModule,
  NbBadgeModule,
  NbButtonModule,
  NbCardModule,
  NbCheckboxModule,
  NbDatepickerModule,
  NbDialogModule,
  NbFormFieldModule,
  NbIconModule,
  NbInputModule,
  NbListModule,
  NbRadioModule,
  NbRouteTabsetModule,
  NbSelectModule,
  NbSpinnerModule,
  NbTagModule,
  NbToggleModule,
  NbTooltipModule,
  NbTreeGridModule
} from '@nebular/theme';
import { ApplicationFeatureComponent } from './feature/application-feature/application-feature.component';
import { StoryRuleComponent } from './feature/story-rule/story-rule.component';
import { SelectEntityDialogComponent } from './story/select-entity-dialog/select-entity-dialog.component';
import { StoryRuntimeSettingsComponent } from './feature/story-runtime-settings/story-runtime-settings.component';
import { StoryTagComponent } from './story/story-tag/story-tag.component';
import { ApplicationFeaturesTableComponent } from './feature/application-feature/application-features-table/application-features-table.component';

import { StoriesListComponent } from './story/search-story/stories-list/stories-list.component';
import { StoriesFilterComponent } from './story/search-story/stories-filter/stories-filter.component';
import { FormsModule, ReactiveFormsModule } from '@angular/forms';
import { EditStoryComponent } from './story/edit-story/edit-story.component';
import { StoriesUploadComponent } from './story/search-story/stories-upload/stories-upload.component';
import { BotRoutingModule } from './bot-routing.module';
import { CreateFeatureComponent } from './feature/application-feature/create-feature/create-feature.component';
import { CreateRuleComponent } from './feature/story-rule/create-rule/create-rule.component';
import { StoryRulesTableComponent } from './feature/story-rule/story-rule-table/story-rules-table.component';
import { I18nFiltersComponent } from './i18n/i18n-filters/i18n-filters.component';
import { I18nExportComponent } from './i18n/i18n-export/i18n-export.component';
import { I18nImportComponent } from './i18n/i18n-import/i18n-import.component';
import { I18nLabelComponent } from './i18n/i18n-label/i18n-label.component';
import { DocumentsStoryComponent } from './story/documents-story/documents-story.component';
import { CreateEntityDialogComponent } from './story/create-entity-dialog/create-entity-dialog.component';

@NgModule({
  imports: [
    CommonModule,
    FormsModule,
    ReactiveFormsModule,
    BotSharedModule,
    BotRoutingModule,
    InfiniteScrollModule,
    MomentModule,
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
    NbFormFieldModule,
    NbIconModule,
    NbButtonModule,
    NbDialogModule,
    NbAutocompleteModule,
    NbBadgeModule,
    NbListModule,
    NbTagModule,
    NbAlertModule,
    NbToggleModule,
    NbTreeGridModule,
    NbDatepickerModule
  ],
  declarations: [
    BotTabsComponent,
    CreateStoryComponent,
    SearchStoryComponent,
    DocumentsStoryComponent,
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
    CreateFeatureComponent,
    CreateRuleComponent,
    StoryRulesTableComponent,
    StoryTagComponent,
    StoriesListComponent,
    StoriesFilterComponent,
    EditStoryComponent,
    StoriesUploadComponent,
    I18nFiltersComponent,
    CreateEntityDialogComponent
  ],
  exports: [StoryComponent]
})
export class BotModule {
  constructor() {}
}
