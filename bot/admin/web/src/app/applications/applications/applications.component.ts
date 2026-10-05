import { saveAs } from 'file-saver-es';
import { Component, OnInit } from '@angular/core';
import { Application } from '../../model/application';
import { StateService } from '../../core-nlp/state.service';
import { ApplicationService } from '../../core-nlp/applications.service';
import { UserRole } from '../../model/auth';
import { NbDialogService, NbToastrService } from '@nebular/theme';
import { ApplicationUploadComponent } from '../application-upload/application-upload.component';
import { ChoiceDialogComponent } from '../../shared/components';
import { getExportFileName } from '../../shared/utils';
import { TranslocoService } from '@jsverse/transloco';

@Component({
    selector: 'tock-applications',
    templateUrl: 'applications.component.html',
    styleUrls: ['applications.component.scss'],
    standalone: false
})
export class ApplicationsComponent implements OnInit {
  UserRole = UserRole;
  loading: boolean = false;

  constructor(
    private toastrService: NbToastrService,
    private nbDialogService: NbDialogService,
    public state: StateService,
    private applicationService: ApplicationService,
    private transloco: TranslocoService
  ) {}

  ngOnInit(): void {
    this.state.sortApplications();
  }

  isAdmin(): boolean {
    return this.state.hasRole(UserRole.admin);
  }

  selectApplication(app: Application): void {
    this.state.changeApplication(app);
    this.toastrService.show(
      this.transloco.translate('applications.applications.applicationSelected', { name: app.name }),
      this.transloco.translate('applications.applications.selectionTitle'),
      { duration: 2000 }
    );
  }

  deleteApplication(application: Application): void {
    const action = this.transloco.translate('applications.applications.deleteAction');
    const dialogRef = this.nbDialogService.open(ChoiceDialogComponent, {
      context: {
        title: this.transloco.translate('applications.applications.deleteApplicationTitle'),
        subtitle: this.transloco.translate('applications.applications.deleteApplicationSubtitle'),
        actions: [
          { actionName: this.transloco.translate('common.actions.cancel'), buttonStatus: 'basic', ghost: true },
          { actionName: action, buttonStatus: 'danger' }
        ],
        modalStatus: 'danger'
      }
    });
    dialogRef.onClose.subscribe((result) => {
      if (result.toLowerCase() === action.toLowerCase()) {
        this.loading = true;
        this.applicationService.deleteApplication(application).subscribe((result) => {
          if (result) {
            this.toastrService.show(
              this.transloco.translate('applications.applications.applicationDeleted', { name: application.name }),
              this.transloco.translate('applications.applications.deleteApplicationSuccessTitle'),
              {
                duration: 2000,
                status: 'success'
              }
            );
            this.state.resetConfiguration();
          } else {
            this.toastrService.show(
              this.transloco.translate('applications.applications.deleteApplicationFailed', { name: application.name }),
              this.transloco.translate('common.messages.error'),
              {
                duration: 5000,
                status: 'danger'
              }
            );
          }
          this.loading = false;
        });
      }
    });
  }

  downloadDump(app: Application): void {
    this.applicationService.getApplicationDump(app).subscribe((blob) => {
      const exportFileName = getExportFileName(this.state.currentApplication.namespace, app.name, 'application-dump', 'json');
      saveAs(blob, exportFileName);
      this.toastrService.show(
        this.transloco.translate('applications.applications.applicationDumpProvided'),
        this.transloco.translate('applications.applications.dumpTitle'),
        { duration: 2000 }
      );
    });
  }

  downloadSentencesDump(app: Application): void {
    this.applicationService.getSentencesDump(app, this.state.hasRole(UserRole.technicalAdmin)).subscribe((blob) => {
      const exportFileName = getExportFileName(this.state.currentApplication.namespace, app.name, 'application-sentences', 'json');
      saveAs(blob, exportFileName);
      this.toastrService.show(
        this.transloco.translate('applications.applications.sentencesDumpProvided'),
        this.transloco.translate('applications.applications.dumpTitle'),
        { duration: 2000 }
      );
    });
  }

  showUploadDumpPanel(): void {
    this.nbDialogService.open(ApplicationUploadComponent);
  }
}
