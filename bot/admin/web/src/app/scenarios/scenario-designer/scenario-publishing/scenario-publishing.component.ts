import { Component, Injectable, Input, OnDestroy, OnInit } from '@angular/core';
import { Subject } from 'rxjs';
import { StateService } from '../../../core-nlp/state.service';
import { Intent } from '../../../model/nlp';
import { NlpService } from '../../../nlp-tabs/nlp.service';
import { Scenario, SCENARIO_ITEM_FROM_BOT, SCENARIO_ITEM_FROM_CLIENT } from '../../models';

@Component({
  selector: 'scenario-publishing',
  templateUrl: './scenario-publishing.component.html',
  styleUrls: ['./scenario-publishing.component.scss']
})
export class ScenarioPublishingComponent implements OnInit, OnDestroy {
  destroy = new Subject();
  @Input() scenario: Scenario;

  readonly SCENARIO_ITEM_FROM_CLIENT = SCENARIO_ITEM_FROM_CLIENT;
  readonly SCENARIO_ITEM_FROM_BOT = SCENARIO_ITEM_FROM_BOT;

  constructor(public state: StateService, private nlp: NlpService) {}

  tickStoryJson: string;
  ngOnInit(): void {
    this.checkScenarioConformity();

    // this.tickStoryJson = this.compileTickStory();
  }

  checkScenarioConformity() {
    let problems = [];

    if (!problems.length) {
      this.checkDependencies();
    }
  }

  dependencies;

  checkDependencies() {
    this.dependencies = {
      intentsToCreate: [],
      intentsToUpdate: [],
      answersToCreate: [],
      answersToUpdate: []
    };

    this.getScenarioIntents().forEach((intent) => {
      if (!intent.intentDefinition.intentId) {
        this.dependencies.intentsToCreate.push({
          done: false,
          data: intent
        });
      } else {
        console.log('TODO : check if intent has been modified');
      }
    });

    this.getScenarioActions().forEach((action) => {
      if (!action.answerId) {
        this.dependencies.answersToCreate.push({
          done: false,
          data: action
        });
      }
    });
  }

  processingDependencies;
  processDependencies() {
    this.processingDependencies = true;

    this.processIntents();
  }

  processIntents() {
    let nextIntent = this.dependencies.intentsToCreate.find((i) => !i.done);
    if (nextIntent) {
      this.createIntent(nextIntent);
    }
  }

  createIntent(intentTask) {
    console.log(intentTask.data.intentDefinition.name);
    this.nlp
      .saveIntent(
        new Intent(
          intentTask.data.intentDefinition.name,
          this.state.user.organization,
          [],
          [this.state.currentApplication._id],
          [],
          [],
          intentTask.data.intentDefinition.label,
          intentTask.data.intentDefinition.description,
          intentTask.data.intentDefinition.category
        )
      )
      .subscribe(
        (intent) => {
          console.log(intent);
          // this.selectedValueLabel = intent.intentLabel();
          this.state.addIntent(intent);
          // this.sentence.classification.intentId = intent._id;
          // this.onIntentChange();
        },
        (error) => {
          console.log(error);
          // this.sentence.classification.intentId = Intent.unknown;
          // this.onIntentChange();
        }
      );
  }

  getScenarioIntents() {
    let intents = [];
    this.scenario.data.scenarioItems.forEach((item) => {
      if (item.from == SCENARIO_ITEM_FROM_CLIENT) {
        intents.push(item);
      }
    });

    return intents;
  }

  getScenarioActions() {
    let actions = [];
    this.scenario.data.scenarioItems.forEach((item) => {
      if (item.from == SCENARIO_ITEM_FROM_BOT) {
        actions.push(item.tickActionDefinition);
      }
    });
    return actions;
  }

  compileTickStory(): string {
    let tickStory = {
      name: this.scenario.name,
      botId: 'new_assistant',
      storyId: 'tickStory id',
      description: 'Story description',
      sagaId: 0,
      mainIntent: 'main intent name',
      primaryIntents: ['primary intent name'],
      secondaryIntents: ['secondary intent name', 'secondary intent name'],
      contexts: this.scenario.data.contexts,
      actions: [],
      stateMachine: this.scenario.data.stateMachine
    };

    this.scenario.data.scenarioItems.forEach((item) => {
      if (item.from == SCENARIO_ITEM_FROM_BOT && item.tickActionDefinition) {
        tickStory.actions.push(item.tickActionDefinition);
      }
    });

    return JSON.stringify(tickStory, null, 4);
  }

  ngOnDestroy(): void {
    this.destroy.next();
    this.destroy.complete();
  }
}
