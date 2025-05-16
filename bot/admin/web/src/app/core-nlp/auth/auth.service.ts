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
import { AuthListener } from './auth.listener';
import { Router } from '@angular/router';
import { AuthenticateRequest, AuthenticateResponse, User } from '../../model/auth';
import { Observable } from 'rxjs';
import { RestService } from '../rest/rest.service';

@Injectable()
export class AuthService {
  private logged: boolean;
  private redirectUrl: string;
  private authListeners: AuthListener[] = [];

  constructor(private rest: RestService, private router: Router) {}

  setRedirectUrl(redirectUrl: string) {
    this.redirectUrl = redirectUrl;
  }

  getRedirectUrl() {
    return this.redirectUrl ? this.redirectUrl : '/';
  }

  isLoggedIn(): boolean {
    return this.logged;
  }

  isSSO(): boolean {
    return this.rest.isSSO();
  }

  addListener(listener: AuthListener) {
    this.authListeners.push(listener);
  }

  logout() {
    this.rest.post('/logout', null, null, this.rest.notAuthenticatedUrl).subscribe(() => {
      this.logged = false;
      this.authListeners.forEach((l) => l.logout());
      this.router.navigateByUrl('/login');
    });
  }

  authenticate(email: string, password: string): Observable<boolean> {
    return this.rest.postNotAuthenticated('/authenticate', new AuthenticateRequest(email, password), (j) =>
      this.login(AuthenticateResponse.fromJSON(j))
    );
  }

  loadUser(): Observable<boolean> {
    return this.rest.getNotAuthenticated('/user', (j) => this.logUser(User.fromJSON(j)));
  }

  private login(response: AuthenticateResponse): boolean {
    return this.logUser(response.toUser());
  }

  private logUser(user: User): boolean {
    if (user.roles && user.roles.length !== 0) {
      this.logged = true;
      this.authListeners.forEach((l) => l.login(user));
      return true;
    } else {
      return false;
    }
  }
}
