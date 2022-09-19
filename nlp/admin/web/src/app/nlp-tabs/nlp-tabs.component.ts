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
  new TabLink('try', 'New Sentence', 'plus-circle-outline'),
  new TabLink('inbox', 'Inbox', 'inbox-outline'),
  new TabLink('search', 'Search', 'search-outline'),
  new TabLink('unknown', 'Unknown', 'question-mark-circle-outline'),
  new TabLink('intents', 'Intents', 'compass-outline'),
  new TabLink('entities', 'Entities', 'attach-outline'),
  new TabLink('logs', 'Logs', 'list-outline')
];

@Component({
  selector: 'tock-nlp-tabs',
  templateUrl: './nlp-tabs.component.html',
  styleUrls: ['./nlp-tabs.component.css']
})
export class NlpTabsComponent implements OnInit {
  tabLinks = tabs;

  constructor(private router: Router) {}

  ngOnInit() {
    if (this.router.routerState.snapshot.url.endsWith('/nlp')) {
      this.router.navigateByUrl('/nlp/inbox');
    }
  }
}
