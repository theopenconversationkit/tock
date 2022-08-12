import { Component, Input, OnDestroy, OnInit } from '@angular/core';
import { Subject } from 'rxjs';
import { take } from 'rxjs/operators';
import { BotService } from '../../../bot/bot-service';
import { CreateI18nLabelRequest, I18nLabel, I18nLabels } from '../../../bot/model/i18n';
import { DialogService } from '../../../core-nlp/dialog.service';
import { StateService } from '../../../core-nlp/state.service';
import { UserInterfaceType } from '../../../core/model/configuration';
import { ClassifiedEntity, Intent } from '../../../model/nlp';
import { NlpService } from '../../../nlp-tabs/nlp.service';
import { JsonPreviewerComponent } from '../../../shared/json-previewer/json-previewer.component';
import { getScenarioActions, getScenarioIntents } from '../../commons/utils';
import {
  Scenario,
  ScenarioItem,
  SCENARIO_ITEM_FROM_BOT,
  SCENARIO_ITEM_FROM_CLIENT,
  TempSentence,
  DependencyUpdateJob,
  SCENARIO_STATE
} from '../../models';
import { ScenarioService } from '../../services/scenario.service';
import { ScenarioDesignerService } from '../scenario-designer.service';

@Component({
  selector: 'scenario-publishing',
  templateUrl: './scenario-publishing.component.html',
  styleUrls: ['./scenario-publishing.component.scss']
})
export class ScenarioPublishingComponent implements OnInit, OnDestroy {
  destroy = new Subject();
  @Input() scenario: Scenario;
  @Input() isReadonly: boolean;

  i18n: I18nLabels;

  readonly SCENARIO_ITEM_FROM_CLIENT = SCENARIO_ITEM_FROM_CLIENT;
  readonly SCENARIO_ITEM_FROM_BOT = SCENARIO_ITEM_FROM_BOT;

  constructor(
    public state: StateService,
    private nlp: NlpService,
    private scenarioDesignerService: ScenarioDesignerService,
    private botService: BotService,
    private dialogService: DialogService,
    private scenarioService: ScenarioService
  ) {}

  tickStoryJson: string;
  ngOnInit(): void {
    if (!this.isReadonly) {
      this.botService
        .i18nLabels()
        .pipe(take(1))
        .subscribe((results) => {
          this.i18n = results;
          this.checkDependencies();
        });
    }
  }

  dependencies: { [key: string]: DependencyUpdateJob[] };

  getJobsType(jobs: DependencyUpdateJob[]): string {
    return jobs[0].type;
  }

  areAllJobsTypeDone(jobs: DependencyUpdateJob[]) {
    return jobs.every((job) => job.done);
  }

  checkDependencies(): void {
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
      } else if (action.tickActionDefinition.answerUpdate) {
        this.dependencies.answersToUpdate.push({
          type: 'update',
          done: false,
          data: action
        });
      }
    });
  }

  processingDependencies: boolean;

  processDependencies(): void {
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
          let nextAnswerUpdate = this.dependencies.answersToUpdate.find((i) => !i.done);
          if (nextAnswerUpdate) {
            this.processAnswer(nextAnswerUpdate);
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
  }

  processIntent(intentTask: DependencyUpdateJob): void {
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

  postNewEntity(task: DependencyUpdateJob, tempEntity): void {
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

  postNewSentence(task: DependencyUpdateJob, intent: Intent, tempSentence: TempSentence): void {
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

  postNewIntent(intentTask: DependencyUpdateJob, intentEntities): void {
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

  processAnswer(answerTask: DependencyUpdateJob): void {
    if (!answerTask.data.tickActionDefinition.answerId) {
      return this.postNewAnswer(answerTask);
    }

    if (answerTask.data.tickActionDefinition.answerUpdate) {
      return this.patchAnswer(answerTask);
    }

    answerTask.done = true;
    this.processDependencies();
  }

  patchAnswer(answerTask: DependencyUpdateJob): void {
    let i18nLabel: I18nLabel = this.i18n.labels.find((i) => {
      return i._id === answerTask.data.tickActionDefinition.answerId;
    });

    let i18n = i18nLabel.i18n.find((i) => {
      return (
        i.interfaceType === UserInterfaceType.textChat && i.locale === this.state.currentLocale
      );
    });
    i18n.label = answerTask.data.tickActionDefinition.answer;

    this.botService.saveI18nLabel(i18nLabel).subscribe(
      (result) => {
        delete answerTask.data.tickActionDefinition.answerUpdate;
        this.processAnswer(answerTask);
      },
      (error) => {
        console.log(error);
      }
    );
  }

  postNewAnswer(answerTask: DependencyUpdateJob): void {
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

  previewTickStory(): void {
    const story = this.compileTickStory();

    const jsonPreviewerRef = this.dialogService.openDialog(JsonPreviewerComponent, {
      context: { jsonData: story }
    });
    jsonPreviewerRef.componentRef.instance.jsonPreviewerRef = jsonPreviewerRef;
  }

  tickStoryPostSuccessfull: boolean = false;
  tickStoryErrors: string[];

  postTickStory(): void {
    this.tickStoryErrors = undefined;
    const story = this.compileTickStory();
    this.scenarioService.postTickStory(story).subscribe(
      (res) => {
        // Successful save. We pass the scenario state to "current" and save it
        this.scenario.state = SCENARIO_STATE.current;
        this.scenarioDesignerService
          .saveScenario(this.scenario.id, this.scenario)
          .subscribe((data) => {
            // Scenario saved. Redirect the user to scenario list view
            this.tickStoryPostSuccessfull = true;
            setTimeout(() => this.scenarioDesignerService.exitDesigner(), 3000);
          });
      },
      (error) => {
        // Errors have occurred, let's inform the user.
        this.tickStoryErrors = error.error;
      }
    );
  }

  compileTickStory(): object {
    const intents: ScenarioItem[] = getScenarioIntents(this.scenario);
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
      storyId: `${this.scenario.name}_${this.scenario.id}`, //A préciser
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
