/*
 * Copyright (C) 2017/2020 e-voyageurs technologies
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

import {Component, OnInit} from "@angular/core";
import {ActivatedRoute, Router, RouterState} from "@angular/router";
class TabLink {
  constructor(public route: string, public title: string) {

  }
}

const tabs = [
  new TabLink("history", "History"),
  new TabLink("dialogs", "Dialogs")
];

@Component({
  selector: 'tock-monitoring-tabs',
  templateUrl: './monitoring-tabs.component.html',
  styleUrls: ['./monitoring-tabs.component.css']
})
export class MonitoringTabsComponent implements OnInit {

  monitoringTabLinks = tabs;

  constructor(private router: Router) {
  }

  ngOnInit() {
    if(this.router.routerState.snapshot.url.endsWith("/monitoring")) {
       this.router.navigateByUrl("/monitoring/history");
    }
  }

}
