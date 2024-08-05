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

import { Component, OnInit } from '@angular/core';
import { Router } from '@angular/router';
import { StateService } from '../core-nlp/state.service';
import { UserRole } from '../model/auth';
import { ApplicationsResolver } from './applications.resolver';

@Component({
  selector: 'tock-configuration-tabs',
  templateUrl: './configuration-tabs.component.html',
  styleUrls: ['./configuration-tabs.component.css', './tabs.component.scss']
})
export class ConfigurationTabsComponent implements OnInit {
  tabs = [
    {
      title: 'Applications',
      route: 'nlu',
      icon: 'browser-outline',
      resolve: {
        applications: ApplicationsResolver
      }
    },
    {
      title: 'Namespaces',
      route: 'namespaces',
      icon: 'folder-outline'
    },
    {
      title: 'Log',
      route: 'users/logs',
      icon: 'eye-outline'
    }
  ];

  configurationTabLinks;

  constructor(private router: Router, private state: StateService) {
    if (!state.hasRole(UserRole.technicalAdmin)) {
      this.tabs = this.tabs.filter((t) => t.route !== 'users/logs');
    }
    this.configurationTabLinks = this.tabs;
  }

  ngOnInit() {
    if (this.router.routerState.snapshot.url.endsWith('/configuration')) {
      this.router.navigateByUrl('/configuration/nlp');
    }
  }
}
