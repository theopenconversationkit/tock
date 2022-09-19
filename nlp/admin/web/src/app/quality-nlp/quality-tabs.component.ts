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

class TabLink {
  constructor(public route: string, public title: string, public icon?: string) {}
}

const tabs = [
  new TabLink('log-stats', 'Model Stats', 'activity-outline'),
  new TabLink('intent-quality', 'Intent Distance', 'pantone-outline'),
  new TabLink('model-builds', 'Model Builds', 'save-outline'),
  new TabLink('test-builds', 'Test Trends', 'trending-down-outline'),
  new TabLink('test-intent-errors', 'Test Intent Errors', 'alert-triangle-outline'),
  new TabLink('test-entity-errors', 'Test Entity Errors', 'alert-triangle-outline')
];

@Component({
  selector: 'tock-quality-tabs',
  templateUrl: './quality-tabs.component.html'
})
export class QualityTabsComponent implements OnInit {
  tabLinks = tabs;

  constructor(private router: Router) {}

  ngOnInit() {
    if (this.router.routerState.snapshot.url.endsWith('/quality')) {
      this.router.navigateByUrl('/quality/log-stats');
    }
  }
}
