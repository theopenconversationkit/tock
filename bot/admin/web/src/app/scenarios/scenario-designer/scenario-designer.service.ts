import { Injectable } from '@angular/core';
import { Router } from '@angular/router';
import { Observable, Subject } from 'rxjs';
import { tap } from 'rxjs/operators';
import { stringifiedCleanObject } from '../commons/utils';
import { ScenarioVersion, ScenarioVersionExtended } from '../models';
import { ScenarioService } from '../services/scenario.service';

@Injectable({
  providedIn: 'root'
})
export class ScenarioDesignerService {
  constructor(private scenarioService: ScenarioService, private router: Router) {}

  public scenarioDesignerCommunication = new Subject<any>();

  saveScenario(scenarioVersion: ScenarioVersionExtended): Observable<ScenarioVersion> {
    const cleanScenario = JSON.parse(stringifiedCleanObject(scenarioVersion));
    delete cleanScenario.creationDate;
    delete cleanScenario.updateDate;

    return this.scenarioService
      .updateScenarioVersion(scenarioVersion._scenarioGroupId, cleanScenario)
      .pipe(tap((data) => this.updateScenarioBackup(data)));
  }

  exitDesigner(): void {
    this.router.navigateByUrl('/scenarios');
  }

  updateScenarioBackup(data: ScenarioVersion): void {
    this.scenarioDesignerCommunication.next({
      type: 'updateScenarioBackup',
      data: data
    });
  }
}
