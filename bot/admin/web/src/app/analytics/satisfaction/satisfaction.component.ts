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
import { AnalyticsService } from '../analytics.service';
import { StateService } from 'src/app/core-nlp/state.service';
import { BotConfigurationService } from '../../core/bot-configuration.service';
import { Subject, take, takeUntil } from 'rxjs';
import { BotApplicationConfiguration } from '../../core/model/configuration';

@Component({
  selector: 'tock-satisfaction',
  templateUrl: './satisfaction.component.html',
  styleUrls: ['./satisfaction.component.css']
})
export class SatisfactionComponent implements OnInit, OnDestroy {
  destroy = new Subject();
  configurations: BotApplicationConfiguration[];
  isStatisfactionActivated: boolean = false;
  errorMsg: string;
  public loading: boolean = true;

  constructor(private analytics: AnalyticsService, private state: StateService, private botConfiguration: BotConfigurationService) {}

  ngOnInit(): void {
    this.botConfiguration.configurations.pipe(takeUntil(this.destroy)).subscribe((confs) => {
      this.configurations = confs;

      if (this.configurations.length) this.isActiveSatisfaction();

      this.loading = false;
    });
  }

  isActiveSatisfaction() {
    this.errorMsg = null;
    this.analytics
      .isActiveSatisfactionByBot()
      .pipe(take(1))
      .subscribe({
        next: (res: boolean) => (this.isStatisfactionActivated = res),
        error: (err) => (this.errorMsg = err)
      });
  }

  ngOnDestroy() {
    this.destroy.next(true);
    this.destroy.complete();
  }
}
