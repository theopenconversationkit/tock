/*
 * Copyright (C) 2017 VSCT
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

import {EventEmitter, Injectable} from "@angular/core";
import {Headers, Http, Response} from "@angular/http";
import {Observable} from "rxjs";
import {environment} from "../../../environments/environment";
import {Router} from "@angular/router";

@Injectable()
export class RestService {

  private url: string;
  private notAuthenticatedUrl: string;
  private authToken: string;

  readonly errorEmitter: EventEmitter<string> = new EventEmitter();

  constructor(private http: Http,
              private router: Router) {
    this.notAuthenticatedUrl = environment.serverUrl;
    this.url = `${environment.serverUrl}/admin`;
  }

  setAuthToken(value: string) {
    this.authToken = value;
  }

  private headers(): Headers {
    const headers = this.notAuthenticatedHeaders();

    headers.append('Authorization', `Basic ${this.authToken}`);
    return headers;
  }

  private notAuthenticatedHeaders(): Headers {
    const headers = new Headers();
    headers.append('Content-Type', 'application/json');
    return headers;
  }

  get<T>(path: string, parseFunction: (value: any) => T): Observable<T> {
    return this.http.get(`${this.url}${path}`, {headers: this.headers()})
      .map((res: Response) => parseFunction(res.json() || {}))
      .catch(e => RestService.handleError(this, e));
  }

  getArray<T>(path: string, parseFunction: (value: any) => T[]): Observable<T[]> {
    return this.http.get(`${this.url}${path}`, {headers: this.headers()})
      .map((res: Response) => parseFunction(res.json() || []))
      .catch(e => RestService.handleError(this, e));
  }

  delete<I>(path: string): Observable<boolean> {
    return this.http.delete(
      `${this.url}${path}`,
      {headers: this.headers()})
      .map((res: Response) => BooleanResponse.fromJSON(res.json() || {}).success)
      .catch(e => RestService.handleError(this, e));
  }

  post<I, O>(path: string, value?: I, parseFunction?: (value: any) => O): Observable<O> {
    return this.http.post(
      `${this.url}${path}`,
      value ? JSON.stringify(value) : "{}",
      {headers: this.headers()})
      .map((res: Response) => parseFunction ? parseFunction(res.json() || {}) : (res.json() || {}))
      .catch(e => RestService.handleError(this, e));
  }

  postNotAuthenticated<I, O>(path: string, value: I, parseFunction: (value: any) => O): Observable<O> {
    return this.http.post(`${this.notAuthenticatedUrl}${path}`, JSON.stringify(value), {headers: this.notAuthenticatedHeaders()})
      .map((res: Response) => parseFunction(res.json() || {}))
      .catch(e => RestService.handleError(this, e));
  }

  private static handleError(rest:RestService, error: Response | any) {
    let errMsg: string;
    if (error instanceof Response) {
      if (error.status == 403) {
        rest.router.navigateByUrl("/login");
        return;
      }
      errMsg = `${error.status} - ${error.statusText || ''}`;
    } else {
      errMsg = error.message ? error.message : error.toString();
    }
    rest.errorEmitter.emit(errMsg);
    return Observable.throw(errMsg);
  }

}

export class BooleanResponse {
  constructor(public success: boolean) {
  }

  static fromJSON(json: any): BooleanResponse {
    const value = Object.create(BooleanResponse.prototype);
    const result = Object.assign(value, json, {});

    return result;
  }
}
