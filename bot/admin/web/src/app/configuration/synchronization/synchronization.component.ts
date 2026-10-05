import { Component, OnInit } from '@angular/core';
import { ApplicationService } from '../../core-nlp/applications.service';
import { Application, UserNamespace } from '../../model/application';
import { StateService } from '../../core-nlp/state.service';
import { BotConfigurationService } from '../../core/bot-configuration.service';
import { DialogService } from '../../core-nlp/dialog.service';
import { NbToastrService } from '@nebular/theme';
import { ActivatedRoute, Router } from '@angular/router';
import { CopyContext, SynchronizationConfiguration } from '../../core/model/synchronizationConfiguration';
import { ChoiceDialogComponent } from '../../shared/components';
import { TranslocoService } from '@jsverse/transloco';

@Component({
    selector: 'tock-bot-synchronization',
    templateUrl: './synchronization.component.html',
    styleUrls: ['./synchronization.component.css'],
    standalone: false
})
export class SynchronizationComponent implements OnInit {
  sourceNamespace: UserNamespace;
  targetNamespace: UserNamespace;
  sourceApplications: Application[];
  targetApplications: Application[];
  sourceApplication: Application;
  targetApplication: Application;
  shouldSynchronizeInboxMessages: boolean = false;

  constructor(
    private applicationService: ApplicationService,
    public state: StateService,
    private botConfigurationService: BotConfigurationService,
    private dialog: DialogService,
    private toastrService: NbToastrService,
    private route: ActivatedRoute,
    private router: Router,
    private transloco: TranslocoService
  ) {}

  ngOnInit(): void {
    this.sourceApplications = this.state.applications;
  }

  selectSourceNamespace(namespace: UserNamespace) {
    this.sourceNamespace = namespace;
    this.applicationService.getApplicationsByNamespace(namespace.namespace).subscribe((apps) => {
      this.sourceApplications = apps;
      this.sourceApplication = undefined;
    });
  }

  selectTargetNamespace(namespace: UserNamespace) {
    this.targetNamespace = namespace;
    this.applicationService.getApplicationsByNamespace(namespace.namespace).subscribe((apps) => {
      this.targetApplications = apps;
      this.targetApplication = undefined;
    });
  }

  copyConfiguration() {
    const inboxMessagesCopySubtitle = this.shouldSynchronizeInboxMessages
      ? this.transloco.translate('configuration.synchronization.inboxMessagesSynchronized')
      : this.transloco.translate('configuration.synchronization.inboxMessagesNotSynchronized');

    const action = this.transloco.translate('common.actions.overwrite');

    let dialogRef = this.dialog.openDialog(ChoiceDialogComponent, {
      context: {
        title: this.transloco.translate('configuration.synchronization.overwriteTitle'),
        subtitle: `${this.transloco.translate('configuration.synchronization.overwriteSubtitlePart1')}
${this.transloco.translate('configuration.synchronization.overwriteSubtitlePart2')}

${this.transloco.translate('configuration.synchronization.overwriteSubtitlePart3')} ${inboxMessagesCopySubtitle}

${this.transloco.translate('configuration.synchronization.overwriteWarning')}`,
        actions: [
          { actionName: this.transloco.translate('common.actions.cancel'), buttonStatus: 'basic', ghost: true },
          { actionName: action, buttonStatus: 'danger' }
        ],
        modalStatus: 'danger'
      }
    });
    dialogRef.onClose.subscribe((result) => {
      if (result.toLowerCase() === action.toLowerCase()) {
        let conf = new SynchronizationConfiguration(
          new CopyContext(this.sourceNamespace.namespace, this.sourceApplication.name, this.sourceApplication._id),
          new CopyContext(this.targetNamespace.namespace, this.targetApplication.name, this.targetApplication._id),
          this.shouldSynchronizeInboxMessages
        );
        this.botConfigurationService.synchronize(conf).subscribe((result) => {
          if (result) {
            this.toastrService.show(
              this.transloco.translate('configuration.synchronization.configurationCopied'),
              this.transloco.translate('configuration.synchronization.overwriteTitle'),
              { duration: 2000, status: 'success' }
            );
            this.state.resetConfiguration();
          } else {
            this.toastrService.show(
              this.transloco.translate('configuration.synchronization.copyFailed'),
              this.transloco.translate('common.messages.error'),
              { duration: 5000, status: 'danger' }
            );
          }
          this.redirect();
        });
      }
    });
  }

  private redirect() {
    let redirect = '../../';
    this.router.navigate([redirect], { relativeTo: this.route });
  }
}
