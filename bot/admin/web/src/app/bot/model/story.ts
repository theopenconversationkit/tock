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

import { PaginatedQuery } from '../../model/commons';
import { Dictionary, EntityDefinition, EntityType, Intent, Sentence } from '../../model/nlp';
import { I18nLabel } from './i18n';
import { BotService } from '../bot-service';
import { Observable, of } from 'rxjs';
import { AttachmentType, BotApplicationConfiguration } from '../../core/model/configuration';
import { StateService } from '../../core-nlp/state.service';

export class CreateStoryRequest {
  constructor(public story: StoryDefinitionConfiguration, public language: string, public firstSentences: string[]) {}
}

export class StorySearchQuery extends PaginatedQuery {
  constructor(
    public override namespace: string,
    public override applicationName: string,
    public override language: string,
    public override start: number,
    public override size: number,
    public category?: string,
    public textSearch?: string,
    public onlyConfiguredStory?: boolean
  ) {
    super(namespace, applicationName, language, start, size);
  }
}

export class Story {
  constructor(public storyDefinition: StoryDefinitionConfiguration, public firstSentences: Sentence[]) {}

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
  protected constructor(public currentType: AnswerConfigurationType, public answers: AnswerConfiguration[], public category: string) {}

  abstract containerId(): string;

  abstract save(bot: BotService): Observable<AnswerContainer>;

  allowNoAnwser(): boolean {
    return false;
  }

  isConfiguredAnswer(): boolean {
    return this.isSimpleAnswer() || this.isMessageAnswer() || this.isScriptAnswer();
  }

  isSimpleAnswer(): boolean {
    return this.currentType === AnswerConfigurationType.simple;
  }

  isSimpleFaqAnswer(): boolean {
    return this.currentType === AnswerConfigurationType.simple && this.category === 'faq';
  }

  isSimpleNonFaqAnswer(): boolean {
    return this.currentType === AnswerConfigurationType.simple && this.category != 'faq';
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
    return this.answers.find((c) => c.answerType === type);
  }

  simpleTextView(wide: boolean): string {
    const current = this.currentAnswer();
    if (current) {
      return current.simpleTextView(wide);
    }
    return '';
  }

  addNewAnswerType(newAnswer: AnswerConfiguration) {
    this.answers.push(newAnswer);
  }

  changeCurrentType(value: AnswerConfigurationType) {
    this.currentType = value;
  }
}

export class StoryDefinitionConfigurationSummary {
  constructor(
    public storyId: string,
    public botId: string,
    public intent: IntentName,
    public currentType: AnswerConfigurationType,
    public category: string = 'default',
    public name: string = storyId,
    public _id: string,
    public description: string = '',
    public lastEdited: Date,
    public answers: AnswerConfiguration
  ) {}

  static fromJSON(json: any): StoryDefinitionConfigurationSummary {
    const value = Object.create(StoryDefinitionConfigurationSummary.prototype);
    return Object.assign(value, json, {
      intent: IntentName.fromJSON(json.intent),
      currentType: AnswerConfigurationType[json.currentType]
    });
  }

  static fromJSONArray(json?: Array<any>): StoryDefinitionConfigurationSummary[] {
    return json ? json.map(StoryDefinitionConfigurationSummary.fromJSON) : [];
  }

  isSimpleAnswer(): boolean {
    return this.currentType === AnswerConfigurationType.simple;
  }

  isSimpleFaqAnswer(): boolean {
    return this.currentType === AnswerConfigurationType.simple && this.category === 'faq';
  }

  isSimpleNonFaqAnswer(): boolean {
    return this.currentType === AnswerConfigurationType.simple && this.category != 'faq';
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
}

export class StoryDefinitionConfiguration extends AnswerContainer {
  constructor(
    public storyId: string,
    public botId: string,
    public intent: IntentName,
    public override currentType: AnswerConfigurationType,
    public namespace: string,
    answers: AnswerConfiguration[] = [],
    category: string = 'default',
    public name: string = storyId,
    public userSentence: string = '',
    public userSentenceLocale: string,
    public features: StoryFeature[],
    public configurationName?: string,
    public _id?: string,
    public version: number = 0,
    public mandatoryEntities: MandatoryEntity[] = [],
    public steps: StoryStep[] = [],
    public description: string = '',
    public tags: string[] = [],
    public configuredAnswers: BotConfiguredAnswer[] = [],
    public configuredSteps: BotConfiguredSteps[] = [],
    public metricStory: boolean = false
  ) {
    super(currentType, answers, category);
  }

  public hideDetails = false;
  public selected = true;

  static fromJSON(json: any): StoryDefinitionConfiguration {
    const value = Object.create(StoryDefinitionConfiguration.prototype);
    return Object.assign(value, json, {
      intent: IntentName.fromJSON(json.intent),
      currentType: AnswerConfigurationType[json.currentType],
      answers: AnswerConfiguration.fromJSONArray(json.answers),
      mandatoryEntities: MandatoryEntity.fromJSONArray(json.mandatoryEntities),
      steps: StoryStep.fromJSONArray(json.steps),
      features: StoryFeature.fromJSONArray(json.features),
      configuredAnswers: BotConfiguredAnswer.fromJSONArray(json.configuredAnswers),
      configuredSteps: BotConfiguredSteps.fromJSONArray(json.configuredSteps)
    });
  }

  static fromJSONArray(json?: Array<any>): StoryDefinitionConfiguration[] {
    return json ? json.map(StoryDefinitionConfiguration.fromJSON) : [];
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
      this.features.map((f) => new StoryFeature(f.botApplicationConfigurationId, f.enabled, f.switchToStoryId, f.endWithStoryId)),
      this.configurationName,
      this._id,
      this.version,
      this.mandatoryEntities,
      this.steps,
      this.description,
      this.tags,
      this.configuredAnswers,
      this.configuredSteps,
      this.metricStory
    );
  }

  save(bot: BotService): Observable<AnswerContainer> {
    return bot.saveStory(this);
  }

  isDisabled(configurationId: string): boolean {
    return !this.features || this.features.length < 1
      ? false
      : this.features.filter(
          (f) =>
            !f.enabled && !f.switchToStoryId && (!f.botApplicationConfigurationId || f.botApplicationConfigurationId === configurationId)
        ).length > 0;
  }
}

export class EntityStepSelection {
  constructor(public value: string, public entityRole: string, public entityType: string) {}

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
  constructor(
    public role: string,
    public entityType: string,
    public intent: IntentName,
    answers: AnswerConfiguration[],
    currentType: AnswerConfigurationType,
    category: string
  ) {
    super(currentType, answers, category);
  }

  public entity: EntityDefinition;
  public intentDefinition: Intent;

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

  containerId(): string {
    return this.role;
  }

  save(bot: BotService): Observable<AnswerContainer> {
    return of(this);
  }

  clone(): MandatoryEntity {
    return new MandatoryEntity(
      this.role,
      this.entityType,
      this.intent.clone(),
      this.answers.slice(0).map((a) => a.clone()),
      this.currentType,
      this.category
    );
  }
}

export interface StoryStepMetric {
  indicatorName: string;
  indicatorValueName: string;
}

export class StoryStep extends AnswerContainer {
  constructor(
    public name: string,
    public intent: IntentName,
    public targetIntent: IntentName,
    answers: AnswerConfiguration[],
    currentType: AnswerConfigurationType,
    category: string,
    public userSentence: I18nLabel,
    public children: StoryStep[],
    public level: number,
    public entity?: EntityStepSelection,
    public metrics?: StoryStepMetric[]
  ) {
    super(currentType, answers, category);
  }

  public intentDefinition: Intent;
  public targetIntentDefinition: Intent;
  public new: boolean;
  public newUserSentence = '';

  static generateEntitySteps(
    intent: IntentName,
    category: string,
    entityType: EntityType,
    entityRole: string,
    dictionary: Dictionary,
    level: number
  ): StoryStep[] {
    return dictionary.values.map((v) => {
      const s = new StoryStep(
        v.value + '_' + level,
        intent,
        new IntentName(''),
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
    steps.forEach((s) => (s.children = StoryStep.filterNew(s.children)));
    return steps.filter((s) => !s.new);
  }

  static findOutcomingIntent(intents: Set<string>, steps: StoryStep[]) {
    steps.forEach((s) => {
      if (s.intent.name.length !== 0) {
        if (s.targetIntent.name.length !== 0) {
          intents.add(s.targetIntent.name);
        } else if (!s.currentAnswer() || s.currentAnswer().isEmpty()) {
          intents.add(s.intent.name);
        }
      }
      StoryStep.findOutcomingIntent(intents, s.children);
    });
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

  containerId(): string {
    return this.name;
  }

  save(bot: BotService): Observable<AnswerContainer> {
    return of(this);
  }

  override allowNoAnwser(): boolean {
    return true;
  }

  clone(): StoryStep {
    return new StoryStep(
      this.name,
      this.intent.clone(),
      this.targetIntent.clone(),
      this.answers.slice(0).map((a) => a.clone()),
      this.currentType,
      this.category,
      this.userSentence.clone(),
      this.children.map((c) => c.clone()),
      this.level
    );
  }

  duplicate(bot: BotService) {
    const storyStep = new StoryStep(
      this.name,
      this.intent.clone(),
      this.targetIntent.clone(),
      this.answers.slice(0).map((a) => a.duplicate(bot)),
      this.currentType,
      this.category,
      this.userSentence.clone(),
      this.children.map((c) => c.duplicate(bot)),
      this.level
    );
    bot.duplicateLabel(storyStep.userSentence, (i18n) => {
      storyStep.userSentence = i18n;
    });
    return storyStep;
  }
}

export class IntentName {
  constructor(public name: string) {}

  private intentLabel: string;

  static fromJSON(json: any): IntentName {
    const value = Object.create(IntentName.prototype);
    const result = Object.assign(value, json, {
      name: json && json.name ? json.name : ''
    });
    return result;
  }

  static fromJSONArray(json?: Array<any>): IntentName[] {
    return json ? json.map(IntentName.fromJSON) : [];
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
}

export abstract class AnswerConfiguration {
  protected constructor(public answerType: AnswerConfigurationType) {}

  public allowNoAnswer = false;

  static fromJSON(json: any): AnswerConfiguration {
    if (!json) {
      return null;
    }

    const answerType = AnswerConfigurationType[json.answerType as string];
    switch (answerType) {
      case AnswerConfigurationType.simple:
        return SimpleAnswerConfiguration.fromJSON(json);
      case AnswerConfigurationType.script:
        return ScriptAnswerConfiguration.fromJSON(json);
      case AnswerConfigurationType.builtin:
        return BuiltinAnswerConfiguration.fromJSON(json);
      default:
        throw new Error('unknown type : ' + json.answerType);
    }
  }

  static fromJSONArray(json?: Array<any>): AnswerConfiguration[] {
    return json ? json.map(AnswerConfiguration.fromJSON) : [];
  }

  abstract simpleTextView(wide: boolean): string;

  invalidMessage(): string {
    return null;
  }

  abstract isEmpty(): boolean;

  checkAfterReset(bot: BotService) {}

  abstract clone(): AnswerConfiguration;

  abstract duplicate(bot: BotService): AnswerConfiguration;
}

export class SimpleAnswerConfiguration extends AnswerConfiguration {
  constructor(public answers: SimpleAnswer[]) {
    super(AnswerConfigurationType.simple);
  }

  static override fromJSON(json: any): SimpleAnswerConfiguration {
    const value = Object.create(SimpleAnswerConfiguration.prototype);
    const result = Object.assign(value, json, {
      answers: SimpleAnswer.fromJSONArray(json.answers),
      answerType: AnswerConfigurationType.simple
    });
    return result;
  }

  isEmpty(): boolean {
    return this.answers.length === 0;
  }

  simpleTextView(wide: boolean): string {
    const r = this.answers && this.answers.length > 0 ? this.answers[0].label.defaultLocalizedLabel().label : '[no text yet]';
    const limit = wide ? 80 : 25;
    return r.substring(0, Math.min(r.length, limit)) + (r.length > limit || this.answers.length > 1 ? '...' : '');
  }

  override invalidMessage(): string {
    if (!this.allowNoAnswer && this.answers.length === 0) {
      return 'Please set at least one sentence';
    } else {
      return null;
    }
  }

  clone(): AnswerConfiguration {
    return new SimpleAnswerConfiguration(this.answers.map((a) => a.clone()));
  }

  duplicate(bot: BotService): AnswerConfiguration {
    return new SimpleAnswerConfiguration(
      this.answers.map((answerConfiguration) => {
        const clonedAnswerConfiguration = answerConfiguration.clone();

        // Default message
        bot.duplicateLabel(clonedAnswerConfiguration.label, (i18n) => {
          clonedAnswerConfiguration.label = i18n;
        });

        // Media Message
        const clonedMediaMessage = clonedAnswerConfiguration.mediaMessage;
        if (clonedMediaMessage instanceof MediaCard) {
          bot.duplicateLabel(clonedMediaMessage.title, (i18n) => {
            clonedMediaMessage.title = i18n;
          });
          bot.duplicateLabel(clonedMediaMessage.subTitle, (i18n) => {
            clonedMediaMessage.subTitle = i18n;
          });
          bot.duplicateLabel(clonedMediaMessage.file?.description, (i18n) => {
            clonedMediaMessage.file.description = i18n;
          });
          clonedMediaMessage.actions.forEach((action) => {
            bot.duplicateLabel(action.title, (i18n) => {
              action.title = i18n;
            });
          });
        }

        return clonedAnswerConfiguration;
      })
    );
  }

  override checkAfterReset(bot: BotService) {
    super.checkAfterReset(bot);
    // save again label if useful
    let count = 0;
    this.answers.forEach((a) => bot.saveI18nLabel(a.label).subscribe((r) => count++));
  }
}

export class SimpleAnswer {
  constructor(public label: I18nLabel, public delay: number = -1, public mediaMessage?: Media) {}

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

  clone(): SimpleAnswer {
    return new SimpleAnswer(this.label.clone(), this.delay, this.mediaMessage ? this.mediaMessage.clone() : null);
  }
}

export enum MediaType {
  action,
  card
}

export class Media {
  constructor(public type: MediaType) {}

  static fromJSON(json: any): Media {
    if (!json) {
      return null;
    }

    const mediaType = MediaType[json.type as string];
    switch (mediaType) {
      case MediaType.action:
        return MediaAction.fromJSON(json);
      case MediaType.card:
        return MediaCard.fromJSON(json);
      default:
        throw new Error('unknown type : ' + json.type);
    }
  }

  clone(): Media {
    return this;
  }
}

export class MediaCard extends Media {
  constructor(
    public actions: MediaAction[],
    public title?: I18nLabel,
    public subTitle?: I18nLabel,
    public file?: MediaFile,
    public fillCarousel?: boolean
  ) {
    super(MediaType.card);
  }

  public titleLabel: string;
  public subTitleLabel: string;

  static override fromJSON(json: any): MediaCard {
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

  override clone(): Media {
    return new MediaCard(
      this.actions.map((a) => a.clone()),
      this.title ? this.title.clone() : null,
      this.subTitle ? this.subTitle.clone() : null,
      this.file,
      this.fillCarousel
    );
  }
}

export class MediaAction extends Media {
  constructor(public title: I18nLabel, public url: string) {
    super(MediaType.action);
  }

  public titleLabel = '';
  public readonly internalId = Math.random();

  static override fromJSON(json: any): MediaAction {
    const value = Object.create(MediaAction.prototype);
    const result = Object.assign(value, json, {
      title: I18nLabel.fromJSON(json.title),
      internalId: Math.random(),
      type: MediaType.action
    });
    return result;
  }

  static fromJSONArray(json?: Array<any>): MediaAction[] {
    return json ? json.map(MediaAction.fromJSON) : [];
  }

  override clone(): MediaAction {
    return new MediaAction(this.title ? this.title.clone() : null, this.url);
  }
}

export class MediaFile {
  constructor(
    public suffix: string,
    public name: string,
    public id: string,
    public type: AttachmentType,
    public externalUrl?: string,
    public description?: I18nLabel
  ) {}

  public descriptionLabel: string;

  url(baseUrl: string): String {
    return this.externalUrl ? this.externalUrl : `${baseUrl}/file/${this.id}.${this.suffix}`;
  }

  static attachmentType(suffix: string): AttachmentType {
    switch (suffix) {
      case 'png':
      case 'jpg':
      case 'jpeg':
      case 'svg':
      case 'gif':
        return AttachmentType.image;
      case 'ogg':
      case 'mp3':
      case 'oga':
        return AttachmentType.audio;
      case 'ogv':
      case 'mp4':
        return AttachmentType.video;
      default:
        return AttachmentType.file;
    }
  }

  static fromJSON(json: any): MediaFile {
    if (!json) {
      return null;
    }
    const value = Object.create(MediaFile.prototype);
    const result = Object.assign(value, json, {
      type: AttachmentType[json.type],
      description: json.description ? I18nLabel.fromJSON(json.description) : null
    });
    return result;
  }

  isImage(): boolean {
    return this.type === AttachmentType.image;
  }
}

export class ScriptAnswerConfiguration extends AnswerConfiguration {
  constructor(public scriptVersions: ScriptAnswerVersionedConfiguration[], public current: ScriptAnswerVersionedConfiguration) {
    super(AnswerConfigurationType.script);
  }

  static override fromJSON(json: any): ScriptAnswerConfiguration {
    const value = Object.create(ScriptAnswerConfiguration.prototype);
    const result = Object.assign(value, json, {
      scriptVersions: ScriptAnswerVersionedConfiguration.fromJSONArray(json.scriptVersions),
      current: ScriptAnswerVersionedConfiguration.fromJSON(json.current),
      answerType: AnswerConfigurationType.script
    });
    return result;
  }

  override invalidMessage(): string {
    if (this.current.script.trim().length === 0) {
      return 'Please set a non empty script';
    } else {
      return null;
    }
  }

  simpleTextView(wide: boolean): string {
    return '[Script]';
  }

  isEmpty(): boolean {
    return false;
  }

  clone(): AnswerConfiguration {
    return new ScriptAnswerConfiguration(
      this.scriptVersions.map((s) => new ScriptAnswerVersionedConfiguration(s.script, s.date)),
      new ScriptAnswerVersionedConfiguration(this.current.script, this.current.date)
    );
  }

  duplicate(bot: BotService): AnswerConfiguration {
    return this.clone();
  }
}

export class ScriptAnswerVersionedConfiguration {
  constructor(public script: string, public date?: Date) {}

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
  constructor(public storyHandlerClassName?: string) {
    super(AnswerConfigurationType.builtin);
  }

  static override fromJSON(json: any): BuiltinAnswerConfiguration {
    const value = Object.create(BuiltinAnswerConfiguration.prototype);
    const result = Object.assign(value, json, {});
    return result;
  }

  override invalidMessage(): string {
    return null;
  }

  simpleTextView(wide: boolean): string {
    return '[Built in]';
  }

  isEmpty(): boolean {
    return false;
  }

  clone(): AnswerConfiguration {
    return new BuiltinAnswerConfiguration(this.storyHandlerClassName);
  }

  duplicate(bot: BotService): AnswerConfiguration {
    return this.clone();
  }
}

export enum RuleType {
  Activation = 'Activation',
  Redirection = 'Redirection',
  Ending = 'Ending'
}

export function ruleTypeValues() {
  return [RuleType.Activation, RuleType.Redirection, RuleType.Ending];
}

export class StoryFeature {
  public story: StoryDefinitionConfiguration;
  public conf: BotApplicationConfiguration;
  public switchToStory: StoryDefinitionConfiguration;
  public endWithStory: StoryDefinitionConfiguration;

  constructor(
    public botApplicationConfigurationId: string,
    public enabled: boolean,
    public switchToStoryId: string,
    public endWithStoryId: string
  ) {}

  static fromJSON(json: any): StoryFeature {
    const value = Object.create(StoryFeature.prototype);
    return Object.assign(value, json, {});
  }

  static fromJSONArray(json?: Array<any>): StoryFeature[] {
    return json ? json.map(StoryFeature.fromJSON) : [];
  }

  getRuleType(): RuleType {
    if (this.switchToStory || this.switchToStoryId) {
      return RuleType.Redirection;
    }
    if (this.endWithStory || this.endWithStoryId) {
      return RuleType.Ending;
    }
    return RuleType.Activation;
  }
}

export class BotConfiguredAnswer {
  constructor(public botConfiguration: String, public currentType: AnswerConfigurationType, public answers: AnswerConfiguration[]) {}

  static fromJSONArray(json?: Array<any>): BotConfiguredAnswer[] {
    return json ? json.map(BotConfiguredAnswer.fromJSON) : [];
  }

  static fromJSON(json: any): BotConfiguredAnswer {
    if (!json) {
      return null;
    }

    const value = Object.create(BotConfiguredAnswer.prototype);
    return Object.assign(value, json, {
      currentType: AnswerConfigurationType[json.currentType],
      answers: AnswerConfiguration.fromJSONArray(json.answers)
    });
  }

  containedIn(story: StoryDefinitionConfiguration): CustomAnswerContainer {
    return new CustomAnswerContainer(this, story);
  }
}

export class CustomAnswerContainer extends AnswerContainer {
  constructor(public botConfigurationAnswer: BotConfiguredAnswer, public story: StoryDefinitionConfiguration) {
    super(botConfigurationAnswer.currentType, botConfigurationAnswer.answers, story.category);
  }

  containerId(): string {
    return this.story.storyId;
  }

  save(bot: BotService): Observable<AnswerContainer> {
    return of(this);
  }

  override changeCurrentType(value: AnswerConfigurationType) {
    super.changeCurrentType(value);
    this.botConfigurationAnswer.currentType = value;
  }
}

export class BotConfiguredSteps {
  constructor(public botConfiguration: String, public steps: StoryStep[]) {}

  static fromJSONArray(json?: Array<any>): BotConfiguredSteps[] {
    return json ? json.map(BotConfiguredSteps.fromJSON) : [];
  }

  static fromJSON(json: any): BotConfiguredSteps {
    const value = Object.create(BotConfiguredSteps.prototype);
    return Object.assign(value, json, {
      steps: StoryStep.fromJSONArray(json.steps)
    });
  }
}
