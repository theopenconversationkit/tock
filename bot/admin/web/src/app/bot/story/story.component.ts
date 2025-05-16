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

import { saveAs } from 'file-saver-es';
import { Component, EventEmitter, Input, OnChanges, Output, SimpleChange, SimpleChanges } from '@angular/core';
import { NbDialogService } from '@nebular/theme';
import { take } from 'rxjs';

import {
  AnswerConfigurationType,
  BotConfiguredAnswer,
  BotConfiguredSteps,
  CreateStoryRequest,
  IntentName,
  StoryDefinitionConfiguration,
  StoryStep
} from '../model/story';
import { BotService } from '../bot-service';
import { StateService } from '../../core-nlp/state.service';
import { StoryDialogComponent } from './story-dialog/story-dialog.component';
import { MandatoryEntitiesDialogComponent } from './mandatory-entities/mandatory-entities-dialog.component';
import { StoryNode } from '../../analytics/flow/node';
import { StepDialogComponent } from './action';
import { AnswerController } from './controller';
import { DialogService } from '../../core-nlp/dialog.service';
import { SelectBotConfigurationDialogComponent } from '../../configuration/bot-configurations/selection-dialog/select-bot-configuration-dialog.component';
import { ChoiceDialogComponent } from '../../shared/components';
import { getExportFileName } from '../../shared/utils';

@Component({
  selector: 'tock-story',
  templateUrl: './story.component.html',
  styleUrls: ['./story.component.scss']
})
export class StoryComponent implements OnChanges {
  @Input()
  story: StoryDefinitionConfiguration = null;

  @Input()
  storyNode: StoryNode = null;

  @Input()
  storyTag: string = '';

  @Input()
  fullDisplay = false;

  @Input()
  displaySteps = false;

  @Input()
  botId: string = null;

  @Input()
  displayCancel = false;

  @Output()
  delete = new EventEmitter<string>();

  @Input()
  submit = new AnswerController();

  @Input()
  displayCount = true;

  @Output()
  closeStory = new EventEmitter<boolean>();

  isSwitchingToManagedStory = false;

  constructor(
    private state: StateService,
    private bot: BotService,
    private dialog: DialogService,
    private nbDialogService: NbDialogService
  ) {}

  ngOnChanges(changes: SimpleChanges) {
    if (changes.storyNode) {
      const c = (changes.storyNode as SimpleChange).currentValue;
      if (!c) {
        this.story = null;
        this.storyTag = '';
      } else if (c.dynamic) {
        this.bot.findStory(this.storyNode.storyDefinitionId).subscribe((s) => {
          // explicit null value if no story found
          this.story = s.storyId ? s : null;
          this.storyTag = s.getFirstTag();
        });
      } else {
        this.initStoryByBotIdAndIntent();
      }
    }
  }

  private initStoryByBotIdAndIntent() {
    this.bot.findStoryByBotIdAndIntent(this.botId, this.storyNode.storyDefinitionId).subscribe((s) => {
      // explicit null value if no story found
      this.story = s.storyId ? s : null;
      this.storyTag = s.getFirstTag();
    });
  }

  deleteStory() {
    const action = 'remove';
    const dialogRef = this.nbDialogService.open(ChoiceDialogComponent, {
      context: {
        title: `Remove the story ${this.story.name}`,
        subtitle: 'Are you sure you want to delete this story?',
        actions: [
          { actionName: 'cancel', buttonStatus: 'basic', ghost: true },
          { actionName: action, buttonStatus: 'danger' }
        ],
        modalStatus: 'danger'
      }
    });
    dialogRef.onClose.subscribe((result) => {
      if (result === action) {
        this.bot.deleteStory(this.story._id).subscribe((_) => {
          this.delete.emit(this.story._id);
          this.story = null;
          this.storyTag = '';
          this.dialog.notify(`Story deleted`, 'Delete');
        });
      }
    });
  }

  editStory() {
    const dialogRef = this.nbDialogService.open(StoryDialogComponent, {
      context: {
        create: !this.story._id,
        name: this.story.storyId,
        label: this.story.name,
        tag: this.story.tags && this.story.tags.length > 0 ? this.story.tags[0] : undefined,
        intent: this.story.intent.name,
        description: this.story.description,
        category: this.story.category,
        // @ts-ignore todo fix this
        freezeIntent: this.storyNode,
        userSentence: this.story.userSentence,
        story: this.story
      }
    });
    dialogRef.onClose.pipe(take(1)).subscribe((result) => {
      if (result && result.name) {
        this.story.storyId = result.name;
        this.story.name = result.label;
        this.storyTag = result.tag;
        this.story.intent.name = result.intent;
        this.story.category = result.category;
        this.story.description = result.description;
        this.story.userSentence = result.userSentence;
        this.saveStory(this.story.selected);
      }
    });
  }

  saveStory(selectStoryAfterSave: boolean) {
    this.story.steps = StoryStep.filterNew(this.story.steps);
    if (!this.canBeMetricStory()) this.story.metricStory = false;
    this.story.tags = !this.storyTag || this.storyTag.length === 0 ? [] : [this.storyTag];
    if (this.story._id) {
      this.bot.saveStory(this.story).subscribe((s) => {
        this.story = s;
        this.story.selected = selectStoryAfterSave;
        // this.state.resetConfiguration();
        this.dialog.notify(`Story ${this.story.name} modified`, 'Update');
      });
    }
  }

  editEntities() {
    const dialogRef = this.nbDialogService.open(MandatoryEntitiesDialogComponent, {
      context: {
        entities: this.story.mandatoryEntities,
        defaultCategory: this.story.category
      }
    });
    dialogRef.onClose.pipe(take(1)).subscribe((result) => {
      if (result && result.entities) {
        this.story.mandatoryEntities = result.entities;
        this.saveStory(this.story.selected);
      }
    });
  }

  editSteps() {
    const dialogRef = this.nbDialogService.open(StepDialogComponent, {
      context: {
        steps: StoryStep.filterNew(this.story.steps),
        defaultCategory: this.story.category
      }
    });
    dialogRef.onClose.pipe(take(1)).subscribe((result) => {
      if (result && result.steps) {
        this.story.steps = result.steps;
        this.saveStory(this.story.selected);
      }
    });
  }

  createStory() {
    this.isSwitchingToManagedStory = false;
    const intent = this.state.findIntentByName(this.storyNode.storyDefinitionId);
    this.story = new StoryDefinitionConfiguration(
      intent.name,
      this.botId,
      new IntentName(intent.name),
      AnswerConfigurationType.simple,
      this.state.user.organization,
      [],
      'build',
      intent.name,
      '',
      this.state.currentLocale,
      []
    );
  }

  saveNewStory() {
    this.isSwitchingToManagedStory = false;
    const invalidMessage = this.story.currentAnswer().invalidMessage();
    if (invalidMessage) {
      this.dialog.notify(`Error: ${invalidMessage}`);
    } else {
      if (!this.canBeMetricStory()) this.story.metricStory = false;
      this.bot.newStory(new CreateStoryRequest(this.story, this.state.currentLocale, [])).subscribe((intent) => {
        this.dialog.notify(`New story ${this.story.name} created for language ${this.state.currentLocale}`, 'New Story');
        this.initStoryByBotIdAndIntent();
      });
    }
  }

  submitClose() {
    this.closeStory.emit(true);
  }

  manageStory() {
    this.story.currentType = AnswerConfigurationType.simple;
    this.isSwitchingToManagedStory = true;
  }

  cancelManagingStory() {
    if (this.isSwitchingToManagedStory) {
      this.isSwitchingToManagedStory = false;
      this.story.currentType = AnswerConfigurationType.builtin;
    }
    this.submitClose();
  }

  download(story: StoryDefinitionConfiguration) {
    setTimeout((_) => {
      this.bot.exportStory(this.state.currentApplication.name, story.storyId).subscribe((blob) => {
        const exportFileName = getExportFileName(
          this.state.currentApplication.namespace,
          this.state.currentApplication.name,
          'Story',
          'json',
          story.storyId
        );
        saveAs(blob, exportFileName);
        this.dialog.notify(`Dump provided`, 'Dump');
      });
    }, 1);
  }

  customiseMainAnswer() {
    this.nbDialogService
      .open(SelectBotConfigurationDialogComponent, {
        closeOnEsc: true,
        context: {
          title: 'Customise Answers'
        }
      })
      .onClose.subscribe((selectedConfig) => {
        if (!selectedConfig || !this.canCustomiseMainAnswer()) {
          return;
        }
        if (this.story.configuredAnswers.find((customAnswer) => customAnswer.botConfiguration === selectedConfig.name)) {
          this.dialog.notify('Custom answer already exists.', 'Customise', {
            status: 'danger',
            duration: 3000
          });
          return;
        }
        if (!this.story.configuredAnswers) {
          this.story.configuredAnswers = [];
        }
        const answerConfigurations = this.story.answers
          .filter((answer) => answer.answerType === this.story.currentType)
          .map((answer) => answer.duplicate(this.bot));
        const configuredAnswer = new BotConfiguredAnswer(selectedConfig.name, this.story.currentType, answerConfigurations);
        this.story.configuredAnswers.push(configuredAnswer);
      });
  }

  canCustomiseMainAnswer(): boolean {
    return this.story.currentType === AnswerConfigurationType.simple || this.story.currentType === AnswerConfigurationType.script;
  }

  deleteCustomAnswers(answer: BotConfiguredAnswer) {
    const action = 'delete';
    this.nbDialogService
      .open(ChoiceDialogComponent, {
        closeOnEsc: true,
        context: {
          title: `Delete "${answer.botConfiguration}" custom answers`,
          subtitle: `Are you sure you want to delete the "${answer.botConfiguration}" custom answers?`,
          actions: [
            { actionName: 'cancel', buttonStatus: 'basic', ghost: true },
            { actionName: action, buttonStatus: 'danger' }
          ],
          modalStatus: 'danger'
        }
      })
      .onClose.subscribe((result) => {
        if (result === action) {
          const foundIndex = this.story.configuredAnswers ? this.story.configuredAnswers.indexOf(answer) : -1;
          if (foundIndex >= 0) {
            this.story.configuredAnswers.splice(foundIndex, 1);
          }
        }
      });
  }

  addCustomSteps() {
    // TODO : check if all steps have a userSentence defined to avoid an error on duplication
    this.nbDialogService
      .open(SelectBotConfigurationDialogComponent, {
        closeOnEsc: true,
        context: {
          title: 'Customise Actions'
        }
      })
      .onClose.subscribe((selectedConfig) => {
        if (!selectedConfig) {
          return;
        }
        if (this.story.configuredSteps.find((customAnswer) => customAnswer.botConfiguration === selectedConfig.name)) {
          this.dialog.notify('Custom actions already exist.', 'Customise', {
            status: 'danger',
            duration: 3000
          });
          return;
        }
        if (!this.story.configuredSteps) {
          this.story.configuredSteps = [];
        }
        const steps = this.story.steps.map((step) => step.duplicate(this.bot));
        this.story.configuredSteps.push(new BotConfiguredSteps(selectedConfig.name, steps));
      });
  }

  deleteCustomSteps(steps: BotConfiguredSteps) {
    const action = 'delete';
    this.nbDialogService
      .open(ChoiceDialogComponent, {
        closeOnEsc: true,
        context: {
          title: `Delete "${steps.botConfiguration}" custom steps`,
          subtitle: `Are you sure you want to delete the "${steps.botConfiguration}" custom steps?`,
          actions: [
            { actionName: 'cancel', buttonStatus: 'basic', ghost: true },
            { actionName: action, buttonStatus: 'danger' }
          ],
          modalStatus: 'danger'
        }
      })
      .onClose.subscribe((result) => {
        if (result === action) {
          const foundIndex = this.story.configuredSteps ? this.story.configuredSteps.indexOf(steps) : -1;
          if (foundIndex >= 0) {
            this.story.configuredSteps.splice(foundIndex, 1);
          }
        }
      });
  }

  toggleStoryPanel(open: boolean): void {
    if (!open) {
      this.story.hideDetails = false;
      this.story.selected = true;
    } else {
      this.story.selected = false;
    }
  }

  canBeMetricStory() {
    for (let i = 0; i < this.story.steps?.length; i++) {
      if (this.story.steps[i].metrics?.length) return true;
    }

    return false;
  }
}
