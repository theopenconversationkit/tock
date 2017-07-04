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
import {RestService} from "tock-nlp-admin/src/app/core/rest/rest.service";
import {StateService} from "tock-nlp-admin/src/app/core/state.service";
import {BotIntent, BotIntentSearchQuery, CreateBotIntentRequest} from "./model/bot-intent";
import {Intent} from "tock-nlp-admin/src/app/model/application";
import {Observable} from "rxjs/Observable";

@Injectable()
export class BotService {

  constructor(private rest: RestService,
              private state: StateService) {
  }

  newBotIntent(request: CreateBotIntentRequest): Observable<Intent> {
    return this.rest.post("/bot/intent", request, Intent.fromJSON);
  }

  getBotIntents(request: BotIntentSearchQuery): Observable<BotIntent[]> {
    return this.rest.post("/bot/intents/search", request, BotIntent.fromJSONArray);
  }

  deleteBotIntent(storyDefinitionId: string): Observable<boolean> {
    return this.rest.delete(`/bot/intent/${storyDefinitionId}`);
  }
}
