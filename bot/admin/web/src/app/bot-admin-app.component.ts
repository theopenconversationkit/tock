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

import {Component, OnDestroy, OnInit} from "@angular/core";
import {AuthService} from "tock-nlp-admin/src/app/core/auth/auth.service";
import {StateService} from "tock-nlp-admin/src/app/core/state.service";
import {RestService} from "tock-nlp-admin/src/app/core/rest/rest.service";
import {MdIconRegistry, MdSnackBar} from "@angular/material";
import {UserRole} from "tock-nlp-admin/src/app/model/auth";
import {DomSanitizer} from "@angular/platform-browser";


@Component({
  selector: 'tock-bot-admin-root',
  templateUrl: './bot-admin-app.component.html',
  styleUrls: ['./bot-admin-app.component.css']
})
export class BotAdminAppComponent implements OnInit, OnDestroy {

  UserRole = UserRole;
  private errorUnsuscriber: any;

  constructor(public auth: AuthService,
              public state: StateService,
              private rest: RestService,
              private snackBar: MdSnackBar,
              iconRegistry: MdIconRegistry,
              sanitizer: DomSanitizer) {
    iconRegistry.addSvgIcon(
      'logo',
      sanitizer.bypassSecurityTrustResourceUrl('assets/images/logo.svg'));
  }

  ngOnInit(): void {
    this.errorUnsuscriber = this.rest.errorEmitter.subscribe(e =>
      this.snackBar.open(`Server error : ${e}`, "Error", {duration: 5000})
    )
  }

  ngOnDestroy(): void {
    this.errorUnsuscriber.unsubscribe();
  }

  changeApplication(newApplicationName: string) {
    this.state.changeApplicationWithName(newApplicationName);
  }

  changeLocale(newLocale: string) {
    this.state.changeLocale(newLocale);
  }

}
