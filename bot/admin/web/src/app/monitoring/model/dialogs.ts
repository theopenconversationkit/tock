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
import {ApplicationScopedQuery} from "tock-nlp-admin/src/app/model/commons";
import {PlayerId, PlayerType} from "./users";
import {AttachmentType, EventType} from "../../shared/configuration";

export class DialogReportRequest extends ApplicationScopedQuery {

  constructor(public namespace: string,
              public applicationName: string,
              public language: string,
              public playerId: PlayerId) {
    super(namespace, applicationName, language)
  }
}

export class DialogReport {

  constructor(public actions: ActionReport[]) {
  }

  static fromJSON(json?: any): DialogReport {
    const value = Object.create(DialogReport.prototype);

    const result = Object.assign(value, json, {
      actions: ActionReport.fromJSONArray(json.actions)
    });

    return result;
  }
}

export class ActionReport {

  constructor(public playerId: PlayerId,
              public date: Date,
              public type: EventType,
              public text?: string,
              public connectorMessages?: any,
              public intent?: string,
              public parameters?: any,
              public url?: string,
              public attachmentType?: AttachmentType,
              public userLocation?: any) {
  }

  isBot(): boolean {
    return this.playerId.type == PlayerType.bot;
  }

  actionDisplay(): string {
    switch (this.type) {
      case EventType.sentence:
        return this.text ? this.text : JSON.stringify(this.connectorMessages);
      case EventType.choice:
        return "(click) " + this.intent + (JSON.stringify(this.parameters) === "{}" ? "" : "(" + JSON.stringify(this.parameters) + ")");
      case EventType.attachment:
        return "Send: " + this.url;
      case EventType.location:
        return JSON.stringify(this.userLocation);
    }
  }

  static fromJSON(json?: any): ActionReport {
    const value = Object.create(ActionReport.prototype);

    const result = Object.assign(value, json, {
      playerId: PlayerId.fromJSON(json.playerId),
      type: EventType[json.type],
      attachmentType: AttachmentType[json.attachmentType]
    });

    return result;
  }

  static fromJSONArray(json?: Array<any>): ActionReport[] {
    return json ? json.map(ActionReport.fromJSON) : [];
  }
}


