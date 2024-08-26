/*
 * Copyright (C) 2017/2021 e-voyageurs technologies
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

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

@Component({
  selector: 'tock-applications',
  templateUrl: 'applications.component.html',
  styleUrls: ['applications.component.scss']
})
export class ApplicationsComponent implements OnInit {
  UserRole = UserRole;
  loading: boolean = false;

  constructor(
    private toastrService: NbToastrService,
    private nbDialogService: NbDialogService,
    public state: StateService,
    private applicationService: ApplicationService
  ) {}

  ngOnInit(): void {
    this.state.sortApplications();
  }

  isAdmin(): boolean {
    return this.state.hasRole(UserRole.admin);
  }

  selectApplication(app: Application): void {
    this.state.changeApplication(app);
    this.toastrService.show(`Application ${app.name} selected`, 'Selection', { duration: 2000 });
  }

  deleteApplication(application): void {
    const action = 'delete';
    let dialogRef = this.nbDialogService.open(ChoiceDialogComponent, {
      context: {
        title: 'Delete the Application',
        subtitle: 'Are you sure?',
        actions: [
          { actionName: 'cancel', buttonStatus: 'basic', ghost: true },
          { actionName: action, buttonStatus: 'danger' }
        ],
        modalStatus: 'danger'
      }
    });
    dialogRef.onClose.subscribe((result) => {
      if (result === action) {
        this.loading = true;
        this.applicationService.deleteApplication(application).subscribe((result) => {
          if (result) {
            this.toastrService.show(`Application ${application.name} deleted`, 'Delete Application', {
              duration: 2000,
              status: 'success'
            });
            this.state.resetConfiguration();
          } else {
            this.toastrService.show(`Delete Application ${application.name} failed`, 'Error', {
              duration: 5000,
              status: 'danger'
            });
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
      this.toastrService.show(`Application dump provided`, 'Dump', { duration: 2000 });
    });
  }

  downloadSentencesDump(app: Application): void {
    this.applicationService.getSentencesDump(app, this.state.hasRole(UserRole.technicalAdmin)).subscribe((blob) => {
      const exportFileName = getExportFileName(this.state.currentApplication.namespace, app.name, 'application-sentences', 'json');
      saveAs(blob, exportFileName);
      this.toastrService.show(`Sentences dump provided`, 'Dump', { duration: 2000 });
    });
  }

  showUploadDumpPanel(): void {
    this.nbDialogService.open(ApplicationUploadComponent);
  }
}
