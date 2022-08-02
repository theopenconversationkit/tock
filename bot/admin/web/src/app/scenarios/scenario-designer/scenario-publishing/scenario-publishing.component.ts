import { Component, Injectable, Input, OnDestroy, OnInit } from '@angular/core';
import { Subject } from 'rxjs';
import { BotService } from '../../../bot/bot-service';
import { CreateI18nLabelRequest } from '../../../bot/model/i18n';
import { DialogService } from '../../../core-nlp/dialog.service';
import { StateService } from '../../../core-nlp/state.service';
import { ClassifiedEntity, EntityDefinition, Intent } from '../../../model/nlp';
import { NlpService } from '../../../nlp-tabs/nlp.service';
import { JsonPreviewerComponent } from '../../../shared/json-previewer/json-previewer.component';
import { getScenarioActions, getScenarioIntents } from '../../commons/utils';
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
    private botService: BotService,
    private dialogService: DialogService
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

    getScenarioIntents(this.scenario).forEach((intent) => {
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

    getScenarioActions(this.scenario).forEach((action) => {
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
              this.postTickStory();
            });
        }
      }
    }
  }

  processIntent(intentTask: dependencyUpdateJob) {
    const intentDefinition = intentTask.data.intentDefinition;

    // Creation of non-existent entities
    if (intentDefinition.sentences?.length) {
      if (intentDefinition.sentences[0].classification.entities.length) {
        const entities = intentDefinition.sentences[0].classification.entities;
        for (let index = 0; index < entities.length; index++) {
          let entity = entities[index];
          const existingEntityType = this.state.findEntityTypeByName(entity.type);
          if (!existingEntityType) {
            return this.postNewEntity(intentTask, entity);
          }
        }
      }
    }

    // Creation of non-existent intents
    if (!intentDefinition.intentId || !this.state.findIntentById(intentDefinition.intentId)) {
      // List of all sentences entities
      let intentEntities = [];
      intentDefinition.sentences.forEach((stnce) => {
        stnce.classification.entities.forEach((entt) => {
          intentEntities.push(entt);
        });
      });

      return this.postNewIntent(intentTask, intentEntities);
    }

    // Creation of non-existent or modified sentences
    if (intentDefinition.sentences?.length) {
      const intent: Intent = this.state.findIntentById(intentDefinition.intentId);
      return this.postNewSentence(intentTask, intent, intentDefinition.sentences[0]);
    }

    intentTask.done = true;
    this.processDependencies();
  }

  postNewEntity(task, tempEntity) {
    this.nlp.createEntityType(tempEntity.type).subscribe(
      (e) => {
        if (e) {
          // update of application entities
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
        tempSentence.classification.entities.forEach((entity) => (entity.subEntities = []));
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

  postNewIntent(intentTask, intentEntities) {
    let entities = [];
    intentEntities.forEach((ie) => {
      entities.push({
        entityColor: ie.entityColor,
        entityTypeName: ie.type,
        qualifiedRole: ie.qualifiedRole,
        role: ie.role
      });
    });

    this.nlp
      .saveIntent(
        new Intent(
          intentTask.data.intentDefinition.name,
          this.state.user.organization,
          entities,
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

  postTickStory(): void {
    const story = this.compileTickStory();

    const jsonPreviewerRef = this.dialogService.openDialog(JsonPreviewerComponent, {
      context: { jsonData: story }
    });
    jsonPreviewerRef.componentRef.instance.jsonPreviewerRef = jsonPreviewerRef;
  }

  compileTickStory(): object {
    const intents: scenarioItem[] = getScenarioIntents(this.scenario);
    let mainIntent;
    let primaryIntents = [];
    let secondaryIntents = [];
    intents.forEach((intent) => {
      if (intent.main) {
        mainIntent = intent.intentDefinition.name;
      } else if (intent.intentDefinition.primary) {
        primaryIntents.push(intent.intentDefinition.name);
      } else {
        secondaryIntents.push(intent.intentDefinition.name);
      }
    });
    const app = this.state.currentApplication;
    let botId = app.name;

    let tickStory = {
      name: this.scenario.name,
      botId: botId,
      storyId: 'tickStory id',
      description: this.scenario.description,
      sagaId: 0,
      mainIntent: mainIntent,
      primaryIntents: primaryIntents,
      secondaryIntents: secondaryIntents,
      contexts: this.scenario.data.contexts,
      actions: [],
      stateMachine: this.scenario.data.stateMachine
    };

    this.scenario.data.scenarioItems.forEach((item) => {
      if (item.from == SCENARIO_ITEM_FROM_BOT && item.tickActionDefinition) {
        tickStory.actions.push(item.tickActionDefinition);
      }
    });

    return tickStory;
  }

  ngOnDestroy(): void {
    this.destroy.next();
    this.destroy.complete();
  }
}
