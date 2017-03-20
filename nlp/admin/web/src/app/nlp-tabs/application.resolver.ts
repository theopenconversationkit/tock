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
import {Resolve, Router, ActivatedRouteSnapshot, RouterStateSnapshot} from "@angular/router";
import {Observable} from "rxjs";
import {Application} from "../model/application";
import {ApplicationsService} from "../applications/applications.service";

@Injectable()
export class ApplicationResolver implements Resolve<Application> {

  constructor(private appService: ApplicationsService, private router: Router) {
  }

  resolve(route: ActivatedRouteSnapshot, state: RouterStateSnapshot): Observable<Application> {
    return this.appService.retrieveCurrentApplication().map(app => {
      if (app) {
        return app;
      } else {
        this.router.navigate(['/applications']);
        return null;
      }
    });
  }
}
