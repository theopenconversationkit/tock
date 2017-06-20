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

import {BotMessage} from "app/shared/dialog-data";
import {ApplicationScopedQuery} from "tock-nlp-admin/src/app/model/commons";

export class BotDialogRequest extends ApplicationScopedQuery {

  constructor(public botApplicationConfigurationId: string,
              public text: string,
              public namespace: string,
              public applicationName: string,
              public language: string) {
    super(namespace, applicationName, language)
  }

}

export class BotDialogResponse {

  constructor(public messages: BotMessage[]) {
  }

  static fromJSON(json?: any): BotDialogResponse {
    const value = Object.create(BotDialogResponse.prototype);

    const result = Object.assign(value, json, {
      messages: BotMessage.fromJSONArray(json.messages),
    });

    return result;
  }

}

export class TestMessage {
  constructor(public bot: boolean,
              public text?: string,
              public message?: BotMessage) {

  }
}
