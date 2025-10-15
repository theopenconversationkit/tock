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

import { AttachmentType, ConnectorType, EventType, UserInterfaceType } from '../../core/model/configuration';
import { JsonUtils } from '../../model/commons';
import { ClassifiedEntity } from '../../model/nlp';
import { IntentName } from '../../bot/model/story';
import { Annotation } from '../components/annotation/annotations';

export class DialogReport {
  displayActions: boolean;

  constructor(
    public actions: ActionReport[],
    public id: string,
    public obfuscated: boolean = false,
    public rating?: number,
    public review?: string
  ) {}

  static fromJSON(json?: any): DialogReport {
    const value = Object.create(DialogReport.prototype);

    const result = Object.assign(value, json, {
      actions: ActionReport.fromJSONArray(json.actions)
    });

    return result;
  }

  static fromJSONArray(json?: Array<any>): DialogReport[] {
    return json ? json.map(DialogReport.fromJSON) : [];
  }
}

export interface ActionReportMetadata {
  isGenAiRagAnswer: boolean;
  observabilityInfo?: {
    traceId: string;
    traceName: string;
    traceUrl: string;
  };
}

export class ActionReport {
  constructor(
    public playerId: PlayerId,
    public date: Date,
    public message: BotMessage,
    public id: string,
    public test: boolean,
    public connectorType?: ConnectorType,
    public applicationId?: string
  ) {}

  metadata?: ActionReportMetadata;

  annotation?: Annotation;

  _nlpStats?: NlpCallStats; // expando to store nlpStats of the action when getting a DialogReport

  isBot(): boolean {
    return this.playerId.type == PlayerType.bot;
  }

  static fromJSON(json?: any): ActionReport {
    const value = Object.create(ActionReport.prototype);

    const result = Object.assign(value, json, {
      playerId: PlayerId.fromJSON(json.playerId),
      message: BotMessage.fromJSON(json.message),
      connectorType: ConnectorType.fromJSON(json.connectorType)
    });

    return result;
  }

  static fromJSONArray(json?: Array<any>): ActionReport[] {
    return json ? json.map(ActionReport.fromJSON) : [];
  }
}

export abstract class BotMessage {
  eventType: string;

  constructor(public eventTypeEnum: EventType, public delay: number) {
    this.eventType = EventType[eventTypeEnum];
  }

  isSentence(): boolean {
    return this.eventTypeEnum === EventType.sentence;
  }

  isAttachment(): boolean {
    return this.eventTypeEnum === EventType.attachment;
  }

  isChoice(): boolean {
    return this.eventTypeEnum === EventType.choice;
  }

  isLocation(): boolean {
    return this.eventTypeEnum === EventType.location;
  }

  isDebug(): boolean {
    return this.eventTypeEnum === EventType.debug;
  }

  isSentenceWithFootnotes(): boolean {
    return this.eventTypeEnum === EventType.sentenceWithFootnotes;
  }

  static fromJSON(json?: any): BotMessage {
    if (!json) {
      return null;
    }

    const eventType = EventType[json.eventType as string];
    switch (eventType) {
      case EventType.sentence:
        return Sentence.fromJSON(json);
      case EventType.choice:
        return Choice.fromJSON(json);
      case EventType.attachment:
        return Attachment.fromJSON(json);
      case EventType.location:
        return Location.fromJSON(json);
      case EventType.debug:
        return Debug.fromJSON(json);
      case EventType.sentenceWithFootnotes:
        return SentenceWithFootnotes.fromJSON(json);
      default:
        throw 'unknown type : ' + json.type;
    }
  }

  static fromJSONArray(json?: Array<any>): BotMessage[] {
    return json ? json.map(BotMessage.fromJSON) : [];
  }
}

export class Attachment extends BotMessage {
  constructor(public override delay: number, public url: string, public type: AttachmentType) {
    super(EventType.attachment, delay);
  }

  isImage(): boolean {
    return this.type == AttachmentType.image;
  }

  static override fromJSON(json?: any): Attachment {
    const value = Object.create(Attachment.prototype);

    const result = Object.assign(value, json, {
      type: AttachmentType[json.type],
      eventTypeEnum: EventType.attachment
    });

    return result;
  }

  static override fromJSONArray(json?: Array<any>): Attachment[] {
    return json ? json.map(Attachment.fromJSON) : [];
  }
}

export class Choice extends BotMessage {
  constructor(public override delay: number, public intentName: String, public parameters: Map<String, String>) {
    super(EventType.choice, delay);
  }

  static override fromJSON(json?: any): Choice {
    const value = Object.create(Choice.prototype);

    const result = Object.assign(value, json, {
      parameters: JsonUtils.jsonToMap(json.parameters),
      eventTypeEnum: EventType.choice
    });

    return result;
  }

  static override fromJSONArray(json?: Array<any>): Choice[] {
    return json ? json.map(Choice.fromJSON) : [];
  }
}

export class Location extends BotMessage {
  constructor(public override delay: number, public location?: UserLocation) {
    super(EventType.location, delay);
  }

  static override fromJSON(json?: any): Location {
    const value = Object.create(Location.prototype);

    const result = Object.assign(value, json, {
      location: UserLocation.fromJSON(json.location),
      eventTypeEnum: EventType.location
    });

    return result;
  }

  static override fromJSONArray(json?: Array<any>): Location[] {
    return json ? json.map(Location.fromJSON) : [];
  }
}

export class UserLocation {
  constructor(public lat: number, public lng: number) {}

  static fromJSON(json?: any): UserLocation {
    const value = Object.create(UserLocation.prototype);

    const result = Object.assign(value, json, {});

    return result;
  }
}

export class Sentence extends BotMessage {
  constructor(
    public override delay: number,
    public messages: SentenceElement[],
    public text?: string,
    public userInterface?: UserInterfaceType
  ) {
    super(EventType.sentence, delay);
  }

  static override fromJSON(json?: any): Sentence {
    const value = Object.create(Sentence.prototype);

    const result = Object.assign(value, json, {
      messages: SentenceElement.fromJSONArray(json.messages),
      eventTypeEnum: EventType.sentence
    });

    return result;
  }

  static override fromJSONArray(json?: Array<any>): Sentence[] {
    return json ? json.map(Sentence.fromJSON) : [];
  }
}

export interface Footnote {
  title: string;
  url: string;
  content?: string;
  score?: number;
  rrfScore?: number;
  _showFullContent?: boolean;
  identifier: string;
}

export class SentenceWithFootnotes extends BotMessage {
  constructor(public override delay: number, public footnotes: Footnote[], public text?: string, public userInterface?: UserInterfaceType) {
    super(EventType.sentenceWithFootnotes, delay);
  }

  static override fromJSON(json?: any): SentenceWithFootnotes {
    const value = Object.create(SentenceWithFootnotes.prototype);

    const result = Object.assign(value, json, {
      footnotes: json.footnotes,
      eventTypeEnum: EventType.sentenceWithFootnotes
    });

    return result;
  }

  static override fromJSONArray(json?: Array<any>): SentenceWithFootnotes[] {
    return json ? json.map(SentenceWithFootnotes.fromJSON) : [];
  }
}

export class Debug extends BotMessage {
  constructor(public override delay: number, public data: any, public text?: string, public userInterface?: UserInterfaceType) {
    super(EventType.debug, delay);
  }

  static override fromJSON(json?: any): BotMessage {
    const value = Object.create(Debug.prototype);

    const result = Object.assign(value, json, {
      data: json.data,
      eventTypeEnum: EventType.debug
    });

    return result;
  }

  static override fromJSONArray(json?: Array<any>): BotMessage[] {
    return json ? json.map(Debug.fromJSON) : [];
  }
}

export class SentenceElement {
  constructor(
    public connectorType: ConnectorType,
    public attachments: Attachment[],
    public choices: Choice[],
    public texts: Map<String, String>,
    public locations: Location[],
    public metadata: Map<String, String>,
    public subElements: SentenceSubElement[]
  ) {}

  isEmptyElement(): boolean {
    return this.attachments.length === 0 && this.choices.length === 0 && this.locations.length === 0 && this.texts.size === 0;
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
      subElements: SentenceSubElement.fromJSONArray(json.subElements)
    });

    return result;
  }

  static fromJSONArray(json?: Array<any>): SentenceElement[] {
    return json ? json.map(SentenceElement.fromJSON) : [];
  }
}

export class SentenceSubElement {
  constructor(
    public attachments: Attachment[],
    public choices: Choice[],
    public texts: Map<String, String>,
    public metadata: Map<String, String>,
    public locations: Location[]
  ) {}

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

export class PlayerId {
  constructor(public id: string, public type: PlayerType = PlayerType.user) {}

  static fromJSON(json?: any): PlayerId {
    const value = Object.create(PlayerId.prototype);

    const result = Object.assign(value, json, {
      type: PlayerType[json.type]
    });

    return result;
  }
}

export enum PlayerType {
  user,
  bot
}

export class NlpCallStats {
  constructor(
    public locale: string,
    public intentResult: IntentName,
    public entityResult: ClassifiedEntity[],
    public entityResultAfterMerge: ClassifiedEntity[],
    public nlpQuery: any,
    public nlpResult: any
  ) {}

  nlpQueryAsJson(): string {
    return JSON.stringify(this.nlpQuery, null, 2);
  }

  nlpResultAsJson(): string {
    return this.nlpResult ? JSON.stringify(this.nlpResult, null, 2) : 'none';
  }

  static fromJSON(json?: any): NlpCallStats {
    const value = Object.create(NlpCallStats.prototype);

    const result = Object.assign(value, json, {
      intentResult: IntentName.fromJSON(value.intentResult),
      entityResult: ClassifiedEntity.fromJSONArray(value.entityResult),
      entityResultAfterMerge: ClassifiedEntity.fromJSONArray(value.entityResultAfterMerge)
    });

    return result;
  }
}

export interface ActionNlpStats {
  dialogId: string;
  actionId: string;
  appNamespace: string;
  date: string;
  stats: NlpCallStats;
}

export interface DialogStatsQuery {
  namespace: string;
  applicationName: string;
  from: Date;
  to: Date;
}

export interface DialogStatsGroupResult {
  test: DialogStatsQueryResult;
  prod: DialogStatsQueryResult;
}

export interface DialogStatsQueryResult {
  allUserActions: CountResult[];
  allUserActionsExceptRag: CountResult[];
  allUserRagActions: CountResult[];
  knownIntentUserActions: CountResult[];
  unknownIntentUserActions: CountResult[];
  unknownIntentUserActionsExceptRag: CountResult[];
}

export interface CountResult {
  _id: string; // applicationName
  total: number;
}

export interface DialogStats {
  test: DialogCounts;
  prod: DialogCounts;
}

export interface DialogCounts {
  allUserActions: number;
  allUserActionsExceptRag: number;
  allUserRagActions: number;
  knownIntentUserActions: number;
  unknownIntentUserActions: number;
  unknownIntentUserActionsExceptRag: number;
}

