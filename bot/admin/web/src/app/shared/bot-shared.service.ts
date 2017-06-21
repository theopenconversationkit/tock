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

import {Injectable, OnDestroy} from "@angular/core";
import {RestService} from "tock-nlp-admin/src/app/core/rest/rest.service";
import {StateService} from "tock-nlp-admin/src/app/core/state.service";
import {Observable} from "rxjs/Observable";
import {ApplicationScopedQuery} from "tock-nlp-admin/src/app/model/commons";
import {BotApplicationConfiguration} from "./configuration";

@Injectable()
export class BotSharedService implements OnDestroy {


  constructor(private rest: RestService,
              private state: StateService) {
  }

  ngOnDestroy(): void {
  }

  configurations(query: ApplicationScopedQuery): Observable<BotApplicationConfiguration[]> {
    return this.rest.post("/configuration/bots", query, BotApplicationConfiguration.fromJSONArray);
  }

}
