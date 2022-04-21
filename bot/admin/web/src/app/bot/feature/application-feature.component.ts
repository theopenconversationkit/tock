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

import { Component, ElementRef, OnInit, ViewChild } from '@angular/core';
import { Feature } from '../model/feature';
import { BotService } from '../bot-service';
import { StateService } from '../../core-nlp/state.service';
import { BotConfigurationService } from '../../core/bot-configuration.service';

@Component({
  selector: 'tock-application-feature',
  templateUrl: './application-feature.component.html',
  styleUrls: ['./application-feature.component.css']
})
export class ApplicationFeatureComponent implements OnInit {
  private currentApplicationUnsuscriber: any;
  botFeatures: Feature[] = [];
  tockFeatures: Feature[] = [];
  create: boolean = false;
  feature: Feature = new Feature('', '', false);
  botApplicationConfigurationId: string;
  loadingApplicationsFeatures: boolean = false;

  constructor(
    private state: StateService,
    private botService: BotService,
    private configurationService: BotConfigurationService
  ) {}

  ngOnInit(): void {
    this.currentApplicationUnsuscriber = this.state.currentApplicationEmitter.subscribe((a) =>
      this.refresh()
    );
    this.refresh();
  }

  refresh() {
    if (this.state.currentApplication) {
      this.loadingApplicationsFeatures = true;
      this.botService.getFeatures(this.state.currentApplication.name).subscribe((f) => {
        f.forEach((feature) => {
          if (feature.applicationId) {
            this.configurationService.configurations.subscribe((_) => {
              feature.configuration =
                this.configurationService.findApplicationConfigurationByApplicationId(
                  feature.applicationId
                );
            });
          }
        });

        this.botFeatures = f.filter((story) => story.category !== 'tock');
        this.tockFeatures = f.filter((story) => story.category === 'tock');
        this.loadingApplicationsFeatures = false;
      });
    }
  }
}
