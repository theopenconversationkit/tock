import { Injectable } from '@angular/core';
import { Router } from '@angular/router';
import { Observable, Subject } from 'rxjs';
import { tap } from 'rxjs/operators';
import { stringifiedCleanScenario } from '../commons/utils';
import { Scenario } from '../models';
import { ScenarioService } from '../services/scenario.service';

@Injectable({
  providedIn: 'root'
})
export class ScenarioDesignerService {
  constructor(private scenarioService: ScenarioService, private router: Router) {}

  public scenarioDesignerCommunication = new Subject<any>();

  saveScenario(scenarioId: string, scenario: Scenario): Observable<Scenario> {
    const cleanScenario = JSON.parse(stringifiedCleanScenario(scenario));
    return this.scenarioService
      .putScenario(scenarioId, cleanScenario)
      .pipe(tap((data) => this.updateScenarioBackup(data)));
  }

  exitDesigner(): void {
    this.router.navigateByUrl('/scenarios');
  }

  updateScenarioBackup(data: Scenario): void {
    this.scenarioDesignerCommunication.next({
      type: 'updateScenarioBackup',
      data: data
    });
  }
}
