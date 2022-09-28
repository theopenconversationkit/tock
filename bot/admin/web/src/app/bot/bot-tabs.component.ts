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
class TabLink {
  constructor(public route: string, public title: string, public icon?: string) {}
}

const tabs = [
  new TabLink('story-create', 'New Story', 'plus-circle-outline'),
  new TabLink('story-search', 'Stories', 'layers-outline'),
  new TabLink('story-rules', 'Rules', 'toggle-right-outline'),
  new TabLink('i18n', 'Answers', 'color-palette-outline')
];

@Component({
  selector: 'tock-bot-tabs',
  templateUrl: './bot-tabs.component.html',
  styleUrls: ['./bot-tabs.component.css']
})
export class BotTabsComponent implements OnInit {
  botTabLinks = tabs;

  constructor(private router: Router, private state: StateService) {
    if (!state.hasRole(UserRole.botUser)) {
      this.botTabLinks = this.botTabLinks.filter(
        (t) => !['story-create', 'story-search', 'story-rules'].includes(t.route)
      );
    }
  }

  ngOnInit() {
    if (this.router.routerState.snapshot.url.endsWith('/build')) {
      this.router.navigateByUrl('/build/story-create');
    }
  }
}
