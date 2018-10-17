/*
 * Copyright (C) 2017 VSCT
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

import {saveAs} from "file-saver";
import {Component, OnInit} from "@angular/core";
import {Application} from "../../model/application";
import {MatSnackBar} from "@angular/material";
import {StateService} from "../../core-nlp/state.service";
import {ApplicationService} from "../../core-nlp/applications.service";
import {UserRole} from "../../model/auth";

@Component({
  selector: 'tock-applications',
  templateUrl: 'applications.component.html',
  styleUrls: ['applications.component.css']
})
export class ApplicationsComponent implements OnInit {

  UserRole = UserRole;
  uploadDump: boolean = false;

  constructor(private snackBar: MatSnackBar,
              public state: StateService,
              private applicationService: ApplicationService) {
  }

  ngOnInit() {
  }

  selectApplication(app: Application) {
    this.state.changeApplication(app);
    this.snackBar.open(`Application ${app.name} selected`, "Selection", {duration: 1000});
  }

  downloadDump(app: Application) {
    this.applicationService.getApplicationDump(app)
      .subscribe(blob => {
        saveAs(blob, app.name + "_app.json");
        this.snackBar.open(`Dump provided`, "Dump", {duration: 1000});
      })
  }

  downloadSentencesDump(app: Application) {
    this.applicationService.getSentencesDump(app, this.state.hasRole(UserRole.technicalAdmin))
      .subscribe(blob => {
        saveAs(blob, app.name + "_sentences.json");
        this.snackBar.open(`Dump provided`, "Dump", {duration: 1000});
      })
  }

  showUploadDumpPanel() {
    this.uploadDump = true;
  }

}
