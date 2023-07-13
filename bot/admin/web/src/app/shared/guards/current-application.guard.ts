import { Injectable } from '@angular/core';
import { ActivatedRouteSnapshot, CanActivate, RouterStateSnapshot } from '@angular/router';
import { iif, Observable, of } from 'rxjs';
import { mergeMap, take } from 'rxjs/operators';
import { ApplicationService } from '../../core-nlp/applications.service';

@Injectable({
  providedIn: 'root'
})
export class CurrentApplicationGuard implements CanActivate {
  constructor(private appService: ApplicationService) {}

  canActivate(route: ActivatedRouteSnapshot, state: RouterStateSnapshot): Observable<boolean> {
    return this.appService.retrieveCurrentApplication().pipe(
      take(1),
      mergeMap((application) => {
        return iif(() => !!application, of(true), of(false));
      })
    );
  }
}
