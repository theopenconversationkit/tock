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

import {Component, OnInit} from '@angular/core';
import {AnalyticsService} from "../analytics.service";
import {StateService} from 'src/app/core-nlp/state.service';

@Component({
  selector: 'tock-satisfaction',
  templateUrl: './satisfaction.component.html',
  styleUrls: ['./satisfaction.component.css']
})
export class SatisfactionComponent implements OnInit {
  isStatisfactionActivated: boolean = false;
  errorMsg: string;
  public loaded: boolean = false;

  constructor(
    private analytics: AnalyticsService,
    private state: StateService
  ) {
  }

  ngOnInit(): void {
    this.state.currentIntents.subscribe(() => {
      this.isActiveSatisfaction()
    });
  }

  isActiveSatisfaction() {
    this.errorMsg = null;
    this.loaded = false;
    this.analytics.isActiveSatisfactionByBot()
      .subscribe((res: boolean) =>
          this.isStatisfactionActivated = res,
        err => this.errorMsg = err,
        () => this.loaded = true);
  }


}

