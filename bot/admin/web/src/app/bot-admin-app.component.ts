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
import { Component, OnDestroy, OnInit } from '@angular/core';
import { MatIconRegistry } from '@angular/material/icon';
import { DomSanitizer } from '@angular/platform-browser';
import { NbMenuItem, NbToastrService } from '@nebular/theme';
import { AuthListener } from './core-nlp/auth/auth.listener';

import { AuthService } from './core-nlp/auth/auth.service';
import { RestService } from './core-nlp/rest/rest.service';
import { StateService } from './core-nlp/state.service';
import { User, UserRole } from './model/auth';

@Component({
  selector: 'tock-bot-admin-root',
  templateUrl: './bot-admin-app.component.html',
  styleUrls: ['./bot-admin-app.component.css']
})
export class BotAdminAppComponent implements AuthListener, OnInit, OnDestroy {

  UserRole = UserRole;

  private errorUnsuscriber: any;
  public menu: NbMenuItem[] = [];

  constructor(
    public auth: AuthService,
    public state: StateService,
    private rest: RestService,
    private toastrService: NbToastrService,
    iconRegistry: MatIconRegistry,
    sanitizer: DomSanitizer
  ) {
    iconRegistry.addSvgIcon(
      'logo',
      sanitizer.bypassSecurityTrustResourceUrl('assets/images/logo.svg')
    );
    this.auth.addListener(this);
  }

  ngOnInit(): void {
    this.errorUnsuscriber = this.rest.errorEmitter.subscribe((e) =>
      this.toastrService.show(e, 'Error', { duration: 5000, status: 'danger' })
    );
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
        title: 'Stories & Answers',
        icon: 'book-open-outline',
        link: '/build',
        hidden: !this.state.hasRole(UserRole.botUser)
      },
      {
        title: 'Test',
        icon: 'play-circle-outline',
        link: '/test',
        hidden: !this.state.hasRole(UserRole.botUser) && !this.state.hasRole(UserRole.faqNlpUser)
      },
      {
        title: 'Analytics',
        icon: 'trending-up-outline',
        link: '/analytics',
        hidden: !this.state.hasRole(UserRole.botUser) && !this.state.hasRole(UserRole.faqBotUser)
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
        link: '/configuration',
        hidden: !this.state.hasRole(UserRole.admin)
      },
      {
        title: 'FAQ Training',
        icon: {
          icon: 'school',
          pack: 'material-icons'
        },
        link: '/faq/train'
        ,
        hidden: !this.state.hasRole(UserRole.faqNlpUser)
      },
      {
        title: 'FAQ Management',
        icon: {
          icon: 'question_answer',
          pack: 'material-icons'
        },
        link: '/faq/qa'
        ,
        hidden: !this.state.hasRole(UserRole.faqBotUser)
      }
    ];
  }
  logout(): void {}

  ngOnDestroy(): void {
    this.errorUnsuscriber.unsubscribe();
  }
}
