import { Component, Injectable, Input, OnDestroy, OnInit } from '@angular/core';
import { Subject } from 'rxjs';
import { BotService } from '../../../bot/bot-service';
import { CreateI18nLabelRequest } from '../../../bot/model/i18n';
import { StateService } from '../../../core-nlp/state.service';
import { ClassifiedEntity, EntityDefinition, Intent } from '../../../model/nlp';
import { NlpService } from '../../../nlp-tabs/nlp.service';
import {
  Scenario,
  scenarioItem,
  SCENARIO_ITEM_FROM_BOT,
  SCENARIO_ITEM_FROM_CLIENT,
  TempSentence,
  dependencyUpdateJob
} from '../../models';
import { ScenarioDesignerService } from '../scenario-designer.service';

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

  constructor(
    public state: StateService,
    private nlp: NlpService,
    private scenarioDesignerService: ScenarioDesignerService,
    private botService: BotService
  ) {}

  tickStoryJson: string;
  ngOnInit(): void {
    this.checkScenarioConformity();
  }

  checkScenarioConformity() {
    let problems = [];

    if (!problems.length) {
      this.checkDependencies();
    }
  }

  dependencies: { [key: string]: dependencyUpdateJob[] };

  getJobsType(jobs: dependencyUpdateJob[]) {
    return jobs[0].type;
  }

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
          type: 'creation',
          done: false,
          data: intent
        });
      } else if (intent.intentDefinition.sentences?.length) {
        this.dependencies.intentsToUpdate.push({
          type: 'update',
          done: false,
          data: intent
        });
      }
    });

    this.getScenarioActions().forEach((action) => {
      if (!action.tickActionDefinition.answerId) {
        this.dependencies.answersToCreate.push({
          type: 'creation',
          done: false,
          data: action
        });
      }
    });
  }

  processingDependencies;
  processDependencies() {
    this.processingDependencies = true;

    let nextIntentCreation = this.dependencies.intentsToCreate.find((i) => !i.done);
    if (nextIntentCreation) {
      this.processIntent(nextIntentCreation);
    } else {
      let nextIntentUpdate = this.dependencies.intentsToUpdate.find((i) => !i.done);
      if (nextIntentUpdate) {
        this.processIntent(nextIntentUpdate);
      } else {
        let nextAnswerCreation = this.dependencies.answersToCreate.find((i) => !i.done);
        if (nextAnswerCreation) {
          this.processAnswer(nextAnswerCreation);
        } else {
          // save the scenario
          this.scenarioDesignerService
            .saveScenario(this.scenario.id, this.scenario)
            .subscribe((data) => {
              // // Finally, reload the app to update the application intents and other dependencies modifications
              // this.state.resetConfiguration();
            });
        }
      }
    }
  }

  processIntent(intentTask: dependencyUpdateJob) {
    if (
      !intentTask.data.intentDefinition.intentId ||
      !this.state.findIntentById(intentTask.data.intentDefinition.intentId)
    ) {
      return this.postNewIntent(intentTask);
    }

    if (intentTask.data.intentDefinition.sentences?.length) {
      if (intentTask.data.intentDefinition.sentences[0].classification.entities.length) {
        const entities = intentTask.data.intentDefinition.sentences[0].classification.entities;
        for (let index = 0; index < entities.length; index++) {
          let entity = entities[index];
          const existingEntityType = this.state.findEntityTypeByName(entity.type);
          if (!existingEntityType) {
            return this.postNewEntity(intentTask, entity);
          }
        }
      }

      const intent: Intent = this.state.findIntentById(intentTask.data.intentDefinition.intentId);
      return this.postNewSentence(
        intentTask,
        intent,
        intentTask.data.intentDefinition.sentences[0]
      );
    }

    intentTask.done = true;
    this.processDependencies();
  }

  postNewEntity(task, tempEntity) {
    console.log(tempEntity);

    this.nlp.createEntityType(tempEntity.type).subscribe(
      (e) => {
        if (e) {
          const entity = new EntityDefinition(e.name, tempEntity.role);
          const entities = this.state.entityTypes.getValue().slice(0);
          entities.push(e);
          this.state.entityTypes.next(entities);
          this.processIntent(task);
        } else {
          console.log(`Error when creating Entity Type ${tempEntity.type}`);
        }
      },
      (error) => {
        console.log(error);
      }
    );
  }

  postNewSentence(task: dependencyUpdateJob, intent: Intent, tempSentence: TempSentence) {
    this.nlp.parse(tempSentence).subscribe(
      (sentence) => {
        // sentence = sentence.withIntent(this.state, intentId);
        sentence.classification.intentId = intent._id;
        sentence.classification.entities = tempSentence.classification
          .entities as ClassifiedEntity[];

        this.nlp.updateSentence(sentence).subscribe(
          (res) => {
            task.data.intentDefinition.sentences = task.data.intentDefinition.sentences.filter(
              (s) => s != tempSentence
            );
            this.processIntent(task);
          },
          (error) => {
            console.log(error);
          }
        );
      },
      (error) => {
        console.log(error);
      }
    );
  }

  postNewIntent(intentTask) {
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
          intentTask.data.intentDefinition.intentId = intent._id;
          this.state.addIntent(intent);
          this.processIntent(intentTask);
        },
        (error) => {
          console.log(error);
        }
      );
  }

  processAnswer(answerTask: dependencyUpdateJob) {
    if (!answerTask.data.tickActionDefinition.answerId) {
      return this.postNewAnswer(answerTask);
    }

    answerTask.done = true;
    this.processDependencies();
  }

  postNewAnswer(answerTask: dependencyUpdateJob) {
    let request = new CreateI18nLabelRequest(
      'scenario',
      answerTask.data.tickActionDefinition.answer,
      this.state.currentLocale
    );
    this.botService.createI18nLabel(request).subscribe(
      (answer) => {
        answerTask.data.tickActionDefinition.answerId = answer._id;
        this.processAnswer(answerTask);
      },
      (error) => {
        console.log(error);
      }
    );
  }

  getScenarioIntents(): scenarioItem[] {
    let intents: scenarioItem[] = [];
    this.scenario.data.scenarioItems.forEach((item) => {
      if (item.from == SCENARIO_ITEM_FROM_CLIENT) {
        intents.push(item);
      }
    });

    return intents;
  }

  getScenarioActions(): scenarioItem[] {
    let actions: scenarioItem[] = [];
    this.scenario.data.scenarioItems.forEach((item) => {
      if (item.from == SCENARIO_ITEM_FROM_BOT) {
        actions.push(item);
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
