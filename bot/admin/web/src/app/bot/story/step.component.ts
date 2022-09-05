/*
 * Copyright (C) 2017/2021 e-voyageurs technologies
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

import { Component, EventEmitter, Input, OnInit, Output } from '@angular/core';
import { EntityStepSelection, IntentName, StoryStep } from '../model/story';
import { MatDialog } from '@angular/material/dialog';
import { Intent, IntentsCategory, ParseQuery } from '../../model/nlp';
import { StateService } from '../../core-nlp/state.service';
import { IntentDialogComponent } from '../../sentence-analysis/intent-dialog/intent-dialog.component';
import { NlpService } from '../../nlp-tabs/nlp.service';
import { DialogService } from '../../core-nlp/dialog.service';
import { CreateI18nLabelRequest, I18nLocalizedLabel } from '../model/i18n';
import { BotService } from '../bot-service';
import { SelectEntityDialogComponent } from './select-entity-dialog.component';
import { defaultUserInterfaceType } from '../../core/model/configuration';

@Component({
  selector: 'tock-step',
  templateUrl: './step.component.html',
  styleUrls: ['./step.component.css']
})
export class StepComponent implements OnInit {
  @Input()
  step: StoryStep;

  @Input()
  defaultCategory: string = 'build';

  @Output()
  delete = new EventEmitter<StoryStep>();

  @Output()
  child = new EventEmitter<StoryStep>();

  @Output()
  duplicate = new EventEmitter<StoryStep>();

  @Output()
  rebuildTree = new EventEmitter<StoryStep>();

  @Input()
  readonly: boolean = false;

  @Input()
  canDownward: boolean = false;

  @Input()
  canUpward: boolean = false;

  @Output()
  downward = new EventEmitter<StoryStep>();

  @Output()
  upward = new EventEmitter<StoryStep>();

  intentCategories: IntentsCategory[] = [];
  currentIntentCategories: IntentsCategory[] = [];

  currentEditedIntent: string;

  displayTargetIntent: boolean = false;

  constructor(
    public state: StateService,
    private dialog: DialogService,
    private matDialog: MatDialog,
    private nlp: NlpService,
    private bot: BotService
  ) {}

  ngOnInit() {
    this.state.currentIntentsCategories.subscribe((c) => {
      this.intentCategories = c;
      this.currentIntentCategories = c;
    });
    this.displayTargetIntent = this.step.targetIntent.name.trim().length !== 0;
  }

  onIntentChange(step: StoryStep, name: string) {
    if (this.currentEditedIntent !== name) {
      this.currentEditedIntent = name;
      const intent = name.trim().toLowerCase();
      let target = this.intentCategories
        .map(
          (c) =>
            new IntentsCategory(
              c.category,
              c.intents.filter((i) => i.intentLabel().toLowerCase().startsWith(intent))
            )
        )
        .filter((c) => c.intents.length !== 0);

      this.currentIntentCategories = target;
    }
  }

  validateIntent(step: StoryStep, targetIntent?: boolean) {
    setTimeout((_) => {
      const intentName = (targetIntent ? step.targetIntent : step.intent).name.trim();
      const intentDef = targetIntent ? step.targetIntentDefinition : step.intentDefinition;
      if (intentName.length !== 0 && (!intentDef || intentDef.name !== intentName)) {
        let intent = this.state.findIntentByName(intentName);
        if (intent) {
          if (targetIntent) {
            step.targetIntentDefinition = intent;
          } else {
            step.intentDefinition = intent;
          }
          if (!step.name || step.name.trim().length === 0) {
            step.name = intentName + '_' + step.level;
          }
          this.checkStep();
        } else {
          let dialogRef = this.dialog.openDialog(IntentDialogComponent, {
            context: {
              create: true,
              category: this.defaultCategory,
              name: intentName,
              label: intentName
            }
          });
          dialogRef.onClose.subscribe((result) => {
            if (result.name) {
              const newIntent = new Intent(
                result.name,
                this.state.currentApplication.namespace,
                [],
                [this.state.currentApplication._id],
                [],
                [],
                result.label,
                result.description,
                result.category
              );
              if (targetIntent) {
                step.targetIntentDefinition = newIntent;
                step.targetIntent.name = result.name;
              } else {
                step.intentDefinition = newIntent;
                step.intent.name = result.name;
                step.name = result.name + '_' + step.level;
              }
            } else {
              if (targetIntent) {
                step.targetIntent.name = step.targetIntentDefinition
                  ? step.targetIntentDefinition.name
                  : '';
              } else {
                step.intent.name = step.intentDefinition ? step.intentDefinition.name : '';
              }
            }
            this.checkStep();
          });
        }
      } else {
        this.checkStep();
      }
    }, 200);
  }

  removeStep() {
    this.delete.emit(this.step);
  }

  duplicateStep() {
    this.duplicate.emit(this.step);
  }

  checkStep() {
    if (this.step.new) {
      let invalidMessage = this.step.currentAnswer().invalidMessage();
      if (invalidMessage) {
        this.dialog.notify(`Error: ${invalidMessage}`);
        return;
      } else if (
        this.step.newUserSentence.trim().length === 0 ||
        this.step.intent.name.trim().length === 0
      ) {
        return;
      } else {
        this.save(this.step);
      }
    }
  }

  private save(step: StoryStep) {
    this.bot
      .createI18nLabel(
        new CreateI18nLabelRequest(
          this.defaultCategory,
          step.newUserSentence.trim(),
          this.state.currentLocale
        )
      )
      .subscribe((i18n) => {
        step.userSentence = i18n;
        step.new = false;
      });
  }

  addChild() {
    this.child.emit(this.step);
  }

  focusTargetIntent(element) {
    this.displayTargetIntent = true;
    setTimeout((_) => element.focus(), 200);
  }

  setEntity() {
    const e = this.step.entity;
    let dialogRef = this.dialog.open(this.matDialog, SelectEntityDialogComponent, {
      data: {
        selectedEntity: e ? e.entityType : null,
        role: e ? e.entityRole : null,
        entityValue: e ? e.value : null
      }
    });

    dialogRef.afterClosed().subscribe((result) => {
      if (result?.entity) {
        if (!result.role) {
          this.step.entity = null;
        } else {
          this.step.entity = new EntityStepSelection(result.value, result.role, result.entity.name);
        }
      }
    });
  }

  userSentenceChange(changedSentence: string) {
    const step = this.step;
    if (changedSentence.trim().length !== 0) {
      if (!step.new) {
        this.updateStepI18nLabel(step, changedSentence);
        this.bot.saveI18nLabel(step.userSentence).subscribe((_) => {});
      }
      if (step.intent.name.length === 0 && !step.entity) {
        const app = this.state.currentApplication;
        const language = this.state.currentLocale;
        this.nlp
          .parse(new ParseQuery(app.namespace, app.name, language, changedSentence, true))
          .subscribe((r) => {
            if (r.classification.intentId) {
              const intent = this.state.findIntentById(r.classification.intentId);
              if (intent) {
                step.intentDefinition = intent;
                step.intent = new IntentName(intent.name);
                this.onIntentChange(step, intent.name);
                this.validateIntent(step, false);
              }
            }
          });
      } else {
        this.checkStep();
      }
    }
  }

  private updateStepI18nLabel(step: StoryStep, changedSentence: string) {
    step.userSentence.defaultLocale = this.state.currentLocale;
    let currentLocaleI18nSentence = step.userSentence.i18n.find(
      (i18nSentence) => i18nSentence.locale === this.state.currentLocale
    );
    if (!currentLocaleI18nSentence) {
      step.userSentence.i18n.push(
        new I18nLocalizedLabel(
          this.state.currentLocale,
          defaultUserInterfaceType,
          changedSentence,
          false,
          null,
          []
        )
      );
    } else {
      currentLocaleI18nSentence.label = changedSentence;
    }
  }

  generateChildren() {
    let dialogRef = this.dialog.open(this.matDialog, SelectEntityDialogComponent, {
      data: { generate: true }
    });
    dialogRef.afterClosed().subscribe((result) => {
      if (result.entity) {
        this.nlp.getDictionary(result.entity).subscribe((dictionary) => {
          //dictionary
          const newSteps = StoryStep.generateEntitySteps(
            result.intent,
            this.defaultCategory,
            result.entity,
            result.role,
            dictionary,
            this.step.level + 1
          );
          newSteps.forEach((s) => {
            this.step.children.push(s);
            this.save(s);
          });
          this.rebuildTree.emit(this.step);
        });
      }
    });
  }

  upwardStep() {
    this.upward.emit(this.step);
  }

  downwardStep() {
    this.downward.emit(this.step);
  }
}
