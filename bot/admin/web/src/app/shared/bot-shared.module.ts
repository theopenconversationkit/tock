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

import { NgModule } from '@angular/core';
import { CommonModule } from '@angular/common';
import { BotSharedService } from './bot-shared.service';
import { MomentModule } from 'ngx-moment';
import {
  NbCalendarRangeModule,
  NbCardModule,
  NbIconModule,
  NbSelectModule,
  NbTooltipModule,
  NbPopoverModule,
  NbButtonModule,
  NbAlertModule,
  NbSpinnerModule,
  NbCheckboxModule,
  NbInputModule,
  NbFormFieldModule,
  NbAutocompleteModule,
  NbRadioModule,
  NbWindowModule,
  NbListModule,
  NbToggleModule,
  NbDatepickerModule
} from '@nebular/theme';

import {
  AutocompleteInputComponent,
  ChatUiComponent,
  ChatUiMessageAttachmentComponent,
  ChatUiMessageChoiceComponent,
  ChatUiMessageComponent,
  ChatUiMessageDebugComponent,
  ChatUiMessageLocationComponent,
  ChatUiMessageSentenceComponent,
  ChatUiMessageSentenceElementComponent,
  ChatUiMessageSentenceFootnotesComponent,
  ChoiceDialogComponent,
  DebugViewerDialogComponent,
  DebugViewerWindowComponent,
  ErrorHelperComponent,
  FileUploadComponent,
  FormControlComponent,
  JsonIteratorComponent,
  NoDataFoundComponent,
  PaginationComponent,
  SentenceReviewRequestComponent,
  SentenceTrainingComponent,
  SentenceTrainingCreateEntityComponent,
  SentenceTrainingDialogComponent,
  SentenceTrainingFiltersComponent,
  SentenceTrainingEntryComponent,
  SentenceTrainingSentenceComponent,
  IntentStoryDetailsComponent,
  SliderComponent,
  TokenViewComponent,
  ScrollTopButtonComponent,
  StickyMenuComponent,
  AiSettingsEngineConfigParamInputComponent,
  SentencesGenerationComponent,
  SentencesGenerationListComponent,
  SentencesGenerationOptionsComponent,
  InfoButtonComponent,
  SelectBotComponent,
  DateRangeCalendarComponent,
  DataExportComponent,
  WysiwygEditorComponent,
  TestDialogComponent,
  BotConfigurationSelectorComponent
} from './components';

import { AutofocusDirective } from './directives';
import { InfiniteScrollModule } from 'ngx-infinite-scroll';
import { FormsModule, ReactiveFormsModule } from '@angular/forms';
import { AnalyticsService } from '../analytics/analytics.service';
import { NgxSliderModule } from '@angular-slider/ngx-slider';
import { ScrollComponent } from '../scroll/scroll.component';

@NgModule({
  imports: [
    CommonModule,
    FormsModule,
    ReactiveFormsModule,
    MomentModule,
    NbDatepickerModule,
    NbCalendarRangeModule,
    NbCardModule,
    NbSelectModule,
    NbTooltipModule,
    NbIconModule,
    NbPopoverModule,
    NbButtonModule,
    NbSpinnerModule,
    NbCheckboxModule,
    NbInputModule,
    NbFormFieldModule,
    InfiniteScrollModule,
    NbAutocompleteModule,
    NbAlertModule,
    NbRadioModule,
    NgxSliderModule,
    NbWindowModule,
    NbListModule,
    NbToggleModule
  ],
  declarations: [
    SelectBotComponent,
    DateRangeCalendarComponent,
    InfoButtonComponent,
    ErrorHelperComponent,
    PaginationComponent,
    NoDataFoundComponent,
    FormControlComponent,
    ChatUiComponent,
    ChatUiMessageComponent,
    ChatUiMessageSentenceComponent,
    ChatUiMessageSentenceElementComponent,
    ChatUiMessageChoiceComponent,
    ChatUiMessageAttachmentComponent,
    ChatUiMessageLocationComponent,
    ChatUiMessageDebugComponent,
    ChatUiMessageSentenceFootnotesComponent,
    ChoiceDialogComponent,
    FileUploadComponent,
    SliderComponent,
    SentenceTrainingComponent,
    SentenceTrainingDialogComponent,
    SentenceTrainingFiltersComponent,
    SentenceTrainingEntryComponent,
    SentenceTrainingSentenceComponent,
    SentenceTrainingCreateEntityComponent,
    IntentStoryDetailsComponent,
    TokenViewComponent,
    SentenceReviewRequestComponent,
    AutocompleteInputComponent,
    JsonIteratorComponent,
    DebugViewerDialogComponent,
    DebugViewerWindowComponent,
    ScrollTopButtonComponent,
    AutofocusDirective,
    StickyMenuComponent,
    AiSettingsEngineConfigParamInputComponent,
    SentencesGenerationOptionsComponent,
    SentencesGenerationListComponent,
    SentencesGenerationComponent,
    ScrollComponent,
    DataExportComponent,
    WysiwygEditorComponent,
    TestDialogComponent,
    BotConfigurationSelectorComponent
  ],
  exports: [
    SelectBotComponent,
    DateRangeCalendarComponent,
    InfoButtonComponent,
    ErrorHelperComponent,
    PaginationComponent,
    NoDataFoundComponent,
    FormControlComponent,
    ChatUiComponent,
    ChatUiMessageComponent,
    ChoiceDialogComponent,
    FileUploadComponent,
    SliderComponent,
    SentenceTrainingComponent,
    SentenceTrainingEntryComponent,
    SentenceTrainingSentenceComponent,
    SentenceTrainingDialogComponent,
    AutocompleteInputComponent,
    JsonIteratorComponent,
    DebugViewerDialogComponent,
    DebugViewerWindowComponent,
    ScrollTopButtonComponent,
    AutofocusDirective,
    StickyMenuComponent,
    AiSettingsEngineConfigParamInputComponent,
    SentencesGenerationComponent,
    DataExportComponent,
    WysiwygEditorComponent
  ],
  providers: [BotSharedService, AnalyticsService]
})
export class BotSharedModule {}
