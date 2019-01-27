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


import {Observable, throwError as observableThrowError} from 'rxjs';

import {catchError, map} from 'rxjs/operators';
import {EventEmitter, Injectable} from "@angular/core";
import {HttpClient, HttpHeaders} from "@angular/common/http";
import {environment} from "../../../environments/environment";
import {Router} from "@angular/router";
import {FileItem, FileUploader, ParsedResponseHeaders} from "ng2-file-upload";

@Injectable()
export class RestService {

  private readonly url: string;
  readonly notAuthenticatedUrl: string;

  readonly errorEmitter: EventEmitter<string> = new EventEmitter();

  constructor(private http: HttpClient,
              private router: Router) {
    this.notAuthenticatedUrl = environment.serverUrl;
    this.url = `${environment.serverUrl}/admin`;
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
      catchError(e => RestService.handleError(this, e)),);
  }

  getArray<T>(path: string, parseFunction: (value: any) => T[]): Observable<T[]> {
    return this.http.get(`${this.url}${path}`, {headers: this.headers(), withCredentials: true}).pipe(
      map((res: string) => parseFunction(res || [])),
      catchError(e => RestService.handleError(this, e)),);
  }

  delete<I>(path: string): Observable<boolean> {
    return this.http.delete(
      `${this.url}${path}`,
      {headers: this.headers(), withCredentials: true}).pipe(
      map((res: string) => BooleanResponse.fromJSON(res || {}).success),
      catchError(e => RestService.handleError(this, e)),);
  }

  post<I, O>(path: string, value?: I, parseFunction?: (value: any) => O, baseUrl?: string): Observable<O> {
    return this.http.post(
      `${baseUrl ? baseUrl : this.url}${path}`,
      value ? JSON.stringify(value) : "{}",
      {headers: this.headers(), withCredentials: true}).pipe(
      map((res: string) => parseFunction ? parseFunction(res || {}) : (res || {}) as O),
      catchError(e => RestService.handleError(this, e)),);
  }

  put<I, O>(path: string, value?: I, parseFunction?: (value: any) => O): Observable<O> {
    return this.http.put(
      `${this.url}${path}`,
      value ? JSON.stringify(value) : "{}",
      {headers: this.headers(), withCredentials: true}).pipe(
      map((res: string) => parseFunction ? parseFunction(res || {}) : (res || {}) as O),
      catchError(e => RestService.handleError(this, e)),);
  }

  getNotAuthenticated<T>(path: string, parseFunction: (value: any) => T): Observable<T> {
    return this.http.get(`${this.notAuthenticatedUrl}${path}`, {headers: this.headers(), withCredentials: true}).pipe(
      map((res: string) => parseFunction(res || {})),
      catchError(e => RestService.handleError(this, e)),);
  }

  postNotAuthenticated<I, O>(path: string, value?: I, parseFunction?: (value: any) => O): Observable<O> {
    return this.http.post(
      `${this.notAuthenticatedUrl}${path}`,
      value ? JSON.stringify(value) : "{}",
      {headers: this.notAuthenticatedHeaders(), withCredentials: true}).pipe(
      map((res: string) => parseFunction ? parseFunction(res || {}) : (res || {}) as O),
      catchError(e => RestService.handleError(this, e)),);
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
        RestService.handleError(this, response ? response : `Error ${status}`);
      };

    return uploader;
  }

  private static handleError(rest: RestService, error: Response | any) {
    console.error(error);
    let errMsg: string;
    if (error instanceof Response) {
      if (error.status === 403) {
        rest.router.navigateByUrl("/login");
        return;
      }
      errMsg = error.status === 400
        ? error.statusText || ''
        : `Server error : ${error.status} - ${error.statusText || ''}`;
    } else {
      errMsg = error.message ? error.message : error.toString();
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
