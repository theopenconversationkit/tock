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
import {RestService} from "tock-nlp-admin/src/app/core/rest/rest.service";
import {StateService} from "tock-nlp-admin/src/app/core/state.service";
import {BotIntent, BotIntentSearchQuery, CreateBotIntentRequest, UpdateBotIntentRequest} from "./model/bot-intent";
import {Intent} from "tock-nlp-admin/src/app/model/nlp";
import {Observable} from "rxjs/Observable";
import {I18nLabel} from "./model/i18n";
import {FileUploader} from "ng2-file-upload";

@Injectable()
export class BotService {

  constructor(private rest: RestService,
              private state: StateService) {
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

  i18nLabels(): Observable<I18nLabel[]> {
    return this.rest.get("/i18n", I18nLabel.fromJSONArray);
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
    return this.rest.delete(`/i18n/${label._id}`);
  }

  downloadI18nLabelsCsv(): Observable<Blob> {
    return this.rest.get("/i18n/export/csv", (r => new Blob([r], {type: 'text/csv'}) ))
  }

  downloadI18nLabelsJson(): Observable<Blob> {
    return this.rest.get("/i18n/export/json", (r => new Blob([r], {type: 'application/json'}) ))
  }

  prepareI18nCsvDumpUploader(uploader: FileUploader) {
    this.rest.setFileUploaderOptions(uploader, "/i18n/import/csv");
  }

  prepareI18nJsonDumpUploader(uploader: FileUploader) {
    this.rest.setFileUploaderOptions(uploader, "/i18n/import/json");
  }

}
