import { Injectable } from '@angular/core';
import { ActivatedRouteSnapshot, CanActivate, RouterStateSnapshot } from '@angular/router';
import { iif, Observable, of } from 'rxjs';
import { CurrentApplicationGuard } from './current-application.guard';
import { BotConfigurationGuard } from './bot-configuration.guard';
import { mergeMap } from 'rxjs/operators';

@Injectable({
  providedIn: 'root'
})
export class CurrentApplicationAndConfigurationsGuard implements CanActivate {
  constructor(private currentApplicationGuard: CurrentApplicationGuard, private botConfigurationGuard: BotConfigurationGuard) {}
  canActivate(route: ActivatedRouteSnapshot, state: RouterStateSnapshot): Observable<boolean> {
    return this.currentApplicationGuard.canActivate(route, state).pipe(
      mergeMap((applicationLoaded) => {
        return iif(
          () => applicationLoaded,
          this.botConfigurationGuard.canActivate(route, state).pipe(
            mergeMap((configurationsLoaded) => {
              if (configurationsLoaded) return of(true);
              return of(false);
            })
          ),
          of(false)
        );
      })
    );
  }
}
