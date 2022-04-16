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
import { ActivatedRoute, Router, RouterState } from '@angular/router';
class TabLink {
  constructor(public route: string, public title: string, public icon?: string) {}
}

const tabs = [
  new TabLink('activity', 'Activity', 'activity-outline'),
  new TabLink('behavior', 'Behavior', 'pie-chart-outline'),
  new TabLink('flow', 'Flow', 'funnel-outline'),
  new TabLink('users', 'Users', 'people-outline'),
  new TabLink('dialogs', 'Search', 'search-outline'),
  new TabLink('preferences', 'Preferences', 'settings-2-outline')
];

@Component({
  selector: 'tock-analytics-tabs',
  templateUrl: './analytics-tabs.component.html',
  styleUrls: ['./analytics-tabs.component.css']
})
export class AnalyticsTabsComponent implements OnInit {
  analyticsTabLinks = tabs;

  constructor(private router: Router) {}

  ngOnInit() {
    if (this.router.routerState.snapshot.url.endsWith('/analytics')) {
      this.router.navigateByUrl('/analytics/activity');
    }
  }
}
