import { Component, Input, OnChanges, OnDestroy, SimpleChanges } from '@angular/core';
import { NbToastrService } from '@nebular/theme';
import { Subject } from 'rxjs';

import { BotService } from '../../../bot/bot-service';
import { CreateI18nLabelRequest, I18nLabel, I18nLabels, I18nLocalizedLabel } from '../../../bot/model/i18n';
import { StoryDefinitionConfigurationSummary } from '../../../bot/model/story';
import { StateService } from '../../../core-nlp/state.service';
import { ClassifiedEntity, Intent, SentenceStatus } from '../../../model/nlp';
import { NlpService } from '../../../nlp-tabs/nlp.service';
import { deepCopy } from '../../../shared/utils';
import { isStepValid } from '../../commons/scenario-validation';
import { getScenarioActionDefinitions, getScenarioActions, getScenarioIntents } from '../../commons/utils';
import {
  ScenarioItem,
  SCENARIO_ITEM_FROM_BOT,
  SCENARIO_ITEM_FROM_CLIENT,
  TempSentence,
  DependencyUpdateJob,
  SCENARIO_STATE,
  TickStory,
  ScenarioVersionExtended,
  SCENARIO_MODE,
  unknownIntentName,
  Handler,
  TempEntity
} from '../../models';
import { ScenarioService } from '../../services';
import { ScenarioDesignerService } from '../scenario-designer.service';

@Component({
  selector: 'tock-scenario-publishing',
  templateUrl: './scenario-publishing.component.html',
  styleUrls: ['./scenario-publishing.component.scss']
})
export class ScenarioPublishingComponent implements OnChanges, OnDestroy {
  @Input() scenario: ScenarioVersionExtended;
  @Input() isReadonly: boolean;
  @Input() i18n: I18nLabels;
  @Input() readonly avalaibleHandlers: Handler[];
  @Input() readonly availableStories: StoryDefinitionConfigurationSummary[];

  destroy = new Subject();

  readonly SCENARIO_ITEM_FROM_CLIENT = SCENARIO_ITEM_FROM_CLIENT;
  readonly SCENARIO_ITEM_FROM_BOT = SCENARIO_ITEM_FROM_BOT;

  constructor(
    private stateService: StateService,
    private nlp: NlpService,
    private scenarioDesignerService: ScenarioDesignerService,
    private botService: BotService,
    private scenarioService: ScenarioService,
    private toastrService: NbToastrService
  ) {}

  tickStoryJson: string;

  // we check the dependencies after each scenario save in order not to lose the reference of the involved objects
  ngOnChanges(changes: SimpleChanges): void {
    if (!this.isReadonly) {
      this.checkDependencies();
    }
  }

  isScenarioUnPublishable(): string {
    let unPublishable: string;
    const isPublishingStepValid =
      isStepValid(this.scenario, SCENARIO_MODE.casting).valid &&
      isStepValid(this.scenario, SCENARIO_MODE.production).valid &&
      isStepValid(this.scenario, SCENARIO_MODE.publishing).valid;

    if (!isPublishingStepValid) {
      unPublishable = 'At least one of the previous steps requires your attention';
    }

    if (!unPublishable) {
      const actionDefs = getScenarioActionDefinitions(this.scenario);

      const expectedHandlers = actionDefs.filter((ad) => ad.handler).map((ad) => ad.handler);
      const unImplementedHandlers = [];
      expectedHandlers.forEach((expectedHandler) => {
        if (!this.avalaibleHandlers.find((h) => h.name === expectedHandler)) {
          unImplementedHandlers.push(expectedHandler);
        }
      });

      if (unImplementedHandlers.length) {
        unPublishable = `The following handlers are not yet implemented on the server side : ${unImplementedHandlers.join(', ')}`;
      }

      const expectedStories = actionDefs.filter((ad) => ad.targetStory).map((ad) => ad.targetStory);
      const missingTargetStories = [];
      expectedStories.forEach((expectedStory) => {
        if (!this.availableStories.find((s) => s.storyId === expectedStory)) {
          missingTargetStories.push(expectedStory);
        }
      });

      if (missingTargetStories.length) {
        if (!unPublishable) {
          unPublishable = '';
        }
        unPublishable += `The following target stories have been removed : ${missingTargetStories.join(', ')}`;
      }
    }

    return unPublishable;
  }

  getJobsType(jobs: DependencyUpdateJob[]): string {
    return jobs[0].type;
  }

  areAllJobsTypeDone(jobs: DependencyUpdateJob[]): boolean {
    return jobs.every((job) => job.done);
  }

  dependencies: { [key: string]: DependencyUpdateJob[] } = {
    intentsToCreate: [],
    intentsToUpdate: [],
    answersToCreate: [],
    answersToUpdate: [],
    unknownAnswersToCreate: [],
    unknownAnswersToUpdate: []
  };

  resetDependencies(): void {
    for (const attibute in this.dependencies) {
      this.dependencies[attibute] = [];
    }
  }

  checkDependencies(): void {
    // We empty any dependencies already listed before redoing the inventory of changes to save
    this.resetDependencies();

    getScenarioIntents(this.scenario).forEach((intent) => {
      if (!intent.intentDefinition.intentId && intent.intentDefinition.sentences?.length) {
        this.dependencies.intentsToCreate.push({
          type: 'creation',
          done: false,
          item: intent
        });
      } else if (intent.intentDefinition.sentences?.length) {
        this.dependencies.intentsToUpdate.push({
          type: 'update',
          done: false,
          item: intent
        });
      }
    });

    getScenarioActions(this.scenario).forEach((action) => {
      action.actionDefinition.answers?.forEach((scenarioAnswer) => {
        if (scenarioAnswer.answer && !action.actionDefinition.answerId) {
          this.dependencies.answersToCreate.push({
            type: 'creation',
            done: false,
            item: action,
            answer: scenarioAnswer
          });
        } else if (scenarioAnswer.answerUpdate) {
          this.dependencies.answersToUpdate.push({
            type: 'update',
            done: false,
            item: action,
            answer: scenarioAnswer
          });
        }
      });

      action.actionDefinition.unknownAnswers?.forEach((unknownAnswer) => {
        if (unknownAnswer.answer && !action.actionDefinition.unknownAnswerId) {
          this.dependencies.unknownAnswersToCreate.push({
            type: 'creation',
            done: false,
            item: action,
            answer: unknownAnswer
          });
        } else if (unknownAnswer.answerUpdate) {
          this.dependencies.unknownAnswersToUpdate.push({
            type: 'update',
            done: false,
            item: action,
            answer: unknownAnswer
          });
        }
      });
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
            let nextUnknownAnswerCreation = this.dependencies.unknownAnswersToCreate.find((i) => !i.done);
            if (nextUnknownAnswerCreation) {
              this.processUnknownAnswer(nextUnknownAnswerCreation);
            } else {
              let nextUnknownAnswerUpdate = this.dependencies.unknownAnswersToUpdate.find((i) => !i.done);
              if (nextUnknownAnswerUpdate) {
                this.processUnknownAnswer(nextUnknownAnswerUpdate);
              } else {
                // save the scenario
                this.scenarioDesignerService.saveScenario(this.scenario).subscribe((data) => {
                  this.postTickStory();
                });
              }
            }
          }
        }
      }
    }
  }

  processAnswer(answerTask: DependencyUpdateJob): void {
    if (!answerTask.item.actionDefinition.answerId) {
      this.postAnswer(answerTask);
    } else {
      this.patchAnswer(answerTask);
    }
  }

  processUnknownAnswer(answerTask: DependencyUpdateJob): void {
    if (!answerTask.item.actionDefinition.unknownAnswerId) {
      this.postAnswer(answerTask, true);
    } else {
      this.patchAnswer(answerTask, true);
    }
  }

  postAnswer(answerTask: DependencyUpdateJob, unknownAnswer = false): void {
    let answersLocalesVersions = answerTask.item.actionDefinition.answers;
    if (unknownAnswer) {
      answersLocalesVersions = answerTask.item.actionDefinition.unknownAnswers;
    }

    this.scenarioService.saveAnswers(answersLocalesVersions).subscribe({
      next: (answer) => {
        if (unknownAnswer) {
          answerTask.item.actionDefinition.unknownAnswerId = answer._id;
        } else {
          answerTask.item.actionDefinition.answerId = answer._id;
        }
        this.i18n.labels.push(answer);

        for (let index = 0; index < answersLocalesVersions.length; index++) {
          delete answersLocalesVersions[index].answerUpdate;
        }

        answerTask.done = true;
        this.processDependencies();
      },
      error: (error) => {
        console.log(error);
      }
    });
  }

  patchAnswer(answerTask: DependencyUpdateJob, unknownAnswer = false): void {
    let existingId = answerTask.item.actionDefinition.answerId;
    if (unknownAnswer) {
      existingId = answerTask.item.actionDefinition.unknownAnswerId;
    }

    const i18nLabel: I18nLabel = this.i18n.labels.find((i) => {
      return i._id === existingId;
    });

    let answersLocalesVersions = answerTask.item.actionDefinition.answers;
    if (unknownAnswer) {
      answersLocalesVersions = answerTask.item.actionDefinition.unknownAnswers;
    }

    this.scenarioService.patchAnswer(i18nLabel, answersLocalesVersions).subscribe({
      next: (result) => {
        for (let index = 0; index < answersLocalesVersions.length; index++) {
          delete answersLocalesVersions[index].answerUpdate;
        }
        answerTask.done = true;
        this.processDependencies();
      },
      error: (error) => {
        console.log(error);
      }
    });
  }

  processIntent(intentTask: DependencyUpdateJob): void {
    const intentDefinition = intentTask.item.intentDefinition;

    // Creation of non-existent entities
    if (intentDefinition.sentences?.length) {
      if (intentDefinition.sentences[0].classification.entities.length) {
        const entities = intentDefinition.sentences[0].classification.entities;
        for (let index = 0; index < entities.length; index++) {
          let entity = entities[index];
          const existingEntityType = this.stateService.findEntityTypeByName(entity.type);
          if (!existingEntityType) {
            return this.postNewEntity(intentTask, entity);
          }
        }
      }
    }

    // Creation of non-existent intents
    if (!intentDefinition.intentId || !this.stateService.findIntentById(intentDefinition.intentId)) {
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
      const intent: Intent = this.stateService.findIntentById(intentDefinition.intentId);
      return this.postNewSentence(intentTask, intent, intentDefinition.sentences[0]);
    }

    intentTask.done = true;
    this.processDependencies();
  }

  postNewEntity(task: DependencyUpdateJob, tempEntity: TempEntity): void {
    this.nlp.createEntityType(tempEntity.type).subscribe({
      next: (e) => {
        if (e) {
          // update of application entities
          const entities = this.stateService.entityTypes.getValue().slice(0);
          entities.push(e);
          this.stateService.entityTypes.next(entities);
          this.processIntent(task);
        } else {
          console.log(`Error when creating Entity Type ${tempEntity.type}`);
        }
      },
      error: (error) => {
        console.log(error);
      }
    });
  }

  postNewSentence(task: DependencyUpdateJob, intent: Intent, tempSentence: TempSentence): void {
    const app = this.stateService.currentApplication;
    tempSentence.applicationName = app.name;
    tempSentence.namespace = app.namespace;

    this.nlp.parse(tempSentence).subscribe(
      (sentence) => {
        sentence.classification.intentId = intent._id;
        tempSentence.classification.entities.forEach((entity) => (entity.subEntities = []));
        sentence.classification.entities = tempSentence.classification.entities as ClassifiedEntity[];
        sentence.status = SentenceStatus.validated;
        this.nlp.updateSentence(sentence).subscribe({
          next: (_res) => {
            task.item.intentDefinition.sentences = task.item.intentDefinition.sentences.filter((s) => s != tempSentence);
            this.processIntent(task);
          },
          error: (error) => {
            console.log(error);
          }
        });
      },
      (error) => {
        console.log(error);
      }
    );
  }

  postNewIntent(intentTask: DependencyUpdateJob, intentEntities: TempEntity[]): void {
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
          intentTask.item.intentDefinition.name,
          this.stateService.user.organization,
          entities,
          [this.stateService.currentApplication._id],
          [],
          [],
          intentTask.item.intentDefinition.label,
          intentTask.item.intentDefinition.description,
          intentTask.item.intentDefinition.category
        )
      )
      .subscribe(
        (intent) => {
          intentTask.item.intentDefinition.intentId = intent._id;
          this.stateService.addIntent(intent);
          this.processIntent(intentTask);
        },
        (error) => {
          console.log(error);
        }
      );
  }

  tickStoryPostSuccessfull: boolean = false;
  tickStoryErrors: string[];

  postTickStory(): void {
    this.tickStoryErrors = undefined;
    const story = this.compileTickStory();

    this.scenarioService.postTickStory(story).subscribe({
      next: (res) => {
        // Successful save. We pass the scenario state to "current" and save it
        this.scenario.state = SCENARIO_STATE.current;
        this.scenarioDesignerService.saveScenario(this.scenario).subscribe((data) => {
          // reset of scenarioService scenarios cache to force the reload of scenarios list on next call to scenarioService.getScenarios
          this.scenarioService.setScenariosGroupsUnloading();

          this.toastrService.success(`Tick story successfully saved`, 'Success', {
            duration: 5000,
            status: 'success'
          });
          // Scenario saved. Redirect the user to scenario list view
          this.tickStoryPostSuccessfull = true;
          setTimeout(() => this.scenarioDesignerService.exitDesigner(), 3000);
        });
      },
      error: (error) => {
        // Errors have occurred, let's inform the user.
        this.tickStoryErrors = error.error?.errors
          ? error.error?.errors
          : typeof error === 'string'
          ? [{ message: error }]
          : [{ message: 'An unknown error occured' }];
      }
    });
  }

  private compileTickStory(): TickStory {
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

    const intentsContexts = [];
    intents.forEach((intent) => {
      if (intent.intentDefinition.outputContextNames?.length) {
        let parentAction = this.scenario.data.scenarioItems.find((item) => item.id === intent.parentIds[0]);
        let intentAlreadyPresent = intentsContexts.find((ic) => ic.intentName === intent.intentDefinition.name);

        if (!intentAlreadyPresent) {
          intentsContexts.push({
            intentName: intent.intentDefinition.name,
            associations: [
              {
                actionName: parentAction.actionDefinition.name,
                contextNames: intent.intentDefinition.outputContextNames
              }
            ]
          });
        } else {
          intentAlreadyPresent.associations.push({
            actionName: parentAction.actionDefinition.name,
            contextNames: intent.intentDefinition.outputContextNames
          });
        }
      }
    });

    const actionsDefinitions = deepCopy(getScenarioActionDefinitions(this.scenario));

    // If an action refers to a targetStory, we delete everything concerning unknown answers
    actionsDefinitions.forEach((actionDef) => {
      if (actionDef.targetStory) {
        delete actionDef.unknownAnswerId;
        delete actionDef.unknownAnswers;
      }
    });

    let unknownAnswerConfigs = [];
    actionsDefinitions.forEach((actionDefinition) => {
      if (actionDefinition.unknownAnswerId) {
        unknownAnswerConfigs.push({
          intent: unknownIntentName,
          action: actionDefinition.name,
          answerId: actionDefinition.unknownAnswerId
        });

        // We reference 'unknown' in the secondary intents if at least one action defines a response to the intent 'unknown' in order to ensure the integrity of the data set sent
        if (!secondaryIntents.includes(unknownIntentName)) {
          secondaryIntents.push(unknownIntentName);
        }
      }
    });

    let tickStory: TickStory = {
      botId: this.stateService.currentApplication.name,
      storyId: this.scenario._scenarioGroupId,
      name: `TickStory from the scenarioGroup "${this.scenario._name}"`,
      description: `TickStory from the scenarioGroup "${this.scenario._name}" with id ${this.scenario._scenarioGroupId}`,
      mainIntent: mainIntent,
      primaryIntents: primaryIntents,
      secondaryIntents: secondaryIntents,
      contexts: this.scenario.data.contexts,
      triggers: this.scenario.data.triggers,
      actions: actionsDefinitions,
      stateMachine: this.scenario.data.stateMachine,
      intentsContexts: intentsContexts,
      unknownAnswerConfigs: unknownAnswerConfigs
    };

    return tickStory;
  }

  ngOnDestroy(): void {
    this.destroy.next(true);
    this.destroy.complete();
  }
}
