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

import {PaginatedQuery} from "tock-nlp-admin/src/app/model/commons";
import {Sentence} from "tock-nlp-admin/src/app/model/nlp";
import {I18nLabel} from "./i18n";

export class CreateBotIntentRequest {

  constructor(public botConfigurationId: string,
              public intent: string,
              public language: string,
              public firstSentences: string[],
              public reply: string) {
  }

}

export class UpdateBotIntentRequest {

  constructor(public storyDefinitionId: string,
              public reply: string) {
  }

}

export class BotIntentSearchQuery extends PaginatedQuery {

  constructor(public namespace: string,
              public applicationName: string,
              public language: string,
              public start: number,
              public size: number) {
    super(namespace, applicationName, language, start, size)
  }

}

export class BotIntent {

  constructor(public storyDefinition: StoryDefinitionConfiguration,
              public firstSentences: Sentence[]) {

  }

  static fromJSON(json: any): BotIntent {
    const value = Object.create(BotIntent.prototype);
    const result = Object.assign(value, json, {
      storyDefinition: StoryDefinitionConfiguration.fromJSON(json.storyDefinition),
      firstSentences: Sentence.fromJSONArray(json.firstSentences)
    });


    return result;
  }

  static fromJSONArray(json?: Array<any>): BotIntent[] {
    return json ? json.map(BotIntent.fromJSON) : [];
  }
}

export class StoryDefinitionConfiguration {

  textAnswer: string;

  constructor(public storyId: string,
              public botId: string,
              public intent: IntentName,
              public currentType: AnswerConfigurationType,
              public answers: AnswerConfiguration[],
              public _id: string) {

  }

  initTextAnswer() {
    this.textAnswer = this.simpleAnswer().answers[0].label.defaultLabel().label;
  }

  simpleAnswer(): SimpleAnswerConfiguration {
    return this.answers[0] as SimpleAnswerConfiguration;
  }

  static fromJSON(json: any): StoryDefinitionConfiguration {
    const value = Object.create(StoryDefinitionConfiguration.prototype);
    const result = Object.assign(value, json, {
      intent: IntentName.fromJSON(json.intent),
      currentType: AnswerConfigurationType[json.currentType],
      answers: AnswerConfiguration.fromJSONArray(json.answers)
    });

    return result;
  }
}

export enum AnswerConfigurationType {
  simple,
  message,
  script,
  builtin
}

export class IntentName {
  constructor(public name: string) {
  }

  static fromJSON(json: any): IntentName {
    const value = Object.create(IntentName.prototype);
    const result = Object.assign(value, json, {});
    return result;
  }
}

export abstract class AnswerConfiguration {

  constructor(public answerType: AnswerConfigurationType) {
  }

  static fromJSON(json: any): AnswerConfiguration {
    const value = Object.create(AnswerConfiguration.prototype);

    if (!json) {
      return null;
    }

    const answerType = AnswerConfigurationType[json.answerType as string];
    switch (answerType) {
      case AnswerConfigurationType.simple :
        return SimpleAnswerConfiguration.fromJSON(json);
      default:
        throw "unknown type : " + json.type
    }
  }

  static fromJSONArray(json?: Array<any>): AnswerConfiguration[] {
    return json ? json.map(AnswerConfiguration.fromJSON) : [];
  }
}

export abstract class SimpleAnswerConfiguration extends AnswerConfiguration {

  constructor(public answers: SimpleAnswer[]) {
    super(AnswerConfigurationType.simple)
  }

  static fromJSON(json: any): SimpleAnswerConfiguration {
    const value = Object.create(SimpleAnswerConfiguration.prototype);
    const result = Object.assign(value, json, {
      answers: SimpleAnswer.fromJSONArray(json.answers)
    });
    return result;
  }
}

export class SimpleAnswer {

  constructor(public label: I18nLabel,
              public delay: number) {
  }

  static fromJSON(json: any): SimpleAnswer {
    const value = Object.create(SimpleAnswer.prototype);
    const result = Object.assign(value, json, {
      label: I18nLabel.fromJSON(json.label)
    });
    return result;
  }

  static fromJSONArray(json?: Array<any>): SimpleAnswer[] {
    return json ? json.map(SimpleAnswer.fromJSON) : [];
  }
}
