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

import { Component, ElementRef, OnInit, ViewChild } from '@angular/core';
import { ActivatedRoute, Router } from '@angular/router';
import { StateService } from '../../core-nlp/state.service';
import { Application } from '../../model/application';
import { ConfirmDialogComponent } from '../../shared-nlp/confirm-dialog/confirm-dialog.component';
import { ApplicationService } from '../../core-nlp/applications.service';
import { Subject } from 'rxjs';
import { NlpEngineType } from '../../model/nlp';
import { NbToastrService } from '@nebular/theme';
import { DialogService } from '../../core-nlp/dialog.service';

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

  @ViewChild('appLabel') appLabel: ElementRef;

  constructor(
    private route: ActivatedRoute,
    private toastrService: NbToastrService,
    private dialog: DialogService,
    public state: StateService,
    private applicationService: ApplicationService,
    private router: Router
  ) {}

  ngOnInit() {
    this.applications = this.state.applications;
    this.route.params.subscribe((params) => {
      const id = params['id'];
      if (id && id.length !== 0) {
        this.application = this.applications.find((a) => a._id === id);
        if (this.application) {
          this.application = this.application.clone();
        }
      } else {
        this.newApplication = true;
        this.application = new Application(
          '',
          '',
          this.state.user.organization,
          [],
          [],
          StateService.DEFAULT_ENGINE,
          true,
          true,
          false,
          0.0,
          false
        );
      }
      this.nlpEngineType = this.application.nlpEngineType.name;
      if (this.application) {
        setTimeout((_) => {
          this.appLabel.nativeElement.focus();
        });
      }
    });
  }

  format() {
    this.formatName(this.application.label);
  }

  private formatName(label: string) {
    if (label && this.newApplication) {
      this.application.name = label
        .replace(/[^A-Za-z0-9_-]*/g, '')
        .toLowerCase()
        .trim();
    }
  }

  saveApplication() {
    this.format();
    if (this.application.name.trim().length === 0) {
      this.toastrService.show(`Please choose an application name`, 'ERROR', {
        duration: 5000,
        status: 'warning'
      });
    } else if (this.application.supportedLocales.length === 0) {
      this.toastrService.show(`Please choose at least one locale`, 'ERROR', {
        duration: 5000,
        status: 'warning'
      });
    } else {
      this.application.nlpEngineType = this.state.supportedNlpEngines.find(
        (e) => e.name === this.nlpEngineType
      );
      this.applicationService.saveApplication(this.application).subscribe(
        (app) => {
          this.applicationService.refreshCurrentApplication(app);
          this.toastrService.show(`Application ${app.name} saved`, 'Save Application', {
            duration: 2000,
            status: 'success'
          });
          if (this.newApplication && this.state.applications.length === 1) {
            this.router.navigateByUrl('/nlp/try');
          } else {
            this.redirect();
          }
        },
        (error) => {
          this.toastrService.show(error, 'Error', { status: 'danger' });
        }
      );
    }
  }

  private redirect() {
    let redirect = '../../';
    if (this.newApplication) {
      redirect = '../';
    }
    this.router.navigate([redirect], { relativeTo: this.route });
  }

  cancel() {
    this.redirect();
  }

  deleteApplication() {
    let dialogRef = this.dialog.openDialog(ConfirmDialogComponent, {
      context: {
        title: 'Delete the Application',
        subtitle: 'Are you sure?',
        action: 'Delete'
      }
    });
    dialogRef.onClose.subscribe((result) => {
      if (result === 'delete') {
        this.applicationService.deleteApplication(this.application).subscribe((result) => {
          if (result) {
            this.toastrService.show(
              `Application ${this.application.name} deleted`,
              'Delete Application',
              { duration: 2000, status: 'success' }
            );
            this.state.resetConfiguration();
          } else {
            this.toastrService.show(`Delete Application ${this.application.name} failed`, 'Error', {
              duration: 5000,
              status: 'danger'
            });
          }
          this.redirect();
        });
      }
    });
  }

  removeLocale(locale: string) {
    this.application.supportedLocales.splice(this.application.supportedLocales.indexOf(locale), 1);
    this.toastrService.show(`${this.state.localeName(locale)} removed`, 'Locale', {
      duration: 2000,
      status: 'success'
    });
  }

  addLocale() {
    this.application.supportedLocales.push(this.newLocale);
    this.toastrService.show(`${this.state.localeName(this.newLocale)} added`, 'Locale', {
      duration: 2000,
      status: 'success'
    });
  }

  changeNlpEngine(type: string) {
    this.nlpEngineTypeChange.next(this.state.supportedNlpEngines.find((e) => e.name === type));
  }
}
