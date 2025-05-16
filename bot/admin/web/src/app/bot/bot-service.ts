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

import { Injectable } from '@angular/core';
import { RestService } from '../core-nlp/rest/rest.service';
import { CreateStoryRequest, StoryDefinitionConfiguration, StoryDefinitionConfigurationSummary, StorySearchQuery } from './model/story';
import { Intent, TranslateReport } from '../model/nlp';
import { Observable } from 'rxjs';
import { CreateI18nLabelRequest, I18LabelQuery, I18nLabel, I18nLabels } from './model/i18n';
import { FileUploader } from 'ng2-file-upload';
import { Feature } from './model/feature';

@Injectable()
export class BotService {
  constructor(private rest: RestService) {}

  newStory(request: CreateStoryRequest): Observable<Intent> {
    return this.rest.post('/bot/story/new', request, Intent.fromJSON);
  }

  getStories(request: StorySearchQuery): Observable<StoryDefinitionConfiguration[]> {
    return this.rest.post('/bot/story/load', request, StoryDefinitionConfiguration.fromJSONArray);
  }

  searchStories(request: StorySearchQuery): Observable<StoryDefinitionConfigurationSummary[]> {
    return this.rest.post('/bot/story/search', request, StoryDefinitionConfigurationSummary.fromJSONArray);
  }

  exportStories(applicationName: string): Observable<Blob> {
    return this.rest.get(`/bot/story/${applicationName}/export`, (j) => new Blob([JSON.stringify(j)], { type: 'application/json' }));
  }

  exportStory(applicationName: string, storyDefinitionId: string): Observable<Blob> {
    return this.rest.get(
      `/bot/story/${applicationName}/export/${storyDefinitionId}`,
      (j) => new Blob([JSON.stringify(j)], { type: 'application/json' })
    );
  }

  saveStory(story: StoryDefinitionConfiguration): Observable<StoryDefinitionConfiguration> {
    return this.rest.post('/bot/story', story.prepareBeforeSend(), StoryDefinitionConfiguration.fromJSON);
  }

  findStory(storyDefinitionId: string): Observable<StoryDefinitionConfiguration> {
    return this.rest.get(`/bot/story/${storyDefinitionId}`, StoryDefinitionConfiguration.fromJSON);
  }

  findStoryDefinitionsByNamespaceAndBotIdWithFileAttached(botId: string): Observable<StoryDefinitionConfiguration[]> {
    return this.rest.get(`/bot/story/${botId}/with_document`, StoryDefinitionConfiguration.fromJSONArray);
  }
  findRuntimeStorySettings(botId: string): Observable<StoryDefinitionConfiguration[]> {
    return this.rest.get(`/bot/story/${botId}/settings`, StoryDefinitionConfiguration.fromJSONArray);
  }

  findStoryByBotIdAndIntent(botId: string, intent: string): Observable<StoryDefinitionConfiguration> {
    return this.rest.get(`/bot/story/${botId}/${intent}`, StoryDefinitionConfiguration.fromJSON);
  }

  deleteStory(storyDefinitionId: string): Observable<boolean> {
    return this.rest.delete(`/bot/story/${storyDefinitionId}`);
  }

  i18nLabels(): Observable<I18nLabels> {
    return this.rest.get('/i18n', I18nLabels.fromJSON);
  }

  completeI18nLabels(labels: I18nLabel[]): Observable<TranslateReport> {
    return this.rest.post('/i18n/complete', labels, TranslateReport.fromJSON, null, true);
  }

  saveI18nLabels(labels: I18nLabel[]): Observable<boolean> {
    return this.rest.post('/i18n/saveAll', labels);
  }

  saveI18nLabel(label: I18nLabel): Observable<boolean> {
    return this.rest.post('/i18n/save', label);
  }

  createI18nLabel(request: CreateI18nLabelRequest): Observable<I18nLabel> {
    return this.rest.post('/i18n/create', request, I18nLabel.fromJSON);
  }

  duplicateLabel(clonedLabel: I18nLabel, callback: (i18n) => void) {
    if (clonedLabel) {
      this.createI18nLabel(
        new CreateI18nLabelRequest(clonedLabel.category, clonedLabel.defaultLocalizedLabel().label, clonedLabel.defaultLocale)
      ).subscribe(callback);
    }
  }

  deleteI18nLabel(label: I18nLabel): Observable<boolean> {
    return this.rest.delete(`/i18n/${encodeURIComponent(label._id)}`);
  }

  downloadAllI18nLabelsCsv(): Observable<Blob> {
    return this.rest.get('/i18n/export/csv', (r) => new Blob([r], { type: 'text/csv;charset=utf-8' }));
  }

  downloadI18nLabelsCsv(query: I18LabelQuery): Observable<Blob> {
    return this.rest.post('/i18n/export/csv', query, (r) => new Blob([r], { type: 'text/csv;charset=utf-8' }));
  }

  downloadAllI18nLabelsJson(): Observable<Blob> {
    return this.rest.get('/i18n/export/json', (r) => new Blob([r], { type: 'application/json' }));
  }

  downloadI18nLabelsJson(query: I18LabelQuery): Observable<Blob> {
    return this.rest.post('/i18n/export/json', query, (r) => new Blob([r], { type: 'application/json' }));
  }

  prepareI18nCsvDumpUploader(uploader: FileUploader) {
    this.rest.setFileUploaderOptions(uploader, '/i18n/import/csv');
  }

  prepareI18nJsonDumpUploader(uploader: FileUploader) {
    this.rest.setFileUploaderOptions(uploader, '/i18n/import/json');
  }

  prepareFileDumpUploader(uploader: FileUploader) {
    this.rest.setFileUploaderOptions(uploader, '/file');
  }

  getFeatures(botId: string): Observable<Feature[]> {
    return this.rest.get(`/feature/${encodeURIComponent(botId)}`, Feature.fromJSONArray);
  }

  toggleFeature(botId: string, feature: Feature): Observable<boolean> {
    return this.rest.post(`/feature/${encodeURIComponent(botId)}/toggle`, feature);
  }

  updateDateAndEnableFeature(botId: string, feature: Feature): Observable<boolean> {
    return this.rest.post(`/feature/${encodeURIComponent(botId)}/update`, feature);
  }

  addFeature(botId: string, feature: Feature): Observable<boolean> {
    return this.rest.post(`/feature/${encodeURIComponent(botId)}/add`, feature);
  }

  deleteFeature(botId: string, category: string, name: string, applicationId: string): Observable<boolean> {
    return this.rest.delete(
      `/feature/${encodeURIComponent(botId)}/${encodeURIComponent(category)}/${encodeURIComponent(name)}/${
        applicationId ? encodeURIComponent(applicationId) : ''
      }`
    );
  }
}
