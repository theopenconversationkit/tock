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

export class BotApplicationConfiguration {

  constructor(public applicationId: string,
              public botId: string,
              public namespace: string,
              public nlpModel: string,
              public connectorType: ConnectorType,
              public name: string,
              public baseUrl?: string,
              public _id?: string,
              public ownerConnectorType?: ConnectorType,) {
  }

  static fromJSON(json?: any): BotApplicationConfiguration {
    const value = Object.create(BotApplicationConfiguration.prototype);

    const result = Object.assign(value, json, {
      connectorType: ConnectorType.fromJSON(json.connectorType),
      ownerConnectorType: ConnectorType.fromJSON(json.ownerConnectorType)
    });

    return result;
  }

  static fromJSONArray(json?: Array<any>): BotApplicationConfiguration[] {
    return json ? json.map(BotApplicationConfiguration.fromJSON) : [];
  }
}

export class ConnectorType {

  constructor(public id: string,
              public userInterfaceType: UserInterfaceType,
              public asynchronous: boolean) {
  }

  isRest(): boolean {
    return this.id === "rest";
  }

  static fromJSON(json?: any): ConnectorType {
    if(!json) {
      return null;
    }
    const value = Object.create(ConnectorType.prototype);

    const result = Object.assign(value, json, {
      userInterfaceType: UserInterfaceType[json.userInterfaceType],
    });

    return result;
  }
}

export enum UserInterfaceType {
  textChat, voiceAssistant
}

export enum EventType {
  sentence, choice, attachment, location
}

export enum AttachmentType {
  image, audio, video, file
}
