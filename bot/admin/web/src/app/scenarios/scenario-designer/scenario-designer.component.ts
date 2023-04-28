import { DOCUMENT } from '@angular/common';
import { Component, ElementRef, HostListener, Inject, Injectable, OnDestroy, OnInit, Renderer2, ViewChild } from '@angular/core';
import { ActivatedRoute, CanDeactivate, Params } from '@angular/router';
import { NbDialogService, NbToastrService } from '@nebular/theme';
import { isEqual } from 'lodash-es';
import { Observable, Subject } from 'rxjs';
import { takeUntil, take, distinctUntilChanged } from 'rxjs/operators';

import {
  ScenarioItem,
  ScenarioVersion,
  ScenarioVersionExtended,
  SCENARIO_ITEM_FROM_BOT,
  SCENARIO_ITEM_FROM_CLIENT,
  SCENARIO_MODE,
  SCENARIO_STATE
} from '../models/scenario.model';
import { ScenarioService } from '../services';
import { StateService } from '../../core-nlp/state.service';
import { ScenarioDesignerService } from './scenario-designer.service';
import { stringifiedCleanObject } from '../commons/utils';
import { ChoiceDialogComponent } from '../../shared/components';
import { Intent } from '../../model/nlp';
import { BotService } from '../../bot/bot-service';
import { I18nLabels } from '../../bot/model/i18n';
import { FullscreenDirective } from '../../shared/directives';
import { Handler } from '../models';
import { deepCopy } from '../../shared/utils';
import { UserInterfaceType } from '../../core/model/configuration';
import { StoryDefinitionConfigurationSummary, StorySearchQuery } from '../../bot/model/story';

@Component({
  selector: 'tock-scenario-designer',
  templateUrl: './scenario-designer.component.html',
  styleUrls: ['./scenario-designer.component.scss']
})
export class ScenarioDesignerComponent implements OnInit, OnDestroy {
  destroy = new Subject();
  @ViewChild('canvasWrapperElem') canvasWrapperElem: ElementRef;
  @ViewChild('canvasElem') canvasElem: ElementRef;
  @ViewChild('fullscreen') fullscreenElem: FullscreenDirective;

  scenarioVersion: ScenarioVersionExtended;
  scenarioVersionBackup: string;
  isReadonly: boolean = false;
  i18n: I18nLabels;
  avalaibleHandlers: Handler[];
  availableStories: StoryDefinitionConfigurationSummary[];
  initialDependenciesCheckDone: boolean = false;

  private footer: HTMLElement;

  readonly SCENARIO_MODE = SCENARIO_MODE;
  readonly SCENARIO_ITEM_FROM_CLIENT = SCENARIO_ITEM_FROM_CLIENT;
  readonly SCENARIO_ITEM_FROM_BOT = SCENARIO_ITEM_FROM_BOT;

  constructor(
    private scenarioService: ScenarioService,
    private route: ActivatedRoute,
    private toastrService: NbToastrService,
    private state: StateService,
    private scenarioDesignerService: ScenarioDesignerService,
    private nbDialogService: NbDialogService,
    private botService: BotService,
    private renderer: Renderer2,
    @Inject(DOCUMENT) private document: Document
  ) {
    this.scenarioDesignerService.scenarioDesignerCommunication.pipe(takeUntil(this.destroy)).subscribe((evt) => {
      if (evt.type == 'updateScenarioBackup') this.updateScenarioBackup(evt.data);
    });
  }

  ngOnInit(): void {
    this.hideFooter();

    this.state.configurationChange.pipe(takeUntil(this.destroy)).subscribe((_) => {
      this.exit();
    });

    this.loadAvalaibleHandlers();
    this.loadAvailableStories();

    this.route.params.pipe(takeUntil(this.destroy)).subscribe((routeParams) => {
      this.loadAndInitScenario(routeParams);
    });
  }

  private loadAndInitScenario(routeParams: Params): void {
    this.scenarioService
      .getScenarioVersion(routeParams.scenarioGroupId, routeParams.scenarioVersionId)
      .pipe(
        distinctUntilChanged((a, b) => isEqual(a, b)),
        takeUntil(this.destroy)
      )
      .subscribe((scenarioVersionExt: ScenarioVersionExtended) => {
        if (scenarioVersionExt === null) {
          return this.informScenarioNotFound();
        }

        this.scenarioVersion = deepCopy(scenarioVersionExt);

        this.isReadonly = this.scenarioVersion.state !== SCENARIO_STATE.draft;

        this.normalizeScenario();

        this.downwardCompatibilityManagement();

        this.switchMode(this.scenarioVersion.data.mode || SCENARIO_MODE.writing);

        const allAnswersIds = this.getAllAnswersIds();
        if (allAnswersIds.length) {
          this.botService
            .searchI18nLabels(allAnswersIds)
            .pipe(take(1))
            .subscribe((results) => {
              this.i18n = results;
              this.checkDependencies();
            });
        } else {
          this.i18n = { labels: [], localeBase: 'en' };
          this.checkDependencies();
        }

        this.updateScenarioBackup(this.scenarioVersion);
      });
  }

  private normalizeScenario(): void {
    if (!this.scenarioVersion.data) this.scenarioVersion.data = { mode: SCENARIO_MODE.writing, scenarioItems: [], contexts: [] };
    if (typeof this.scenarioVersion.data.mode == 'undefined') this.scenarioVersion.data.mode = SCENARIO_MODE.writing;

    if (!this.scenarioVersion.data.scenarioItems.length) {
      this.scenarioVersion.data.scenarioItems.push({
        id: 0,
        from: SCENARIO_ITEM_FROM_CLIENT,
        text: '',
        main: true
      });
    }
    if (!this.scenarioVersion.data.contexts) {
      this.scenarioVersion.data.contexts = [];
    }
    if (!this.scenarioVersion.data.triggers) {
      this.scenarioVersion.data.triggers = [];
    }
  }

  private downwardCompatibilityManagement(): void {
    this.scenarioVersion.data.scenarioItems.forEach((item) => {
      if (item['tickActionDefinition']) {
        item.actionDefinition = item['tickActionDefinition'];
        delete item['tickActionDefinition'];
      }

      if (item.actionDefinition) {
        // backwards compatibility management for answers before internationalization
        if (item.actionDefinition['answer']) {
          if (!item.actionDefinition.answers) {
            item.actionDefinition.answers = [];
          }
          item.actionDefinition.answers.push({
            answer: item.actionDefinition['answer'],
            interfaceType: UserInterfaceType.textChat,
            locale: this.state.currentLocale
          });
          delete item.actionDefinition['answer'];
        }

        // management of answers locales defined without interfaceType
        item.actionDefinition.answers?.forEach((scenarioAnswer) => {
          if (!scenarioAnswer.interfaceType) scenarioAnswer.interfaceType = UserInterfaceType.textChat;
        });
        item.actionDefinition.unknownAnswers?.forEach((scenarioAnswer) => {
          if (!scenarioAnswer.interfaceType) scenarioAnswer.interfaceType = UserInterfaceType.textChat;
        });
        // deduplication if necessary
        if (item.actionDefinition.answers) {
          item.actionDefinition.answers = item.actionDefinition.answers.filter(
            (value, index, self) => index === self.findIndex((t) => t.locale === value.locale && t.interfaceType === value.interfaceType)
          );
        }
        if (item.actionDefinition.unknownAnswers) {
          item.actionDefinition.unknownAnswers = item.actionDefinition.unknownAnswers.filter(
            (value, index, self) => index === self.findIndex((t) => t.locale === value.locale && t.interfaceType === value.interfaceType)
          );
        }
      }
    });
  }

  private loadAvalaibleHandlers(): void {
    this.scenarioService.getActionHandlers().subscribe((handlers) => {
      this.avalaibleHandlers = handlers;
    });
  }

  private loadAvailableStories(): void {
    this.botService
      .searchStories(
        new StorySearchQuery(
          this.state.currentApplication.namespace,
          this.state.currentApplication.name,
          this.state.currentLocale,
          0,
          10000,
          undefined,
          undefined,
          false
        )
      )
      .pipe(take(1))
      .subscribe((stories: StoryDefinitionConfigurationSummary[]) => {
        this.availableStories = stories;
      });
  }

  private getAllAnswersIds(): string[] {
    let answersIds = new Set<string>();
    this.scenarioVersion.data.scenarioItems.forEach((item) => {
      if (item.actionDefinition?.answerId) {
        answersIds.add(item.actionDefinition.answerId);
      }
      if (item.actionDefinition?.unknownAnswerId) {
        answersIds.add(item.actionDefinition.unknownAnswerId);
      }
    });
    return [...answersIds];
  }

  private checkDependencies(): void {
    let deletedIntents = [];
    let deletedAnswers = [];
    this.scenarioVersion.data.scenarioItems.forEach((item) => {
      if (item.intentDefinition?.intentId) {
        const existingIntent: Intent = this.state.findSharedNamespaceIntentById(item.intentDefinition.intentId);
        if (!existingIntent) {
          // The intent has been removed. We delete the lapsed intentId
          delete item.intentDefinition.intentId;
          deletedIntents.push(item.intentDefinition.label || item.intentDefinition.name);
        } else {
          this.scenarioDesignerService.grabIntentSentences(item);
        }
      }

      if (item.actionDefinition?.answerId) {
        this.reflectExternalChangesInAnswer(item, false, deletedAnswers);
      }
      if (item.actionDefinition?.unknownAnswerId) {
        this.reflectExternalChangesInAnswer(item, true, deletedAnswers);
      }
    });

    if (deletedIntents.length) {
      this.nbDialogService.open(ChoiceDialogComponent, {
        context: {
          modalStatus: 'warning',
          title: 'Intents deleted',
          subtitle: 'The following intents have been removed:',
          list: deletedIntents,
          actions: [{ actionName: 'Ok', buttonStatus: 'basic', ghost: true }]
        }
      });
    }

    if (deletedAnswers.length) {
      this.nbDialogService.open(ChoiceDialogComponent, {
        context: {
          modalStatus: 'warning',
          title: 'Answers deleted',
          subtitle: 'The following answers have been removed:',
          list: deletedAnswers,
          actions: [{ actionName: 'Ok', buttonStatus: 'basic', ghost: true }]
        }
      });
    }

    this.initialDependenciesCheckDone = true;
  }

  private reflectExternalChangesInAnswer(item: ScenarioItem, unknownAnswer: boolean, deletedAnswersList: string[]): void {
    let storedAnswerId = item.actionDefinition.answerId;
    if (unknownAnswer) {
      storedAnswerId = item.actionDefinition.unknownAnswerId;
    }

    let answersArray = item.actionDefinition.answers;
    if (unknownAnswer) {
      answersArray = item.actionDefinition.unknownAnswers;
    }

    let existingI18nLabel = this.i18n.labels.find((ans) => {
      return ans._id === storedAnswerId;
    });

    if (!existingI18nLabel) {
      // The answer has been removed. We delete the lapsed answer id
      if (unknownAnswer) {
        delete item.actionDefinition.unknownAnswerId;
      } else {
        delete item.actionDefinition.answerId;
      }

      // We retrieve the wording of the answer (if any) to inform the user
      let scenarioAnswer = answersArray.find((sa) => sa.locale === this.state.currentLocale);
      if (!scenarioAnswer?.answer) {
        scenarioAnswer = answersArray.find((sa) => sa.answer);
      }
      if (scenarioAnswer) deletedAnswersList.push(scenarioAnswer.answer);
    } else {
      // We update the answers of the item to reflect any changes made from outside the designer
      existingI18nLabel.i18n.forEach((i18n) => {
        const existingLabelLocale = answersArray.find((la) => la.locale === i18n.locale && la.interfaceType === i18n.interfaceType);
        // We update thee locale answer with the version stored on database (if any) if the locale answer has not been modifief inside the designer
        if (existingLabelLocale) {
          if (!existingLabelLocale.answerUpdate) existingLabelLocale.answer = i18n.label;
        } else {
          answersArray.push({
            locale: i18n.locale,
            interfaceType: i18n.interfaceType,
            answer: i18n.label
          });
        }
      });
    }
  }

  private informScenarioNotFound(): void {
    const modal = this.nbDialogService.open(ChoiceDialogComponent, {
      context: {
        title: `No scenario found`,
        subtitle: 'No scenario with this identifier was found',
        actions: [{ actionName: 'Ok', buttonStatus: 'basic' }]
      }
    });
    modal.onClose.subscribe((res) => {
      this.exit();
    });
  }

  switchMode(mode: SCENARIO_MODE): void {
    this.scenarioVersion.data.mode = mode;
  }

  save(exit: boolean = false, silent: boolean = false): void {
    if (this.isReadonly) return;

    this.scenarioDesignerService.saveScenario(this.scenarioVersion).subscribe((_data) => {
      if (!silent) {
        this.toastrService.success(`Scenario successfully saved`, 'Success', {
          duration: 5000
        });
      }

      if (exit) this.exit();
    });
  }

  private updateScenarioBackup(scenario: ScenarioVersion): void {
    const backup = JSON.parse(stringifiedCleanObject(scenario));
    delete backup.creationDate;
    delete backup.updateDate;
    this.scenarioVersionBackup = JSON.stringify(backup);
  }

  exit(): void {
    if (this.document.fullscreenElement) {
      this.fullscreenElem.close();
    }
    this.scenarioDesignerService.exitDesigner();
  }

  private hideFooter(): void {
    this.footer = this.document.getElementById('app-layout-footer');
    if (this.footer) {
      this.renderer.setStyle(this.footer, 'display', 'none');
    }
  }

  private showFooter(): void {
    if (this.footer) {
      this.renderer.setStyle(this.footer, 'display', 'initial');
    }
  }

  ngOnDestroy(): void {
    this.showFooter();
    this.destroy.next(true);
    this.destroy.complete();
  }

  @HostListener('window:beforeunload', ['$event'])
  unloadNotification($event: any): void {
    if (!this.canDeactivate()) {
      $event.returnValue = true;
    }
  }

  canDeactivate(): boolean {
    if (this.isReadonly) return true;
    if (this.scenarioVersion) {
      const current = JSON.parse(stringifiedCleanObject(this.scenarioVersion));
      delete current.creationDate;
      delete current.updateDate;
      return isEqual(JSON.parse(this.scenarioVersionBackup), current);
    }
    return true;
  }
}

@Injectable()
export class ScenarioDesignerNavigationGuard implements CanDeactivate<ScenarioDesignerComponent> {
  constructor(private nbDialogService: NbDialogService) {}

  canDeactivate(component: ScenarioDesignerComponent): boolean | Observable<boolean> {
    const canDeactivate = component.canDeactivate();

    if (!canDeactivate) {
      const subject = new Subject<boolean>();
      const dialogResponseVerb = 'Exit';
      const modal = this.nbDialogService.open(ChoiceDialogComponent, {
        context: {
          title: `You're about to leave without saving the changes`,
          subtitle: 'Are you sure?',
          actions: [
            { actionName: 'cancel', buttonStatus: 'basic', ghost: true },
            { actionName: dialogResponseVerb, buttonStatus: 'danger' }
          ],
          modalStatus: 'danger'
        }
      });
      modal.onClose.subscribe((res) => {
        subject.next(res == dialogResponseVerb.toLowerCase());
      });

      return subject.asObservable();
    }
    return true;
  }
}
