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


import {map} from 'rxjs/operators';
import {Injectable} from "@angular/core";
import {ActivatedRouteSnapshot, Resolve, Router, RouterStateSnapshot} from "@angular/router";
import {Observable} from "rxjs";
import {Application} from "../model/application";
import {ApplicationService} from "./applications.service";
import {ApplicationConfig} from "./application.config";

@Injectable()
export class ApplicationResolver implements Resolve<Application> {

  constructor(private config: ApplicationConfig,
              private appService: ApplicationService,
              private router: Router) {
  }

  resolve(route: ActivatedRouteSnapshot, state: RouterStateSnapshot): Observable<Application> {
    return this.appService.retrieveCurrentApplication().pipe(map(app => {
      if (app) {
        return app;
      } else {
        this.router.navigateByUrl(this.config.configurationUrl);
        return null;
      }
    }));
  }
}
