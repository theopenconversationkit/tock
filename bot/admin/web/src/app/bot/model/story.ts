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

import {PaginatedQuery} from "../../model/commons";
import {EntityDefinition, Intent, Sentence} from "../../model/nlp";
import {I18nLabel} from "./i18n";
import {BotService} from "../bot-service";
import {Observable, of} from "rxjs";

export class CreateStoryRequest {

  constructor(public story: StoryDefinitionConfiguration,
              public language: string,
              public firstSentences: string[]) {
  }

}

export class StorySearchQuery extends PaginatedQuery {

  constructor(public namespace: string,
              public applicationName: string,
              public language: string,
              public start: number,
              public size: number) {
    super(namespace, applicationName, language, start, size)
  }

}

export class Story {

  constructor(public storyDefinition: StoryDefinitionConfiguration,
              public firstSentences: Sentence[]) {

  }

  static fromJSON(json: any): Story {
    const value = Object.create(Story.prototype);
    const result = Object.assign(value, json, {
      storyDefinition: StoryDefinitionConfiguration.fromJSON(json.storyDefinition),
      firstSentences: Sentence.fromJSONArray(json.firstSentences)
    });


    return result;
  }

  static fromJSONArray(json?: Array<any>): Story[] {
    return json ? json.map(Story.fromJSON) : [];
  }
}

export abstract class AnswerContainer {

  protected constructor(
    public currentType: AnswerConfigurationType,
    public answers: AnswerConfiguration[],
    public category: string) {
  }

  abstract containerId(): string

  abstract save(bot: BotService): Observable<AnswerContainer>

  isSimpleAnswer(): boolean {
    return this.currentType === AnswerConfigurationType.simple;
  }

  isScriptAnswer(): boolean {
    return this.currentType === AnswerConfigurationType.script;
  }

  simpleAnswer(): SimpleAnswerConfiguration {
    return this.findAnswer(AnswerConfigurationType.simple) as SimpleAnswerConfiguration;
  }

  scriptAnswer(): ScriptAnswerConfiguration {
    return this.findAnswer(AnswerConfigurationType.script) as ScriptAnswerConfiguration;
  }

  currentAnswer(): AnswerConfiguration {
    return this.findAnswer(this.currentType);
  }

  private findAnswer(type: AnswerConfigurationType): AnswerConfiguration {
    return this.answers.find(c => c.answerType === type)
  }

  simpleTextView(): string {
    return this.currentAnswer().simpleTextView();
  }

}

export class StoryDefinitionConfiguration extends AnswerContainer {

  public version: number = 0;
  public mandatoryEntities: MandatoryEntity[] = [];
  public steps: StoryStep[] = [];
  public description: string = "";
  public _id: string;

  constructor(public storyId: string,
              public botId: string,
              public intent: IntentName,
              currentType: AnswerConfigurationType,
              public namespace: string,
              answers: AnswerConfiguration[] = [],
              category: string = "default",
              public name: string = storyId,
              public userSentence: string = ""
  ) {
    super(currentType, answers, category);
  }

  containerId(): string {
    return this.storyId;
  }


  save(bot: BotService): Observable<AnswerContainer> {
    return bot.saveStory(this)
  }

  static fromJSON(json: any): StoryDefinitionConfiguration {
    const value = Object.create(StoryDefinitionConfiguration.prototype);
    const result = Object.assign(value, json, {
      intent: IntentName.fromJSON(json.intent),
      currentType: AnswerConfigurationType[json.currentType],
      answers: AnswerConfiguration.fromJSONArray(json.answers),
      mandatoryEntities: MandatoryEntity.fromJSONArray(json.mandatoryEntities),
      steps: StoryStep.fromJSONArray(json.steps)
    });

    return result;
  }

  static fromJSONArray(json?: Array<any>): StoryDefinitionConfiguration[] {
    return json ? json.map(StoryDefinitionConfiguration.fromJSON) : [];
  }
}

export enum AnswerConfigurationType {
  simple,
  message,
  script,
  builtin
}

export class MandatoryEntity extends AnswerContainer {

  public entity: EntityDefinition;
  public intentDefinition: Intent;

  constructor(public role: string,
              public intent: IntentName,
              answers: AnswerConfiguration[],
              currentType: AnswerConfigurationType,
              category: string) {
    super(currentType, answers, category)
  }

  containerId(): string {
    return this.role;
  }

  save(bot: BotService): Observable<AnswerContainer> {
    return of(this);
  }

  clone(): MandatoryEntity {
    return new MandatoryEntity(this.role, this.intent.clone(), this.answers.slice(0).map(a => a.clone()), this.currentType, this.category);
  }

  static fromJSON(json: any): MandatoryEntity {
    const value = Object.create(MandatoryEntity.prototype);
    const result = Object.assign(value, json, {
      intent: IntentName.fromJSON(json.intent),
      currentType: AnswerConfigurationType[json.currentType],
      answers: AnswerConfiguration.fromJSONArray(json.answers)
    });

    return result;
  }

  static fromJSONArray(json?: Array<any>): MandatoryEntity[] {
    return json ? json.map(MandatoryEntity.fromJSON) : [];
  }
}

export class StoryStep extends AnswerContainer {

  public intentDefinition: Intent;
  public new: boolean;

  constructor(public name: string,
              public intent: IntentName,
              answers: AnswerConfiguration[],
              currentType: AnswerConfigurationType,
              category: string,
              public userSentence: string,
              public parentName?: string) {
    super(currentType, answers, category)
  }

  containerId(): string {
    return this.name;
  }

  save(bot: BotService): Observable<AnswerContainer> {
    return of(this);
  }

  clone(): StoryStep {
    return new StoryStep(
      this.name,
      this.intent.clone(),
      this.answers.slice(0).map(a => a.clone()),
      this.currentType,
      this.category,
      this.userSentence,
      this.parentName
    );
  }

  static fromJSON(json: any): StoryStep {
    const value = Object.create(StoryStep.prototype);
    const result = Object.assign(value, json, {
      intent: IntentName.fromJSON(json.intent),
      currentType: AnswerConfigurationType[json.currentType],
      answers: AnswerConfiguration.fromJSONArray(json.answers)
    });

    return result;
  }

  static fromJSONArray(json?: Array<any>): StoryStep[] {
    return json ? json.map(StoryStep.fromJSON) : [];
  }
}

export class IntentName {
  constructor(public name: string) {
  }

  clone(): IntentName {
    return new IntentName(this.name);
  }

  static fromJSON(json: any): IntentName {
    const value = Object.create(IntentName.prototype);
    const result = Object.assign(value, json, {});
    return result;
  }

  static fromJSONArray(json?: Array<any>): IntentName[] {
    return json ? json.map(IntentName.fromJSON) : [];
  }
}

export abstract class AnswerConfiguration {

  protected constructor(public answerType: AnswerConfigurationType, public modified: boolean = false) {
  }

  abstract simpleTextView(): string

  invalidMessage(): string {
    return null;
  }

  checkAfterReset(bot: BotService) {
    this.modified = false;
  }

  abstract clone(): AnswerConfiguration

  static fromJSON(json: any): AnswerConfiguration {
    if (!json) {
      return null;
    }

    const answerType = AnswerConfigurationType[json.answerType as string];
    switch (answerType) {
      case AnswerConfigurationType.simple :
        return SimpleAnswerConfiguration.fromJSON(json);
      case AnswerConfigurationType.script :
        return ScriptAnswerConfiguration.fromJSON(json);
      default:
        throw "unknown type : " + json.type
    }
  }

  static fromJSONArray(json?: Array<any>): AnswerConfiguration[] {
    return json ? json.map(AnswerConfiguration.fromJSON) : [];
  }
}

export class SimpleAnswerConfiguration extends AnswerConfiguration {

  constructor(public answers: SimpleAnswer[]) {
    super(AnswerConfigurationType.simple, true)
  }

  simpleTextView(): string {
    const r = this.answers && this.answers.length > 0 ? this.answers[0].label.defaultLocalizedLabel().label : "[no text yet]";

    return r.substring(0, Math.min(r.length, 30)) + (r.length > 30 || this.answers.length > 1 ? "..." : "");
  }

  invalidMessage(): string {
    if (this.answers.length === 0) {
      return "Please set at least one sentence";
    } else {
      return null;
    }
  }

  clone(): AnswerConfiguration {
    return new SimpleAnswerConfiguration(
      this.answers.map(a => a.clone())
    );
  }

  checkAfterReset(bot: BotService) {
    super.checkAfterReset(bot);
    //save again label if useful
    let count = 0;
    this.answers.forEach(a => bot.saveI18nLabel(a.label).subscribe(r => count++));
  }

  static fromJSON(json: any): SimpleAnswerConfiguration {
    const value = Object.create(SimpleAnswerConfiguration.prototype);
    const result = Object.assign(value, json, {
      answers: SimpleAnswer.fromJSONArray(json.answers),
      answerType: AnswerConfigurationType.simple
    });
    return result;
  }
}

export class SimpleAnswer {

  constructor(public label: I18nLabel,
              public delay: number = -1) {
  }

  clone(): SimpleAnswer {
    return new SimpleAnswer(this.label.clone(), this.delay);
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

export class ScriptAnswerConfiguration extends AnswerConfiguration {

  constructor(
    public scriptVersions: ScriptAnswerVersionedConfiguration[],
    public current: ScriptAnswerVersionedConfiguration) {
    super(AnswerConfigurationType.script, true)
  }

  invalidMessage(): string {
    if (this.current.script.trim().length === 0) {
      return "Please set a non empty script";
    } else {
      return null;
    }
  }

  simpleTextView(): string {
    return "[Script]";
  }

  clone(): AnswerConfiguration {
    return new ScriptAnswerConfiguration(
      this.scriptVersions.map(s => new ScriptAnswerVersionedConfiguration(s.script, s.date)),
      new ScriptAnswerVersionedConfiguration(this.current.script, this.current.date)
    )
  }

  static fromJSON(json: any): ScriptAnswerConfiguration {
    const value = Object.create(ScriptAnswerConfiguration.prototype);
    const result = Object.assign(value, json, {
      scriptVersions: ScriptAnswerVersionedConfiguration.fromJSONArray(json.scriptVersions),
      current: ScriptAnswerVersionedConfiguration.fromJSON(json.current),
      answerType: AnswerConfigurationType.script
    });
    return result;
  }
}

export class ScriptAnswerVersionedConfiguration {

  constructor(public script: string, public date?: Date) {
  }

  static fromJSON(json: any): ScriptAnswerVersionedConfiguration {
    const value = Object.create(ScriptAnswerVersionedConfiguration.prototype);
    const result = Object.assign(value, json, {});
    return result;
  }

  static fromJSONArray(json?: Array<any>): ScriptAnswerVersionedConfiguration[] {
    return json ? json.map(ScriptAnswerVersionedConfiguration.fromJSON) : [];
  }
}
