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

  constructor(public auth: AuthService, public state: StateService, private rest: RestService, private toastrService: NbToastrService) {
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
        title: 'Custom Metrics',
        icon: 'pie-chart-outline',
        link: '/business-metrics',
        hidden: !this.state.hasRole(UserRole.botUser) && !this.state.hasRole(UserRole.faqBotUser)
      },
      {
        title: 'Model Quality',
        icon: 'clipboard-outline',
        link: '/quality',
        hidden: !this.state.hasRole(UserRole.nlpUser)
      },
      {
        title: 'FAQ',
        icon: 'message-square-outline',
        hidden: !this.state.hasRole(UserRole.faqBotUser),
        children: [
          {
            link: '/faq/management',
            title: 'Management',
            icon: 'book-open-outline'
          },
          {
            link: '/faq/training',
            title: 'Training',
            icon: 'checkmark-square-outline'
          }
        ]
      },
      {
        title: 'FAQ training',
        icon: 'checkmark-square-outline',
        link: '/faq/training',
        hidden: !this.state.hasRole(UserRole.faqNlpUser) || this.state.hasRole(UserRole.faqBotUser)
      },
      {
        title: 'RAG',
        icon: 'bulb-outline',
        children: [
          {
            link: '/rag/sources',
            title: 'Rag sources',
            icon: 'cloud-download-outline',
            hidden: !this.state.hasRole(UserRole.admin)
          },
          {
            link: '/rag/exclusions',
            title: 'Rag exclusions',
            icon: 'alert-triangle-outline'
          },
          {
            link: '/rag/settings',
            title: 'Rag settings',
            icon: 'settings-outline'
          }
        ]
      },
      {
        title: 'Answers',
        icon: 'color-palette-outline',
        link: '/build/i18n',
        hidden: this.state.hasRole(UserRole.botUser) || !this.state.hasRole(UserRole.faqBotUser)
      },
      {
        title: 'Settings',
        icon: 'settings-outline',
        link: '/configuration'
      }
    ];
  }
  logout(): void {}

  ngOnDestroy(): void {
    this.errorUnsuscriber.unsubscribe();
  }
}
