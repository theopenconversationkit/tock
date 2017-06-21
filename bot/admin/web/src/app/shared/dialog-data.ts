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

import {AttachmentType, ConnectorType, EventType} from "./configuration";
import {JsonUtils} from "../monitoring/model/json";
export abstract class BotMessage {

  constructor(public eventType: EventType,
              public delay: number) {
  }

  isSentence(): boolean {
    return this.eventType === EventType.sentence;
  }

  isAttachment(): boolean {
    return this.eventType === EventType.attachment;
  }

  isChoice(): boolean {
    return this.eventType === EventType.choice;
  }

  isLocation(): boolean {
    return this.eventType === EventType.location;
  }

  static fromJSON(json?: any): BotMessage {
    const eventType = EventType[json.eventType as string];
    switch (eventType) {
      case EventType.sentence :
        return Sentence.fromJSON(json);
      case EventType.choice :
        return Choice.fromJSON(json);
      case EventType.attachment :
        return Attachment.fromJSON(json);
      case EventType.location :
        return Location.fromJSON(json);
      default:
        throw "unknown type : " + json.eventType
    }
  }

  static fromJSONArray(json?: Array<any>): BotMessage[] {
    return json ? json.map(BotMessage.fromJSON) : [];
  }
}

export class Attachment extends BotMessage {
  constructor(public delay: number,
              public url: String,
              public type: AttachmentType) {
    super(EventType.attachment, delay)
  }

  isImage(): boolean {
    return this.type == AttachmentType.image;
  }

  static fromJSON(json?: any): Attachment {
    const value = Object.create(Attachment.prototype);

    const result = Object.assign(value, json, {
      type: AttachmentType[json.type],
      eventType: EventType.attachment
    });

    return result;
  }

  static fromJSONArray(json?: Array<any>): Attachment[] {
    return json ? json.map(Attachment.fromJSON) : [];
  }
}

export class Choice extends BotMessage {
  constructor(public delay: number,
              public intentName: String,
              public parameters: Map<String, String>) {
    super(EventType.choice, delay)
  }

  static fromJSON(json?: any): Choice {
    const value = Object.create(Choice.prototype);

    const result = Object.assign(value, json, {
      parameters: JsonUtils.jsonToMap(json.parameters),
      eventType: EventType.choice
    });

    return result;
  }

  static fromJSONArray(json?: Array<any>): Choice[] {
    return json ? json.map(Choice.fromJSON) : [];
  }
}

export class Location extends BotMessage {
  constructor(public delay: number,
              public location?: UserLocation) {
    super(EventType.location, delay)
  }

  static fromJSON(json?: any): Location {
    const value = Object.create(Location.prototype);

    const result = Object.assign(value, json, {
      location: UserLocation.fromJSON(json.location),
      eventType: EventType.location
    });

    return result;
  }

  static fromJSONArray(json?: Array<any>): Location[] {
    return json ? json.map(Location.fromJSON) : [];
  }
}

export class UserLocation {
  constructor(public lat: number,
              public lng: number) {
  }

  static fromJSON(json?: any): UserLocation {
    const value = Object.create(UserLocation.prototype);

    const result = Object.assign(value, json, {});

    return result;
  }
}

export class Sentence extends BotMessage {
  constructor(public delay: number,
              public messages: SentenceElement[],
              public text?: String) {
    super(EventType.sentence, delay)
  }

  static fromJSON(json?: any): Sentence {
    const value = Object.create(Sentence.prototype);

    const result = Object.assign(value, json, {
      messages: SentenceElement.fromJSONArray(json.messages),
      eventType: EventType.sentence
    });

    return result;
  }

  static fromJSONArray(json?: Array<any>): Sentence[] {
    return json ? json.map(Sentence.fromJSON) : [];
  }
}

export class SentenceElement {
  constructor(public connectorType: ConnectorType,
              public attachments: Attachment[],
              public choices: Choice[],
              public texts: Map<String, String>,
              public locations: Location[],
              public metadata: Map<String, String>,
              public subElements: SentenceSubElement[],) {
  }

  isEmptyElement(): boolean {
    return this.attachments.length === 0
      && this.choices.length === 0
      && this.locations.length === 0
      && this.texts.size === 0
  }

  static fromJSON(json?: any): SentenceElement {
    const value = Object.create(SentenceElement.prototype);

    const result = Object.assign(value, json, {
      connectorType: ConnectorType.fromJSON(json.connectorType),
      attachments: Attachment.fromJSONArray(json.attachments),
      choices: Choice.fromJSONArray(json.choices),
      texts: JsonUtils.jsonToMap(json.texts),
      metadata: JsonUtils.jsonToMap(json.metadata),
      locations: Location.fromJSONArray(json.locations),
      subElements: SentenceSubElement.fromJSONArray(json.subElements),
    });

    return result;
  }

  static fromJSONArray(json?: Array<any>): SentenceElement[] {
    return json ? json.map(SentenceElement.fromJSON) : [];
  }
}

export class SentenceSubElement {
  constructor(public attachments: Attachment[],
              public choices: Choice[],
              public texts: Map<String, String>,
              public metadata: Map<String, String>,
              public locations: Location[]) {

  }

  static fromJSON(json?: any): SentenceSubElement {
    const value = Object.create(SentenceSubElement.prototype);

    const result = Object.assign(value, json, {
      attachments: Attachment.fromJSONArray(json.attachments),
      choices: Choice.fromJSONArray(json.choices),
      texts: JsonUtils.jsonToMap(json.texts),
      metadata: JsonUtils.jsonToMap(json.metadata),
      locations: Location.fromJSONArray(json.locations)
    });

    return result;
  }

  static fromJSONArray(json?: Array<any>): SentenceSubElement[] {
    return json ? json.map(SentenceSubElement.fromJSON) : [];
  }
}

