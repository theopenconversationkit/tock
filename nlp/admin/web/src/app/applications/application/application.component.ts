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

import {Component, OnInit} from "@angular/core";
import {MatDialog, MatSnackBar} from "@angular/material";
import {ActivatedRoute, Router} from "@angular/router";
import {StateService} from "../../core-nlp/state.service";
import {Application} from "../../model/application";
import {ConfirmDialogComponent} from "../../shared-nlp/confirm-dialog/confirm-dialog.component";
import {ApplicationService} from "../../core-nlp/applications.service";
import {Subject} from "rxjs";
import {NlpEngineType} from "../../model/nlp";

@Component({
  selector: 'tock-application',
  templateUrl: './application.component.html',
  styleUrls: ['./application.component.css']
})
export class ApplicationComponent implements OnInit {

  applications: Application[];
  application: Application;
  newApplication: boolean;
  newLocale: string;
  nlpEngineType: string;
  nlpEngineTypeChange: Subject<NlpEngineType> = new Subject();

  constructor(private route: ActivatedRoute,
              private snackBar: MatSnackBar,
              private dialog: MatDialog,
              public state: StateService,
              private applicationService: ApplicationService,
              private router: Router) {
  }

  ngOnInit() {
    this.applications = this.state.applications;
    this.route.params.subscribe(params => {
        const id = params['id'];
        if (id && id.length !== 0) {
          this.application = this.applications.find(a => a._id === id);
          if (this.application) {
            this.application = this.application.clone();
          }
        } else {
          this.newApplication = true;
          this.application = new Application("", this.state.user.organization, [], [], StateService.DEFAULT_ENGINE, true, true, false);
        }
        this.nlpEngineType = this.application.nlpEngineType.name;
      }
    );
  }

  saveApplication() {
    if (this.application.name.trim().length === 0) {
      this.snackBar.open(`Please choose an application name`, "ERROR", {duration: 5000});
    } else if (this.application.supportedLocales.length === 0) {
      this.snackBar.open(`Please choose at least one locale`, "ERROR", {duration: 5000});
    } else {
      this.application.nlpEngineType = this.state.supportedNlpEngines.find(e => e.name === this.nlpEngineType);
      this.applicationService.saveApplication(this.application)
        .subscribe(app => {
          this.applicationService.refreshCurrentApplication(app);
          this.snackBar.open(`Application ${app.name} saved`, "Save Application", {duration: 1000});
          if (this.newApplication && this.state.applications.length === 1) {
            this.router.navigateByUrl("/nlp/try");
          }
          else {
            this.redirect();
          }
        }, error => {
          this.snackBar.open(error, "Error", {});
        });
    }
  }

  private redirect() {
    let redirect = '../../';
    if (this.newApplication) {
      redirect = '../'
    }
    this.router.navigate([redirect], {relativeTo: this.route});
  }

  cancel() {
    this.redirect();
  }

  deleteApplication() {
    let dialogRef = this.dialog.open(ConfirmDialogComponent, {
      data: {
        title: "Delete the Application",
        subtitle: "Are you sure?",
        action: "Delete"
      }
    });
    dialogRef.afterClosed().subscribe(result => {
      if (result === "delete") {
        this.applicationService.deleteApplication(this.application).subscribe(
          result => {
            if (result) {
              this.snackBar.open(`Application ${this.application.name} deleted`, "Delete Application", {duration: 1000});
            } else {
              this.snackBar.open(`Delete Application ${this.application.name} failed`, "Error", {duration: 5000});
            }
            this.redirect();
          });
      }
    });
  }

  removeLocale(locale: string) {
    this.application.supportedLocales.splice(this.application.supportedLocales.indexOf(locale), 1);
    this.snackBar.open(`${this.state.localeName(locale)} removed`, "Locale", {duration: 1000});
  }

  addLocale() {
    this.application.supportedLocales.push(this.newLocale);
    this.snackBar.open(`${this.state.localeName(this.newLocale)} added`, "Locale", {duration: 1000});
  }

  changeNlpEngine(type: string) {
    this.nlpEngineTypeChange.next(this.state.supportedNlpEngines.find(e => e.name === type));
  }
}
