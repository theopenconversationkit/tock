import { Injectable } from '@angular/core';
import { ActivatedRouteSnapshot, Resolve, RouterStateSnapshot } from '@angular/router';
import { Observable } from 'rxjs';
import { take, map } from 'rxjs/operators';

import { ScenarioGroup } from './models';
import { ScenarioService } from './services/scenario.service';

@Injectable()
export class ScenariosResolver implements Resolve<ScenarioGroup[]> {
  constructor(private scenarioService: ScenarioService) {}

  resolve(_route: ActivatedRouteSnapshot, _state: RouterStateSnapshot): Observable<ScenarioGroup[]> {
    return this.scenarioService.getScenariosGroups(true).pipe(
      take(1),
      map((scenarios: ScenarioGroup[]) => scenarios)
    );
  }
}
