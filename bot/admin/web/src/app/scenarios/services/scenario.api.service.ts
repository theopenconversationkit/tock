import { Injectable } from '@angular/core';
import { Observable } from 'rxjs';
import { switchMap } from 'rxjs/operators';

import { ApplicationService } from '../../core-nlp/applications.service';
import { Application } from '../../model/application';
import { RestService } from '../../core-nlp/rest/rest.service';
import { Handler, ScenarioDebug, ScenarioGroup, ScenarioVersion, ScenarioSettings, TickStory } from '../models';

@Injectable()
export class ScenarioApiService {
  constructor(private rest: RestService, private applicationService: ApplicationService) {}

  getScenariosGroups(): Observable<Array<ScenarioGroup>> {
    return this.applicationService
      .retrieveCurrentApplication()
      .pipe(
        switchMap((currentApplication: Application) =>
          this.rest.get<Array<ScenarioGroup>>(
            `/bot/${currentApplication.name}/scenarios/groups`,
            (scenariosGroups: ScenarioGroup[]) => scenariosGroups
          )
        )
      );
  }

  postScenarioGroup(scenarioGroup: ScenarioGroup): Observable<ScenarioGroup> {
    return this.applicationService
      .retrieveCurrentApplication()
      .pipe(
        switchMap((currentApplication: Application) =>
          this.rest.post<ScenarioGroup, ScenarioGroup>(`/bot/${currentApplication.name}/scenarios/groups`, scenarioGroup)
        )
      );
  }

  importScenarioGroup(scenarioGroup: ScenarioGroup): Observable<ScenarioGroup> {
    return this.applicationService
      .retrieveCurrentApplication()
      .pipe(
        switchMap((currentApplication: Application) =>
          this.rest.post<ScenarioGroup, ScenarioGroup>(`/bot/${currentApplication.name}/scenarios/import/groups`, scenarioGroup)
        )
      );
  }

  updateScenarioGroup(scenarioGroup: Partial<ScenarioGroup>): Observable<ScenarioGroup> {
    return this.applicationService
      .retrieveCurrentApplication()
      .pipe(
        switchMap((currentApplication: Application) =>
          this.rest.put<ScenarioGroup, ScenarioGroup>(
            `/bot/${currentApplication.name}/scenarios/groups/${scenarioGroup.id}`,
            scenarioGroup as ScenarioGroup
          )
        )
      );
  }

  deleteScenarioGroup(scenarioGroupId: string): Observable<boolean> {
    return this.applicationService
      .retrieveCurrentApplication()
      .pipe(
        switchMap((currentApplication: Application) =>
          this.rest.delete<boolean>(`/bot/${currentApplication.name}/scenarios/groups/${scenarioGroupId}`)
        )
      );
  }

  getScenarioVersion(scenarioGroupId: string, scenarioVersionId: string): Observable<ScenarioVersion> {
    return this.applicationService
      .retrieveCurrentApplication()
      .pipe(
        switchMap((currentApplication: Application) =>
          this.rest.get<ScenarioVersion>(
            `/bot/${currentApplication.name}/scenarios/groups/${scenarioGroupId}/versions/${scenarioVersionId}`,
            (scenarioVersion: ScenarioVersion) => scenarioVersion
          )
        )
      );
  }

  postScenarioVersion(scenarioGroupId: string, scenarioVersion: ScenarioVersion): Observable<ScenarioVersion> {
    return this.applicationService
      .retrieveCurrentApplication()
      .pipe(
        switchMap((currentApplication: Application) =>
          this.rest.post<ScenarioVersion, ScenarioVersion>(
            `/bot/${currentApplication.name}/scenarios/groups/${scenarioGroupId}/versions`,
            scenarioVersion
          )
        )
      );
  }

  // TODO : Import many versions as once
  // "/bot/:$botId/scenarios/import/groups/:$groupId/versions"

  updateScenarioVersion(scenarioGroupId: string, scenarioVersion: ScenarioVersion): Observable<ScenarioVersion> {
    return this.applicationService
      .retrieveCurrentApplication()
      .pipe(
        switchMap((currentApplication: Application) =>
          this.rest.put<ScenarioVersion, ScenarioVersion>(
            `/bot/${currentApplication.name}/scenarios/groups/${scenarioGroupId}/versions/${scenarioVersion.id}`,
            scenarioVersion
          )
        )
      );
  }

  deleteScenarioVersion(scenarioGroupId: string, scenarioVersionId: string): Observable<boolean> {
    return this.applicationService
      .retrieveCurrentApplication()
      .pipe(
        switchMap((currentApplication: Application) =>
          this.rest.delete<boolean>(`/bot/${currentApplication.name}/scenarios/groups/${scenarioGroupId}/versions/${scenarioVersionId}`)
        )
      );
  }

  getActionHandlers(): Observable<Handler[]> {
    return this.applicationService
      .retrieveCurrentApplication()
      .pipe(
        switchMap((currentApplication: Application) =>
          this.rest.get<Handler[]>(`/bot/${currentApplication.name}/dialog-manager/action-handlers`, (handlers: Handler[]) =>
            handlers.map((h) => h)
          )
        )
      );
  }

  // TODO MASS : do rollback when front debug is ready
  getScenarioDebug(): Observable<ScenarioDebug> {
    return this.applicationService
      .retrieveCurrentApplication()
      .pipe(
        switchMap((currentApplication: Application) =>
          this.rest.get<ScenarioDebug>(`/bot/${currentApplication.name}/scenarios/debug`, (debug: ScenarioDebug) => debug)
        )
      );
  }

  postTickStory(tickStory: TickStory): Observable<TickStory> {
    return this.rest.post<TickStory, TickStory>('/bot/story/tick', tickStory, null, null, true);
  }

  getSettings(applicationId: string): Observable<ScenarioSettings> {
    return this.rest.get<ScenarioSettings>(`/scenarios/settings/${applicationId}`, (settings: ScenarioSettings) => settings);
  }

  saveSettings(applicationId: string, settings: ScenarioSettings): Observable<ScenarioSettings> {
    return this.rest.post<ScenarioSettings, ScenarioSettings>(`/scenarios/settings/${applicationId}`, settings);
  }
}
