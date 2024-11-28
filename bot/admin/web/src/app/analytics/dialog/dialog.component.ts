import { Component, OnDestroy, OnInit } from '@angular/core';
import { ActivatedRoute, Router } from '@angular/router';
import { Subject, take, takeUntil } from 'rxjs';
import { AnalyticsService } from '../analytics.service';
import { StateService } from '../../core-nlp/state.service';
import { BotConfigurationService } from '../../core/bot-configuration.service';
import { BotApplicationConfiguration } from '../../core/model/configuration';
import { ActionReport, Debug, DialogReport, Sentence, SentenceWithFootnotes } from '../../shared/model/dialog-data';
import { ApplicationService } from '../../core-nlp/applications.service';
import { AuthService } from '../../core-nlp/auth/auth.service';
import { SettingsService } from '../../core-nlp/settings.service';
import { getDialogMessageUserAvatar, getDialogMessageUserQualifier } from '../../shared/utils';

@Component({
  selector: 'tock-dialog',
  templateUrl: './dialog.component.html',
  styleUrl: './dialog.component.scss'
})
export class DialogComponent implements OnInit, OnDestroy {
  destroy = new Subject();

  dialog: DialogReport;

  accessDenied: boolean = false;

  constructor(
    private route: ActivatedRoute,
    private analytics: AnalyticsService,
    private state: StateService,
    private applicationService: ApplicationService,
    public auth: AuthService,
    public settings: SettingsService,
    private router: Router
  ) {}

  ngOnInit(): void {
    this.initialize();
  }

  initialize(): void {
    this.route.params.pipe(take(1)).subscribe((routeParams) => {
      // User does not have rights to access this namespace
      if (!this.state.namespaces.find((ns) => ns.namespace === routeParams.namespace)) {
        this.accessDenied = true;

        return;
      }

      // The current application does not belong to the requested namespace, so we move to the target namespace.
      if (routeParams.namespace !== this.state.currentApplication.namespace) {
        this.applicationService
          .selectNamespace(routeParams.namespace)
          .pipe(take(1))
          .subscribe((_) => {
            this.auth.loadUser().subscribe((_) => {
              this.state.currentApplicationEmitter.pipe(take(1)).subscribe((arg) => {
                this.initialize();
              });
              this.applicationService.resetConfiguration();
            });
          });

        return;
      }

      // The current application is not the requested one, we move to the targeted application.
      if (routeParams.applicationId !== this.state.currentApplication._id) {
        const targetApp = this.state.applications.find((app) => app._id === routeParams.applicationId);
        if (targetApp) {
          this.state.currentApplicationEmitter.pipe(take(1)).subscribe((arg) => {
            this.initialize();
          });
          this.state.changeApplicationWithName(targetApp.name);
        } else {
          this.accessDenied = true;
        }

        return;
      }

      this.analytics
        .dialog(this.state.currentApplication._id, routeParams.dialogId)
        .pipe(take(1))
        .subscribe((dialog) => {
          if (dialog?.actions?.length) {
            this.dialog = dialog;
          } else {
            this.accessDenied = true;
          }
        });

      // We monitor changes in the current application to redirect to the dialogs page in case of change.
      this.state.currentApplicationEmitter.pipe(takeUntil(this.destroy)).subscribe((arg) => {
        if (
          routeParams.namespace !== this.state.currentApplication.namespace ||
          routeParams.applicationId !== this.state.currentApplication._id
        ) {
          this.jumpToDialogs();
        }
      });
    });
  }

  getUserName(action: ActionReport): string {
    return getDialogMessageUserQualifier(action.isBot());
  }

  getUserAvatar(action: ActionReport): string {
    return getDialogMessageUserAvatar(action.isBot());
  }

  createFaq(action: ActionReport, actionsStack: ActionReport[]): void {
    const actionIndex = actionsStack.findIndex((act) => act === action);
    if (actionIndex > 0) {
      const answerSentence = action.message as unknown as SentenceWithFootnotes;
      const answer = answerSentence.text;

      let question;
      const questionAction = actionsStack[actionIndex - 1];

      if (questionAction.message.isDebug()) {
        const actionDebug = questionAction.message as unknown as Debug;
        question = actionDebug.data.condense_question || actionDebug.data.user_question;
      } else if (!questionAction.isBot()) {
        const questionSentence = questionAction.message as unknown as Sentence;
        question = questionSentence.text;
      }

      if (question && answer) {
        this.router.navigate(['faq/management'], { state: { question, answer } });
      }
    }
  }

  jumpToDialogs(addAnchorRef: boolean = false): void {
    const extras = addAnchorRef && this.dialog?.id ? { state: { dialogId: this.dialog.id } } : undefined;
    this.router.navigateByUrl('/analytics/dialogs', extras);
  }

  ngOnDestroy(): void {
    this.destroy.next(true);
    this.destroy.complete();
  }
}
