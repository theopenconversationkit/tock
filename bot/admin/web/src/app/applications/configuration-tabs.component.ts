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
  constructor(private router: Router) {}

  ngOnInit() {
    if (this.router.routerState.snapshot.url.endsWith('/configuration')) {
      this.router.navigateByUrl('/configuration/nlp');
    }
  }
}
