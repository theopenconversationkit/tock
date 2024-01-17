
import {Component, OnInit} from "@angular/core";
import {ApplicationService} from "../../core-nlp/applications.service";
import {Application, UserNamespace} from "../../model/application";
import {AuthService} from "../../core-nlp/auth/auth.service";
import {StateService} from "../../core-nlp/state.service";
import {BotConfigurationService} from "../../core/bot-configuration.service";
import {ConfirmDialogComponent} from "../../shared-nlp/confirm-dialog/confirm-dialog.component";
import {DialogService} from "../../core-nlp/dialog.service";
import {NbToastrService} from "@nebular/theme";
import {ActivatedRoute, Router} from "@angular/router";
import {CopyContext, SynchronizationConfiguration} from "../../core/model/synchronizationConfiguration";

@Component({
  selector: 'tock-bot-synchronization',
  templateUrl: './synchronization.component.html',
  styleUrls: ['./synchronization.component.css']
})
export class SynchronizationComponent implements OnInit{
  namespaces: UserNamespace[];
  sourceNamespace: UserNamespace;
  targetNamespace: UserNamespace;
  sourceApplications: Application[];
  targetApplications: Application[];
  sourceApplication: Application;
  targetApplication: Application;
  shouldSynchronizeInboxMessages: boolean = false;

  constructor(
    private applicationService: ApplicationService,
    private authService: AuthService,
    public state: StateService,
    private botConfigurationService: BotConfigurationService,
    private dialog: DialogService,
    private toastrService: NbToastrService,
    private route: ActivatedRoute,
    private router: Router
  ) {}
  ngOnInit(): void {
    this.applicationService.getNamespaces().subscribe((n) => (this.namespaces = n));
    this.sourceApplications = this.state.applications
  }

  selectSourceNamespace(namespace: UserNamespace) {
    this.sourceNamespace = namespace
    this.applicationService.getApplicationsByNamespace(namespace.namespace).subscribe(
      (apps) => {
        this.sourceApplications = apps
        this.sourceApplication = undefined
      })
  }

  selectTargetNamespace(namespace: UserNamespace) {
    this.targetNamespace = namespace
    this.applicationService.getApplicationsByNamespace(namespace.namespace).subscribe(
      (apps) => {
        this.targetApplications = apps
        this.targetApplication = undefined
      })
  }

  copyConfiguration() {
    const inboxMessagesCopySubtitle = this.shouldSynchronizeInboxMessages
      ? 'Inbox messages will be synchronized.'
      : 'Inbox messages will <b>NOT</b> be synchronized by default.<br/>If you want to, just check the "Copy inbox messages" checkbox on the duplication form.';

    let dialogRef = this.dialog.openDialog(ConfirmDialogComponent, {
      context: {
        title: 'Overwrite configuration?',
        subtitle: `
          <p>During synchronization, configuration will be copied from the source application to the target application (answers, stories, training).</p>
          <p><b>Please note : </b>${inboxMessagesCopySubtitle}</p>
          <p>The synchronization of both applications will be permanent, and there will be no way to reverse it. Do you want to continue?</p>
        `,
        action: 'Overwrite'
      }
    });
    dialogRef.onClose.subscribe((result) => {
      if (result === 'overwrite') {
        let conf = new SynchronizationConfiguration(
          new CopyContext(this.sourceNamespace.namespace, this.sourceApplication.name, this.sourceApplication._id),
          new CopyContext(this.targetNamespace.namespace, this.targetApplication.name, this.targetApplication._id),
          this.shouldSynchronizeInboxMessages
        )
        this.botConfigurationService.synchronize(conf).subscribe((result) => {
          if (result) {
            this.toastrService.show(`Configuration has been copied`, 'Overwrite configuration', {
              duration: 2000,
              status: 'success'
            });
            this.state.resetConfiguration();
          } else {
            this.toastrService.show(`Copy configuration failed`, 'Error', {
              duration: 5000,
              status: 'danger'
            });
          }
          this.redirect();
        });
      }
    })}

  private redirect() {
    let redirect = '../../';
    this.router.navigate([redirect], { relativeTo: this.route });
  }

}
