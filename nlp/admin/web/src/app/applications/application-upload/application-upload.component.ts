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

import { Component, EventEmitter, Input, OnInit, Output } from '@angular/core';
import { FileItem, FileUploader, ParsedResponseHeaders } from 'ng2-file-upload';
import { ApplicationImportConfiguration, ImportReport } from '../../model/application';
import { StateService } from '../../core-nlp/state.service';
import { ApplicationService } from '../../core-nlp/applications.service';
import { UserRole } from '../../model/auth';

@Component({
  selector: 'tock-application-upload',
  templateUrl: 'application-upload.component.html',
  styleUrls: ['application-upload.component.css']
})
export class ApplicationUploadComponent implements OnInit {
  UserRole = UserRole;
  public uploader: FileUploader;
  public configuration: ApplicationImportConfiguration;
  public report: ImportReport;
  public uploading: boolean = false;

  public type: string = 'application';

  @Input() applicationName: string = null;
  @Output() closed = new EventEmitter();

  constructor(private applicationService: ApplicationService, public state: StateService) {}

  ngOnInit(): void {
    this.report = null;
    this.uploader = new FileUploader({ removeAfterUpload: true });
    this.uploader.onCompleteItem = (item: FileItem, response: string, status: number, headers: ParsedResponseHeaders) => {
      this.uploading = false;
      this.report = ImportReport.fromJSON(JSON.parse(response));
      if (this.report.modified) {
        this.state.resetConfiguration();
      }
    };
    this.configuration = new ApplicationImportConfiguration();
    this.configuration.newApplicationName = this.applicationName;
  }

  cancel(): void {
    this.close();
  }

  close() {
    this.ngOnInit();
    this.closed.emit(true);
  }

  upload() {
    if (this.type === 'application') {
      this.applicationService.prepareApplicationDumpUploader(this.uploader, this.configuration);
    } else {
      this.applicationService.prepareSentencesDumpUploader(
        this.uploader,
        this.state.hasRole(UserRole.technicalAdmin),
        this.configuration.newApplicationName
      );
    }
    this.uploading = true;
    this.uploader.uploadAll();
  }
}
