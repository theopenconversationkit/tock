/*
 * Copyright (C) 2017/2020 e-voyageurs technologies
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

import {ChangeDetectorRef, Component, OnDestroy, OnInit} from "@angular/core";
import {AuthService} from "./core-nlp/auth/auth.service";
import {StateService} from "./core-nlp/state.service";
import {DialogService} from "./core-nlp/dialog.service";
import {RestService} from "./core-nlp/rest/rest.service";
import { MatIconRegistry } from "@angular/material/icon";
import {UserRole} from "./model/auth";
import {DomSanitizer} from "@angular/platform-browser";
import {NbMenuItem} from "@nebular/theme";
import { NbToastrService } from '@nebular/theme';


@Component({
  selector: 'tock-bot-admin-root',
  templateUrl: './bot-admin-app.component.html',
  styleUrls: ['./bot-admin-app.component.css']
})
export class BotAdminAppComponent implements OnInit, OnDestroy {

  UserRole = UserRole;

  private errorUnsuscriber: any;
  public menu: NbMenuItem[];

  constructor(public auth: AuthService,
              public state: StateService,
              private rest: RestService,
              private dialog: DialogService,
              private changeDetectorRef: ChangeDetectorRef,
              private toastrService: NbToastrService,
              iconRegistry: MatIconRegistry,
              sanitizer: DomSanitizer) {
    dialog.setupRootChangeDetector(changeDetectorRef);
    iconRegistry.addSvgIcon(
      'logo',
      sanitizer.bypassSecurityTrustResourceUrl('assets/images/logo.svg'));
  }

  ngOnInit(): void {
    this.errorUnsuscriber = this.rest.errorEmitter.subscribe(e =>
      this.toastrService.show(e, "Error", {duration: 5000, status: 'danger'})
    );
    this.menu = [
      {
        title: 'Configuration',
        icon: 'settings-outline',
        link: '/configuration',
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
      },
      {
        title: 'Build',
        icon: 'edit-outline',
        link: '/build',
        hidden: this.state.hasRole(UserRole.botUser)
      },
      {
        title: 'Monitoring',
        icon: 'message-circle-outline',
        link: '/monitoring'
        ,
        hidden: this.state.hasRole(UserRole.botUser)
      },
      {
        title: 'Test',
        icon: 'text-outline',
        link: '/test',
        hidden: this.state.hasRole(UserRole.botUser)
      }

    ];
  }

  ngOnDestroy(): void {
    this.errorUnsuscriber.unsubscribe();
  }

}
