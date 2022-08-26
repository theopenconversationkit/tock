import { Injectable } from '@angular/core';
import { Observable } from 'rxjs';

import { RestService } from '../../core-nlp/rest/rest.service';
import { Scenario, TickStory } from '../models';

@Injectable()
export class ScenarioApiService {
  constructor(private rest: RestService) {}

  getScenarios(): Observable<Array<Scenario>> {
    return this.rest.get<Array<Scenario>>('/scenarios', (scenarios: Scenario[]) => scenarios);
  }

  postScenario(scenario: Scenario): Observable<Scenario> {
    return this.rest.post<Scenario, Scenario>('/scenarios', scenario);
  }

  putScenario(id: string, scenario: Scenario): Observable<Scenario> {
    return this.rest.put<Scenario, Scenario>(`/scenarios/${id}`, scenario);
  }

  deleteScenario(id: string): Observable<any> {
    return this.rest.delete<Scenario>(`/scenarios/${id}`);
  }

  postTickStory(tickStory): Observable<any> {
    return this.rest.post<TickStory, any>('/bot/story/tick', tickStory, null, null, true);
  }
}
