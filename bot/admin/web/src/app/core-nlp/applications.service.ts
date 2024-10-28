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

import { map, share } from 'rxjs/operators';
import { Injectable, OnDestroy } from '@angular/core';
import { Observable, of } from 'rxjs';
import {
  Application,
  ApplicationImportConfiguration,
  ModelBuildQueryResult,
  NamespaceConfiguration,
  NlpApplicationConfiguration,
  UserLogQueryResult,
  UserNamespace
} from '../model/application';
import { RestService } from './rest/rest.service';
import { StateService } from './state.service';
import { ApplicationScopedQuery, Entry, PaginatedQuery } from '../model/commons';
import { Intent, NlpEngineType } from '../model/nlp';
import { FileUploader } from 'ng2-file-upload';

@Injectable()
export class ApplicationService implements OnDestroy {
  private resetConfigurationUnsuscriber: any;

  constructor(private rest: RestService, private state: StateService) {
    this.resetConfiguration();
    this.resetConfigurationUnsuscriber = this.state.resetConfigurationEmitter.subscribe((_) => this.resetConfiguration());
  }

  ngOnDestroy(): void {
    this.resetConfigurationUnsuscriber.unsubscribe();
  }

  reloadCurrentApplication(): Observable<Application> {
    return this.getApplicationById(this.state.currentApplication._id).pipe(
      map((app) => {
        this.refreshCurrentApplication(app);
        return app;
      })
    );
  }

  refreshCurrentApplication(app: Application) {
    this.state.applications = this.state.applications.filter((a) => a._id !== app._id);
    this.state.applications.push(app);
    this.state.sortApplications();
    this.state.changeApplication(app);
  }

  resetConfiguration() {
    this.getLocales().subscribe((locales) => (this.state.locales = locales));
    this.getNlpEngineTypes().subscribe((engines) => (this.state.supportedNlpEngines = engines));
    this.getNamespaces().subscribe((namespaces) => (this.state.namespaces = namespaces));
    this.getApplications().subscribe((applications) => {
      this.state.applications = applications;
      this.state.currentApplication = null;
      this.retrieveCurrentApplication();
    });
  }

  getApplicationsPending: Observable<Application[]>;
  getApplications(): Observable<Application[]> {
    if (!this.getApplicationsPending) {
      this.getApplicationsPending = this.rest.getArray('/applications', Application.fromJSONArray).pipe(share());
    }
    return this.getApplicationsPending;
  }

  getNamespacesPending: Observable<UserNamespace[]>;
  getNamespaces(): Observable<UserNamespace[]> {
    if (!this.getNamespacesPending) {
      this.getNamespacesPending = this.rest.get(`/namespaces`, UserNamespace.fromJSONArray).pipe(share());
    }
    return this.getNamespacesPending;
  }

  getLocalesPending: Observable<Entry<string, string>[]>;
  getLocales(): Observable<Entry<string, string>[]> {
    if (!this.getLocalesPending) {
      this.getLocalesPending = this.rest.get(`/locales`, (m) => Entry.fromJSONArray<string, string>(m)).pipe(share());
    }
    return this.getLocalesPending;
  }

  getNlpEngineTypesPending: Observable<NlpEngineType[]>;
  getNlpEngineTypes(): Observable<NlpEngineType[]> {
    if (!this.getNlpEngineTypesPending) {
      this.getNlpEngineTypesPending = this.rest.getArray('/nlp-engines', NlpEngineType.fromJSONArray).pipe(share());
    }
    return this.getNlpEngineTypesPending;
  }

  triggerBuild(application: Application) {
    return this.rest.post(`/application/build/trigger`, application);
  }

  builds(query: PaginatedQuery): Observable<ModelBuildQueryResult> {
    return this.rest.post(`/application/builds`, query, ModelBuildQueryResult.fromJSON);
  }

  getApplicationById(id: string): Observable<Application> {
    return this.rest.get(`/application/${id}`, Application.fromJSON);
  }

  getApplicationsByNamespace(namespace: string): Observable<Application[]> {
    return this.rest.get(`/applications/${namespace}`, Application.fromJSONArray);
  }

  saveApplication(application: Application): Observable<Application> {
    return this.rest.post('/application', application, Application.fromJSON);
  }

  deleteApplication(application: Application): Observable<boolean> {
    return this.rest.delete(`/application/${application._id}`).pipe(
      map((r) => {
        if (r) {
          const app = this.state.applications.find((app) => app._id === application._id);
          this.state.applications.splice(this.state.applications.indexOf(app), 1);
          if (this.state.currentApplication === app) {
            if (this.state.applications.length !== 0) {
              this.state.changeApplication(this.state.applications[0]);
            } else {
              this.state.currentApplication = null;
            }
          }
        }
        return r;
      })
    );
  }

  retrieveCurrentApplication(): Observable<Application> {
    if (this.state.applications) {
      return of(this.state.findCurrentApplication());
    } else {
      return this.getApplications().pipe(
        map((apps) => {
          this.state.applications = apps;
          return this.state.findCurrentApplication();
        })
      );
    }
  }

  getApplicationDump(application: Application): Observable<Blob> {
    return this.rest.get(`/application/dump/${application._id}`, (r) => new Blob([JSON.stringify(r)], { type: 'application/json' }));
  }

  getSentencesDump(application: Application, full: boolean): Observable<Blob> {
    return this.rest.get(
      `/sentences/dump/${full ? 'full/' : ''}${application._id}`,
      (r) => new Blob([JSON.stringify(r)], { type: 'application/json' })
    );
  }

  getSentencesDumpForIntent(application: Application, intent: Intent, locale: string, full: boolean): Observable<Blob> {
    return this.rest.get(
      `/sentences/dump/${full ? 'full/' : ''}${application._id}/${encodeURIComponent(intent.qualifiedName())}/${locale}`,
      (r) => new Blob([JSON.stringify(r)], { type: 'application/json' })
    );
  }

  prepareApplicationDumpUploader(uploader: FileUploader, configuration: ApplicationImportConfiguration) {
    let url: string;
    if (configuration.newApplicationName) {
      url = `/dump/application/${configuration.newApplicationName.trim()}`;
    } else {
      url = `/dump/application`;
    }
    this.rest.setFileUploaderOptions(uploader, url);
  }

  getUsersForNamespace(namespace: string): Observable<UserNamespace[]> {
    return this.rest.get(`/namespaces/${namespace}`, UserNamespace.fromJSONArray);
  }

  createNamespace(namespace: string): Observable<boolean> {
    return this.rest.post(`/namespace/${namespace}`, namespace);
  }

  saveNamespace(userNamespace: UserNamespace): Observable<boolean> {
    return this.rest.post(`/namespace`, userNamespace);
  }

  selectNamespace(namespace: string): Observable<boolean> {
    return this.rest.post(`/namespace/select/${namespace}`);
  }

  deleteNamespace(userNamespace: UserNamespace): Observable<boolean> {
    return this.rest.delete(`/namespace/${userNamespace.login}/${userNamespace.namespace}`);
  }

  saveNamespaceConfiguration(configuration: NamespaceConfiguration): Observable<boolean> {
    return this.rest.post(`/configuration/namespace`, configuration);
  }

  getNamespaceConfiguration(namespace: string): Observable<NamespaceConfiguration> {
    return this.rest.get(`/configuration/namespace/${namespace}`, NamespaceConfiguration.fromJSON);
  }

  getSharableNamespaceConfiguration(): Observable<NamespaceConfiguration[]> {
    return this.rest.get(`/configuration/namespaces/shared`, NamespaceConfiguration.fromJSONArray);
  }

  prepareSentencesDumpUploader(uploader: FileUploader, full: boolean, name?: string) {
    let url: string;
    if (name) {
      url = `/dump/sentences/${name}`;
    } else {
      url = `/dump/sentences/`;
    }
    this.rest.setFileUploaderOptions(uploader, url);
  }

  getAlexaExport(query: ApplicationScopedQuery): Observable<Blob> {
    return this.rest.post(`/alexa/export`, query, (r) => new Blob([JSON.stringify(r)], { type: 'application/json' }));
  }

  getNlpConfiguration(applicationId: string, engineType: NlpEngineType): Observable<NlpApplicationConfiguration> {
    return this.rest.get(`/application/${applicationId}/model/${engineType.name}/configuration`, NlpApplicationConfiguration.fromJSON);
  }

  updateModelConfiguration(
    applicationId: string,
    engineType: NlpEngineType,
    conf: NlpApplicationConfiguration
  ): Observable<NlpApplicationConfiguration> {
    return this.rest.post(`/application/${applicationId}/model/${engineType.name}/configuration`, conf);
  }

  searchUserLogs(query: PaginatedQuery): Observable<UserLogQueryResult> {
    return this.rest.post(`/users/logs/search`, query, UserLogQueryResult.fromJSON);
  }
}
