/*
 * Copyright (C) 2017/2021 e-voyageurs technologies
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

import { Injectable } from '@angular/core';
import {
  ActivatedRouteSnapshot,
  CanActivate,
  CanActivateChild,
  Router,
  RouterStateSnapshot
} from '@angular/router';
import { AuthService } from './auth.service';
import { environment } from '../../../environments/environment';
import { StateService } from '../state.service';
import { UserRole } from '../../model/auth';
import { CoreConfig } from '../core.config';

@Injectable()
export class AuthGuard implements CanActivate, CanActivateChild {
  private autologin = environment.autologin;
  private rolesMap: Map<UserRole, string>;

  constructor(
    private authService: AuthService,
    private router: Router,
    private userState: StateService,
    private configuration: CoreConfig
  ) {
    this.rolesMap = configuration.roleMap;
  }

  canActivate(route: ActivatedRouteSnapshot, state: RouterStateSnapshot) {
    const url: string = state.url;
    return this.checkLogin(url);
  }

  canActivateChild(route: ActivatedRouteSnapshot, state: RouterStateSnapshot): boolean {
    return this.canActivate(route, state);
  }

  private checkLogin(url: string): boolean {
    const login = this.authService.isLoggedIn();
    if (login) {
      if (
        (!this.userState.hasRole(UserRole.nlpUser) &&
          url.startsWith(this.rolesMap.get(UserRole.nlpUser))) ||
        (!this.userState.hasRole(UserRole.botUser) &&
          url.startsWith(this.rolesMap.get(UserRole.botUser)))
      ) {
        setTimeout((_) => {
          if (this.userState.hasRole(UserRole.nlpUser)) {
            this.router.navigateByUrl(this.rolesMap.get(UserRole.nlpUser));
          } else if (this.userState.hasRole(UserRole.botUser)) {
            this.router.navigateByUrl(this.rolesMap.get(UserRole.botUser));
          } else if (this.userState.hasRole(UserRole.admin)) {
            this.router.navigateByUrl(this.rolesMap.get(UserRole.admin));
          } else if (this.userState.hasRole(UserRole.technicalAdmin)) {
            this.router.navigateByUrl(this.configuration.roleMap.get(UserRole.technicalAdmin));
          }
        });
        return false;
      }
      return true;
    } else {
      if (this.authService.isSSO()) {
        this.authService
          .loadUser()
          .subscribe((u) => this.router.navigateByUrl(url === '/login' ? '/' : url));
        return false;
      } else if (this.autologin) {
        this.autologin = false;
        this.authService
          .authenticate(environment.default_user, environment.default_password)
          .subscribe((_) => this.router.navigateByUrl(url));
        return false;
      }
    }

    // Store the attempted URL for redirecting
    this.authService.setRedirectUrl(url);

    // Navigate to the login page
    this.router.navigate(['/login']);

    return false;
  }
}
