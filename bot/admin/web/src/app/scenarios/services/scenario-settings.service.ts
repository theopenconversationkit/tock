import { Injectable } from '@angular/core';
import { BehaviorSubject, merge, Observable } from 'rxjs';
import { filter, map, switchMap, tap } from 'rxjs/operators';

import { ScenarioSettingsState, ScenarioSettings } from '../models';
import { ScenarioApiService } from './scenario.api.service';

const scenariosSettingsInitialState: ScenarioSettingsState = {
  loaded: false,
  settings: {
    actionRepetitionNumber: 2,
    redirectStoryId: null
  }
};

@Injectable({
  providedIn: 'root'
})
export class ScenarioSettingsService {
  private _state: BehaviorSubject<ScenarioSettingsState>;
  state$: Observable<ScenarioSettingsState>;

  constructor(private scenarioApiService: ScenarioApiService) {
    this._state = new BehaviorSubject(scenariosSettingsInitialState);
    this.state$ = this._state.asObservable();
  }

  getState(): ScenarioSettingsState {
    return this._state.getValue();
  }

  setState(state: ScenarioSettingsState): void {
    return this._state.next(state);
  }

  /**
   * Retrieves global scenario settings from the server and stores them in a local state. If the parameters are already present in the local state, they are used and no new call to the server is made
   * @param {string} applicationId
   * @returns {Observable<ScenarioSettings>}
   */
  getSettings(applicationId: string): Observable<ScenarioSettings> {
    const scenarioSettignsState = this.state$;
    const notLoaded = scenarioSettignsState.pipe(
      filter((state: ScenarioSettingsState) => !state.loaded),
      switchMap(() => this.scenarioApiService.getSettings(applicationId)),
      tap((settings: ScenarioSettings) =>
        this.setState({
          loaded: true,
          settings
        })
      ),
      switchMap(() => scenarioSettignsState),
      map((state: ScenarioSettingsState) => state.settings)
    );
    const loaded = scenarioSettignsState.pipe(
      filter((state: ScenarioSettingsState) => state.loaded),
      map((state: ScenarioSettingsState) => state.settings)
    );

    return merge(notLoaded, loaded);
  }

  /**
   * Save global scenario settings. If the backup to the server is successful, the state is updated
   * @param {string} applicationId
   * @param {ScenarioSettings} settings
   * @returns {Observable<ScenarioSettings>}
   */
  saveSettings(applicationId: string, settings: ScenarioSettings): Observable<ScenarioSettings> {
    return this.scenarioApiService.saveSettings(applicationId, settings).pipe(
      tap((settings: ScenarioSettings) => {
        this.setState({
          ...this.getState(),
          settings
        });
      })
    );
  }
}
