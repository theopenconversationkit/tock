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

import { ChangeDetectorRef, Component, OnDestroy, OnInit } from '@angular/core';
import { AuthService } from './core-nlp/auth/auth.service';
import { StateService } from './core-nlp/state.service';
import { RestService } from './core-nlp/rest/rest.service';
import { MatIconRegistry } from '@angular/material/icon';
import { User, UserRole } from './model/auth';
import { DomSanitizer } from '@angular/platform-browser';
import { NbMenuItem, NbToastrService } from '@nebular/theme';
import { DialogService } from './core-nlp/dialog.service';
import { AuthListener } from './core-nlp/auth/auth.listener';

@Component({
  selector: 'tock-nlp-admin-root',
  templateUrl: './nlp-admin-app.component.html',
  styleUrls: ['./nlp-admin-app.component.css']
})
export class NlpAdminAppComponent implements AuthListener, OnInit, OnDestroy {
  UserRole = UserRole;

  private errorUnsuscriber: any;
  public menu: NbMenuItem[] = [];

  constructor(
    public auth: AuthService,
    public state: StateService,
    private rest: RestService,
    private toastrService: NbToastrService,
    private changeDetectorRef: ChangeDetectorRef,
    private dialog: DialogService,
    iconRegistry: MatIconRegistry,
    sanitizer: DomSanitizer
  ) {
    iconRegistry.addSvgIcon('logo', sanitizer.bypassSecurityTrustResourceUrl('assets/images/logo.svg'));
    this.auth.addListener(this);
  }

  ngOnInit(): void {
    this.errorUnsuscriber = this.rest.errorEmitter.subscribe((e) => this.toastrService.show(e, 'Error', { duration: 5000 }));
  }

  login(user: User): void {
    this.menu = [
      {
        title: 'Language Understanding',
        icon: 'message-circle-outline',
        link: '/nlp',
        hidden: !this.state.hasRole(UserRole.nlpUser)
      },
      {
        title: 'Model Quality',
        icon: 'clipboard-outline',
        link: '/quality',
        hidden: !this.state.hasRole(UserRole.nlpUser)
      },
      {
        title: 'Settings',
        icon: 'settings-outline',
        link: '/applications/nlu'
      }
    ];
  }

  logout(): void {}

  ngOnDestroy(): void {
    this.errorUnsuscriber.unsubscribe();
  }
}
