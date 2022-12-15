import { Component, Input, OnDestroy, OnInit } from '@angular/core';
import { NbDialogService, NbToastrService } from '@nebular/theme';
import { Subject } from 'rxjs';
import { BotService } from '../../../bot/bot-service';
import { CreateI18nLabelRequest, I18nLabel, I18nLabels } from '../../../bot/model/i18n';
import { StateService } from '../../../core-nlp/state.service';
import { UserInterfaceType } from '../../../core/model/configuration';
import { ClassifiedEntity, Intent, SentenceStatus } from '../../../model/nlp';
import { NlpService } from '../../../nlp-tabs/nlp.service';
import { JsonPreviewerComponent } from '../../../shared/components/json-previewer/json-previewer.component';
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
  SCENARIO_MODE
} from '../../models';
import { ScenarioService } from '../../services/scenario.service';
import { ScenarioDesignerService } from '../scenario-designer.service';

@Component({
  selector: 'scenario-publishing',
  templateUrl: './scenario-publishing.component.html',
  styleUrls: ['./scenario-publishing.component.scss']
})
export class ScenarioPublishingComponent implements OnInit, OnDestroy {
  @Input() scenario: ScenarioVersionExtended;
  @Input() isReadonly: boolean;
  @Input() i18n: I18nLabels;
  @Input() readonly avalaibleHandlers: string[];

  destroy = new Subject();

  readonly SCENARIO_ITEM_FROM_CLIENT = SCENARIO_ITEM_FROM_CLIENT;
  readonly SCENARIO_ITEM_FROM_BOT = SCENARIO_ITEM_FROM_BOT;

  constructor(
    public state: StateService,
    private nlp: NlpService,
    private scenarioDesignerService: ScenarioDesignerService,
    private botService: BotService,
    private nbDialogService: NbDialogService,
    private scenarioService: ScenarioService,
    private toastrService: NbToastrService
  ) {}

  tickStoryJson: string;
  ngOnInit(): void {
    if (!this.isReadonly) {
      this.checkDependencies();
    }
  }

  dependencies: { [key: string]: DependencyUpdateJob[] } = {
    intentsToCreate: [],
    intentsToUpdate: [],
    answersToCreate: [],
    answersToUpdate: []
  };

  getJobsType(jobs: DependencyUpdateJob[]): string {
    return jobs[0].type;
  }

  areAllJobsTypeDone(jobs: DependencyUpdateJob[]) {
    return jobs.every((job) => job.done);
  }

  checkDependencies(): void {
    getScenarioIntents(this.scenario).forEach((intent) => {
      if (!intent.intentDefinition.intentId && intent.intentDefinition.sentences?.length) {
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
      if (action.actionDefinition.answer && !action.actionDefinition.answerId) {
        this.dependencies.answersToCreate.push({
          type: 'creation',
          done: false,
          data: action
        });
      } else if (action.actionDefinition.answerUpdate) {
        this.dependencies.answersToUpdate.push({
          type: 'update',
          done: false,
          data: action
        });
      }
    });
  }

  isScenarioUnPublishable() {
    let unPublishable;

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
      let unImplementedHandlers = [];
      expectedHandlers.forEach((eh) => {
        if (!this.avalaibleHandlers.includes(eh)) {
          unImplementedHandlers.push(eh);
        }
      });

      if (unImplementedHandlers.length) {
        unPublishable = `The following handlers are not yet implemented on the server side : ${unImplementedHandlers.join(', ')}`;
      }
    }

    return unPublishable;
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
            this.scenarioDesignerService.saveScenario(this.scenario).subscribe((data) => {
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
        sentence.classification.entities = tempSentence.classification.entities as ClassifiedEntity[];
        sentence.status = SentenceStatus.validated;
        this.nlp.updateSentence(sentence).subscribe(
          (_res) => {
            task.data.intentDefinition.sentences = task.data.intentDefinition.sentences.filter((s) => s != tempSentence);
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
    if (!answerTask.data.actionDefinition.answerId) {
      return this.postNewAnswer(answerTask);
    }

    if (answerTask.data.actionDefinition.answerUpdate) {
      return this.patchAnswer(answerTask);
    }

    answerTask.done = true;
    this.processDependencies();
  }

  patchAnswer(answerTask: DependencyUpdateJob): void {
    let i18nLabel: I18nLabel = this.i18n.labels.find((i) => {
      return i._id === answerTask.data.actionDefinition.answerId;
    });

    let i18n = i18nLabel.i18n.find((i) => {
      return i.interfaceType === UserInterfaceType.textChat && i.locale === this.state.currentLocale;
    });
    i18n.label = answerTask.data.actionDefinition.answer;

    this.botService.saveI18nLabel(i18nLabel).subscribe(
      (result) => {
        delete answerTask.data.actionDefinition.answerUpdate;
        this.processAnswer(answerTask);
      },
      (error) => {
        console.log(error);
      }
    );
  }

  postNewAnswer(answerTask: DependencyUpdateJob): void {
    let request = new CreateI18nLabelRequest('scenario', answerTask.data.actionDefinition.answer, this.state.currentLocale);
    this.botService.createI18nLabel(request).subscribe(
      (answer) => {
        answerTask.data.actionDefinition.answerId = answer._id;
        this.processAnswer(answerTask);
      },
      (error) => {
        console.log(error);
      }
    );
  }

  previewTickStory(): void {
    const story = this.compileTickStory();

    const jsonPreviewerRef = this.nbDialogService.open(JsonPreviewerComponent, {
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
      (error) => {
        // Errors have occurred, let's inform the user.
        this.tickStoryErrors = error.error?.errors
          ? error.error?.errors
          : typeof error === 'string'
          ? [{ message: error }]
          : [{ message: 'An unknown error occured' }];
      }
    );
  }

  compileTickStory(): TickStory {
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

    /*
    intentsContexts: [
      {
          intentName: "oui",
          associations: [
              {
                  actionName: "toto1",
                  contextNames: [ "111" ]
              },
              {
                  actionName: "toto2",
                  contextNames: [ "222" ]
              }
          ]
      },
      {
          intentName: "bbb",
          associations: [
              {
                  actionName: "toto3",
                  contextNames: [ "333" ]
              }
          ]
      }
    ]
    */

    let tickStory: TickStory = {
      botId: this.state.currentApplication.name,
      storyId: this.scenario._scenarioGroupId,
      name: `TickStory from the scenarioGroup "${this.scenario._name}"`,
      description: `TickStory from the scenarioGroup "${this.scenario._name}" with id ${this.scenario._scenarioGroupId}`,
      mainIntent: mainIntent,
      primaryIntents: primaryIntents,
      secondaryIntents: secondaryIntents,
      contexts: this.scenario.data.contexts,
      triggers: this.scenario.data.triggers,
      actions: [],
      stateMachine: this.scenario.data.stateMachine,
      intentsContexts: intentsContexts
    };

    tickStory.actions = getScenarioActionDefinitions(this.scenario);

    return tickStory;
  }

  ngOnDestroy(): void {
    this.destroy.next();
    this.destroy.complete();
  }
}
