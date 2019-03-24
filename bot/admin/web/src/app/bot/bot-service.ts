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

import {Injectable} from "@angular/core";
import {RestService} from "../core-nlp/rest/rest.service";
import {BotIntent, BotIntentSearchQuery, CreateBotIntentRequest, UpdateBotIntentRequest} from "./model/bot-intent";
import {Intent} from "../model/nlp";
import {Observable} from "rxjs";
import {I18nLabel, I18nLabels} from "./model/i18n";
import {FileUploader} from "ng2-file-upload";
import {Feature} from "./model/feature";
import {ApplicationDialogFlow, DialogFlowRequest} from "./model/flow";

@Injectable()
export class BotService {

  constructor(private rest: RestService) {
  }

  newBotIntent(request: CreateBotIntentRequest): Observable<Intent> {
    return this.rest.post("/bot/intent/new", request, Intent.fromJSON);
  }

  updateBotIntent(request: UpdateBotIntentRequest): Observable<Intent> {
    return this.rest.post("/bot/intent", request, Intent.fromJSON);
  }

  getBotIntents(request: BotIntentSearchQuery): Observable<BotIntent[]> {
    return this.rest.post("/bot/intents/search", request, BotIntent.fromJSONArray);
  }

  deleteBotIntent(storyDefinitionId: string): Observable<boolean> {
    return this.rest.delete(`/bot/intent/${storyDefinitionId}`);
  }

  i18nLabels(): Observable<I18nLabels> {
    return this.rest.get("/i18n", I18nLabels.fromJSON);
  }

  completeI18nLabels(labels: I18nLabel[]): Observable<boolean> {
    return this.rest.post("/i18n/complete", labels);
  }

  saveI18nLabels(labels: I18nLabel[]): Observable<boolean> {
    return this.rest.post("/i18n/saveAll", labels);
  }

  saveI18nLabel(label: I18nLabel): Observable<boolean> {
    return this.rest.post("/i18n/save", label);
  }

  deleteI18nLabel(label: I18nLabel): Observable<boolean> {
    return this.rest.delete(`/i18n/${encodeURIComponent(label._id)}`);
  }

  downloadI18nLabelsCsv(): Observable<Blob> {
    return this.rest.get("/i18n/export/csv", (r => new Blob([r], {type: 'text/csv;charset=utf-8'})))
  }

  downloadI18nLabelsJson(): Observable<Blob> {
    return this.rest.get("/i18n/export/json", (r => new Blob([r], {type: 'application/json'})))
  }

  prepareI18nCsvDumpUploader(uploader: FileUploader) {
    this.rest.setFileUploaderOptions(uploader, "/i18n/import/csv");
  }

  prepareI18nJsonDumpUploader(uploader: FileUploader) {
    this.rest.setFileUploaderOptions(uploader, "/i18n/import/json");
  }

  getFeatures(botId: string): Observable<Feature[]> {
    return this.rest.get(`/feature/${encodeURIComponent(botId)}`, Feature.fromJSONArray);
  }

  toggleFeature(botId: string, category: string, name: string): Observable<boolean> {
    return this.rest.post(`/feature/${encodeURIComponent(botId)}/toggle/${encodeURIComponent(category)}/${encodeURIComponent(name)}`);
  }

  addFeature(botId: string, enabled: boolean, category: string, name: string): Observable<boolean> {
    return this.rest.post(`/feature/${encodeURIComponent(botId)}/add/${encodeURIComponent(category)}/${encodeURIComponent(name)}/${enabled}`);
  }

  deleteFeature(botId: string, category: string, name: string): Observable<boolean> {
    return this.rest.delete(`/feature/${encodeURIComponent(botId)}/${encodeURIComponent(category)}/${encodeURIComponent(name)}`);
  }

  getApplicationFlow(request: DialogFlowRequest): Observable<ApplicationDialogFlow> {
    return this.rest.post(`/flow`, request, ApplicationDialogFlow.fromJSON);
  }

}
