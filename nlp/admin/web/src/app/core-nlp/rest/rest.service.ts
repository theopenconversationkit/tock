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


import {NEVER, Observable, throwError as observableThrowError} from 'rxjs';

import {catchError, map} from 'rxjs/operators';
import {EventEmitter, Injectable} from "@angular/core";
import {HttpClient, HttpErrorResponse, HttpHeaders} from "@angular/common/http";
import {environment} from "../../../environments/environment";
import {Router} from "@angular/router";
import {FileItem, FileUploader, ParsedResponseHeaders} from "ng2-file-upload";
import {JsonUtils} from "../../model/commons";

@Injectable()
export class RestService {

  readonly url: string;
  readonly notAuthenticatedUrl: string;
  private ssologin = environment.ssologin;

  readonly errorEmitter: EventEmitter<string> = new EventEmitter();

  constructor(private http: HttpClient,
              private router: Router) {
    this.notAuthenticatedUrl = environment.serverUrl;
    this.url = `${environment.serverUrl}/admin`;
  }

  isSSO(): boolean {
    return this.ssologin || document.cookie.indexOf("tock-sso=") !== -1;
  }

  private headers(): HttpHeaders {
    const headers = this.notAuthenticatedHeaders();
    //hack for dev env
    if (environment.autologin) {
      headers.append('Authorization', btoa(`${environment.default_user}:${environment.default_password}`));
    }
    return headers;
  }

  private notAuthenticatedHeaders(): HttpHeaders {
    const headers = new HttpHeaders();
    headers.append('Content-Type', 'application/json');
    return headers;
  }

  get<T>(path: string, parseFunction: (value: any) => T): Observable<T> {
    return this.http.get(`${this.url}${path}`, {headers: this.headers(), withCredentials: true}).pipe(
      map((res: string) => parseFunction(res || {})),
      catchError(e => this.handleError(this, e)),);
  }

  getArray<T>(path: string, parseFunction: (value: any) => T[]): Observable<T[]> {
    return this.http.get(`${this.url}${path}`, {headers: this.headers(), withCredentials: true}).pipe(
      map((res: string) => parseFunction(res || [])),
      catchError(e => this.handleError(this, e)),);
  }

  delete<I>(path: string): Observable<boolean> {
    return this.http.delete(
      `${this.url}${path}`,
      {headers: this.headers(), withCredentials: true}).pipe(
      map((res: string) => BooleanResponse.fromJSON(res || {}).success),
      catchError(e => this.handleError(this, e)),);
  }

  post<I, O>(path: string, value?: I, parseFunction?: (value: any) => O, baseUrl?: string): Observable<O> {
    return this.http.post(
      `${baseUrl ? baseUrl : this.url}${path}`,
      JsonUtils.stringify(value),
      {headers: this.headers(), withCredentials: true}).pipe(
      map((res: string) => parseFunction ? parseFunction(res || {}) : (res || {}) as O),
      catchError(e => this.handleError(this, e)),);
  }

  put<I, O>(path: string, value?: I, parseFunction?: (value: any) => O): Observable<O> {
    return this.http.put(
      `${this.url}${path}`,
      JsonUtils.stringify(value),
      {headers: this.headers(), withCredentials: true}).pipe(
      map((res: string) => parseFunction ? parseFunction(res || {}) : (res || {}) as O),
      catchError(e => this.handleError(this, e)),);
  }

  getNotAuthenticated<T>(path: string, parseFunction: (value: any) => T): Observable<T> {
    return this.http.get(`${this.notAuthenticatedUrl}${path}`, {headers: this.headers(), withCredentials: true}).pipe(
      map((res: string) => parseFunction(res || {})),
      catchError(e => this.handleError(this, e)),);
  }

  postNotAuthenticated<I, O>(path: string, value?: I, parseFunction?: (value: any) => O): Observable<O> {
    return this.http.post(
      `${this.notAuthenticatedUrl}${path}`,
      JsonUtils.stringify(value),
      {headers: this.notAuthenticatedHeaders(), withCredentials: true}).pipe(
      map((res: string) => parseFunction ? parseFunction(res || {}) : (res || {}) as O),
      catchError(e => this.handleError(this, e)),);
  }

  fileUploader(path: string): FileUploader {
    const uploader = new FileUploader({removeAfterUpload: true});
    this.setFileUploaderOptions(uploader, path);
    return uploader;
  }

  setFileUploaderOptions(uploader: FileUploader, path: string) {
    uploader.setOptions({url: `${this.url}${path}`});
    uploader.onErrorItem =
      (item: FileItem, response: string, status: number, headers: ParsedResponseHeaders) => {
        uploader.removeFromQueue(item);
        this.handleError(this, response ? response : `Error ${status}`);
      };

    return uploader;
  }

  private handleError(rest: RestService, error: Response | any) {
    console.error(error);
    let errMsg: string;
    const e = Array.isArray(error) ? error[0] : error;
    if (e instanceof Response) {
      if (e.status === 403 || e.status === 401) {
        rest.router.navigateByUrl("/login");
        return NEVER;
      }
      errMsg = error.status === 400
        ? error.statusText || ''
        : `Server error : ${error.status} - ${error.statusText || ''}`;
    } else {
      //strange things happen
      if (e && e.status === 0 && this.isSSO()) {
        console.error("invalid token - refresh");
        location.reload();
        return NEVER;
      }
      if (e instanceof HttpErrorResponse && e.status !== 400) {
        errMsg = e.message ? e.message : e.error;
      } else {
        errMsg = e.error ? e.error : (e.message ? e.message : e.toString());
      }
    }
    rest.errorEmitter.emit(errMsg);
    return observableThrowError(errMsg);
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
