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

import {ChangeDetectorRef, Component, OnDestroy, OnInit} from "@angular/core";
import {AuthService} from "./core-nlp/auth/auth.service";
import {StateService} from "./core-nlp/state.service";
import {RestService} from "./core-nlp/rest/rest.service";
import {MatIconRegistry, MatSnackBar} from "@angular/material";
import {UserRole} from "./model/auth";
import {DomSanitizer} from "@angular/platform-browser";
import {NbMenuItem} from "@nebular/theme";
import {DialogService} from "./core-nlp/dialog.service";


@Component({
  selector: 'tock-nlp-admin-root',
  templateUrl: './nlp-admin-app.component.html',
  styleUrls: ['./nlp-admin-app.component.css']
})
export class NlpAdminAppComponent implements OnInit, OnDestroy {

  UserRole = UserRole;

  private errorUnsuscriber: any;
  public menu: NbMenuItem[];

  constructor(public auth: AuthService,
              public state: StateService,
              private rest: RestService,
              private snackBar: MatSnackBar,
              private changeDetectorRef: ChangeDetectorRef,
              private dialog: DialogService,
              iconRegistry: MatIconRegistry,
              sanitizer: DomSanitizer) {
    dialog.setupRootChangeDetector(changeDetectorRef);
    iconRegistry.addSvgIcon(
      'logo',
      sanitizer.bypassSecurityTrustResourceUrl('assets/images/logo.svg'));
  }

  ngOnInit(): void {
    this.errorUnsuscriber = this.rest.errorEmitter.subscribe(e =>
      this.snackBar.open(e, "Error", {duration: 5000})
    );
    this.menu = [
      {
        title: 'Configuration',
        icon: 'settings-outline',
        link: '/applications',
        hidden: this.state.hasRole(UserRole.admin)
      },
      {
        title: 'NLU',
        icon: 'list',
        link: '/nlp',
        hidden: this.state.hasRole(UserRole.nlpUser)
      },
      {
        title: 'NLU QA',
        icon: 'bar-chart-outline',
        link: '/quality',
        hidden: this.state.hasRole(UserRole.nlpUser)
      }

    ];
  }

  ngOnDestroy(): void {
    this.errorUnsuscriber.unsubscribe();
  }

}
