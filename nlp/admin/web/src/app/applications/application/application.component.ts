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
import {MdDialog, MdSnackBar} from "@angular/material";
import {ActivatedRoute, Router} from "@angular/router";
import {StateService} from "../../core/state.service";
import {Application} from "../../model/application";
import {ConfirmDialogComponent} from "../../shared/confirm-dialog/confirm-dialog.component";
import {ApplicationService} from "../../core/applications.service";
import {saveAs} from "file-saver";
import {ApplicationScopedQuery} from "../../model/commons";

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

  uploadDump: boolean = false;
  exportAlexa: boolean = false;
  alexaLocale: string;

  constructor(private route: ActivatedRoute,
              private snackBar: MdSnackBar,
              private dialog: MdDialog,
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
            this.alexaLocale = this.application.supportedLocales[0];
          }
        } else {
          this.newApplication = true;
          this.application = new Application("", this.state.user.organization, [], [], StateService.DEFAULT_ENGINE, true, false);
        }
        this.nlpEngineType = this.application.nlpEngineType.name;
      }
    );
  }

  saveApplication() {
    if (this.application.supportedLocales.length === 0) {
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

  addLocale(newLocale: string) {
    this.application.supportedLocales.push(newLocale);
    this.snackBar.open(`${this.state.localeName(newLocale)} added`, "Locale", {duration: 1000});
  }

  triggerBuild() {
    this.applicationService.triggerBuild(this.application).subscribe(_ =>
      this.snackBar.open(`Application build started`, "Build", {duration: 1000})
    )
  }

  downloadAlexaExport() {
    setTimeout(_ => {
      const query = new ApplicationScopedQuery(
        this.application.namespace,
        this.application.name,
        this.alexaLocale
      );
      this.applicationService.getAlexaExport(query)
        .subscribe(blob => {
          this.exportAlexa = false;
          saveAs(blob, this.application.name + "_alexa.json");
          this.snackBar.open(`Alexa export file provided`, "Alexa", {duration: 1000});
        })
    });
  }

}
