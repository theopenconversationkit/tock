/*
 * Copyright (C) 2017/2025 SNCF Connect & Tech
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
import { ActivatedRouteSnapshot, Router, RouterStateSnapshot } from '@angular/router';
import { AuthService } from './auth.service';
import { environment } from '../../../environments/environment';
import { StateService } from '../state.service';
import { UserRole } from '../../model/auth';
import { CoreConfig } from '../core.config';

@Injectable()
export class AuthGuard  {
  private autologin = environment.autologin;
  private rolesMap: Map<UserRole, string[]>;

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

  private isAllowedToAccess(url) {
    let rolesMapEntriesMatchingUrl = [];
    this.rolesMap.forEach((urlFragmnts, roleIndex) => {
      urlFragmnts.forEach((urlFrag) => {
        if (url.startsWith(urlFrag)) {
          rolesMapEntriesMatchingUrl.push(roleIndex);
        }
      });
    });

    if (!rolesMapEntriesMatchingUrl.length) return true;

    return rolesMapEntriesMatchingUrl.some((e) => this.userState.hasRole(e));
  }

  private checkLogin(url: string): boolean {
    const login = this.authService.isLoggedIn();
    if (login) {
      // check the user connected has the role
      // and check there is an url present in configuration.roleMap in core.module.ts
      // in bot-core.module.ts in tock-bot-admin
      if (!this.isAllowedToAccess(url)) {
        // try to navigate to the first url present for the role
        if (this.userState.hasRole(UserRole.nlpUser)) {
          this.router.navigateByUrl(this.rolesMap.get(UserRole.nlpUser)[0]);
        } else if (this.userState.hasRole(UserRole.botUser)) {
          this.router.navigateByUrl(this.rolesMap.get(UserRole.botUser)[0]);
        } else if (this.userState.hasRole(UserRole.admin)) {
          this.router.navigateByUrl(this.rolesMap.get(UserRole.admin)[0]);
        } else if (this.userState.hasRole(UserRole.technicalAdmin)) {
          this.router.navigateByUrl(this.configuration.roleMap.get(UserRole.technicalAdmin)[0]);
        }

        return false;
      }
      return true;
    } else {
      if (this.authService.isSSO()) {
        this.authService.loadUser().subscribe((u) => this.router.navigateByUrl(url === '/login' ? '/' : url));
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
