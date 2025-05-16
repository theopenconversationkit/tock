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

import { Component, OnDestroy, OnInit } from '@angular/core';
import { Feature } from '../../model/feature';
import { BotService } from '../../bot-service';
import { StateService } from '../../../core-nlp/state.service';
import { BotConfigurationService } from '../../../core/bot-configuration.service';
import { Subject, takeUntil } from 'rxjs';

@Component({
  selector: 'tock-application-feature',
  templateUrl: './application-feature.component.html',
  styleUrls: ['./application-feature.component.css']
})
export class ApplicationFeatureComponent implements OnInit, OnDestroy {
  destroy = new Subject();

  botFeatures: Feature[] = [];

  tockFeatures: Feature[] = [];

  loading: boolean = false;

  constructor(private state: StateService, private botService: BotService, private configurationService: BotConfigurationService) {}

  ngOnInit(): void {
    this.state.configurationChange.pipe(takeUntil(this.destroy)).subscribe((res) => {
      this.refresh();
    });

    this.refresh();
  }

  refresh() {
    if (this.state.currentApplication) {
      this.loading = true;

      this.botService.getFeatures(this.state.currentApplication.name).subscribe((f) => {
        f.forEach((feature) => {
          if (feature.applicationId) {
            this.configurationService.configurations.subscribe((_) => {
              feature.configuration = this.configurationService.findApplicationConfigurationByApplicationId(feature.applicationId);
            });
          }
        });

        this.botFeatures = f.filter((story) => story.category !== 'tock');
        this.tockFeatures = f.filter((story) => story.category === 'tock');

        this.loading = false;
      });
    }
  }

  ngOnDestroy(): void {
    this.destroy.next(true);
    this.destroy.complete();
  }
}
