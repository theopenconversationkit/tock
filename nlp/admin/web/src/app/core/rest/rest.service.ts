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

import {Injectable} from "@angular/core";
import {Http, Headers, Response} from "@angular/http";
import {Observable} from "rxjs";
import {environment} from "../../../environments/environment";
import {Router} from "@angular/router";

@Injectable()
export class RestService {

  private url: string;
  private notAuthenticatedUrl: string;
  private authToken: string;

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
      .catch(this.handleError);
  }

  getArray<T>(path: string, parseFunction: (value: any) => T[]): Observable<T[]> {
    return this.http.get(`${this.url}${path}`, {headers: this.headers()})
      .map((res: Response) => parseFunction(res.json() || []))
      .catch(this.handleError);
  }

  delete<I>(path: string): Observable<any> {
    return this.http.delete(
      `${this.url}${path}`,
      {headers: this.headers()})
      .map((res: Response) => (res.json() || {}))
      .catch(this.handleError);
  }

  post<I,O>(path: string, value?: I, parseFunction?: (value: any) => O): Observable<O> {
    return this.http.post(
      `${this.url}${path}`,
      value ? JSON.stringify(value) : "{}",
      {headers: this.headers()})
      .map((res: Response) => parseFunction ? parseFunction(res.json() || {}) : (res.json() || {}))
      .catch(this.handleError);
  }

  postNotAuthenticated<I,O>(path: string, value: I, parseFunction: (value: any) => O): Observable<O> {
    return this.http.post(`${this.notAuthenticatedUrl}${path}`, JSON.stringify(value), {headers: this.notAuthenticatedHeaders()})
      .map((res: Response) => parseFunction(res.json() || {}))
      .catch(this.handleError);
  }

  private handleError(error: Response | any) {
    let errMsg: string;
    if (error instanceof Response) {
      if (error.status == 403) {
        this.router.navigateByUrl("/login");
        return;
      }
      errMsg = `${error.status} - ${error.statusText || ''}`;
    } else {
      errMsg = error.message ? error.message : error.toString();
    }
    console.error(errMsg);
    return Observable.throw(errMsg);
  }

}
