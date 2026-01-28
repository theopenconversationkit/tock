import { Component, EventEmitter, Input, OnDestroy, OnInit, Output } from '@angular/core';
import { filter, Subject, take, takeUntil } from 'rxjs';
import { ActionReport, DialogReport, Sentence } from '../../../model/dialog-data';
import { StateService } from '../../../../core-nlp/state.service';
import { BotConfigurationService } from '../../../../core/bot-configuration.service';
import { BotApplicationConfiguration } from '../../../../core/model/configuration';
import { getDialogMessageUserAvatar, getDialogMessageUserQualifier } from '../../../utils';
import { EvaluationStatus } from '../../../../quality/samples/models';
import { NbMenuBag, NbMenuItem, NbMenuService } from '@nebular/theme';
import { ResponseIssueReasons } from '../../../model/response-issue';

@Component({
  selector: 'tock-chat-ui-dialog-evaluator',

  templateUrl: './chat-ui-dialog-evaluator.component.html',
  styleUrl: './chat-ui-dialog-evaluator.component.scss'
})
export class ChatUiDialogEvaluatorComponent implements OnInit, OnDestroy {
  private readonly destroy$: Subject<boolean> = new Subject();

  @Input() dialog: DialogReport;

  @Output() onActionEvaluation = new EventEmitter<any>();

  allConfigurations: BotApplicationConfiguration[];

  evaluationDirection = EvaluationStatus;

  reasonItems: NbMenuItem[] = [];

  isVisible: boolean = true;
  visibilityManualControl: boolean = false;

  constructor(public state: StateService, private botConfiguration: BotConfigurationService, private nbMenuService: NbMenuService) {}

  switchVisibility() {
    if (!this.isDialogEvaluated()) return;
    this.visibilityManualControl = true;
    this.isVisible = !this.isVisible;
  }

  ngOnInit() {
    this.reasonItems = ResponseIssueReasons.map((e) => {
      return { title: e.label, data: e.value };
    });

    this.botConfiguration.configurations.pipe(take(1)).subscribe((conf) => {
      this.allConfigurations = conf;
    });

    this.nbMenuService
      .onItemClick()
      .pipe(
        takeUntil(this.destroy$),
        filter(({ tag }) => tag && tag.startsWith('actionEvaluation_' + this.dialog.id + '_'))
      )
      .subscribe((menuBag: NbMenuBag) => {
        this.evaluateActionNok(menuBag);
      });

    if (this.isDialogEvaluated()) {
      this.isVisible = false;
    }
  }

  getUserName(action: ActionReport): string {
    return getDialogMessageUserQualifier(action.isBot());
  }

  getUserAvatar(action: ActionReport): string {
    return getDialogMessageUserAvatar(action.isBot());
  }

  dialogConnector() {
    if (!this.allConfigurations) return;
    const firstAction = this.dialog.actions.find((action) => action.applicationId);
    if (firstAction) {
      const applicationId = firstAction.applicationId;
      if (applicationId) {
        const configuration = this.allConfigurations.find((conf) => conf.applicationId === applicationId);
        if (configuration) {
          return configuration;
        }
      }
    }
  }

  getDialogConnectorLabel() {
    const configuration = this.dialogConnector();
    if (configuration) {
      return configuration.connectorType.label();
    }
  }

  getDialogConnectorIconUrl() {
    const configuration = this.dialogConnector();
    if (configuration) {
      return configuration.connectorType.iconUrl();
    }
  }

  getDialogConfigurationDetail() {
    const configuration = this.dialogConnector();
    if (configuration) {
      return `${configuration.name} > ${configuration.connectorType.label()} (${configuration.applicationId})`;
    }
    return 'na';
  }

  normalizeLocaleCode(code: string): string {
    return StateService.normalizeLocaleCode(code);
  }

  nbUserQuestions(): number {
    return this.dialog.actions.filter((action) => !action.isBot()).length;
  }

  nbBotAnswers(): number {
    return this.dialog.actions.filter((action) => action.isBotAnswerWithContent()).length;
  }

  nbAssessableBotAnswers(): number {
    return this.dialog.actions.filter((action) => action.isBotAnswerWithContent() && action._evaluation).length;
  }

  nbEvaluatedBotAnswers(): number {
    return this.dialog.actions.filter(
      (action) => action.isBotAnswerWithContent() && action._evaluation && action._evaluation.status !== EvaluationStatus.UNSET
    ).length;
  }

  isDialogEvaluated(): boolean {
    const nbAssessableBotAnswers = this.nbAssessableBotAnswers();
    return nbAssessableBotAnswers > 0 && nbAssessableBotAnswers === this.nbEvaluatedBotAnswers();
  }

  nbRagAnswers(): number {
    return this.dialog.actions.filter((action) => action.isBot() && action.metadata?.isGenAiRagAnswer).length;
  }

  getActionEvaluationReason(action: ActionReport) {
    return ResponseIssueReasons.find((r) => r.value === action._evaluation.reason)?.label;
  }

  evaluateActionOk(action: ActionReport): void {
    if (action._evaluation.status === EvaluationStatus.UP) {
      return;
    }

    this.onActionEvaluation.emit({
      action,
      evaluation: EvaluationStatus.UP
    });

    if (this.isDialogEvaluated() && !this.visibilityManualControl) this.isVisible = false;
  }

  evaluateActionNok(menuBag: NbMenuBag): void {
    const actionId = menuBag.tag.split('_')[2];
    const action = this.dialog.actions.find((a) => a.id === actionId);
    if (!action) {
      return;
    }

    this.onActionEvaluation.emit({
      action,
      evaluation: EvaluationStatus.DOWN,
      reason: menuBag.item.data
    });

    if (this.isDialogEvaluated() && !this.visibilityManualControl) this.isVisible = false;
  }

  ngOnDestroy(): void {
    this.destroy$.next(true);
    this.destroy$.complete();
  }
}
