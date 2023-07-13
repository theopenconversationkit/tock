import { Injectable } from '@angular/core';
import { ActivatedRouteSnapshot, CanActivate, Router, RouterStateSnapshot } from '@angular/router';
import { NbDialogService } from '@nebular/theme';
import { iif, Observable, of } from 'rxjs';
import { mergeMap, take, tap } from 'rxjs/operators';
import { BotConfigurationService } from '../../core/bot-configuration.service';
import { ChoiceDialogComponent } from '../components';

@Injectable({
  providedIn: 'root'
})
export class BotConfigurationGuard implements CanActivate {
  constructor(private botConfigurationService: BotConfigurationService, private nbDialogService: NbDialogService, private router: Router) {}

  canActivate(route: ActivatedRouteSnapshot, state: RouterStateSnapshot): Observable<boolean> {
    return this.botConfigurationService.grabConfigurations().pipe(
      take(1),
      mergeMap((configurations) => {
        return iif(() => !!configurations?.length, of(true), of(false));
      }),
      tap((configurationsAvalaible) => {
        if (!configurationsAvalaible) {
          let redirectToConfig = 'create configuration';
          const modal = this.nbDialogService.open(ChoiceDialogComponent, {
            context: {
              title: `Bot configuration missing`,
              subtitle: 'A bot configuration for the current application is required to access this part of the app',
              actions: [
                { actionName: 'abort', buttonStatus: 'basic', ghost: true },
                { actionName: redirectToConfig, buttonStatus: 'primary' }
              ],
              modalStatus: 'danger'
            }
          });

          modal.onClose.subscribe((res) => {
            if (res === redirectToConfig) {
              this.router.navigateByUrl('configuration/bot');
            }
          });
        }
      })
    );
  }
}
