import { Component, ElementRef, HostListener, Inject, Injectable, OnDestroy, OnInit, ViewChild } from '@angular/core';
import { Subject } from 'rxjs';
import { takeUntil, take, distinctUntilChanged } from 'rxjs/operators';
import {
  ScenarioVersion,
  ScenarioVersionExtended,
  SCENARIO_ITEM_FROM_BOT,
  SCENARIO_ITEM_FROM_CLIENT,
  SCENARIO_MODE,
  SCENARIO_STATE
} from '../models/scenario.model';
import { ScenarioService } from '../services/scenario.service';
import { ActivatedRoute, CanDeactivate } from '@angular/router';
import { DialogService } from 'src/app/core-nlp/dialog.service';
import { ConfirmDialogComponent } from 'src/app/shared-nlp/confirm-dialog/confirm-dialog.component';
import { NbToastrService } from '@nebular/theme';
import { StateService } from 'src/app/core-nlp/state.service';
import { ScenarioDesignerService } from './scenario-designer.service';

import { deepCopy, stringifiedCleanObject } from '../commons/utils';
import { ChoiceDialogComponent } from '../../shared/components';

import { Intent } from '../../model/nlp';
import { BotService } from '../../bot/bot-service';
import { I18nLabels } from '../../bot/model/i18n';
import { isEqual } from 'lodash-es';
import { DOCUMENT } from '@angular/common';

@Component({
  selector: 'scenario-designer',
  templateUrl: './scenario-designer.component.html',
  styleUrls: ['./scenario-designer.component.scss']
})
@Injectable()
export class ScenarioDesignerComponent implements OnInit, OnDestroy {
  destroy = new Subject();
  @ViewChild('canvasWrapperElem') canvasWrapperElem: ElementRef;
  @ViewChild('canvasElem') canvasElem: ElementRef;

  scenarioVersion: ScenarioVersionExtended;
  scenarioVersionBackup: string;
  isReadonly: boolean = false;
  i18n: I18nLabels;
  avalaibleHandlers: string[];
  initialDependenciesCheckDone: boolean = false;

  readonly SCENARIO_MODE = SCENARIO_MODE;
  readonly SCENARIO_ITEM_FROM_CLIENT = SCENARIO_ITEM_FROM_CLIENT;
  readonly SCENARIO_ITEM_FROM_BOT = SCENARIO_ITEM_FROM_BOT;

  constructor(
    private scenarioService: ScenarioService,
    private route: ActivatedRoute,
    private toastrService: NbToastrService,
    protected state: StateService,
    private scenarioDesignerService: ScenarioDesignerService,
    private dialogService: DialogService,
    private botService: BotService,
    @Inject(DOCUMENT) private document: Document
  ) {
    this.scenarioDesignerService.scenarioDesignerCommunication.pipe(takeUntil(this.destroy)).subscribe((evt) => {
      if (evt.type == 'updateScenarioBackup') this.updateScenarioBackup(evt.data);
    });
  }

  ngOnInit(): void {
    this.route.params.pipe(takeUntil(this.destroy)).subscribe((routeParams) => {
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

          if (!this.scenarioVersion.data) this.scenarioVersion.data = { mode: SCENARIO_MODE.writing, scenarioItems: [], contexts: [] };
          if (typeof this.scenarioVersion.data.mode == 'undefined') this.scenarioVersion.data.mode = SCENARIO_MODE.writing;

          this.switchMode(this.scenarioVersion.data.mode || SCENARIO_MODE.writing);

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

          // backward compatibility update
          this.scenarioVersion.data.scenarioItems.forEach((item) => {
            if (item['tickActionDefinition']) {
              item.actionDefinition = item['tickActionDefinition'];
              delete item['tickActionDefinition'];
            }
          });
          // backward compatibility update

          this.botService
            .i18nLabels()
            .pipe(take(1))
            .subscribe((results) => {
              this.i18n = results;
              this.checkDependencies();
            });

          this.updateScenarioBackup(this.scenarioVersion);
        });
    });

    this.scenarioService.getActionHandlers().subscribe((handlers) => {
      this.avalaibleHandlers = handlers;
    });

    this.state.configurationChange.pipe(takeUntil(this.destroy)).subscribe((_) => {
      this.exit();
    });

    this.document.getElementById('app-layout-footer').style.display = 'none';
  }

  checkDependencies() {
    let deletedIntents = [];
    let deletedAnswers = [];
    this.scenarioVersion.data.scenarioItems.forEach((item) => {
      if (item.intentDefinition?.intentId) {
        const existingIntent: Intent = this.state.findIntentById(item.intentDefinition.intentId);
        if (!existingIntent) {
          // The intent has been removed. We delete the lapsed intentId
          delete item.intentDefinition.intentId;
          deletedIntents.push(item.intentDefinition.label || item.intentDefinition.name);
        } else {
          this.scenarioDesignerService.grabIntentSentences(item);
        }
      }

      if (item.actionDefinition?.answerId) {
        let existingAnswer = this.i18n.labels.find((ans) => {
          return ans._id === item.actionDefinition.answerId;
        });
        if (!existingAnswer) {
          // The answer has been removed. We delete the lapsed answerId
          delete item.actionDefinition.answerId;
          deletedAnswers.push(item.actionDefinition.answer);
        }
      }
    });

    if (deletedIntents.length) {
      let title = 'Intents deleted';
      let subtitle = 'The following intents have been removed:';
      this.dialogService.openDialog(ChoiceDialogComponent, {
        context: {
          modalStatus: 'warning',
          title: title,
          subtitle: subtitle,
          list: deletedIntents,
          actions: [{ actionName: 'Ok', buttonStatus: 'basic' }]
        }
      });
    }

    if (deletedAnswers.length) {
      let title = 'Answers deleted';
      let subtitle = 'The following answers have been removed:';
      this.dialogService.openDialog(ChoiceDialogComponent, {
        context: {
          modalStatus: 'warning',
          title: title,
          subtitle: subtitle,
          list: deletedAnswers,
          actions: [{ actionName: 'Ok', buttonStatus: 'basic' }]
        }
      });
    }

    this.initialDependenciesCheckDone = true;
  }

  informScenarioNotFound() {
    const modal = this.dialogService.openDialog(ChoiceDialogComponent, {
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

    this.scenarioDesignerService.saveScenario(this.scenarioVersion).subscribe((data) => {
      if (!silent) {
        this.toastrService.success(`Scenario successfully saved`, 'Success', {
          duration: 5000,
          status: 'success'
        });
      }

      if (exit) this.exit();
    });
  }

  updateScenarioBackup(scenario: ScenarioVersion): void {
    let backup = JSON.parse(stringifiedCleanObject(scenario));
    delete backup.creationDate;
    delete backup.updateDate;
    this.scenarioVersionBackup = JSON.stringify(backup);
  }

  exit(): void {
    this.scenarioDesignerService.exitDesigner();
  }

  ngOnDestroy(): void {
    this.document.getElementById('app-layout-footer').style.display = 'initial';
    this.destroy.next();
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
export class ScenarioDesignerNavigationGuard implements CanDeactivate<any> {
  constructor(private dialogService: DialogService) {}

  canDeactivate(component: any) {
    const canDeactivate = component.canDeactivate();

    if (!canDeactivate) {
      const subject = new Subject<boolean>();
      const dialogResponseVerb = 'Exit';
      const modal = this.dialogService.openDialog(ConfirmDialogComponent, {
        context: {
          title: `You're about to leave without saving the changes`,
          subtitle: 'Are you sure?',
          action: dialogResponseVerb
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
