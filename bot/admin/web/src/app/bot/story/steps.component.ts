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

import { Component, Input, OnChanges, OnInit, SimpleChanges } from '@angular/core';
import { AnswerConfigurationType, IntentName, SimpleAnswerConfiguration, StoryStep } from '../model/story';
import { StateService } from '../../core-nlp/state.service';
import { FlatTreeControl } from '@angular/cdk/tree';
import { MatTreeFlatDataSource, MatTreeFlattener } from '@angular/material/tree';
import { SelectEntityDialogComponent } from './select-entity-dialog.component';
import { DialogService } from '../../core-nlp/dialog.service';
import { MatDialog } from '@angular/material/dialog';
import { NlpService } from '../../nlp-tabs/nlp.service';
import { CreateI18nLabelRequest } from '../model/i18n';
import { BotService } from '../bot-service';

@Component({
  selector: 'tock-steps',
  templateUrl: './steps.component.html',
  styleUrls: ['./steps.component.css']
})
export class StepsComponent implements OnInit, OnChanges {
  @Input()
  steps: StoryStep[] = [];

  @Input()
  defaultCategory: string = 'build';

  @Input()
  readonly: boolean = false;

  treeControl: FlatTreeControl<StoryStep>;
  treeFlattener: MatTreeFlattener<StoryStep, StoryStep>;
  dataSource: MatTreeFlatDataSource<StoryStep, StoryStep>;

  constructor(
    public state: StateService,
    private dialog: DialogService,
    private matDialog: MatDialog,
    private nlp: NlpService,
    private bot: BotService
  ) {}

  ngOnInit() {
    this.treeControl = new FlatTreeControl<StoryStep>(
      (s) => s.level,
      (s) => s.children.length !== 0
    );
    this.treeFlattener = new MatTreeFlattener(
      (s) => s,
      (s) => s.level,
      (s) => s.children.length !== 0,
      (s) => s.children
    );

    this.dataSource = new MatTreeFlatDataSource(this.treeControl, this.treeFlattener);
    this.dataSource.data = this.steps;
    this.treeControl.expandAll();
  }

  ngOnChanges(changes: SimpleChanges) {
    if (this.dataSource) {
      this.dataSource.data = this.steps;
      this.treeControl.expandAll();
    }
  }

  isExpandable(s: StoryStep): boolean {
    return s.children.length !== 0;
  }

  addNewStep(step?: StoryStep) {
    const answer = new SimpleAnswerConfiguration([]);
    answer.allowNoAnswer = true;
    const newStep = new StoryStep(
      '',
      new IntentName(''),
      new IntentName(''),
      [answer],
      AnswerConfigurationType.simple,
      this.defaultCategory,
      null,
      [],
      step ? step.level + 1 : 0
    );
    newStep.new = true;
    if (step) {
      step.children.push(newStep);
      this.treeControl.expand(step);
    } else {
      this.steps.push(newStep);
    }
    this.validate();
  }

  duplicateStep(step: StoryStep) {
    // Create new step from step
    const newStep = this.copyFromStep(step);

    // Find step parent
    let parent = this.findParent(this.steps, step);
    if (parent) {
      // add the duplicate step in it
      parent.children.push(newStep);
      this.treeControl.expand(parent);
    } else {
      // or add it in the steps list
      this.steps.push(newStep);
    }
    this.validate();
  }

  private copyFromStep(step: StoryStep) {
    const answer = new SimpleAnswerConfiguration([]);
    answer.allowNoAnswer = true;
    const newStep = new StoryStep(
      '',
      new IntentName(step.intent.name),
      new IntentName(step.targetIntent.name),
      [answer],
      AnswerConfigurationType.simple,
      this.defaultCategory,
      null,
      [],
      step.level
    );
    newStep.new = true;
    newStep.newUserSentence = step.userSentence.defaultLabel;
    newStep.answers = step.answers.map((stepAnswer) => stepAnswer.duplicate(this.bot));
    this.saveI18nLabel(newStep);

    let stepChildren = step.children;
    if (stepChildren) {
      newStep.children = stepChildren.map((child) => this.copyFromStep(child));
    }

    return newStep;
  }

  rebuildTree(step: StoryStep) {
    this.treeControl.expand(step);
    this.validate();
  }

  private findParent(steps: StoryStep[], step: StoryStep): StoryStep {
    if (step.level === 0 || steps.length === 0) {
      return null;
    }
    const c = steps.find((p) => p.children.indexOf(step) !== -1);
    if (c) {
      return c;
    }
    const parents = steps.map((s) => this.findParent(s.children, step)).filter((s) => s !== null);
    return parents.length === 0 ? null : parents[0];
  }

  private findParentFromRootSteps(step: StoryStep): StoryStep {
    return this.findParent(this.steps, step);
  }

  deleteStep(step: StoryStep) {
    const parent = this.findParentFromRootSteps(step);
    if (parent) {
      parent.children.splice(parent.children.indexOf(step), 1);
    } else {
      this.steps.splice(this.steps.indexOf(step), 1);
    }
    this.validate();
  }

  validate() {
    this.dataSource.data = this.steps.slice(0);
  }

  generate() {
    let dialogRef = this.dialog.open(this.matDialog, SelectEntityDialogComponent, {
      data: { generate: true }
    });
    dialogRef.afterClosed().subscribe((result) => {
      if (result?.entity) {
        this.nlp.getDictionary(result.entity).subscribe((dictionary) => {
          //dictionary
          const newSteps = StoryStep.generateEntitySteps(result.intent, this.defaultCategory, result.entity, result.role, dictionary, 0);
          newSteps.forEach((s) => {
            this.steps.push(s);
            this.saveI18nLabel(s);
          });
          this.validate();
        });
      }
    });
  }

  private saveI18nLabel(step: StoryStep) {
    this.bot
      .createI18nLabel(new CreateI18nLabelRequest(this.defaultCategory, step.newUserSentence.trim(), this.state.currentLocale))
      .subscribe((i18n) => {
        step.userSentence = i18n;
        step.new = false;
      });
  }

  private findNeighbourSteps(step: StoryStep): StoryStep[] {
    const p = this.findParent(this.steps, step);
    return p ? p.children : this.steps;
  }

  canDownward(step: StoryStep): boolean {
    const steps = this.findNeighbourSteps(step);
    return steps.length > 1 && steps[steps.length - 1] !== step;
  }

  canUpward(step: StoryStep): boolean {
    const steps = this.findNeighbourSteps(step);
    return steps.length > 1 && steps[0] !== step;
  }

  upward(step: StoryStep) {
    const parent = this.findParentFromRootSteps(step);
    const steps = parent ? parent.children : this.steps;
    const index = steps.indexOf(step);
    if (index !== -1) {
      steps[index] = steps[index - 1];
      steps[index - 1] = step;
    }
    this.validate();
  }

  downward(step: StoryStep) {
    const parent = this.findParentFromRootSteps(step);
    const steps = parent ? parent.children : this.steps;
    const index = steps.indexOf(step);
    if (index !== -1) {
      steps[index] = steps[index + 1];
      steps[index + 1] = step;
    }
    this.validate();
  }
}
