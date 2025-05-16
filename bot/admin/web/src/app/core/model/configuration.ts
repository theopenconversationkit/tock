/*
 * Copyright (C) 2017/2025 SNCF Connect & Tech
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

import { RestService } from '../../core-nlp/rest/rest.service';

export class BotConfiguration {
  constructor(
    public botId: string,
    public name: string,
    public namespace: string,
    public nlpModel: string,
    public configurations?: BotApplicationConfiguration[],
    public webhookUrl?: string,
    public apiKey?: string,
    public supportedLocales?: string[]
  ) {}

  static fromJSON(json?: any): BotConfiguration {
    const value = Object.create(BotConfiguration.prototype);

    const result = Object.assign(value, json, {});

    return result;
  }

  static fromJSONArray(json?: Array<any>): BotConfiguration[] {
    return json ? json.map(BotConfiguration.fromJSON) : [];
  }
}

export class BotApplicationConfiguration {
  constructor(
    public applicationId: string,
    public botId: string,
    public namespace: string,
    public nlpModel: string,
    public connectorType: ConnectorType,
    public name: string,
    public parameters: Map<string, string>,
    public baseUrl?: string,
    public _id?: string,
    public ownerConnectorType?: ConnectorType,
    public path?: string,
    public fillMandatoryValues?: boolean,
    public targetConfigurationId?: string
  ) {}

  static getRestConfiguration(allConfs: BotApplicationConfiguration[], conf: BotApplicationConfiguration): BotApplicationConfiguration {
    if (conf.connectorType.isRest()) {
      return conf;
    }

    return allConfs.find((c) => c.targetConfigurationId === conf._id);
  }

  ownConnectorType(): ConnectorType {
    return this.ownerConnectorType ? this.ownerConnectorType : this.connectorType;
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
  constructor(public id: string, public userInterfaceType: UserInterfaceType) {}

  public label(): string {
    return this.isRest() ? 'test' : this.id;
  }

  iconUrl(): string {
    return RestService.connectorIconUrl(this.id);
  }

  isRest(): boolean {
    return this.id === 'rest';
  }

  static fromJSON(json?: any): ConnectorType {
    if (!json) {
      return null;
    }
    const value = Object.create(ConnectorType.prototype);

    const result = Object.assign(value, json, {
      userInterfaceType: UserInterfaceType[json.userInterfaceType]
    });

    return result;
  }

  static fromJSONArray(json?: Array<any>): ConnectorType[] {
    return json ? json.map(ConnectorType.fromJSON) : [];
  }
}

export class ConnectorTypeConfiguration {
  constructor(public connectorType: ConnectorType, public fields: ConnectorTypeConfigurationField[], public svgIcon: string) {}

  static fromJSON(json?: any): ConnectorTypeConfiguration {
    if (!json) {
      return null;
    }
    const value = Object.create(ConnectorTypeConfiguration.prototype);

    const result = Object.assign(value, json, {
      connectorType: ConnectorType.fromJSON(json.connectorType),
      fields: ConnectorTypeConfigurationField.fromJSONArray(json.fields)
    });

    return result;
  }

  static fromJSONArray(json?: Array<any>): ConnectorTypeConfiguration[] {
    return json ? json.map(ConnectorTypeConfiguration.fromJSON) : [];
  }
}

export class ConnectorTypeConfigurationField {
  constructor(public label: string, public key: string, public mandatory: boolean) {}

  static fromJSON(json?: any): ConnectorTypeConfigurationField {
    if (!json) {
      return null;
    }
    const value = Object.create(ConnectorTypeConfigurationField.prototype);

    const result = Object.assign(value, json, {});

    return result;
  }

  static fromJSONArray(json?: Array<any>): ConnectorTypeConfigurationField[] {
    return json ? json.map(ConnectorTypeConfigurationField.fromJSON) : [];
  }
}

export enum UserInterfaceType {
  textChat,
  voiceAssistant,
  textAndVoiceAssistant
}

export enum EventType {
  sentence,
  choice,
  attachment,
  location,
  debug,
  sentenceWithFootnotes
}

export enum AttachmentType {
  image,
  audio,
  video,
  file
}

export const defaultUserInterfaceType = UserInterfaceType.textChat;
