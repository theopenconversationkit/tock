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

import {Injectable, OnDestroy} from "@angular/core";
import {RestService} from "../core-nlp/rest/rest.service";
import {StateService} from "../core-nlp/state.service";
import {
  Dictionary,
  EntityDefinition,
  EntityType,
  Intent,
  LogsQuery,
  LogsResult,
  ParseQuery,
  PredefinedLabelQuery,
  PredefinedValueQuery,
  SearchQuery,
  Sentence,
  SentencesResult,
  TranslateReport,
  TranslateSentencesQuery,
  UpdateEntityDefinitionQuery,
  UpdateSentencesQuery,
  UpdateSentencesReport
} from "../model/nlp";
import {Observable} from "rxjs";
import {Application} from "../model/application";
import {FileUploader} from "ng2-file-upload";

@Injectable()
export class NlpService implements OnDestroy {

  private resetConfigurationUnsuscriber: any;

  constructor(private rest: RestService,
              private state: StateService) {
    this.resetConfiguration();
    this.resetConfigurationUnsuscriber = this.state.resetConfigurationEmitter.subscribe(_ => this.resetConfiguration());
  }

  ngOnDestroy(): void {
    this.resetConfigurationUnsuscriber.unsubscribe();
  }

  resetConfiguration() {
    this.getEntityTypes().subscribe(types => this.state.entityTypes.next(types));
  }

  parse(parseQuery: ParseQuery): Observable<Sentence> {
    return this.rest.post("/parse", parseQuery, Sentence.fromJSON);
  }

  saveIntent(intent: Intent): Observable<Intent> {
    return this.rest.post("/intent", intent, Intent.fromJSON);
  }

  removeState(application: Application, intent: Intent, state: string): Observable<boolean> {
    return this.rest.delete(`/application/${application._id}/intent/${intent._id}/state/${encodeURIComponent(state)}`);
  }

  removeSharedIntent(application: Application, intent: Intent, intentId: string): Observable<boolean> {
    return this.rest.delete(`/application/${application._id}/intent/${intent._id}/shared/${intentId}`);
  }

  removeIntent(application: Application, intent: Intent): Observable<boolean> {
    return this.rest.delete(`/application/${application._id}/intent/${intent._id}`);
  }

  removeEntity(application: Application, intent: Intent, entity: EntityDefinition): Observable<boolean> {
    return this.rest.delete(`/application/${application._id}/intent/${intent._id}/entity/${encodeURIComponent(entity.entityTypeName)}/${encodeURIComponent(entity.role)}`);
  }

  removeSubEntity(application: Application, entityType: EntityType, entity: EntityDefinition): Observable<boolean> {
    return this.rest.delete(`/application/${application._id}/entity/${encodeURIComponent(entityType.name)}/${encodeURIComponent(entity.role)}`);
  }

  getEntityTypes(): Observable<EntityType[]> {
    return this.rest.get("/entity-types", EntityType.fromJSONArray);
  }

  updateEntityDefinition(query: UpdateEntityDefinitionQuery): Observable<boolean> {
    return this.rest.post("/entity", query);
  }

  createEntityType(type: string): Observable<EntityType> {
    return this.rest.post("/entity-type/create", {type: type}, EntityType.fromJSON);
  }

  updateEntityType(entityType: EntityType): Observable<boolean> {
    return this.rest.post("/entity-type", entityType);
  }

  removeEntityType(entityType: EntityType): Observable<boolean> {
    return this.rest.delete(`/entity-type/${encodeURIComponent(entityType.name)}`);
  }

  prepareDictionaryJsonDumpUploader(uploader: FileUploader, entityName: string) {
    this.rest.setFileUploaderOptions(uploader, `/dump/dictionary/${entityName}`);
  }

  saveDictionary(dictionary: Dictionary): Observable<boolean> {
    return this.rest.post('/dictionary', dictionary)
  }

  getDictionary(entityType: EntityType): Observable<Dictionary> {
    return this.rest.get(`/dictionary/${entityType.name}`, Dictionary.fromJSON)
  }

  updateSentence(sentence: Sentence): Observable<Sentence> {
    return this.rest.post("/sentence", sentence)
  }

  searchSentences(query: SearchQuery): Observable<SentencesResult> {
    return this.rest.post("/sentences/search", query, SentencesResult.fromJSON)
  }

  updateSentences(query: UpdateSentencesQuery): Observable<UpdateSentencesReport> {
    return this.rest.post("/sentences/update", query, UpdateSentencesReport.fromJSON)
  }

  searchLogs(query: LogsQuery): Observable<LogsResult> {
    return this.rest.post("/logs/search", query, LogsResult.fromJSON)
  }

  exportLogs(application: Application, locale: string): Observable<Blob> {
    return this.rest.get(`/logs/${application._id}/${locale}/export`, (r => new Blob([r], {type: 'text/csv;charset=utf-8'})));
  }

  getSentencesDump(application: Application, query: SearchQuery, full: boolean): Observable<Blob> {
    return this.rest.post(`/sentences/dump/${full ? 'full/' : ''}${application._id}`, query, (r => new Blob([JSON.stringify(r)], {type: 'application/json'})));
  }

  createOrUpdatePredefinedValue(query: PredefinedValueQuery): Observable<Dictionary> {
    return this.rest.post(`/dictionary/predefined-values`, query, Dictionary.fromJSON)
  }

  deletePredefinedValue(query: PredefinedValueQuery): Observable<boolean> {
    return this.rest.delete(`/dictionary/predefined-values/${encodeURIComponent(query.entityTypeName)}/${encodeURIComponent(query.predefinedValue)}`)
  }

  createLabel(query: PredefinedLabelQuery): Observable<Dictionary> {
    return this.rest.post(`/dictionary/predefined-value/labels`, query, Dictionary.fromJSON)
  }

  deleteLabel(query: PredefinedLabelQuery): Observable<boolean> {
    return this.rest.delete(`/dictionary/predefined-value/labels/${encodeURIComponent(query.entityTypeName)}/${encodeURIComponent(query.predefinedValue)}/${encodeURIComponent(query.locale)}/${encodeURIComponent(query.label)}`)
  }

  translateSentences(query: TranslateSentencesQuery): Observable<TranslateReport> {
    return this.rest.post("/translation/sentence", query, TranslateReport.fromJSON)
  }

}
