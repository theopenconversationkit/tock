import { Injectable } from '@angular/core';
import { BehaviorSubject, merge, Observable } from 'rxjs';
import { map, switchMap, takeUntil, tap, filter } from 'rxjs/operators';

import { RestService } from '../../core-nlp/rest/rest.service';
import { Settings } from '../models';

interface FaqState {
  loaded: boolean;
  settings: Settings;
}

const faqInitialState: { loaded: boolean; settings: Settings } = {
  loaded: false,
  settings: {
    satisfactionEnabled: false,
    satisfactionStoryId: null
  }
};

@Injectable()
export class FaqService {
  private _state: BehaviorSubject<FaqState>;
  state$: Observable<FaqState>;

  constructor(private rest: RestService) {
    this._state = new BehaviorSubject(faqInitialState);
    this.state$ = this._state.asObservable();
  }

  getState(): FaqState {
    return this._state.getValue();
  }

  setState(state: FaqState): void {
    return this._state.next(state);
  }

  getSettings(applicationId: string): Observable<Settings> {
    const faqState = this.state$;
    const notLoaded = faqState.pipe(
      filter((state) => !state.loaded),
      switchMap(() =>
        this.rest.get<Settings>(`/faq/settings/${applicationId}`, (settings: Settings) => ({
          satisfactionEnabled: settings.satisfactionEnabled,
          satisfactionStoryId: settings.satisfactionStoryId
        }))
      ),
      tap((settings: Settings) =>
        this.setState({
          loaded: true,
          settings
        })
      ),
      switchMap(() => faqState),
      map((state) => state.settings)
    );
    const loaded = faqState.pipe(
      filter((state) => state.loaded === true),
      map((state) => state.settings)
    );
    return merge(notLoaded, loaded);
  }

  saveSettings(applicationId: string, settings: Settings): Observable<any> {
    return this.rest.post(`/faq/settings/${applicationId}`, settings).pipe(
      tap((newSettings: Settings) => {
        let state = this.getState();
        state.settings = { ...newSettings };
        this.setState(state);
      })
    );
  }
}
