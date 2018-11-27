/*
 * Copyright (C) 2017 VSCT
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

import {Component, OnInit} from "@angular/core";
import {Router} from "@angular/router";

class TabLink {
  constructor(public link: string, public label: string) {

  }
}

const tabs = [
  new TabLink("try", "Try it"),
  new TabLink("inbox", "Inbox"),
  new TabLink("unknown", "Unknown"),
  new TabLink("search", "Search"),
  new TabLink("intents", "Intents"),
  new TabLink("entities", "Entities"),
  new TabLink("logs", "Logs")
];

@Component({
  selector: 'tock-nlp-tabs',
  templateUrl: './nlp-tabs.component.html',
  styleUrls: ['./nlp-tabs.component.css']
})
export class NlpTabsComponent implements OnInit {

  tabLinks = tabs;

  constructor(private router: Router) {
  }

  ngOnInit() {
    if(this.router.routerState.snapshot.url.endsWith("/nlp")) {
      this.router.navigateByUrl("/nlp/inbox");
    }
  }

}
