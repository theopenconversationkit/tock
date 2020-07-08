/*
 * Copyright (C) 2017/2020 e-voyageurs technologies
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

import {PaginatedQuery} from "../../model/commons";
import {Dictionary, EntityDefinition, EntityType, Intent, Sentence} from "../../model/nlp";
import {I18nLabel} from "./i18n";
import {BotService} from "../bot-service";
import {Observable, of} from "rxjs";
import {AttachmentType, BotApplicationConfiguration} from "../../core/model/configuration";
import {StateService} from "../../core-nlp/state.service";

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
              public size: number,
              public category?: string,
              public textSearch?: string,
              public onlyConfiguredStory?: boolean) {
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

  allowNoAnwser(): boolean {
    return false;
  }

  isConfiguredAnswer(): boolean {
    return this.isSimpleAnswer() || this.isMessageAnswer() || this.isScriptAnswer();
  }

  isSimpleAnswer(): boolean {
    return this.currentType === AnswerConfigurationType.simple;
  }

  isMessageAnswer(): boolean {
    return this.currentType === AnswerConfigurationType.message;
  }

  isScriptAnswer(): boolean {
    return this.currentType === AnswerConfigurationType.script;
  }

  isBuiltIn(): boolean {
    return this.currentType === AnswerConfigurationType.builtin;
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

  simpleTextView(wide: boolean): string {
    const current = this.currentAnswer();
    if (current) return current.simpleTextView(wide);
    return "";
  }

}

export class StoryDefinitionConfigurationSummary {

  constructor(public storyId: string,
              public botId: string,
              public intent: IntentName,
              public currentType: AnswerConfigurationType,
              public category: string = "default",
              public name: string = storyId,
              public _id: string,
              public description: string = ""
  ) {

  }

  isSimpleAnswer(): boolean {
    return this.currentType === AnswerConfigurationType.simple;
  }

  isMessageAnswer(): boolean {
    return this.currentType === AnswerConfigurationType.message;
  }

  isScriptAnswer(): boolean {
    return this.currentType === AnswerConfigurationType.script;
  }

  isBuiltIn(): boolean {
    return this.currentType === AnswerConfigurationType.builtin;
  }

  static fromJSON(json: any): StoryDefinitionConfigurationSummary {
    const value = Object.create(StoryDefinitionConfigurationSummary.prototype);
    const result = Object.assign(value, json, {
      intent: IntentName.fromJSON(json.intent),
      currentType: AnswerConfigurationType[json.currentType]
    });

    return result;
  }

  static fromJSONArray(json?: Array<any>): StoryDefinitionConfigurationSummary[] {
    return json ? json.map(StoryDefinitionConfigurationSummary.fromJSON) : [];
  }

}


export class StoryDefinitionConfiguration extends AnswerContainer {

  public hideDetails: boolean = false;
  public selected: boolean = true;

  constructor(public storyId: string,
              public botId: string,
              public intent: IntentName,
              public currentType: AnswerConfigurationType,
              public namespace: string,
              answers: AnswerConfiguration[] = [],
              category: string = "default",
              public name: string = storyId,
              public userSentence: string = "",
              public userSentenceLocale: string,
              public features: StoryFeature[],
              public configurationName?: string,
              public _id?: string,
              public version: number = 0,
              public mandatoryEntities: MandatoryEntity[] = [],
              public steps: StoryStep[] = [],
              public description: string = "",
              public tags: string[] = []
  ) {
    super(currentType, answers, category);
  }

  containerId(): string {
    return this.storyId;
  }

  getFirstTag(): string {
    return this.tags && this.tags.length > 0 ? this.tags[0] : '';
  }


  prepareBeforeSend(): StoryDefinitionConfiguration {
    return new StoryDefinitionConfiguration(
      this.storyId,
      this.botId,
      this.intent,
      this.currentType,
      this.namespace,
      this.answers,
      this.category,
      this.name,
      this.userSentence,
      this.userSentenceLocale,
      this.features.map(f => new StoryFeature(f.botApplicationConfigurationId, f.enabled, f.switchToStoryId)),
      this.configurationName,
      this._id,
      this.version,
      this.mandatoryEntities,
      this.steps,
      this.description,
      this.tags
    );
  }

  save(bot: BotService): Observable<AnswerContainer> {
    return bot.saveStory(this)
  }

  isDisabled(configurationId: string): boolean {
    return !this.features || this.features.length < 1
      ? false
      : this.features.filter(f =>
      !f.enabled && !f.switchToStoryId
      && (!f.botApplicationConfigurationId || f.botApplicationConfigurationId == null
      || f.botApplicationConfigurationId == configurationId)
    ).length > 0;
  }

  isRedirected(configurationId: string): boolean {
    return !this.features || this.features.length < 1
      ? false
      : this.features.filter(f =>
      f.enabled && f.switchToStoryId
      && (!f.botApplicationConfigurationId || f.botApplicationConfigurationId == null
      || f.botApplicationConfigurationId == configurationId)
    ).length > 0;
  }

  static fromJSON(json: any): StoryDefinitionConfiguration {
    const value = Object.create(StoryDefinitionConfiguration.prototype);
    const result = Object.assign(value, json, {
      intent: IntentName.fromJSON(json.intent),
      currentType: AnswerConfigurationType[json.currentType],
      answers: AnswerConfiguration.fromJSONArray(json.answers),
      mandatoryEntities: MandatoryEntity.fromJSONArray(json.mandatoryEntities),
      steps: StoryStep.fromJSONArray(json.steps),
      features: StoryFeature.fromJSONArray(json.features)
    });

    return result;
  }

  static fromJSONArray(json?: Array<any>): StoryDefinitionConfiguration[] {
    return json ? json.map(StoryDefinitionConfiguration.fromJSON) : [];
  }
}

export class EntityStepSelection {

  constructor(public value: string,
              public entityRole: string,
              public entityType: string
  ) {
  }

  static fromJSON(json: any): EntityStepSelection {
    if (!json) {
      return null;
    }
    const value = Object.create(EntityStepSelection.prototype);
    return Object.assign(value, json, {});
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
              public entityType: string,
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
    return new MandatoryEntity(this.role, this.entityType, this.intent.clone(), this.answers.slice(0).map(a => a.clone()), this.currentType, this.category);
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
  public targetIntentDefinition: Intent;
  public new: boolean;
  public newUserSentence: string = "";

  static generateEntitySteps(
    intent: IntentName,
    category: string,
    entityType: EntityType,
    entityRole: string,
    dictionary: Dictionary,
    level: number): StoryStep[] {
    return dictionary.values.map(v => {
      const s = new StoryStep(
        v.value + "_" + level,
        intent,
        new IntentName(""),
        [new SimpleAnswerConfiguration([])],
        AnswerConfigurationType.simple,
        category,
        null,
        [],
        level,
        new EntityStepSelection(v.value, entityRole, entityType.name)
      );
      s.new = true;
      s.newUserSentence = v.value;
      return s;
    });
  }

  static filterNew(steps: StoryStep[]): StoryStep[] {
    steps.forEach(s => s.children = StoryStep.filterNew(s.children));
    return steps.filter(s => !s.new);
  }

  static findOutcomingIntent(intents: Set<string>, steps: StoryStep[]) {
    steps.forEach(s => {
      if (s.intent.name.length !== 0) {
        if (s.targetIntent.name.length !== 0) {
          intents.add(s.targetIntent.name);
        } else if (!s.currentAnswer() || s.currentAnswer().isEmpty()) {
          intents.add(s.intent.name)
        }
      }
      StoryStep.findOutcomingIntent(intents, s.children)
    });
  }

  constructor(public name: string,
              public intent: IntentName,
              public targetIntent: IntentName,
              answers: AnswerConfiguration[],
              currentType: AnswerConfigurationType,
              category: string,
              public userSentence: I18nLabel,
              public children: StoryStep[],
              public level: number,
              public entity?: EntityStepSelection) {
    super(currentType, answers, category)
  }

  containerId(): string {
    return this.name;
  }

  save(bot: BotService): Observable<AnswerContainer> {
    return of(this);
  }

  allowNoAnwser(): boolean {
    return true;
  }

  clone(): StoryStep {
    return new StoryStep(
      this.name,
      this.intent.clone(),
      this.targetIntent.clone(),
      this.answers.slice(0).map(a => a.clone()),
      this.currentType,
      this.category,
      this.userSentence.clone(),
      this.children.map(c => c.clone()),
      this.level
    );
  }

  static fromJSON(json: any): StoryStep {
    const value = Object.create(StoryStep.prototype);
    const result = Object.assign(value, json, {
      intent: IntentName.fromJSON(json.intent),
      targetIntent: IntentName.fromJSON(json.targetIntent),
      currentType: AnswerConfigurationType[json.currentType],
      answers: AnswerConfiguration.fromJSONArray(json.answers),
      children: StoryStep.fromJSONArray(json.children),
      userSentence: I18nLabel.fromJSON(json.userSentence),
      entity: EntityStepSelection.fromJSON(json.entity)
    });

    return result;
  }

  static fromJSONArray(json?: Array<any>): StoryStep[] {
    return json ? json.map(StoryStep.fromJSON) : [];
  }
}

export class IntentName {

  private intentLabel: string;

  constructor(public name: string) {
  }

  getIntentLabel(state: StateService): string {
    if (!this.intentLabel) {
      this.intentLabel = state.intentLabelByName(this.name);
    }
    return this.intentLabel;
  }

  clone(): IntentName {
    return new IntentName(this.name);
  }

  static fromJSON(json: any): IntentName {
    const value = Object.create(IntentName.prototype);
    const result = Object.assign(value, json, {
      name: json && json.name ? json.name : ""
    });
    return result;
  }

  static fromJSONArray(json?: Array<any>): IntentName[] {
    return json ? json.map(IntentName.fromJSON) : [];
  }
}

export abstract class AnswerConfiguration {

  public allowNoAnswer: boolean = false;

  protected constructor(public answerType: AnswerConfigurationType) {
  }

  abstract simpleTextView(wide: boolean): string

  invalidMessage(): string {
    return null;
  }

  abstract isEmpty(): boolean

  checkAfterReset(bot: BotService) {
  }

  abstract clone(): AnswerConfiguration

  abstract duplicate(bot: BotService): AnswerConfiguration

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
      case AnswerConfigurationType.builtin :
        return BuiltinAnswerConfiguration.fromJSON(json);
      default:
        throw "unknown type : " + json.answerType
    }
  }

  static fromJSONArray(json?: Array<any>): AnswerConfiguration[] {
    return json ? json.map(AnswerConfiguration.fromJSON) : [];
  }
}

export class SimpleAnswerConfiguration extends AnswerConfiguration {

  constructor(public answers: SimpleAnswer[]) {
    super(AnswerConfigurationType.simple)
  }

  isEmpty(): boolean {
    return this.answers.length === 0;
  }

  simpleTextView(wide: boolean): string {
    const r = this.answers && this.answers.length > 0 ? this.answers[0].label.defaultLocalizedLabel().label : "[no text yet]";
    const limit = wide ? 80 : 25;
    return r.substring(0, Math.min(r.length, limit)) + (r.length > limit || this.answers.length > 1 ? "..." : "");
  }

  invalidMessage(): string {
    if (!this.allowNoAnswer && this.answers.length === 0) {
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

  duplicate(bot: BotService): AnswerConfiguration {
    return new SimpleAnswerConfiguration(
      this.answers.map(answerConfiguration => {
        const clonedAnswerConfiguration = answerConfiguration.clone();

        // Default message
        bot.duplicateLabel(clonedAnswerConfiguration.label, i18n => {
          clonedAnswerConfiguration.label = i18n;
        });

        // Media Message
        const clonedMediaMessage = clonedAnswerConfiguration.mediaMessage;
        if (clonedMediaMessage instanceof MediaCard) {
          bot.duplicateLabel(clonedMediaMessage.title, i18n => {
            clonedMediaMessage.title = i18n;
          });
          bot.duplicateLabel(clonedMediaMessage.subTitle, i18n => {
            clonedMediaMessage.subTitle = i18n;
          });
          clonedMediaMessage.actions.forEach(action => {
            bot.duplicateLabel(action.title, i18n => {
              action.title = i18n;
            });
          });
        }

        return clonedAnswerConfiguration;
      })
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
              public delay: number = -1,
              public mediaMessage?: Media) {
  }

  clone(): SimpleAnswer {
    return new SimpleAnswer(this.label.clone(), this.delay, this.mediaMessage ? this.mediaMessage.clone() : null);
  }

  static fromJSON(json: any): SimpleAnswer {
    const value = Object.create(SimpleAnswer.prototype);
    const result = Object.assign(value, json, {
      label: I18nLabel.fromJSON(json.label),
      mediaMessage: json.mediaMessage ? Media.fromJSON(json.mediaMessage) : null
    });
    return result;
  }

  static fromJSONArray(json?: Array<any>): SimpleAnswer[] {
    return json ? json.map(SimpleAnswer.fromJSON) : [];
  }
}

export enum MediaType {
  action,
  card
}

export class Media {

  constructor(public type: MediaType) {
  }

  clone(): Media {
    return this;
  }

  static fromJSON(json: any): Media {
    if (!json) {
      return null;
    }

    const mediaType = MediaType[json.type as string];
    switch (mediaType) {
      case MediaType.action :
        return MediaAction.fromJSON(json);
      case MediaType.card :
        return MediaCard.fromJSON(json);
      default:
        throw "unknown type : " + json.type
    }
  }
}

export class MediaCard extends Media {

  public titleLabel: string;
  public subTitleLabel: string;

  constructor(
    public actions: MediaAction[],
    public title?: I18nLabel,
    public subTitle?: I18nLabel,
    public file?: MediaFile
  ) {
    super(MediaType.card);
  }

  clone(): Media {
    return new MediaCard(
      this.actions.map(a => a.clone()),
      this.title ? this.title.clone() : null,
      this.subTitle ? this.subTitle.clone() : null,
      this.file
    );
  }

  static fromJSON(json: any): MediaCard {

    const value = Object.create(MediaCard.prototype);
    const result = Object.assign(value, json, {
      title: json.title ? I18nLabel.fromJSON(json.title) : null,
      subTitle: json.subTitle ? I18nLabel.fromJSON(json.subTitle) : null,
      actions: MediaAction.fromJSONArray(json.actions),
      file: MediaFile.fromJSON(json.file),
      type: MediaType.card
    });
    return result;
  }

}

export class MediaAction extends Media {

  public titleLabel: string = "";

  constructor(
    public title: I18nLabel,
    public url: string
  ) {
    super(MediaType.action);
  }

  clone(): MediaAction {
    return new MediaAction(
      this.title ? this.title.clone() : null,
      this.url
    );
  }

  static fromJSON(json: any): MediaAction {
    const value = Object.create(MediaAction.prototype);
    const result = Object.assign(value, json, {
      title: I18nLabel.fromJSON(json.title),
      type: MediaType.action
    });
    return result;
  }

  static fromJSONArray(json?: Array<any>): MediaAction[] {
    return json ? json.map(MediaAction.fromJSON) : [];
  }
}

export class MediaFile {

  constructor(
    public suffix: string,
    public name: string,
    public id: string,
    public type: AttachmentType
  ) {
  }

  isImage(): boolean {
    return this.type === AttachmentType.image;
  }

  static fromJSON(json: any): MediaFile {
    if (!json) {
      return null;
    }
    const value = Object.create(MediaFile.prototype);
    const result = Object.assign(value, json, {
      type: AttachmentType[json.type]
    });
    return result;
  }

}

export class ScriptAnswerConfiguration extends AnswerConfiguration {

  constructor(
    public scriptVersions: ScriptAnswerVersionedConfiguration[],
    public current: ScriptAnswerVersionedConfiguration) {
    super(AnswerConfigurationType.script)
  }

  invalidMessage(): string {
    if (this.current.script.trim().length === 0) {
      return "Please set a non empty script";
    } else {
      return null;
    }
  }

  simpleTextView(wide: boolean): string {
    return "[Script]";
  }

  isEmpty(): boolean {
    return false;
  }

  clone(): AnswerConfiguration {
    return new ScriptAnswerConfiguration(
      this.scriptVersions.map(s => new ScriptAnswerVersionedConfiguration(s.script, s.date)),
      new ScriptAnswerVersionedConfiguration(this.current.script, this.current.date)
    )
  }

  duplicate(bot: BotService): AnswerConfiguration {
    return this.clone()
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

export class BuiltinAnswerConfiguration extends AnswerConfiguration {

  constructor(
    public storyHandlerClassName?: string) {
    super(AnswerConfigurationType.builtin)
  }

  invalidMessage(): string {
    return null;
  }

  simpleTextView(wide: boolean): string {
    return "[Built in]";
  }

  isEmpty(): boolean {
    return false;
  }

  clone(): AnswerConfiguration {
    return new BuiltinAnswerConfiguration(
      this.storyHandlerClassName
    );
  }

  duplicate(bot: BotService): AnswerConfiguration {
    return this.clone()
  }

  static fromJSON(json: any): BuiltinAnswerConfiguration {
    const value = Object.create(BuiltinAnswerConfiguration.prototype);
    const result = Object.assign(value, json, {});
    return result;
  }
}

export class StoryFeature {

  public story: StoryDefinitionConfiguration;
  public conf: BotApplicationConfiguration;
  public switchToStory: StoryDefinitionConfiguration;

  constructor(public botApplicationConfigurationId: string,
              public enabled: boolean,
              public switchToStoryId: string
  ) {
  }

  static fromJSON(json: any): StoryFeature {
    const value = Object.create(StoryFeature.prototype);
    const result = Object.assign(value, json, {});

    return result;
  }

  static fromJSONArray(json?: Array<any>): StoryFeature[] {
    return json ? json.map(StoryFeature.fromJSON) : [];
  }
}
