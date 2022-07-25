import { Injectable } from '@angular/core';
import { Observable, Subject } from 'rxjs';
import { tap } from 'rxjs/operators';
import { stringifiedCleanScenario } from '../commons/utils';
import { Scenario } from '../models';
import { ScenarioService } from '../services/scenario.service';

@Injectable({
  providedIn: 'root'
})
export class ScenarioDesignerService {
  constructor(private scenarioService: ScenarioService) {}

  public scenarioDesignerCommunication = new Subject<any>();

  saveScenario(scenarioId: number, scenario: Scenario): Observable<Scenario> {
    const cleanScenario = JSON.parse(stringifiedCleanScenario(scenario));
    return this.scenarioService
      .putScenario(scenarioId, cleanScenario)
      .pipe(tap((data) => this.updateScenarioBackup(data)));
  }

  updateScenarioBackup(data) {
    this.scenarioDesignerCommunication.next({
      type: 'updateScenarioBackup',
      data: data
    });
  }
}
