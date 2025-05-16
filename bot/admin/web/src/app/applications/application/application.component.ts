/*
 * Copyright (C) 2017/2025 SNCF Connect & Tech
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
import { ApplicationService } from '../../core-nlp/applications.service';
import { Subject } from 'rxjs';
import { NlpEngineType } from '../../model/nlp';
import { NbToastrService } from '@nebular/theme';

@Component({
  selector: 'tock-application',
  templateUrl: './application.component.html',
  styleUrls: ['./application.component.scss']
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
    public state: StateService,
    private applicationService: ApplicationService,
    private router: Router
  ) {}

  ngOnInit(): void {
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
          0.7,
          0.1,
          false,
          []
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

  format(): void {
    this.formatName(this.application.label);
  }

  private formatName(label: string): void {
    if (label && this.newApplication) {
      this.application.name = label
        .replace(/[^A-Za-z0-9_-]*/g, '')
        .toLowerCase()
        .trim();
    }
  }

  saveApplication(): void {
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
      this.application.nlpEngineType = this.state.supportedNlpEngines.find((e) => e.name === this.nlpEngineType);
      this.applicationService.saveApplication(this.application).subscribe({
        next: (app) => {
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
        error: (error) => {
          this.toastrService.show(error, 'Error', { status: 'danger' });
        }
      });
    }
  }

  private redirect(): void {
    let redirect = '../../';
    if (this.newApplication) {
      redirect = '../';
    }
    this.router.navigate([redirect], { relativeTo: this.route });
  }

  cancel(): void {
    this.redirect();
  }

  removeLocale(locale: string): void {
    this.application.supportedLocales.splice(this.application.supportedLocales.indexOf(locale), 1);
  }

  addLocale(): void {
    this.application.supportedLocales.push(this.newLocale);
    this.toastrService.show(`${this.state.localeName(this.newLocale)} added`, 'Locale', {
      duration: 2000,
      status: 'success'
    });
  }

  changeNlpEngine(type: string): void {
    this.nlpEngineTypeChange.next(this.state.supportedNlpEngines.find((e) => e.name === type));
  }
}
